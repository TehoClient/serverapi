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

const test = require('node:test');
const assert = require('node:assert');
const fs = require('fs');
const path = require('path');
const t = require('../src/index');

const hex = (text) => Uint8Array.from(Buffer.from(text.replace(/ /g, ''), 'hex'));
const camel = (name) => name.replace(/_([a-z])/g, (_, c) => c.toUpperCase());

function fromJson(name, values) {
  const out = {};
  for (const [, field, kind, repeated] of t.MESSAGES[name].fields) {
    const raw = values[field.replace(/[A-Z]/g, (c) => '_' + c.toLowerCase())];
    if (raw === undefined) {
      continue;
    }
    const convert = (item) => {
      if (kind === 'bytes') return hex(item);
      if (kind === 'uint64') return BigInt(item);
      if (t.MESSAGES[kind]) return fromJson(kind, item);
      return item;
    };
    out[field] = repeated ? raw.map(convert) : convert(raw);
  }
  return out;
}

function toJson(name, message) {
  const out = {};
  for (const [, field, kind, repeated] of t.MESSAGES[name].fields) {
    const convert = (item) => {
      if (kind === 'bytes') return Buffer.from(item).toString('hex');
      if (kind === 'uint64') return item.toString();
      if (t.MESSAGES[kind]) return toJson(kind, item);
      return item;
    };
    const key = field.replace(/[A-Z]/g, (c) => '_' + c.toLowerCase());
    out[key] = repeated ? message[field].map(convert) : convert(message[field]);
  }
  return out;
}

test('hello vector', () => {
  const frame = hex('01 08 01 12 01 74 1a 07 31 2e 32 31 2e 31 31 22 06 66 61 62 72 69 63 2a 08 6e 6f 74 69 66 79 3a 31');
  const hello = { protocol: 1, client: 't', minecraft: '1.21.11', platform: 'fabric', capabilities: ['notify:1'] };
  assert.deepStrictEqual(t.encode('Hello', hello), frame);
  assert.deepStrictEqual(t.decode(frame), { type: 'Hello', ...hello });
});

test('notify and waypoint vectors', () => {
  assert.deepStrictEqual(t.encode('Notify', { id: 'a', title: 'T', body: 'B', accent: 0x4DA9FF, durationMs: 3000, sound: true }), hex('10 0a 01 61 12 01 54 1a 01 42 25 ff a9 4d 00 28 b8 17 30 01'));
  const frame = hex('20 0a 01 73 12 05 53 70 61 77 6e 18 01 20 80 01 28 00 32 09 6f 76 65 72 77 6f 72 6c 64 3d 00 00 ff 00 40 00');
  const waypoint = { id: 's', name: 'Spawn', x: -1, y: 64, z: 0, dimension: 'overworld', color: 0xFF0000, focus: false };
  assert.deepStrictEqual(t.encode('WaypointSet', waypoint), frame);
  assert.deepStrictEqual(t.decode(frame), { type: 'WaypointSet', ...waypoint });
  assert.deepStrictEqual(t.encode('Reset'), hex('04'));
});

test('unknown fields and types', () => {
  assert.deepStrictEqual(t.decode(hex('11 0a 01 61 f8 ff 03 01 9a 01 03 61 62 63 3d 01 02 03 04 11 01 02 03 04 05 06 07 08')), { type: 'Dismiss', id: 'a' });
  assert.strictEqual(t.decode(hex('ff 7f 0a 01 61')), null);
});

test('limits', () => {
  assert.throws(() => t.decode(new Uint8Array(32001).fill(0x10)), t.ProtocolError);
  assert.throws(() => t.decode(t.encode('Dismiss', { id: 'x'.repeat(2000) })), t.ProtocolError);
  assert.throws(() => t.decode(hex('11 0d 01 61')), t.ProtocolError);
});

test('corpus matches the Java and Python encoders', () => {
  const corpus = JSON.parse(fs.readFileSync(path.join(__dirname, '..', '..', 'corpus.json'), 'utf8'));
  assert.strictEqual(corpus.length, Object.keys(t.TYPES).length);
  for (const entry of corpus) {
    const frame = hex(entry.frame);
    const message = t.decode(frame);
    assert.strictEqual(message.type, entry.name);
    assert.deepStrictEqual(toJson(entry.name, message), entry.values, entry.name);
    assert.deepStrictEqual(t.encode(entry.name, fromJson(entry.name, entry.values)), frame, entry.name);
  }
});

test('chunking', () => {
  const png = new Uint8Array(102400).map((_, i) => i & 0xFF);
  const parts = t.frames('ScreenshotResult', { requestId: 7, granted: true, png, width: 1920, height: 1080 }, 3);
  assert.ok(parts.length > 1);
  const joiner = new t.Reassembler();
  let result = null;
  for (const part of parts.reverse()) {
    result = joiner.accept(t.decode(part));
  }
  assert.deepStrictEqual(result.png, png);
  assert.strictEqual(result.width, 1920);
});

test('session handshake, gating and requests', async () => {
  const sent = [];
  const session = new t.Session({ serverName: 'Example', supported: ['notify:1', 'info:1'], send: (f) => sent.push(f) });
  session.receive(t.encode('Hello', { protocol: 2, client: 'teho', capabilities: ['notify:1', 'info:1', 'world:1'] }));
  const welcome = t.decode(sent[0]);
  assert.deepStrictEqual(welcome.enabled, ['notify:1', 'info:1']);
  assert.strictEqual(welcome.serverName, 'Example');
  assert.strictEqual(session.send('Notify', { id: 'hi', title: 'Hello' }), true);
  assert.strictEqual(session.send('Timer', { id: 'round' }), false);
  const reply = session.request('InfoRequest', { fields: ['input'] });
  const request = t.decode(sent[sent.length - 1]);
  session.receive(t.encode('InfoReply', { requestId: request.requestId, granted: ['input'], input: 'controller' }));
  assert.strictEqual((await reply).input, 'controller');
  await assert.rejects(session.request('AssertRequest', { nonce: 'n' }), t.ProtocolError);
});

test('field names are camelCase', () => {
  for (const spec of Object.values(t.MESSAGES)) {
    for (const [, field] of spec.fields) {
      assert.strictEqual(field, camel(field));
    }
  }
});
