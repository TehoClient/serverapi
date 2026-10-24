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

import io
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.abspath(__file__))
SDK = os.path.dirname(ROOT)
PROTO = os.path.join(SDK, "protocol", "teho_api.proto")
JAVA = os.path.join(SDK, "protocol", "src", "main", "java", "dev", "tehoclient", "api", "protocol")

SIGNALS = {"HelloRequest": "HELLO_REQUEST", "Reset": "RESET", "WaypointClear": "WAYPOINT_CLEAR", "CooldownClear": "COOLDOWN_CLEAR", "SidebarClear": "SIDEBAR_CLEAR"}
SCALARS = {"string", "uint32", "uint64", "sint32", "fixed32", "bool", "bytes"}
PY_KEYWORDS = {"from", "global", "import", "in", "is", "lambda", "pass", "class", "def", "return", "yield", "with", "as", "async", "await", "not", "or", "and", "del", "try", "except", "raise", "finally", "while", "for", "if", "else", "elif", "break", "continue", "None", "True", "False", "nonlocal", "assert"}
RUST_KEYWORDS = {"type", "loop", "ref", "move", "match", "mod", "fn", "impl", "use", "where", "struct", "enum", "trait", "self", "super", "crate", "static", "const", "mut", "box", "in", "for", "if", "else", "while", "return", "break", "continue", "let", "pub", "as", "async", "await", "dyn", "abstract", "final", "override", "priv", "virtual", "yield", "unsafe", "extern", "macro", "try"}


def read(path):
    with io.open(path, encoding="utf-8", newline="") as handle:
        return handle.read()


def write(path, text):
    if path.endswith((".js", ".ts", ".py", ".rs")):
        notice = read(os.path.join(SDK, "LICENSE_HEADER.txt")).strip()
        if path.endswith(".py"):
            header = "\n".join("# " + line if line else "#" for line in notice.splitlines())
        else:
            header = "/*\n" + "\n".join(" * " + line if line else " *" for line in notice.splitlines()) + "\n */"
        text = header + "\n\n" + text
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with io.open(path, "w", encoding="utf-8", newline="\n") as handle:
        handle.write(text)


def camel(name):
    head, *rest = name.split("_")
    return head + "".join(part[:1].upper() + part[1:] for part in rest)


def pascal(name):
    return "".join(part[:1].upper() + part[1:] for part in name.split("_"))


def parse_proto():
    text = read(PROTO)
    messages = []
    for match in re.finditer(r"message (\w+) \{(.*?)\}", text, re.S):
        fields = []
        for line in match.group(2).splitlines():
            field = re.match(r"\s*(repeated )?(\w+) (\w+) = (\d+)", line)
            if field:
                fields.append({"name": field.group(3), "kind": field.group(2), "repeated": bool(field.group(1)), "number": int(field.group(4))})
        messages.append({"name": match.group(1), "fields": sorted(fields, key=lambda f: f["number"])})
    return messages


def parse_constants():
    text = read(os.path.join(JAVA, "TehoProtocol.java"))
    ints = {name: int(value, 0) for name, value in re.findall(r"public static final int (\w+) = (0x[0-9A-Fa-f]+|\d+);", text)}
    strings = dict(re.findall(r'public static final String (\w+) = "([^"]+)";', text))
    cases = {}
    for block in re.finditer(r"((?:\s*case \w+:)+)\s*return (\w+)\.read\(in\);", text):
        for const in re.findall(r"case (\w+):", block.group(1)):
            cases[block.group(2)] = const
    ranges = []
    for low, high, cap in re.findall(r"if \(type >= (0x[0-9A-Fa-f]+) && type <= (0x[0-9A-Fa-f]+)\) \{\s*return (\w+);", text):
        ranges.append((int(low, 16), int(high, 16), strings[cap]))
    return ints, strings, cases, ranges


