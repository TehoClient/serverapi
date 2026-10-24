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

import struct


class ProtocolError(ValueError):
    pass


SCALAR_WIRE = {"string": 2, "bytes": 2, "uint32": 0, "uint64": 0, "sint32": 0, "bool": 0, "fixed32": 5}


class Writer:
    def __init__(self):
        self.out = bytearray()

    def varint(self, value):
        value &= 0xFFFFFFFFFFFFFFFF
        while value > 0x7F:
            self.out.append((value & 0x7F) | 0x80)
            value >>= 7
        self.out.append(value)

    def tag(self, number, wire):
        self.varint((number << 3) | wire)

    def scalar(self, number, kind, value):
        if kind == "string":
            data = value.encode("utf-8")
            self.tag(number, 2)
            self.varint(len(data))
            self.out += data
        elif kind == "bytes":
            self.tag(number, 2)
            self.varint(len(value))
            self.out += bytes(value)
        elif kind in ("uint32", "uint64"):
            self.tag(number, 0)
            self.varint(int(value) & (0xFFFFFFFF if kind == "uint32" else 0xFFFFFFFFFFFFFFFF))
        elif kind == "sint32":
            value = int(value)
            if value < -0x80000000 or value > 0x7FFFFFFF:
                raise ProtocolError("sint32 out of range")
            self.tag(number, 0)
            self.varint(((value << 1) ^ (value >> 31)) & 0xFFFFFFFF)
        elif kind == "bool":
            self.tag(number, 0)
            self.varint(1 if value else 0)
        elif kind == "fixed32":
            self.tag(number, 5)
            self.out += struct.pack("<I", int(value) & 0xFFFFFFFF)
        else:
            raise ProtocolError("unknown kind " + kind)


class Reader:
    def __init__(self, data):
        self.data = memoryview(data)
        self.pos = 0
        self.wire = -1

    def has_next(self):
        return self.pos < len(self.data)

    def varint(self):
        result = 0
        shift = 0
        while shift < 64:
            if self.pos >= len(self.data):
                raise ProtocolError("truncated varint")
            byte = self.data[self.pos]
            self.pos += 1
            result |= (byte & 0x7F) << shift
            if not byte & 0x80:
                return result & 0xFFFFFFFFFFFFFFFF
            shift += 7
        raise ProtocolError("varint too long")

    def next_field(self):
        tag = self.varint()
        self.wire = tag & 7
        number = tag >> 3
        if number < 1 or number > 0x1FFFFFFF:
            raise ProtocolError("field number")
        return number

    def expect(self, wire):
        if self.wire != wire:
            raise ProtocolError("wire type %d, expected %d" % (self.wire, wire))

    def take(self, count):
        if count < 0 or self.pos + count > len(self.data):
            raise ProtocolError("truncated field")
        chunk = bytes(self.data[self.pos:self.pos + count])
        self.pos += count
        return chunk

    def length_delimited(self, limit):
        self.expect(2)
        size = self.varint()
        if size > limit:
            raise ProtocolError("field of %d bytes over %d" % (size, limit))
        return self.take(size)

    def skip(self):
        if self.wire == 0:
            self.varint()
        elif self.wire == 1:
            self.take(8)
        elif self.wire == 2:
            self.take(self.varint())
        elif self.wire == 5:
            self.take(4)
        else:
            raise ProtocolError("wire type %d" % self.wire)

    def scalar(self, kind, string_limit, bytes_limit):
        if kind == "string":
            try:
                return self.length_delimited(string_limit).decode("utf-8")
            except UnicodeDecodeError as error:
                raise ProtocolError("invalid utf-8") from error
        if kind == "bytes":
            return self.length_delimited(bytes_limit)
        if kind == "fixed32":
            self.expect(5)
            return struct.unpack("<I", self.take(4))[0]
        self.expect(0)
        value = self.varint()
        if kind == "uint32":
            return value & 0xFFFFFFFF
        if kind == "uint64":
            return value
        if kind == "sint32":
            value &= 0xFFFFFFFF
            return (value >> 1) ^ -(value & 1)
        if kind == "bool":
            return value != 0
        raise ProtocolError("unknown kind " + kind)
