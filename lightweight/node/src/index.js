/*
 * This file is part of Teho Server API, licensed under the MIT License.
 *
 * Copyright (c) 2026 SGM Studios LTD
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

'use strict';

const crypto = require('crypto');
const { CONSTANTS, CAPABILITIES, RANGES, MESSAGES } = require('./schema');

class ProtocolError extends Error {}

const WIRE = { string: 2, bytes: 2, uint32: 0, uint64: 0, sint32: 0, bool: 0, fixed32: 5 };
const BY_TYPE = new Map();
const TYPES = {};
for (const [name, spec] of Object.entries(MESSAGES)) {
  if (spec.type !== null) {
    BY_TYPE.set(spec.type, name);
    TYPES[name] = spec.type;
  }
}
const encoder = new TextEncoder();
const decoder = new TextDecoder('utf-8', { fatal: true });

class Writer {
  constructor() {
    this.buffer = new Uint8Array(64);
    this.length = 0;
  }

  ensure(extra) {
    if (this.length + extra > this.buffer.length) {
      const next = new Uint8Array(Math.max(this.buffer.length * 2, this.length + extra));
      next.set(this.buffer.subarray(0, this.length));
      this.buffer = next;
    }
  }

  byte(value) {
    this.ensure(1);
    this.buffer[this.length++] = value;
  }

  varint(value) {
    let v = BigInt.asUintN(64, BigInt(value));
    while (v > 0x7Fn) {
      this.byte(Number(v & 0x7Fn) | 0x80);
      v >>= 7n;
    }
    this.byte(Number(v));
  }

  tag(number, wire) {
    this.varint(number * 8 + wire);
  }

  raw(bytes) {
    this.varint(bytes.length);
    this.ensure(bytes.length);
    this.buffer.set(bytes, this.length);
    this.length += bytes.length;
  }

  scalar(number, kind, value) {
    switch (kind) {
      case 'string':
        this.tag(number, 2);
        this.raw(encoder.encode(String(value)));
        break;
      case 'bytes':
        this.tag(number, 2);
        this.raw(value);
        break;
      case 'uint32':
        this.tag(number, 0);
        this.varint(Number(value) >>> 0);
        break;
      case 'uint64':
        this.tag(number, 0);
        this.varint(value);
        break;
      case 'sint32': {
        const v = Number(value) | 0;
        this.tag(number, 0);
        this.varint(((v << 1) ^ (v >> 31)) >>> 0);
        break;
      }
      case 'bool':
        this.tag(number, 0);
        this.varint(value ? 1 : 0);
        break;
      case 'fixed32': {
        const v = Number(value) >>> 0;
        this.tag(number, 5);
        this.byte(v & 0xFF);
        this.byte((v >>> 8) & 0xFF);
        this.byte((v >>> 16) & 0xFF);
        this.byte((v >>> 24) & 0xFF);
        break;
      }
      default:
        throw new ProtocolError('unknown kind ' + kind);
    }
  }

  bytes() {
    return this.buffer.slice(0, this.length);
  }
}

class Reader {
  constructor(data) {
    this.data = data;
    this.pos = 0;
    this.wire = -1;
  }

  hasNext() {
    return this.pos < this.data.length;
  }

  varint() {
    let result = 0n;
    for (let shift = 0n; shift < 64n; shift += 7n) {
      if (this.pos >= this.data.length) {
        throw new ProtocolError('truncated varint');
      }
      const b = this.data[this.pos++];
      result |= BigInt(b & 0x7F) << shift;
      if ((b & 0x80) === 0) {
        return BigInt.asUintN(64, result);
      }
    }
    throw new ProtocolError('varint too long');
  }

  nextField() {
    const tag = this.varint();
    this.wire = Number(tag & 7n);
    const number = tag >> 3n;
    if (number < 1n || number > 0x1FFFFFFFn) {
      throw new ProtocolError('field number');
    }
    return Number(number);
  }

  expect(wire) {
    if (this.wire !== wire) {
      throw new ProtocolError('wire type ' + this.wire + ', expected ' + wire);
    }
  }

  take(count) {
    if (count < 0 || this.pos + count > this.data.length) {
      throw new ProtocolError('truncated field');
    }
    const out = this.data.slice(this.pos, this.pos + count);
    this.pos += count;
    return out;
  }

  delimited(limit) {
    this.expect(2);
    const size = this.varint();
    if (size > BigInt(limit)) {
      throw new ProtocolError('field of ' + size + ' bytes over ' + limit);
    }
    return this.take(Number(size));
  }

  skip() {
    switch (this.wire) {
      case 0:
        this.varint();
        break;
      case 1:
        this.take(8);
        break;
      case 2:
        this.take(Number(this.varint()));
        break;
      case 5:
        this.take(4);
        break;
      default:
        throw new ProtocolError('wire type ' + this.wire);
    }
  }

  scalar(kind, stringLimit, bytesLimit) {
    switch (kind) {
      case 'string':
        try {
          return decoder.decode(this.delimited(stringLimit));
        } catch (error) {
          if (error instanceof ProtocolError) {
            throw error;
          }
          throw new ProtocolError('invalid utf-8');
        }
      case 'bytes':
        return this.delimited(bytesLimit);
      case 'fixed32': {
        this.expect(5);
        const b = this.take(4);
        return (b[0] | (b[1] << 8) | (b[2] << 16) | (b[3] << 24)) >>> 0;
      }
      default:
        break;
    }
    this.expect(0);
    const v = this.varint();
    switch (kind) {
      case 'uint32':
        return Number(BigInt.asUintN(32, v));
      case 'uint64':
        return v;
      case 'sint32': {
        const u = Number(BigInt.asUintN(32, v));
        return (u >>> 1) ^ -(u & 1);
      }
      case 'bool':
        return v !== 0n;
      default:
        throw new ProtocolError('unknown kind ' + kind);
    }
  }
}

function defaultFor(kind, repeated) {
  if (repeated) {
    return [];
  }
  switch (kind) {
    case 'string':
      return '';
    case 'bytes':
      return new Uint8Array(0);
    case 'bool':
      return false;
    case 'uint64':
      return 0n;
    case 'uint32':
    case 'sint32':
    case 'fixed32':
      return 0;
    default:
      return complete(kind, {});
  }
}

function complete(name, message) {
  const out = {};
  for (const [, field, kind, repeated] of MESSAGES[name].fields) {
    out[field] = message[field] === undefined ? defaultFor(kind, repeated) : message[field];
  }
  return out;
}

function writeBody(name, message, writer) {
  for (const [number, field, kind, repeated] of MESSAGES[name].fields) {
    let value = message[field];
    if (value === undefined || value === null) {
      value = defaultFor(kind, repeated);
    }
    const items = repeated ? value : [value];
    for (const item of items) {
      if (kind in WIRE) {
        writer.scalar(number, kind, item);
      } else {
        const nested = new Writer();
        writeBody(kind, item || {}, nested);
        writer.tag(number, 2);
        writer.raw(nested.bytes());
      }
    }
  }
}

function readBody(name, data) {
  const spec = MESSAGES[name];
  const table = new Map(spec.fields.map((f) => [f[0], f]));
  const out = complete(name, {});
  const reader = new Reader(data);
  while (reader.hasNext()) {
    const number = reader.nextField();
    const field = table.get(number);
    if (!field) {
      reader.skip();
      continue;
    }
    const [, key, kind, repeated, stringLimit, bytesLimit, listLimit] = field;
    const item = kind in WIRE ? reader.scalar(kind, stringLimit, bytesLimit) : readBody(kind, reader.delimited(bytesLimit));
    if (repeated) {
      if (out[key].length < listLimit) {
        out[key].push(item);
      }
    } else {
      out[key] = item;
    }
  }
  return out;
}

function whole(name, message) {
  const spec = MESSAGES[name];
  if (!spec || spec.type === null) {
    throw new ProtocolError(name + ' is not a framed message');
  }
  const writer = new Writer();
  writer.varint(spec.type);
  writeBody(name, message || {}, writer);
  return writer.bytes();
}

function encode(name, message) {
  const frame = whole(name, message);
  if (frame.length > CONSTANTS.max_frame) {
    throw new ProtocolError('frame ' + frame.length + ' over ' + CONSTANTS.max_frame);
  }
  return frame;
}

function decodeAny(frame) {
  if (!frame || frame.length === 0) {
    throw new ProtocolError('empty frame');
  }
  const bytes = frame instanceof Uint8Array ? frame : new Uint8Array(frame);
  const reader = new Reader(bytes);
  const type = reader.varint();
  const name = type <= 0xFFFFFFFFn ? BY_TYPE.get(Number(type)) : undefined;
  if (!name) {
    return null;
  }
  return { type: name, ...readBody(name, bytes.subarray(reader.pos)) };
}

function decode(frame) {
  if (frame && frame.length > CONSTANTS.max_frame) {
    throw new ProtocolError('frame ' + frame.length + ' over ' + CONSTANTS.max_frame);
  }
  return decodeAny(frame);
}

function frames(name, message, stream) {
  const all = whole(name, message);
  if (all.length <= CONSTANTS.max_frame) {
    return [all];
  }
  if (all.length > CONSTANTS.max_stream) {
    throw new ProtocolError('message ' + all.length + ' over ' + CONSTANTS.max_stream);
  }
  const total = Math.ceil(all.length / CONSTANTS.slice);
  const out = [];
  for (let index = 0; index < total; index++) {
    out.push(encode('Chunk', { stream, index, total, data: all.subarray(index * CONSTANTS.slice, (index + 1) * CONSTANTS.slice) }));
  }
  return out;
}

function capabilityFor(type) {
  if (type === TYPES.AssetOffer || type === TYPES.AssetWant || type === TYPES.AssetData) {
    return 'asset:1';
  }
  for (const [low, high, capability] of RANGES) {
    if (type >= low && type <= high) {
      return capability;
    }
  }
  return null;
}

function welcome(hello, options) {
  const offered = new Set(hello.capabilities);
  const wanted = options.supported || CAPABILITIES;
  return {
    protocol: Math.min(CONSTANTS.version, Math.max(1, hello.protocol)),
    session: options.session === undefined ? crypto.randomBytes(8).readBigUInt64BE() : options.session,
    enabled: wanted.filter((c) => offered.has(c)),
    serverName: options.serverName,
    grant: options.grant || '',
    verified: options.verified || '',
  };
}

class Reassembler {
  constructor() {
    this.open = new Map();
  }

  accept(chunk, now = Date.now()) {
    for (const [stream, partial] of this.open) {
      if (now - partial.touched > 30000) {
        this.open.delete(stream);
      }
    }
    if (chunk.total < 1 || chunk.index >= chunk.total || chunk.total * CONSTANTS.slice > CONSTANTS.max_stream + CONSTANTS.slice) {
      throw new ProtocolError('chunk bounds');
    }
    let partial = this.open.get(chunk.stream);
    if (!partial || partial.parts.length !== chunk.total) {
      if (!partial && this.open.size >= 8) {
        throw new ProtocolError('too many open streams');
      }
      partial = { parts: new Array(chunk.total).fill(null), bytes: 0, received: 0, touched: now };
      this.open.set(chunk.stream, partial);
    }
    if (partial.parts[chunk.index] === null) {
      partial.bytes += chunk.data.length;
      if (partial.bytes > CONSTANTS.max_stream) {
        this.open.delete(chunk.stream);
        throw new ProtocolError('stream over ' + CONSTANTS.max_stream);
      }
      partial.parts[chunk.index] = chunk.data;
      partial.received++;
    }
    partial.touched = now;
    if (partial.received < chunk.total) {
      return null;
    }
    this.open.delete(chunk.stream);
    const joined = new Uint8Array(partial.bytes);
    let offset = 0;
    for (const part of partial.parts) {
      joined.set(part, offset);
      offset += part.length;
    }
    if (new Reader(joined).varint() === BigInt(CONSTANTS.chunk)) {
      throw new ProtocolError('nested chunk');
    }
    return decodeAny(joined);
  }

  clear() {
    this.open.clear();
  }
}

const SERVER_REQUESTS = new Set(['InfoRequest', 'AssertRequest', 'AttestRequest', 'InspectRequest', 'ScreenshotRequest', 'ModListRequest']);

class Session {
  constructor(options) {
    this.options = options;
    this.hello = null;
    this.enabled = [];
    this.reassembler = new Reassembler();
    this.listeners = new Set();
    this.pending = new Map();
    this.nextId = 1;
    this.nextStream = 1;
  }

  has(capability) {
    return this.enabled.includes(capability);
  }

  on(listener) {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  receive(frame) {
    let message = decode(frame);
    if (message && message.type === 'Chunk') {
      message = this.reassembler.accept(message);
    }
    if (!message) {
      return null;
    }
    if (message.type === 'Hello') {
      this.hello = message;
      const reply = welcome(message, this.options);
      this.enabled = reply.enabled;
      this.reassembler.clear();
      this.options.send(encode('Welcome', reply));
    }
    if (message.requestId && !SERVER_REQUESTS.has(message.type)) {
      const waiter = this.pending.get(message.requestId);
      if (waiter) {
        this.pending.delete(message.requestId);
        waiter(message);
      }
    }
    for (const listener of [...this.listeners]) {
      listener(message);
    }
    return message;
  }

  send(name, message = {}) {
    if (name !== 'Welcome' && name !== 'HelloRequest' && name !== 'Reset') {
      const capability = capabilityFor(TYPES[name]);
      if (capability && !this.has(capability)) {
        return false;
      }
    }
    for (const frame of frames(name, message, this.nextStream++ & 0x7FFFFFFF)) {
      this.options.send(frame);
    }
    return true;
  }

  request(name, message = {}) {
    const requestId = this.nextId++ & 0x7FFFFFFF;
    return new Promise((resolve, reject) => {
      this.pending.set(requestId, resolve);
      if (!this.send(name, { ...message, requestId })) {
        this.pending.delete(requestId);
        reject(new ProtocolError(name + ' needs ' + capabilityFor(TYPES[name]) + ', which this player did not enable'));
      }
    });
  }
}

module.exports = {
  CHANNEL: CONSTANTS.channel,
  VERSION: CONSTANTS.version,
  MAX_FRAME: CONSTANTS.max_frame,
  CAPABILITIES,
  TYPES,
  MESSAGES,
  ProtocolError,
  encode,
  decode,
  frames,
  capabilityFor,
  welcome,
  Reassembler,
  Session,
};
