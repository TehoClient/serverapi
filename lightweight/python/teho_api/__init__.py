# This file is part of Teho Server API, licensed under the MIT License.
#
# Copyright (c) 2026 SGM Studios LTD
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

import itertools
import os
import threading
import time

from .messages import *
from .messages import BY_TYPE, CAPABILITIES, CHANNEL, MAX_FRAME, MAX_STREAM, MESSAGES, RANGES, SLICE, VERSION
from .wire import SCALAR_WIRE, ProtocolError, Reader, Writer

__all__ = ["CHANNEL", "VERSION", "MAX_FRAME", "CAPABILITIES", "MESSAGES", "ProtocolError", "encode", "decode", "frames",
           "capability_for", "welcome", "from_json", "to_json", "Reassembler", "Session"] + list(MESSAGES)


def _write_body(message, writer):
    for number, name, kind, repeated, _, _, _ in message.FIELDS:
        value = getattr(message, name)
        items = value if repeated else (value,)
        for item in items:
            if kind in SCALAR_WIRE:
                writer.scalar(number, kind, item)
            else:
                nested = Writer()
                _write_body(item, nested)
                writer.scalar(number, "bytes", nested.out)


def _read_body(cls, data):
    reader = Reader(data)
    values = {}
    for number, name, kind, repeated, _, _, _ in cls.FIELDS:
        if repeated:
            values[name] = []
    table = {spec[0]: spec for spec in cls.FIELDS}
    while reader.has_next():
        number = reader.next_field()
        spec = table.get(number)
        if spec is None:
            reader.skip()
            continue
        _, name, kind, repeated, string_limit, bytes_limit, list_limit = spec
        if kind in SCALAR_WIRE:
            item = reader.scalar(kind, string_limit, bytes_limit)
        else:
            item = _read_body(MESSAGES[kind], reader.length_delimited(bytes_limit))
        if repeated:
            if len(values[name]) < list_limit:
                values[name].append(item)
        else:
            values[name] = item
    return cls(**values)


def _whole(message):
    if getattr(message, "TYPE", None) is None:
        raise ProtocolError("%s is not a framed message" % type(message).__name__)
    writer = Writer()
    writer.varint(message.TYPE)
    _write_body(message, writer)
    return bytes(writer.out)


def encode(message):
    frame = _whole(message)
    if len(frame) > MAX_FRAME:
        raise ProtocolError("frame %d over %d" % (len(frame), MAX_FRAME))
    return frame


def _decode_any(frame):
    if not frame:
        raise ProtocolError("empty frame")
    reader = Reader(frame)
    type_id = reader.varint()
    cls = BY_TYPE.get(type_id)
    if cls is None:
        return None
    return _read_body(cls, bytes(frame[reader.pos:]))


def decode(frame):
    if frame is not None and len(frame) > MAX_FRAME:
        raise ProtocolError("frame %d over %d" % (len(frame), MAX_FRAME))
    return _decode_any(frame)


def frames(message, stream):
    whole = _whole(message)
    if len(whole) <= MAX_FRAME:
        return [whole]
    if len(whole) > MAX_STREAM:
        raise ProtocolError("message %d over %d" % (len(whole), MAX_STREAM))
    total = (len(whole) + SLICE - 1) // SLICE
    return [encode(Chunk(stream=stream, index=i, total=total, data=whole[i * SLICE:(i + 1) * SLICE])) for i in range(total)]


def capability_for(type_id):
    if type_id in (AssetOffer.TYPE, AssetWant.TYPE, AssetData.TYPE):
        return "asset:1"
    for low, high, capability in RANGES:
        if low <= type_id <= high:
            return capability
    return None


def welcome(hello, server_name, supported=None, session=None, grant="", verified=""):
    offered = set(hello.capabilities)
    wanted = CAPABILITIES if supported is None else supported
    enabled = [c for c in wanted if c in offered]
    return Welcome(protocol=min(VERSION, max(1, hello.protocol)), session=session if session is not None else int.from_bytes(os.urandom(8), "big"),
                   enabled=enabled, server_name=server_name, grant=grant, verified=verified)