def parse_limits(name):
    path = os.path.join(JAVA, name + ".java")
    limits = {}
    if not os.path.exists(path):
        return limits
    text = read(path)
    body = text[text.find(" read(") if " read(" in text else 0:]
    for case in re.finditer(r"case (\d+):(.*?)break;", body, re.S):
        number = int(case.group(1))
        chunk = case.group(2)
        entry = {}
        string = re.search(r"readString\(TehoProtocol\.(\w+)\)", chunk)
        if string:
            entry["string"] = string.group(1)
        raw = re.search(r"readBytes\((?:TehoProtocol|Chunking)\.(\w+)\)", chunk)
        if raw:
            entry["bytes"] = raw.group(1)
        cap = re.search(r"size\(\) < TehoProtocol\.(\w+)", chunk)
        if cap:
            entry["list"] = cap.group(1)
        limits[number] = entry
    return limits


def build():
    messages = parse_proto()
    ints, strings, cases, ranges = parse_constants()
    values = {"MAX_STRING": ints["MAX_STRING"], "MAX_GRANT": ints["MAX_GRANT"], "MAX_LIST": ints["MAX_LIST"], "MAX_POINTS": ints["MAX_POINTS"], "MAX_STREAM": 4 * 1024 * 1024}
    names = {m["name"] for m in messages}
    for message in messages:
        const = SIGNALS.get(message["name"]) or cases.get(message["name"])
        message["type"] = ints[const] if const else None
        message["capability"] = None
        if message["type"] is not None:
            for low, high, cap in ranges:
                if low <= message["type"] <= high:
                    message["capability"] = cap
            if message["type"] in (ints["ASSET_OFFER"], ints["ASSET_WANT"], ints["ASSET_DATA"]):
                message["capability"] = strings["ASSET"]
        limits = parse_limits(message["name"])
        for field in message["fields"]:
            entry = limits.get(field["number"], {})
            field["string_limit"] = values[entry.get("string", "MAX_STRING")]
            field["bytes_limit"] = values[entry.get("bytes", "MAX_STREAM")]
            field["list_limit"] = values[entry.get("list", "MAX_LIST")]
            if field["kind"] not in SCALARS and field["kind"] not in names:
                raise SystemExit("unknown field kind " + field["kind"])
    missing = [m["name"] for m in messages if m["type"] is None and not any(f["kind"] == m["name"] for o in messages for f in o["fields"])]
    if missing:
        raise SystemExit("no type id for " + ", ".join(missing))
    framed = sorted({m["type"] for m in messages if m["type"] is not None})
    if len(framed) != len([m for m in messages if m["type"] is not None]):
        raise SystemExit("duplicate type ids")
    constants = {"channel": "teho:api", "version": ints["VERSION"], "max_frame": ints["MAX_FRAME"], "max_string": ints["MAX_STRING"], "max_list": ints["MAX_LIST"], "max_stream": values["MAX_STREAM"], "slice": 30000, "chunk": ints["CHUNK"]}
    capabilities = [strings[c] for c in re.findall(r"Arrays\.asList\(([^)]*)\)", read(os.path.join(JAVA, "TehoProtocol.java")))[0].replace(" ", "").split(",")]
    return messages, constants, ranges, capabilities, strings


def js_schema(messages, constants, ranges, capabilities):
    out = ["'use strict';", ""]
    out.append("const CONSTANTS = " + json.dumps(constants, indent=2) + ";")
    out.append("")
    out.append("const CAPABILITIES = " + json.dumps(capabilities) + ";")
    out.append("")
    out.append("const RANGES = " + json.dumps([[lo, hi, cap] for lo, hi, cap in ranges]) + ";")
    out.append("")
    out.append("const MESSAGES = {")
    for message in messages:
        fields = ", ".join("[%d, %s, %s, %s, %d, %d, %d]" % (f["number"], json.dumps(camel(f["name"])), json.dumps(f["kind"]), "true" if f["repeated"] else "false", f["string_limit"], f["bytes_limit"], f["list_limit"]) for f in message["fields"])
        out.append("  %s: { type: %s, capability: %s, fields: [%s] }," % (message["name"], "null" if message["type"] is None else "0x%X" % message["type"], json.dumps(message["capability"]), fields))
    out.append("};")
    out.append("")
    out.append("module.exports = { CONSTANTS, CAPABILITIES, RANGES, MESSAGES };")
    return "\n".join(out) + "\n"


