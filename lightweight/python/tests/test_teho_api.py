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

import json
import os
import sys
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.dirname(HERE))

import teho_api as t


def hexbytes(text):
    return bytes.fromhex(text.replace(" ", ""))


class Vectors(unittest.TestCase):
    def test_hello(self):
        frame = hexbytes("01 08 01 12 01 74 1a 07 31 2e 32 31 2e 31 31 22 06 66 61 62 72 69 63 2a 08 6e 6f 74 69 66 79 3a 31")
        hello = t.Hello(protocol=1, client="t", minecraft="1.21.11", platform="fabric", capabilities=["notify:1"])
        self.assertEqual(t.encode(hello), frame)
        self.assertEqual(t.decode(frame), hello)

    def test_notify(self):
        frame = hexbytes("10 0a 01 61 12 01 54 1a 01 42 25 ff a9 4d 00 28 b8 17 30 01")
        notify = t.Notify(id="a", title="T", body="B", accent=0x4DA9FF, duration_ms=3000, sound=True)
        self.assertEqual(t.encode(notify), frame)

    def test_waypoint(self):
        frame = hexbytes("20 0a 01 73 12 05 53 70 61 77 6e 18 01 20 80 01 28 00 32 09 6f 76 65 72 77 6f 72 6c 64 3d 00 00 ff 00 40 00")
        waypoint = t.WaypointSet(id="s", name="Spawn", x=-1, y=64, z=0, dimension="overworld", color=0xFF0000, focus=False)
        self.assertEqual(t.encode(waypoint), frame)
        self.assertEqual(t.decode(frame), waypoint)

    def test_reset(self):
        self.assertEqual(t.encode(t.Reset()), b"\x04")

    def test_unknown_fields_are_skipped(self):
        frame = hexbytes("11 0a 01 61 f8 ff 03 01 9a 01 03 61 62 63 3d 01 02 03 04 11 01 02 03 04 05 06 07 08")
        self.assertEqual(t.decode(frame), t.Dismiss(id="a"))

    def test_unknown_type_is_ignored(self):
        self.assertIsNone(t.decode(hexbytes("ff 7f 0a 01 61")))

    def test_limits(self):
        with self.assertRaises(t.ProtocolError):
            t.decode(b"\x10" + b"\x00" * 32000)
        with self.assertRaises(t.ProtocolError):
            t.decode(t.encode(t.Dismiss(id="x" * 2000)))
        with self.assertRaises(t.ProtocolError):
            t.decode(hexbytes("11 0d 01 61"))


class Corpus(unittest.TestCase):
    def test_corpus_round_trips(self):
        with open(os.path.join(os.path.dirname(os.path.dirname(HERE)), "corpus.json"), encoding="utf-8") as handle:
            corpus = json.load(handle)
        self.assertEqual(len(corpus), len(t.BY_TYPE))
        for entry in corpus:
            frame = bytes.fromhex(entry["frame"])
            message = t.decode(frame)
            self.assertEqual(type(message).__name__, entry["name"])
            self.assertEqual(t.to_json(message), entry["values"])
            self.assertEqual(t.encode(t.from_json(entry["name"], entry["values"])), frame)

    def test_defaults_round_trip(self):
        for cls in t.BY_TYPE.values():
            self.assertEqual(t.decode(t.encode(cls())), cls())


class Chunking(unittest.TestCase):
    def test_large_messages_split_and_join(self):
        big = t.ScreenshotResult(request_id=7, granted=True, png=bytes(range(256)) * 400, width=1920, height=1080)
        parts = t.frames(big, 3)
        self.assertGreater(len(parts), 1)
        self.assertTrue(all(len(p) <= t.MAX_FRAME for p in parts))
        joiner = t.Reassembler()
        result = None
        for part in reversed(parts):
            result = joiner.accept(t.decode(part))
        self.assertEqual(result, big)


class Sessions(unittest.TestCase):
    def test_handshake_and_gating(self):
        sent = []
        session = t.Session(sent.append, "Example", supported=["notify:1", "hud:1"])
        session.receive(t.encode(t.Hello(protocol=2, client="teho", minecraft="1.21.11", platform="fabric", capabilities=["notify:1", "world:1"])))
        welcome = t.decode(sent[0])
        self.assertEqual(welcome.enabled, ["notify:1"])
        self.assertEqual(welcome.server_name, "Example")
        self.assertTrue(session.send(t.Notify(id="hi", title="Hello")))
        self.assertFalse(session.send(t.Timer(id="round")))
        self.assertEqual(len(sent), 2)

    def test_requests_resolve(self):
        sent = []
        replies = []
        session = t.Session(sent.append, "Example", supported=["info:1"])
        session.receive(t.encode(t.Hello(protocol=2, capabilities=["info:1"])))
        self.assertTrue(session.request(t.InfoRequest(fields=["input"]), replies.append))
        request = t.decode(sent[-1])
        session.receive(t.encode(t.InfoReply(request_id=request.request_id, granted=["input"], input="controller")))
        self.assertEqual(replies[0].input, "controller")


if __name__ == "__main__":
    unittest.main()