def from_json(name, values):
    cls = MESSAGES[name]
    out = {}
    for number, field_name, kind, repeated, _, _, _ in cls.FIELDS:
        key = field_name.rstrip("_") if field_name.rstrip("_") in values else field_name
        if key not in values:
            continue
        raw = values[key]
        items = raw if repeated else [raw]
        converted = []
        for item in items:
            if kind == "bytes":
                converted.append(bytes.fromhex(item) if isinstance(item, str) else bytes(item))
            elif kind == "uint64":
                converted.append(int(item))
            elif kind in SCALAR_WIRE:
                converted.append(item)
            else:
                converted.append(from_json(kind, item))
        out[field_name] = converted if repeated else converted[0]
    return cls(**out)


def to_json(message):
    out = {}
    for number, field_name, kind, repeated, _, _, _ in message.FIELDS:
        value = getattr(message, field_name)
        items = value if repeated else [value]
        converted = []
        for item in items:
            if kind == "bytes":
                converted.append(bytes(item).hex())
            elif kind == "uint64":
                converted.append(str(item))
            elif kind in SCALAR_WIRE:
                converted.append(item)
            else:
                converted.append(to_json(item))
        out[field_name.rstrip("_")] = converted if repeated else converted[0]
    return out


class Reassembler:
    MAX_OPEN = 8
    STALE = 30.0

    def __init__(self):
        self.open = {}
        self.lock = threading.Lock()

    def accept(self, chunk, now=None):
        now = time.monotonic() if now is None else now
        with self.lock:
            for stream in [s for s, p in self.open.items() if now - p["touched"] > self.STALE]:
                del self.open[stream]
            if chunk.total < 1 or chunk.index >= chunk.total or chunk.total * SLICE > MAX_STREAM + SLICE:
                raise ProtocolError("chunk bounds")
            partial = self.open.get(chunk.stream)
            if partial is None or len(partial["parts"]) != chunk.total:
                if partial is None and len(self.open) >= self.MAX_OPEN:
                    raise ProtocolError("too many open streams")
                partial = {"parts": [None] * chunk.total, "bytes": 0, "received": 0, "touched": now}
                self.open[chunk.stream] = partial
            if partial["parts"][chunk.index] is None:
                partial["bytes"] += len(chunk.data)
                if partial["bytes"] > MAX_STREAM:
                    del self.open[chunk.stream]
                    raise ProtocolError("stream over %d" % MAX_STREAM)
                partial["parts"][chunk.index] = chunk.data
                partial["received"] += 1
            partial["touched"] = now
            if partial["received"] < chunk.total:
                return None
            del self.open[chunk.stream]
            whole = b"".join(partial["parts"])
        reader = Reader(whole)
        if reader.varint() == Chunk.TYPE:
            raise ProtocolError("nested chunk")
        return _decode_any(whole)

    def clear(self):
        with self.lock:
            self.open.clear()


class Session:
    def __init__(self, send, server_name, supported=None, grant="", verified=""):
        self._send = send
        self.server_name = server_name
        self.supported = supported
        self.grant = grant
        self.verified = verified
        self.hello = None
        self.enabled = []
        self.reassembler = Reassembler()
        self.listeners = []
        self.pending = {}
        self.ids = itertools.count(1)
        self.streams = itertools.count(1)

    def has(self, capability):
        return capability in self.enabled

    def on(self, listener):
        self.listeners.append(listener)
        return lambda: self.listeners.remove(listener)

    def receive(self, frame):
        message = decode(frame)
        if isinstance(message, Chunk):
            message = self.reassembler.accept(message)
        if message is None:
            return None
        if isinstance(message, Hello):
            self.hello = message
            reply = welcome(message, self.server_name, self.supported, grant=self.grant, verified=self.verified)
            self.enabled = list(reply.enabled)
            self.reassembler.clear()
            self._send(encode(reply))
        request_id = getattr(message, "request_id", 0)
        waiter = self.pending.pop(request_id, None) if request_id and not isinstance(message, (InfoRequest, AssertRequest, AttestRequest, InspectRequest, ScreenshotRequest, ModListRequest)) else None
        if waiter is not None:
            waiter(message)
        for listener in list(self.listeners):
            listener(message)
        return message

    def send(self, message):
        if not isinstance(message, (Welcome, HelloRequest, Reset)):
            capability = capability_for(message.TYPE)
            if capability is not None and capability not in self.enabled:
                return False
        for frame in frames(message, next(self.streams) & 0x7FFFFFFF):
            self._send(frame)
        return True

    def request(self, message, callback):
        request_id = next(self.ids) & 0x7FFFFFFF
        message.request_id = request_id
        self.pending[request_id] = callback
        if not self.send(message):
            self.pending.pop(request_id, None)
            return False
        return True