def ts_type(field):
    kind = field["kind"]
    base = {"string": "string", "uint32": "number", "sint32": "number", "fixed32": "number", "bool": "boolean", "bytes": "Uint8Array", "uint64": "bigint"}.get(kind, kind)
    return base + "[]" if field["repeated"] else base


def ts_declarations(messages):
    out = []
    for message in messages:
        out.append("export interface %s {" % message["name"])
        for field in message["fields"]:
            out.append("  %s?: %s;" % (camel(field["name"]), ts_type(field)))
        out.append("}")
        out.append("")
    framed = [m for m in messages if m["type"] is not None]
    out.append("export interface MessageMap {")
    for message in framed:
        out.append("  %s: %s;" % (message["name"], message["name"]))
    out.append("}")
    out.append("")
    out.append("export type MessageName = keyof MessageMap;")
    out.append("")
    out.append("export type Decoded = { [K in MessageName]: { type: K } & Required<MessageMap[K]> }[MessageName];")
    out.append("")
    out.append("export declare const CHANNEL: string;")
    out.append("export declare const VERSION: number;")
    out.append("export declare const MAX_FRAME: number;")
    out.append("export declare const CAPABILITIES: string[];")
    out.append("export declare const TYPES: { [K in MessageName]: number };")
    out.append("export declare class ProtocolError extends Error {}")
    out.append("export declare function encode<K extends MessageName>(name: K, message?: MessageMap[K]): Uint8Array;")
    out.append("export declare function decode(frame: Uint8Array): Decoded | null;")
    out.append("export declare function frames<K extends MessageName>(name: K, message: MessageMap[K], stream: number): Uint8Array[];")
    out.append("export declare function capabilityFor(type: number): string | null;")
    out.append("export declare function welcome(hello: Required<Hello>, options: { serverName: string; supported?: string[]; session?: bigint; grant?: string; verified?: string }): Welcome;")
    out.append("export declare class Reassembler {")
    out.append("  accept(chunk: Required<Chunk>, now?: number): Decoded | null;")
    out.append("  clear(): void;")
    out.append("}")
    out.append("export declare class Session {")
    out.append("  constructor(options: { serverName: string; supported?: string[]; grant?: string; verified?: string; send: (frame: Uint8Array) => void });")
    out.append("  readonly hello: Required<Hello> | null;")
    out.append("  readonly enabled: string[];")
    out.append("  has(capability: string): boolean;")
    out.append("  receive(frame: Uint8Array): Decoded | null;")
    out.append("  send<K extends MessageName>(name: K, message?: MessageMap[K]): boolean;")
    out.append("  request<K extends MessageName>(name: K, message?: MessageMap[K]): Promise<Decoded>;")
    out.append("  on(listener: (message: Decoded) => void): () => void;")
    out.append("}")
    return "\n".join(out) + "\n"


def py_type(field):
    kind = field["kind"]
    base = {"string": "str", "uint32": "int", "sint32": "int", "fixed32": "int", "bool": "bool", "bytes": "bytes", "uint64": "int"}.get(kind, '"%s"' % kind)
    return "List[%s]" % base if field["repeated"] else base


def py_default(field):
    if field["repeated"]:
        return "field(default_factory=list)"
    kind = field["kind"]
    if kind in ("string",):
        return '""'
    if kind == "bytes":
        return 'b""'
    if kind == "bool":
        return "False"
    if kind in SCALARS:
        return "0"
    return "field(default_factory=lambda: %s())" % kind


def py_name(name):
    return name + "_" if name in PY_KEYWORDS else name


def py_messages(messages, constants, ranges, capabilities):
    out = ["from dataclasses import dataclass, field", "from typing import List", ""]
    out.append("CHANNEL = %s" % json.dumps(constants["channel"]))
    out.append("VERSION = %d" % constants["version"])
    out.append("MAX_FRAME = %d" % constants["max_frame"])
    out.append("MAX_STRING = %d" % constants["max_string"])
    out.append("MAX_LIST = %d" % constants["max_list"])
    out.append("MAX_STREAM = %d" % constants["max_stream"])
    out.append("SLICE = %d" % constants["slice"])
    out.append("CAPABILITIES = %s" % json.dumps(capabilities))
    out.append("RANGES = %s" % json.dumps([[lo, hi, cap] for lo, hi, cap in ranges]))
    out.append("")
    for message in messages:
        out.append("")
        out.append("@dataclass")
        out.append("class %s:" % message["name"])
        out.append("    TYPE = %s" % ("None" if message["type"] is None else "0x%X" % message["type"]))
        out.append("    CAPABILITY = %s" % ("None" if message["capability"] is None else json.dumps(message["capability"])))
        out.append("    FIELDS = (%s)" % "".join("(%d, %s, %s, %s, %d, %d, %d), " % (f["number"], json.dumps(py_name(f["name"])), json.dumps(f["kind"]), "True" if f["repeated"] else "False", f["string_limit"], f["bytes_limit"], f["list_limit"]) for f in message["fields"]))
        for f in message["fields"]:
            out.append("    %s: %s = %s" % (py_name(f["name"]), py_type(f), py_default(f)))
    out.append("")
    out.append("")
    out.append("MESSAGES = {cls.__name__: cls for cls in (%s)}" % ", ".join(m["name"] for m in messages))
    out.append("BY_TYPE = {cls.TYPE: cls for cls in MESSAGES.values() if cls.TYPE is not None}")
    return "\n".join(out) + "\n"


def rust_ident(name):
    return "r#" + name if name in RUST_KEYWORDS else name


def rust_type(field):
    kind = field["kind"]
    base = {"string": "String", "uint32": "u32", "sint32": "i32", "fixed32": "u32", "bool": "bool", "bytes": "Vec<u8>", "uint64": "u64"}.get(kind, kind)
    return "Vec<%s>" % base if field["repeated"] else base


def rust_write(field):
    kind = field["kind"]
    n = field["number"]
    if field["repeated"]:
        value, deref = "item", "*item"
    else:
        value = "self." + rust_ident(field["name"])
        deref = value
    if kind == "string":
        call = "w.string(%d, &%s)" % (n, value) if not field["repeated"] else "w.string(%d, item)" % n
    elif kind == "bytes":
        call = "w.bytes(%d, &%s)" % (n, value) if not field["repeated"] else "w.bytes(%d, item)" % n
    elif kind in ("uint32", "bool"):
        call = "w.uint(%d, u64::from(%s))" % (n, deref)
    elif kind == "uint64":
        call = "w.uint(%d, %s)" % (n, deref)
    elif kind == "sint32":
        call = "w.sint(%d, %s)" % (n, deref)
    elif kind == "fixed32":
        call = "w.fixed32(%d, %s)" % (n, deref)
    else:
        call = "w.bytes(%d, &%s.encode_body())" % (n, value)
    if field["repeated"]:
        return "        for item in &self.%s {\n            %s;\n        }" % (rust_ident(field["name"]), call)
    return "        %s;" % call


def rust_read(field):
    kind = field["kind"]
    if kind == "string":
        expr = "r.string(%d)?" % field["string_limit"]
    elif kind == "bytes":
        expr = "r.bytes(%d)?.to_vec()" % field["bytes_limit"]
    elif kind == "uint32":
        expr = "r.varint()? as u32"
    elif kind == "uint64":
        expr = "r.varint()?"
    elif kind == "sint32":
        expr = "r.sint()?"
    elif kind == "fixed32":
        expr = "r.fixed32()?"
    elif kind == "bool":
        expr = "r.varint()? != 0"
    else:
        expr = "%s::decode_body(r.bytes(%d)?)?" % (kind, field["bytes_limit"])
    ident = "out." + rust_ident(field["name"])
    if field["repeated"]:
        return "                %d => {\n                    let item = %s;\n                    if %s.len() < %d {\n                        %s.push(item);\n                    }\n                }" % (field["number"], expr, ident, field["list_limit"], ident)
    return "                %d => %s = %s," % (field["number"], ident, expr)


def rust_messages(messages, constants, ranges, capabilities):
    out = ["use crate::wire::{Error, Reader, Writer};", ""]
    out.append("pub const CHANNEL: &str = %s;" % json.dumps(constants["channel"]))
    out.append("pub const VERSION: u32 = %d;" % constants["version"])
    out.append("pub const MAX_FRAME: usize = %d;" % constants["max_frame"])
    out.append("pub const MAX_STREAM: usize = %d;" % constants["max_stream"])
    out.append("pub const SLICE: usize = %d;" % constants["slice"])
    out.append("pub const CAPABILITIES: &[&str] = &[%s];" % ", ".join(json.dumps(c) for c in capabilities))
    out.append("pub const RANGES: &[(u32, u32, &str)] = &[%s];" % ", ".join("(0x%X, 0x%X, %s)" % (lo, hi, json.dumps(cap)) for lo, hi, cap in ranges))
    out.append("")
    for message in messages:
        out.append("#[derive(Debug, Clone, Default, PartialEq)]")
        if message["fields"]:
            out.append("pub struct %s {" % message["name"])
            for f in message["fields"]:
                out.append("    pub %s: %s," % (rust_ident(f["name"]), rust_type(f)))
            out.append("}")
        else:
            out.append("pub struct %s {}" % message["name"])
        out.append("")
        out.append("impl %s {" % message["name"])
        if message["type"] is not None:
            out.append("    pub const TYPE: u32 = 0x%X;" % message["type"])
            out.append("")
        out.append("    pub fn encode_body(&self) -> Vec<u8> {")
        if message["fields"]:
            out.append("        let mut w = Writer::new();")
            for f in message["fields"]:
                out.append(rust_write(f))
            out.append("        w.into_bytes()")
        else:
            out.append("        Vec::new()")
        out.append("    }")
        out.append("")
        out.append("    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {")
        out.append("        let mut r = Reader::new(bytes);")
        out.append("        let mut out = Self::default();" if message["fields"] else "        let out = Self::default();")
        out.append("        while r.has_next() {")
        out.append("            let number = r.next_field()?;")
        if message["fields"]:
            out.append("            match number {")
            for f in message["fields"]:
                out.append(rust_read_checked(f))
            out.append("                _ => r.skip()?,")
            out.append("            }")
        else:
            out.append("            let _ = number;")
            out.append("            r.skip()?;")
        out.append("        }")
        out.append("        Ok(out)")
        out.append("    }")
        out.append("}")
        out.append("")
    framed = [m for m in messages if m["type"] is not None]
    out.append("#[derive(Debug, Clone, PartialEq)]")
    out.append("pub enum Message {")
    for m in framed:
        out.append("    %s(%s)," % (m["name"], m["name"]))
    out.append("}")
    out.append("")
    out.append("impl Message {")
    out.append("    pub fn type_id(&self) -> u32 {")
    out.append("        match self {")
    for m in framed:
        out.append("            Message::%s(_) => %s::TYPE," % (m["name"], m["name"]))
    out.append("        }")
    out.append("    }")
    out.append("")
    out.append("    pub fn name(&self) -> &'static str {")
    out.append("        match self {")
    for m in framed:
        out.append("            Message::%s(_) => %s," % (m["name"], json.dumps(m["name"])))
    out.append("        }")
    out.append("    }")
    out.append("")
    out.append("    pub fn encode_body(&self) -> Vec<u8> {")
    out.append("        match self {")
    for m in framed:
        out.append("            Message::%s(m) => m.encode_body()," % m["name"])
    out.append("        }")
    out.append("    }")
    out.append("")
    out.append("    pub fn decode_body(type_id: u32, body: &[u8]) -> Result<Option<Message>, Error> {")
    out.append("        Ok(Some(match type_id {")
    for m in framed:
        out.append("            0x%X => Message::%s(%s::decode_body(body)?)," % (m["type"], m["name"], m["name"]))
    out.append("            _ => return Ok(None),")
    out.append("        }))")
    out.append("    }")
    out.append("}")
    out.append("")
    for m in framed:
        out.append("impl From<%s> for Message {" % m["name"])
        out.append("    fn from(message: %s) -> Self {" % m["name"])
        out.append("        Message::%s(message)" % m["name"])
        out.append("    }")
        out.append("}")
        out.append("")
    return "\n".join(out).rstrip() + "\n"


def rust_read_checked(field):
    wire = {"string": 2, "bytes": 2, "uint32": 0, "uint64": 0, "sint32": 0, "bool": 0, "fixed32": 5}.get(field["kind"], 2)
    text = rust_read(field)
    if field["repeated"]:
        return text.replace("=> {\n", "=> {\n                    r.expect(%d)?;\n" % wire, 1)
    head, expr = text.split(" = ", 1)
    return "%s => {\n                    r.expect(%d)?;\n                    %s = %s\n                }" % (head.split(" => ")[0], wire, head.split(" => ")[1], expr.rstrip(","))


def sample_value(kind, name, index, messages_by_name, depth):
    if kind == "string":
        return "%s-%d-é猫" % (name, index)
    if kind == "uint32":
        return 300 + index * 4099
    if kind == "uint64":
        return str((1 << 60) + index * 977)
    if kind == "sint32":
        return -(index * 1237 + 5) if index % 2 == 0 else index * 911 + 3
    if kind == "fixed32":
        return (0xFF4DA9FF + index) & 0xFFFFFFFF
    if kind == "bool":
        return index % 2 == 0
    if kind == "bytes":
        return bytes((index * 7 + i) & 0xFF for i in range(5 + index % 4)).hex()
    return sample_message(messages_by_name[kind], index, messages_by_name, depth + 1)


def sample_message(message, index, messages_by_name, depth=0):
    out = {}
    for f in message["fields"]:
        if f["repeated"]:
            count = 0 if depth > 2 else 2
            out[f["name"]] = [sample_value(f["kind"], f["name"], index + i + f["number"], messages_by_name, depth) for i in range(count)]
        else:
            out[f["name"]] = sample_value(f["kind"], f["name"], index + f["number"], messages_by_name, depth)
    return out


def main():
    messages, constants, ranges, capabilities, strings = build()
    write(os.path.join(ROOT, "node", "src", "schema.js"), js_schema(messages, constants, ranges, capabilities))
    write(os.path.join(ROOT, "node", "index.d.ts"), ts_declarations(messages))
    write(os.path.join(ROOT, "python", "teho_api", "messages.py"), py_messages(messages, constants, ranges, capabilities))
    write(os.path.join(ROOT, "rust", "src", "messages.rs"), rust_messages(messages, constants, ranges, capabilities))
    by_name = {m["name"]: m for m in messages}
    sys.path.insert(0, os.path.join(ROOT, "python"))
    for module in [k for k in sys.modules if k.startswith("teho_api")]:
        del sys.modules[module]
    import teho_api
    corpus = []
    for index, message in enumerate(m for m in messages if m["type"] is not None):
        values = sample_message(message, index + 1, by_name)
        frame = teho_api.encode(teho_api.from_json(message["name"], values))
        if len(frame) > constants["max_frame"]:
            raise SystemExit("sample too large for " + message["name"])
        corpus.append({"name": message["name"], "type": message["type"], "values": values, "frame": frame.hex()})
    write(os.path.join(ROOT, "corpus.json"), json.dumps(corpus, indent=1, ensure_ascii=False) + "\n")
    write(os.path.join(ROOT, "corpus.txt"), "".join("%s\t%s\n" % (c["name"], c["frame"]) for c in corpus))
    print("generated %d messages, %d framed" % (len(messages), len(corpus)))


if __name__ == "__main__":
    main()
