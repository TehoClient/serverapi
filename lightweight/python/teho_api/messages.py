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

from dataclasses import dataclass, field
from typing import List

CHANNEL = "teho:api"
VERSION = 2
MAX_FRAME = 32000
MAX_STRING = 1024
MAX_LIST = 512
MAX_STREAM = 4194304
SLICE = 30000
CAPABILITIES = ["notify:1", "waypoint:1", "asset:1", "modules:1", "staff:1", "hud:1", "suspect:1", "npc:1", "world:1", "players:1", "keybind:1", "camera:1", "audio:1", "social:1", "ui:1", "info:1", "assert:1", "attest:1", "pack:1", "clips:1", "broadcast:1", "statue:1", "branding:1", "showcase:1"]
RANGES = [[16, 31, "notify:1"], [32, 47, "waypoint:1"], [48, 63, "modules:1"], [64, 79, "staff:1"], [80, 95, "hud:1"], [96, 111, "suspect:1"], [112, 127, "npc:1"], [128, 143, "world:1"], [144, 159, "players:1"], [160, 175, "keybind:1"], [176, 191, "camera:1"], [192, 207, "audio:1"], [208, 223, "social:1"], [224, 239, "ui:1"], [240, 255, "info:1"], [272, 279, "assert:1"], [280, 287, "attest:1"], [288, 303, "pack:1"], [304, 319, "clips:1"], [320, 335, "broadcast:1"], [368, 383, "statue:1"], [384, 399, "branding:1"], [400, 415, "showcase:1"]]


@dataclass
class Hello:
    TYPE = 0x1
    CAPABILITY = None
    FIELDS = ((1, "protocol", "uint32", False, 1024, 4194304, 512), (2, "client", "string", False, 1024, 4194304, 512), (3, "minecraft", "string", False, 1024, 4194304, 512), (4, "platform", "string", False, 1024, 4194304, 512), (5, "capabilities", "string", True, 1024, 4194304, 512), )
    protocol: int = 0
    client: str = ""
    minecraft: str = ""
    platform: str = ""
    capabilities: List[str] = field(default_factory=list)

@dataclass
class Welcome:
    TYPE = 0x2
    CAPABILITY = None
    FIELDS = ((1, "protocol", "uint32", False, 1024, 4194304, 512), (2, "session", "uint64", False, 1024, 4194304, 512), (3, "enabled", "string", True, 1024, 4194304, 512), (4, "server_name", "string", False, 1024, 4194304, 512), (5, "grant", "string", False, 8192, 4194304, 512), (6, "verified", "string", False, 8192, 4194304, 512), )
    protocol: int = 0
    session: int = 0
    enabled: List[str] = field(default_factory=list)
    server_name: str = ""
    grant: str = ""
    verified: str = ""

@dataclass
class HelloRequest:
    TYPE = 0x3
    CAPABILITY = None
    FIELDS = ()

@dataclass
class Reset:
    TYPE = 0x4
    CAPABILITY = None
    FIELDS = ()

@dataclass
class Notify:
    TYPE = 0x10
    CAPABILITY = "notify:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "title", "string", False, 1024, 4194304, 512), (3, "body", "string", False, 1024, 4194304, 512), (4, "accent", "fixed32", False, 1024, 4194304, 512), (5, "duration_ms", "uint32", False, 1024, 4194304, 512), (6, "sound", "bool", False, 1024, 4194304, 512), )
    id: str = ""
    title: str = ""
    body: str = ""
    accent: int = 0
    duration_ms: int = 0
    sound: bool = False

@dataclass
class Dismiss:
    TYPE = 0x11
    CAPABILITY = "notify:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), )
    id: str = ""

@dataclass
class WaypointSet:
    TYPE = 0x20
    CAPABILITY = "waypoint:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "name", "string", False, 1024, 4194304, 512), (3, "x", "sint32", False, 1024, 4194304, 512), (4, "y", "sint32", False, 1024, 4194304, 512), (5, "z", "sint32", False, 1024, 4194304, 512), (6, "dimension", "string", False, 1024, 4194304, 512), (7, "color", "fixed32", False, 1024, 4194304, 512), (8, "focus", "bool", False, 1024, 4194304, 512), )
    id: str = ""
    name: str = ""
    x: int = 0
    y: int = 0
    z: int = 0
    dimension: str = ""
    color: int = 0
    focus: bool = False

@dataclass
class WaypointRemove:
    TYPE = 0x21
    CAPABILITY = "waypoint:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), )
    id: str = ""

@dataclass
class WaypointClear:
    TYPE = 0x22
    CAPABILITY = "waypoint:1"
    FIELDS = ()

@dataclass
class Reply:
    TYPE = 0x5
    CAPABILITY = None
    FIELDS = ((1, "request_id", "uint32", False, 1024, 4194304, 512), (2, "ok", "bool", False, 1024, 4194304, 512), (3, "error", "string", False, 1024, 4194304, 512), (4, "data", "bytes", False, 1024, 4194304, 512), )
    request_id: int = 0
    ok: bool = False
    error: str = ""
    data: bytes = b""

@dataclass
class Event:
    TYPE = 0x6
    CAPABILITY = None
    FIELDS = ((1, "kind", "string", False, 1024, 4194304, 512), (2, "target", "string", False, 1024, 4194304, 512), (3, "value", "string", False, 1024, 4194304, 512), (4, "request_id", "uint32", False, 1024, 4194304, 512), )
    kind: str = ""
    target: str = ""
    value: str = ""
    request_id: int = 0

@dataclass
class Chunk:
    TYPE = 0x7
    CAPABILITY = None
    FIELDS = ((1, "stream", "uint32", False, 1024, 4194304, 512), (2, "index", "uint32", False, 1024, 4194304, 512), (3, "total", "uint32", False, 1024, 4194304, 512), (4, "data", "bytes", False, 1024, 4194304, 512), )
    stream: int = 0
    index: int = 0
    total: int = 0
    data: bytes = b""

@dataclass
class AssetOffer:
    TYPE = 0x8
    CAPABILITY = "asset:1"
    FIELDS = ((1, "hash", "bytes", False, 1024, 4194304, 512), (2, "kind", "uint32", False, 1024, 4194304, 512), (3, "size", "uint32", False, 1024, 4194304, 512), )
    hash: bytes = b""
    kind: int = 0
    size: int = 0

@dataclass
class AssetWant:
    TYPE = 0x9
    CAPABILITY = "asset:1"
    FIELDS = ((1, "hashes", "bytes", True, 1024, 1024, 512), )
    hashes: List[bytes] = field(default_factory=list)

@dataclass
class AssetData:
    TYPE = 0xC
    CAPABILITY = "asset:1"
    FIELDS = ((1, "hash", "bytes", False, 1024, 4194304, 512), (2, "kind", "uint32", False, 1024, 4194304, 512), (3, "data", "bytes", False, 1024, 4194304, 512), )
    hash: bytes = b""
    kind: int = 0
    data: bytes = b""

@dataclass
class ConsentResult:
    TYPE = 0xD
    CAPABILITY = None
    FIELDS = ((1, "scope", "string", False, 1024, 4194304, 512), (2, "granted", "bool", False, 1024, 4194304, 512), (3, "items", "string", True, 1024, 4194304, 512), )
    scope: str = ""
    granted: bool = False
    items: List[str] = field(default_factory=list)

@dataclass
class ModuleRules:
    TYPE = 0x30
    CAPABILITY = "modules:1"
    FIELDS = ((1, "disabled", "string", True, 1024, 4194304, 512), (2, "disabled_categories", "string", True, 1024, 4194304, 512), (3, "reason", "string", False, 1024, 4194304, 512), (4, "competitive", "bool", False, 1024, 4194304, 512), (5, "allowed", "string", True, 1024, 4194304, 512), )
    disabled: List[str] = field(default_factory=list)
    disabled_categories: List[str] = field(default_factory=list)
    reason: str = ""
    competitive: bool = False
    allowed: List[str] = field(default_factory=list)

@dataclass
class SettingCap:
    TYPE = None
    CAPABILITY = None
    FIELDS = ((1, "module", "string", False, 1024, 4194304, 512), (2, "key", "string", False, 1024, 4194304, 512), (3, "min", "sint32", False, 1024, 4194304, 512), (4, "max", "sint32", False, 1024, 4194304, 512), (5, "bounded", "bool", False, 1024, 4194304, 512), (6, "pinned", "string", False, 1024, 4194304, 512), )
    module: str = ""
    key: str = ""
    min: int = 0
    max: int = 0
    bounded: bool = False
    pinned: str = ""

@dataclass
class SettingCaps:
    TYPE = 0x31
    CAPABILITY = "modules:1"
    FIELDS = ((1, "caps", "SettingCap", True, 1024, 4194304, 512), )
    caps: List["SettingCap"] = field(default_factory=list)

@dataclass
class XrayBlock:
    TYPE = None
    CAPABILITY = None
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "color", "fixed32", False, 1024, 4194304, 512), )
    id: str = ""
    color: int = 0

@dataclass
class StaffGrant:
    TYPE = 0x40
    CAPABILITY = "staff:1"
    FIELDS = ((1, "tools", "string", True, 1024, 4194304, 512), (2, "reason", "string", False, 1024, 4194304, 512), (3, "xray_blocks", "XrayBlock", True, 1024, 4194304, 512), (4, "radius", "uint32", False, 1024, 4194304, 512), (5, "storage_blocks", "XrayBlock", True, 1024, 4194304, 512), (6, "entity_types", "XrayBlock", True, 1024, 4194304, 512), )
    tools: List[str] = field(default_factory=list)
    reason: str = ""
    xray_blocks: List["XrayBlock"] = field(default_factory=list)
    radius: int = 0
    storage_blocks: List["XrayBlock"] = field(default_factory=list)
    entity_types: List["XrayBlock"] = field(default_factory=list)

@dataclass
class StaffRevoke:
    TYPE = 0x41
    CAPABILITY = "staff:1"
    FIELDS = ((1, "tools", "string", True, 1024, 4194304, 512), )
    tools: List[str] = field(default_factory=list)

@dataclass
class StaffState:
    TYPE = 0x42
    CAPABILITY = "staff:1"
    FIELDS = ((1, "tool", "string", False, 1024, 4194304, 512), (2, "active", "bool", False, 1024, 4194304, 512), )
    tool: str = ""
    active: bool = False

@dataclass
class EspEntry:
    TYPE = None
    CAPABILITY = None
    FIELDS = ((1, "player", "string", False, 1024, 4194304, 512), (2, "color", "fixed32", False, 1024, 4194304, 512), (3, "label", "string", False, 1024, 4194304, 512), )
    player: str = ""
    color: int = 0
    label: str = ""

@dataclass
class EspStyle:
    TYPE = 0x43
    CAPABILITY = "staff:1"
    FIELDS = ((1, "entries", "EspEntry", True, 1024, 4194304, 512), )
    entries: List["EspEntry"] = field(default_factory=list)

@dataclass
class Cooldown:
    TYPE = 0x50
    CAPABILITY = "hud:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "label", "string", False, 1024, 4194304, 512), (3, "duration_ms", "uint32", False, 1024, 4194304, 512), (4, "item", "string", False, 1024, 4194304, 512), (5, "asset", "bytes", False, 1024, 4194304, 512), (6, "color", "fixed32", False, 1024, 4194304, 512), )
    id: str = ""
    label: str = ""
    duration_ms: int = 0
    item: str = ""
    asset: bytes = b""
    color: int = 0

@dataclass
class CooldownClear:
    TYPE = 0x51
    CAPABILITY = "hud:1"
    FIELDS = ()

@dataclass
class Timer:
    TYPE = 0x52
    CAPABILITY = "hud:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "label", "string", False, 1024, 4194304, 512), (3, "mode", "uint32", False, 1024, 4194304, 512), (4, "millis", "sint32", False, 1024, 4194304, 512), (5, "paused", "bool", False, 1024, 4194304, 512), (6, "color", "fixed32", False, 1024, 4194304, 512), )
    id: str = ""
    label: str = ""
    mode: int = 0
    millis: int = 0
    paused: bool = False
    color: int = 0

@dataclass
class TimerRemove:
    TYPE = 0x53
    CAPABILITY = "hud:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), )
    id: str = ""

@dataclass
class StaffSupport:
    TYPE = 0x44
    CAPABILITY = "staff:1"
    FIELDS = ((1, "tools", "string", True, 1024, 4194304, 512), )
    tools: List[str] = field(default_factory=list)

@dataclass
class Highlight:
    TYPE = 0x45
    CAPABILITY = "staff:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "kind", "uint32", False, 1024, 4194304, 512), (3, "entity", "string", False, 1024, 4194304, 512), (4, "x", "sint32", False, 1024, 4194304, 512), (5, "y", "sint32", False, 1024, 4194304, 512), (6, "z", "sint32", False, 1024, 4194304, 512), (7, "x2", "sint32", False, 1024, 4194304, 512), (8, "y2", "sint32", False, 1024, 4194304, 512), (9, "z2", "sint32", False, 1024, 4194304, 512), (10, "color", "fixed32", False, 1024, 4194304, 512), (11, "label", "string", False, 1024, 4194304, 512), (12, "style", "uint32", False, 1024, 4194304, 512), (13, "tracer", "bool", False, 1024, 4194304, 512), (14, "ttl_ms", "uint32", False, 1024, 4194304, 512), (15, "dimension", "string", False, 1024, 4194304, 512), )
    id: str = ""
    kind: int = 0
    entity: str = ""
    x: int = 0
    y: int = 0
    z: int = 0
    x2: int = 0
    y2: int = 0
    z2: int = 0
    color: int = 0
    label: str = ""
    style: int = 0
    tracer: bool = False
    ttl_ms: int = 0
    dimension: str = ""

@dataclass
class HighlightRemove:
    TYPE = 0x46
    CAPABILITY = "staff:1"
    FIELDS = ((1, "ids", "string", True, 1024, 4194304, 512), )
    ids: List[str] = field(default_factory=list)

@dataclass
class Trail:
    TYPE = 0x47
    CAPABILITY = "staff:1"
    FIELDS = ((1, "player", "string", False, 1024, 4194304, 512), (2, "points", "sint32", True, 1024, 4194304, 3072), (3, "color", "fixed32", False, 1024, 4194304, 512), (4, "label", "string", False, 1024, 4194304, 512), (5, "ttl_ms", "uint32", False, 1024, 4194304, 512), (6, "dimension", "string", False, 1024, 4194304, 512), )
    player: str = ""
    points: List[int] = field(default_factory=list)
    color: int = 0
    label: str = ""
    ttl_ms: int = 0
    dimension: str = ""

@dataclass
class InspectRequest:
    TYPE = 0x48
    CAPABILITY = "staff:1"
    FIELDS = ((1, "request_id", "uint32", False, 1024, 4194304, 512), (2, "kind", "uint32", False, 1024, 4194304, 512), (3, "target", "string", False, 1024, 4194304, 512), (4, "x", "sint32", False, 1024, 4194304, 512), (5, "y", "sint32", False, 1024, 4194304, 512), (6, "z", "sint32", False, 1024, 4194304, 512), )
    request_id: int = 0
    kind: int = 0
    target: str = ""
    x: int = 0
    y: int = 0
    z: int = 0

@dataclass
class InspectRow:
    TYPE = None
    CAPABILITY = None
    FIELDS = ((1, "label", "string", False, 1024, 4194304, 512), (2, "value", "string", False, 1024, 4194304, 512), (3, "color", "fixed32", False, 1024, 4194304, 512), )
    label: str = ""
    value: str = ""
    color: int = 0

@dataclass
class InspectAction:
    TYPE = None
    CAPABILITY = None
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "label", "string", False, 1024, 4194304, 512), )
    id: str = ""
    label: str = ""

@dataclass
class InspectCard:
    TYPE = 0x49
    CAPABILITY = "staff:1"
    FIELDS = ((1, "request_id", "uint32", False, 1024, 4194304, 512), (2, "title", "string", False, 1024, 4194304, 512), (3, "subtitle", "string", False, 1024, 4194304, 512), (4, "rows", "InspectRow", True, 1024, 4194304, 512), (5, "actions", "InspectAction", True, 1024, 4194304, 512), (6, "ttl_ms", "uint32", False, 1024, 4194304, 512), (7, "target", "string", False, 1024, 4194304, 512), )
    request_id: int = 0
    title: str = ""
    subtitle: str = ""
    rows: List["InspectRow"] = field(default_factory=list)
    actions: List["InspectAction"] = field(default_factory=list)
    ttl_ms: int = 0
    target: str = ""

@dataclass
class WheelAction:
    TYPE = None
    CAPABILITY = None
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "label", "string", False, 1024, 4194304, 512), (3, "item", "string", False, 1024, 4194304, 512), (4, "color", "fixed32", False, 1024, 4194304, 512), (5, "target", "uint32", False, 1024, 4194304, 512), )
    id: str = ""
    label: str = ""
    item: str = ""
    color: int = 0
    target: int = 0

@dataclass
class ActionWheel:
    TYPE = 0x4A
    CAPABILITY = "staff:1"
    FIELDS = ((1, "actions", "WheelAction", True, 1024, 4194304, 512), (2, "title", "string", False, 1024, 4194304, 512), )
    actions: List["WheelAction"] = field(default_factory=list)
    title: str = ""

@dataclass
class Alert:
    TYPE = 0x4B
    CAPABILITY = "staff:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "severity", "uint32", False, 1024, 4194304, 512), (3, "title", "string", False, 1024, 4194304, 512), (4, "body", "string", False, 1024, 4194304, 512), (5, "player", "string", False, 1024, 4194304, 512), (6, "x", "sint32", False, 1024, 4194304, 512), (7, "y", "sint32", False, 1024, 4194304, 512), (8, "z", "sint32", False, 1024, 4194304, 512), (9, "dimension", "string", False, 1024, 4194304, 512), (10, "ttl_ms", "uint32", False, 1024, 4194304, 512), )
    id: str = ""
    severity: int = 0
    title: str = ""
    body: str = ""
    player: str = ""
    x: int = 0
    y: int = 0
    z: int = 0
    dimension: str = ""
    ttl_ms: int = 0

@dataclass
class TargetStats:
    TYPE = 0x4C
    CAPABILITY = "staff:1"
    FIELDS = ((1, "player", "string", False, 1024, 4194304, 512), (2, "rows", "InspectRow", True, 1024, 4194304, 512), (3, "ttl_ms", "uint32", False, 1024, 4194304, 512), )
    player: str = ""
    rows: List["InspectRow"] = field(default_factory=list)
    ttl_ms: int = 0

@dataclass
class Spectate:
    TYPE = 0x4D
    CAPABILITY = "staff:1"
    FIELDS = ((1, "player", "string", False, 1024, 4194304, 512), (2, "distance", "uint32", False, 1024, 4194304, 512), )
    player: str = ""
    distance: int = 0

@dataclass
class StaffAction:
    TYPE = 0x4E
    CAPABILITY = "staff:1"
    FIELDS = ((1, "action", "string", False, 1024, 4194304, 512), (2, "target_kind", "uint32", False, 1024, 4194304, 512), (3, "target", "string", False, 1024, 4194304, 512), (4, "x", "sint32", False, 1024, 4194304, 512), (5, "y", "sint32", False, 1024, 4194304, 512), (6, "z", "sint32", False, 1024, 4194304, 512), )
    action: str = ""
    target_kind: int = 0
    target: str = ""
    x: int = 0
    y: int = 0
    z: int = 0

@dataclass
class LagCell:
    TYPE = None
    CAPABILITY = None
    FIELDS = ((1, "chunk_x", "sint32", False, 1024, 4194304, 512), (2, "chunk_z", "sint32", False, 1024, 4194304, 512), (3, "score", "uint32", False, 1024, 4194304, 512), (4, "label", "string", False, 1024, 4194304, 512), )
    chunk_x: int = 0
    chunk_z: int = 0
    score: int = 0
    label: str = ""

@dataclass
class LagMap:
    TYPE = 0x4F
    CAPABILITY = "staff:1"
    FIELDS = ((1, "cells", "LagCell", True, 1024, 4194304, 512), (2, "dimension", "string", False, 1024, 4194304, 512), (3, "ttl_ms", "uint32", False, 1024, 4194304, 512), )
    cells: List["LagCell"] = field(default_factory=list)
    dimension: str = ""
    ttl_ms: int = 0

@dataclass
class FreezeScreen:
    TYPE = 0x60
    CAPABILITY = "suspect:1"
    FIELDS = ((1, "on", "bool", False, 1024, 4194304, 512), (2, "title", "string", False, 1024, 4194304, 512), (3, "body", "string", False, 1024, 4194304, 512), )
    on: bool = False
    title: str = ""
    body: str = ""

@dataclass
class ScreenshotRequest:
    TYPE = 0x61
    CAPABILITY = "suspect:1"
    FIELDS = ((1, "request_id", "uint32", False, 1024, 4194304, 512), (2, "reason", "string", False, 1024, 4194304, 512), (3, "max_width", "uint32", False, 1024, 4194304, 512), )
    request_id: int = 0
    reason: str = ""
    max_width: int = 0

@dataclass
class ScreenshotResult:
    TYPE = 0x62
    CAPABILITY = "suspect:1"
    FIELDS = ((1, "request_id", "uint32", False, 1024, 4194304, 512), (2, "granted", "bool", False, 1024, 4194304, 512), (3, "png", "bytes", False, 1024, 4194304, 512), (4, "width", "uint32", False, 1024, 4194304, 512), (5, "height", "uint32", False, 1024, 4194304, 512), )
    request_id: int = 0
    granted: bool = False
    png: bytes = b""
    width: int = 0
    height: int = 0

@dataclass
class ModListRequest:
    TYPE = 0x63
    CAPABILITY = "suspect:1"
    FIELDS = ((1, "request_id", "uint32", False, 1024, 4194304, 512), (2, "reason", "string", False, 1024, 4194304, 512), )
    request_id: int = 0
    reason: str = ""

@dataclass
class ModEntry:
    TYPE = None
    CAPABILITY = None
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "name", "string", False, 1024, 4194304, 512), (3, "version", "string", False, 1024, 4194304, 512), )
    id: str = ""
    name: str = ""
    version: str = ""

@dataclass
class ModList:
    TYPE = 0x64
    CAPABILITY = "suspect:1"
    FIELDS = ((1, "request_id", "uint32", False, 1024, 4194304, 512), (2, "granted", "bool", False, 1024, 4194304, 512), (3, "mods", "ModEntry", True, 1024, 4194304, 512), (4, "teho_modules", "string", True, 1024, 4194304, 512), )
    request_id: int = 0
    granted: bool = False
    mods: List["ModEntry"] = field(default_factory=list)
    teho_modules: List[str] = field(default_factory=list)

@dataclass
class PrivateStatus:
    TYPE = 0xE
    CAPABILITY = None
    FIELDS = ((1, "capabilities", "string", True, 1024, 4194304, 512), (2, "error", "string", False, 1024, 4194304, 512), (3, "server", "string", False, 1024, 4194304, 512), )
    capabilities: List[str] = field(default_factory=list)
    error: str = ""
    server: str = ""

@dataclass
class NpcSlot:
    TYPE = None
    CAPABILITY = None
    FIELDS = ((1, "slot", "string", False, 1024, 4194304, 512), (2, "id", "string", False, 1024, 4194304, 512), )
    slot: str = ""
    id: str = ""

@dataclass
class NpcLook:
    TYPE = 0x70
    CAPABILITY = "npc:1"
    FIELDS = ((1, "npc", "string", False, 1024, 4194304, 512), (2, "mode", "uint32", False, 1024, 4194304, 512), (3, "slots", "NpcSlot", True, 1024, 4194304, 512), )
    npc: str = ""
    mode: int = 0
    slots: List["NpcSlot"] = field(default_factory=list)

@dataclass
class NpcEmote:
    TYPE = 0x71
    CAPABILITY = "npc:1"
    FIELDS = ((1, "npc", "string", False, 1024, 4194304, 512), (2, "action", "uint32", False, 1024, 4194304, 512), (3, "emote", "string", False, 1024, 4194304, 512), (4, "loop", "bool", False, 1024, 4194304, 512), )
    npc: str = ""
    action: int = 0
    emote: str = ""
    loop: bool = False

@dataclass
class NpcClear:
    TYPE = 0x72
    CAPABILITY = "npc:1"
    FIELDS = ((1, "npcs", "string", True, 1024, 4194304, 512), )
    npcs: List[str] = field(default_factory=list)

@dataclass
class CombatRules:
    TYPE = 0x32
    CAPABILITY = "modules:1"
    FIELDS = ((1, "legacy_swing", "bool", False, 1024, 4194304, 512), (2, "legacy_blockhit", "bool", False, 1024, 4194304, 512), (3, "no_hit_delay_visual", "bool", False, 1024, 4194304, 512), (4, "miss_penalty_visual", "bool", False, 1024, 4194304, 512), (5, "legacy_hit_color", "bool", False, 1024, 4194304, 512), )
    legacy_swing: bool = False
    legacy_blockhit: bool = False
    no_hit_delay_visual: bool = False
    miss_penalty_visual: bool = False
    legacy_hit_color: bool = False

@dataclass
class ThirdPartyPolicy:
    TYPE = 0x33
    CAPABILITY = "modules:1"
    FIELDS = ((1, "deny", "string", True, 1024, 4194304, 512), (2, "reason", "string", False, 1024, 4194304, 512), (3, "report", "bool", False, 1024, 4194304, 512), )
    deny: List[str] = field(default_factory=list)
    reason: str = ""
    report: bool = False

@dataclass
class PolicyReport:
    TYPE = 0x34
    CAPABILITY = "modules:1"
    FIELDS = ((1, "granted", "bool", False, 1024, 4194304, 512), (2, "violations", "ModEntry", True, 1024, 4194304, 512), )
    granted: bool = False
    violations: List["ModEntry"] = field(default_factory=list)

@dataclass
class SidebarRow:
    TYPE = None
    CAPABILITY = None
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "text", "string", False, 1024, 4194304, 512), (3, "value", "string", False, 1024, 4194304, 512), (4, "item", "string", False, 1024, 4194304, 512), (5, "color", "fixed32", False, 1024, 4194304, 512), (6, "progress", "uint32", False, 1024, 4194304, 512), (7, "asset", "bytes", False, 1024, 4194304, 512), (8, "order", "sint32", False, 1024, 4194304, 512), )
    id: str = ""
    text: str = ""
    value: str = ""
    item: str = ""
    color: int = 0
    progress: int = 0
    asset: bytes = b""
    order: int = 0

@dataclass
class SidebarSet:
    TYPE = 0x54
    CAPABILITY = "hud:1"
    FIELDS = ((1, "title", "string", False, 1024, 4194304, 512), (2, "rows", "SidebarRow", True, 1024, 4194304, 512), (3, "remove", "string", True, 1024, 4194304, 512), (4, "replace", "bool", False, 1024, 4194304, 512), (5, "accent", "fixed32", False, 1024, 4194304, 512), (6, "footer", "string", False, 1024, 4194304, 512), )
    title: str = ""
    rows: List["SidebarRow"] = field(default_factory=list)
    remove: List[str] = field(default_factory=list)
    replace: bool = False
    accent: int = 0
    footer: str = ""

@dataclass
class SidebarClear:
    TYPE = 0x55
    CAPABILITY = "hud:1"
    FIELDS = ()

@dataclass
class Bar:
    TYPE = 0x56
    CAPABILITY = "hud:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "text", "string", False, 1024, 4194304, 512), (3, "progress", "uint32", False, 1024, 4194304, 512), (4, "color", "fixed32", False, 1024, 4194304, 512), (5, "style", "uint32", False, 1024, 4194304, 512), (6, "ttl_ms", "uint32", False, 1024, 4194304, 512), (7, "item", "string", False, 1024, 4194304, 512), )
    id: str = ""
    text: str = ""
    progress: int = 0
    color: int = 0
    style: int = 0
    ttl_ms: int = 0
    item: str = ""

@dataclass
class BarRemove:
    TYPE = 0x57
    CAPABILITY = "hud:1"
    FIELDS = ((1, "ids", "string", True, 1024, 4194304, 512), )
    ids: List[str] = field(default_factory=list)

@dataclass
class KillFeed:
    TYPE = 0x58
    CAPABILITY = "hud:1"
    FIELDS = ((1, "killer", "string", False, 1024, 4194304, 512), (2, "victim", "string", False, 1024, 4194304, 512), (3, "weapon", "string", False, 1024, 4194304, 512), (4, "text", "string", False, 1024, 4194304, 512), (5, "color", "fixed32", False, 1024, 4194304, 512), (6, "killer_id", "string", False, 1024, 4194304, 512), (7, "victim_id", "string", False, 1024, 4194304, 512), (8, "ttl_ms", "uint32", False, 1024, 4194304, 512), )
    killer: str = ""
    victim: str = ""
    weapon: str = ""
    text: str = ""
    color: int = 0
    killer_id: str = ""
    victim_id: str = ""
    ttl_ms: int = 0

@dataclass
class ScreenTint:
    TYPE = 0x59
    CAPABILITY = "hud:1"
    FIELDS = ((1, "color", "fixed32", False, 1024, 4194304, 512), (2, "vignette", "bool", False, 1024, 4194304, 512), (3, "fade_ms", "uint32", False, 1024, 4194304, 512), (4, "ttl_ms", "uint32", False, 1024, 4194304, 512), (5, "strength", "uint32", False, 1024, 4194304, 512), )
    color: int = 0
    vignette: bool = False
    fade_ms: int = 0
    ttl_ms: int = 0
    strength: int = 0

@dataclass
class Title:
    TYPE = 0x5A
    CAPABILITY = "hud:1"
    FIELDS = ((1, "title", "string", False, 1024, 4194304, 512), (2, "subtitle", "string", False, 1024, 4194304, 512), (3, "animation", "uint32", False, 1024, 4194304, 512), (4, "fade_in_ms", "uint32", False, 1024, 4194304, 512), (5, "stay_ms", "uint32", False, 1024, 4194304, 512), (6, "fade_out_ms", "uint32", False, 1024, 4194304, 512), (7, "color", "fixed32", False, 1024, 4194304, 512), (8, "scale", "uint32", False, 1024, 4194304, 512), )
    title: str = ""
    subtitle: str = ""
    animation: int = 0
    fade_in_ms: int = 0
    stay_ms: int = 0
    fade_out_ms: int = 0
    color: int = 0
    scale: int = 0

@dataclass
class TntFuse:
    TYPE = 0x5B
    CAPABILITY = "hud:1"
    FIELDS = ((1, "fuse_ticks", "uint32", False, 1024, 4194304, 512), )
    fuse_ticks: int = 0

@dataclass
class Beam:
    TYPE = 0x80
    CAPABILITY = "world:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "x", "sint32", False, 1024, 4194304, 512), (3, "y", "sint32", False, 1024, 4194304, 512), (4, "z", "sint32", False, 1024, 4194304, 512), (5, "entity", "string", False, 1024, 4194304, 512), (6, "color", "fixed32", False, 1024, 4194304, 512), (7, "height", "uint32", False, 1024, 4194304, 512), (8, "width", "uint32", False, 1024, 4194304, 512), (9, "pulse", "bool", False, 1024, 4194304, 512), (10, "dimension", "string", False, 1024, 4194304, 512), (11, "ttl_ms", "uint32", False, 1024, 4194304, 512), )
    id: str = ""
    x: int = 0
    y: int = 0
    z: int = 0
    entity: str = ""
    color: int = 0
    height: int = 0
    width: int = 0
    pulse: bool = False
    dimension: str = ""
    ttl_ms: int = 0

@dataclass
class Hologram:
    TYPE = 0x81
    CAPABILITY = "world:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "x", "sint32", False, 1024, 4194304, 512), (3, "y", "sint32", False, 1024, 4194304, 512), (4, "z", "sint32", False, 1024, 4194304, 512), (5, "entity", "string", False, 1024, 4194304, 512), (6, "lines", "string", True, 1024, 4194304, 512), (7, "item", "string", False, 1024, 4194304, 512), (8, "asset", "bytes", False, 1024, 4194304, 512), (9, "scale", "uint32", False, 1024, 4194304, 512), (10, "background", "fixed32", False, 1024, 4194304, 512), (11, "clickable", "bool", False, 1024, 4194304, 512), (12, "through_walls", "bool", False, 1024, 4194304, 512), (13, "dimension", "string", False, 1024, 4194304, 512), (14, "ttl_ms", "uint32", False, 1024, 4194304, 512), (15, "range", "uint32", False, 1024, 4194304, 512), )
    id: str = ""
    x: int = 0
    y: int = 0
    z: int = 0
    entity: str = ""
    lines: List[str] = field(default_factory=list)
    item: str = ""
    asset: bytes = b""
    scale: int = 0
    background: int = 0
    clickable: bool = False
    through_walls: bool = False
    dimension: str = ""
    ttl_ms: int = 0
    range: int = 0

@dataclass
class Shape:
    TYPE = 0x82
    CAPABILITY = "world:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "kind", "uint32", False, 1024, 4194304, 512), (3, "points", "sint32", True, 1024, 4194304, 3072), (4, "radius", "uint32", False, 1024, 4194304, 512), (5, "height", "uint32", False, 1024, 4194304, 512), (6, "color", "fixed32", False, 1024, 4194304, 512), (7, "fill", "fixed32", False, 1024, 4194304, 512), (8, "width", "uint32", False, 1024, 4194304, 512), (9, "through_walls", "bool", False, 1024, 4194304, 512), (10, "label", "string", False, 1024, 4194304, 512), (11, "dimension", "string", False, 1024, 4194304, 512), (12, "ttl_ms", "uint32", False, 1024, 4194304, 512), )
    id: str = ""
    kind: int = 0
    points: List[int] = field(default_factory=list)
    radius: int = 0
    height: int = 0
    color: int = 0
    fill: int = 0
    width: int = 0
    through_walls: bool = False
    label: str = ""
    dimension: str = ""
    ttl_ms: int = 0

@dataclass
class WorldRemove:
    TYPE = 0x83
    CAPABILITY = "world:1"
    FIELDS = ((1, "kind", "uint32", False, 1024, 4194304, 512), (2, "ids", "string", True, 1024, 4194304, 512), )
    kind: int = 0
    ids: List[str] = field(default_factory=list)

@dataclass
class EntityStyle:
    TYPE = 0x84
    CAPABILITY = "world:1"
    FIELDS = ((1, "entity", "string", False, 1024, 4194304, 512), (2, "glow", "fixed32", False, 1024, 4194304, 512), (3, "name", "string", False, 1024, 4194304, 512), (4, "scale", "uint32", False, 1024, 4194304, 512), (5, "rainbow", "bool", False, 1024, 4194304, 512), (6, "hide", "string", True, 1024, 4194304, 512), (7, "ttl_ms", "uint32", False, 1024, 4194304, 512), (8, "hide_name", "bool", False, 1024, 4194304, 512), )
    entity: str = ""
    glow: int = 0
    name: str = ""
    scale: int = 0
    rainbow: bool = False
    hide: List[str] = field(default_factory=list)
    ttl_ms: int = 0
    hide_name: bool = False

@dataclass
class EntityStyleClear:
    TYPE = 0x85
    CAPABILITY = "world:1"
    FIELDS = ((1, "entities", "string", True, 1024, 4194304, 512), )
    entities: List[str] = field(default_factory=list)

@dataclass
class BorderStyle:
    TYPE = 0x86
    CAPABILITY = "world:1"
    FIELDS = ((1, "color", "fixed32", False, 1024, 4194304, 512), (2, "hide_wall", "bool", False, 1024, 4194304, 512), (3, "reset", "bool", False, 1024, 4194304, 512), )
    color: int = 0
    hide_wall: bool = False
    reset: bool = False

@dataclass
class FireColor:
    TYPE = 0x87
    CAPABILITY = "world:1"
    FIELDS = ((1, "entity", "string", False, 1024, 4194304, 512), (2, "color", "fixed32", False, 1024, 4194304, 512), (3, "reset", "bool", False, 1024, 4194304, 512), )
    entity: str = ""
    color: int = 0
    reset: bool = False

@dataclass
class Nametag:
    TYPE = 0x90
    CAPABILITY = "players:1"
    FIELDS = ((1, "player", "string", False, 1024, 4194304, 512), (2, "above", "string", True, 1024, 4194304, 512), (3, "below", "string", True, 1024, 4194304, 512), (4, "prefix", "string", False, 1024, 4194304, 512), (5, "suffix", "string", False, 1024, 4194304, 512), (6, "badge", "bytes", False, 1024, 4194304, 512), (7, "hide", "bool", False, 1024, 4194304, 512), (8, "badge_item", "string", False, 1024, 4194304, 512), )
    player: str = ""
    above: List[str] = field(default_factory=list)
    below: List[str] = field(default_factory=list)
    prefix: str = ""
    suffix: str = ""
    badge: bytes = b""
    hide: bool = False
    badge_item: str = ""

@dataclass
class NametagClear:
    TYPE = 0x91
    CAPABILITY = "players:1"
    FIELDS = ((1, "players", "string", True, 1024, 4194304, 512), )
    players: List[str] = field(default_factory=list)

@dataclass
class TabEntry:
    TYPE = None
    CAPABILITY = None
    FIELDS = ((1, "player", "string", False, 1024, 4194304, 512), (2, "display", "string", False, 1024, 4194304, 512), (3, "group", "string", False, 1024, 4194304, 512), (4, "icon", "bytes", False, 1024, 4194304, 512), (5, "ping", "string", False, 1024, 4194304, 512), (6, "order", "sint32", False, 1024, 4194304, 512), (7, "color", "fixed32", False, 1024, 4194304, 512), )
    player: str = ""
    display: str = ""
    group: str = ""
    icon: bytes = b""
    ping: str = ""
    order: int = 0
    color: int = 0

@dataclass
class TabList:
    TYPE = 0x92
    CAPABILITY = "players:1"
    FIELDS = ((1, "header", "string", True, 1024, 4194304, 512), (2, "footer", "string", True, 1024, 4194304, 512), (3, "entries", "TabEntry", True, 1024, 4194304, 512), (4, "hide_ping", "bool", False, 1024, 4194304, 512), (5, "replace", "bool", False, 1024, 4194304, 512), (6, "remove", "string", True, 1024, 4194304, 512), (7, "reset", "bool", False, 1024, 4194304, 512), )
    header: List[str] = field(default_factory=list)
    footer: List[str] = field(default_factory=list)
    entries: List["TabEntry"] = field(default_factory=list)
    hide_ping: bool = False
    replace: bool = False
    remove: List[str] = field(default_factory=list)
    reset: bool = False

@dataclass
class Team:
    TYPE = 0x93
    CAPABILITY = "players:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "name", "string", False, 1024, 4194304, 512), (3, "color", "fixed32", False, 1024, 4194304, 512), (4, "members", "string", True, 1024, 4194304, 512), (5, "markers", "bool", False, 1024, 4194304, 512), (6, "through_walls", "bool", False, 1024, 4194304, 512), (7, "health", "bool", False, 1024, 4194304, 512), (8, "compass", "bool", False, 1024, 4194304, 512), )
    id: str = ""
    name: str = ""
    color: int = 0
    members: List[str] = field(default_factory=list)
    markers: bool = False
    through_walls: bool = False
    health: bool = False
    compass: bool = False

@dataclass
class TeamRemove:
    TYPE = 0x94
    CAPABILITY = "players:1"
    FIELDS = ((1, "ids", "string", True, 1024, 4194304, 512), )
    ids: List[str] = field(default_factory=list)

@dataclass
class NickHint:
    TYPE = 0x95
    CAPABILITY = "players:1"
    FIELDS = ((1, "nick", "string", False, 1024, 4194304, 512), (2, "player", "string", False, 1024, 4194304, 512), )
    nick: str = ""
    player: str = ""

@dataclass
class Keybind:
    TYPE = None
    CAPABILITY = None
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "label", "string", False, 1024, 4194304, 512), (3, "key", "sint32", False, 1024, 4194304, 512), (4, "button", "string", False, 1024, 4194304, 512), (5, "hold", "bool", False, 1024, 4194304, 512), (6, "description", "string", False, 1024, 4194304, 512), )
    id: str = ""
    label: str = ""
    key: int = 0
    button: str = ""
    hold: bool = False
    description: str = ""

@dataclass
class Keybinds:
    TYPE = 0xA0
    CAPABILITY = "keybind:1"
    FIELDS = ((1, "binds", "Keybind", True, 1024, 4194304, 512), (2, "category", "string", False, 1024, 4194304, 512), )
    binds: List["Keybind"] = field(default_factory=list)
    category: str = ""

@dataclass
class KeyPress:
    TYPE = 0xA1
    CAPABILITY = "keybind:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "pressed", "bool", False, 1024, 4194304, 512), (3, "held_ms", "uint32", False, 1024, 4194304, 512), )
    id: str = ""
    pressed: bool = False
    held_ms: int = 0

@dataclass
class CameraShake:
    TYPE = 0xB0
    CAPABILITY = "camera:1"
    FIELDS = ((1, "intensity", "uint32", False, 1024, 4194304, 512), (2, "duration_ms", "uint32", False, 1024, 4194304, 512), (3, "frequency", "uint32", False, 1024, 4194304, 512), )
    intensity: int = 0
    duration_ms: int = 0
    frequency: int = 0

@dataclass
class FovPunch:
    TYPE = 0xB1
    CAPABILITY = "camera:1"
    FIELDS = ((1, "amount", "sint32", False, 1024, 4194304, 512), (2, "duration_ms", "uint32", False, 1024, 4194304, 512), )
    amount: int = 0
    duration_ms: int = 0

@dataclass
class LookAt:
    TYPE = 0xB2
    CAPABILITY = "camera:1"
    FIELDS = ((1, "x", "sint32", False, 1024, 4194304, 512), (2, "y", "sint32", False, 1024, 4194304, 512), (3, "z", "sint32", False, 1024, 4194304, 512), (4, "entity", "string", False, 1024, 4194304, 512), (5, "duration_ms", "uint32", False, 1024, 4194304, 512), (6, "hold_ms", "uint32", False, 1024, 4194304, 512), )
    x: int = 0
    y: int = 0
    z: int = 0
    entity: str = ""
    duration_ms: int = 0
    hold_ms: int = 0

@dataclass
class Letterbox:
    TYPE = 0xB3
    CAPABILITY = "camera:1"
    FIELDS = ((1, "on", "bool", False, 1024, 4194304, 512), (2, "size", "uint32", False, 1024, 4194304, 512), (3, "fade_ms", "uint32", False, 1024, 4194304, 512), )
    on: bool = False
    size: int = 0
    fade_ms: int = 0

@dataclass
class Keyframe:
    TYPE = None
    CAPABILITY = None
    FIELDS = ((1, "x", "sint32", False, 1024, 4194304, 512), (2, "y", "sint32", False, 1024, 4194304, 512), (3, "z", "sint32", False, 1024, 4194304, 512), (4, "yaw", "sint32", False, 1024, 4194304, 512), (5, "pitch", "sint32", False, 1024, 4194304, 512), (6, "roll", "sint32", False, 1024, 4194304, 512), (7, "fov", "uint32", False, 1024, 4194304, 512), (8, "at_ms", "uint32", False, 1024, 4194304, 512), (9, "ease", "uint32", False, 1024, 4194304, 512), )
    x: int = 0
    y: int = 0
    z: int = 0
    yaw: int = 0
    pitch: int = 0
    roll: int = 0
    fov: int = 0
    at_ms: int = 0
    ease: int = 0

@dataclass
class Cutscene:
    TYPE = 0xB4
    CAPABILITY = "camera:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "frames", "Keyframe", True, 1024, 4194304, 512), (3, "skippable", "bool", False, 1024, 4194304, 512), (4, "hide_hud", "bool", False, 1024, 4194304, 512), (5, "letterbox", "bool", False, 1024, 4194304, 512), (6, "loop", "bool", False, 1024, 4194304, 512), (7, "dimension", "string", False, 1024, 4194304, 512), )
    id: str = ""
    frames: List["Keyframe"] = field(default_factory=list)
    skippable: bool = False
    hide_hud: bool = False
    letterbox: bool = False
    loop: bool = False
    dimension: str = ""

@dataclass
class CutsceneStop:
    TYPE = 0xB5
    CAPABILITY = "camera:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), )
    id: str = ""

@dataclass
class ScreenFade:
    TYPE = 0xB6
    CAPABILITY = "camera:1"
    FIELDS = ((1, "color", "fixed32", False, 1024, 4194304, 512), (2, "in_ms", "uint32", False, 1024, 4194304, 512), (3, "hold_ms", "uint32", False, 1024, 4194304, 512), (4, "out_ms", "uint32", False, 1024, 4194304, 512), )
    color: int = 0
    in_ms: int = 0
    hold_ms: int = 0
    out_ms: int = 0

@dataclass
class CutsceneEnded:
    TYPE = 0xB7
    CAPABILITY = "camera:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "skipped", "bool", False, 1024, 4194304, 512), )
    id: str = ""
    skipped: bool = False

@dataclass
class PlaySound:
    TYPE = 0xC0
    CAPABILITY = "audio:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "asset", "bytes", False, 1024, 4194304, 512), (3, "sound", "string", False, 1024, 4194304, 512), (4, "x", "sint32", False, 1024, 4194304, 512), (5, "y", "sint32", False, 1024, 4194304, 512), (6, "z", "sint32", False, 1024, 4194304, 512), (7, "entity", "string", False, 1024, 4194304, 512), (8, "positional", "bool", False, 1024, 4194304, 512), (9, "volume", "uint32", False, 1024, 4194304, 512), (10, "pitch", "uint32", False, 1024, 4194304, 512), (11, "loop", "bool", False, 1024, 4194304, 512), (12, "fade_in_ms", "uint32", False, 1024, 4194304, 512), (13, "music", "bool", False, 1024, 4194304, 512), )
    id: str = ""
    asset: bytes = b""
    sound: str = ""
    x: int = 0
    y: int = 0
    z: int = 0
    entity: str = ""
    positional: bool = False
    volume: int = 0
    pitch: int = 0
    loop: bool = False
    fade_in_ms: int = 0
    music: bool = False

@dataclass
class StopSound:
    TYPE = 0xC1
    CAPABILITY = "audio:1"
    FIELDS = ((1, "ids", "string", True, 1024, 4194304, 512), (2, "fade_out_ms", "uint32", False, 1024, 4194304, 512), )
    ids: List[str] = field(default_factory=list)
    fade_out_ms: int = 0

@dataclass
class Presence:
    TYPE = 0xD0
    CAPABILITY = "social:1"
    FIELDS = ((1, "game", "string", False, 1024, 4194304, 512), (2, "map", "string", False, 1024, 4194304, 512), (3, "state", "string", False, 1024, 4194304, 512), (4, "started", "uint64", False, 1024, 4194304, 512), (5, "party_size", "uint32", False, 1024, 4194304, 512), (6, "party_max", "uint32", False, 1024, 4194304, 512), (7, "joinable", "bool", False, 1024, 4194304, 512), (8, "clear", "bool", False, 1024, 4194304, 512), )
    game: str = ""
    map: str = ""
    state: str = ""
    started: int = 0
    party_size: int = 0
    party_max: int = 0
    joinable: bool = False
    clear: bool = False

@dataclass
class InviteSheet:
    TYPE = 0xD1
    CAPABILITY = "social:1"
    FIELDS = ((1, "reason", "string", False, 1024, 4194304, 512), (2, "party", "string", False, 1024, 4194304, 512), (3, "address", "string", False, 1024, 4194304, 512), )
    reason: str = ""
    party: str = ""
    address: str = ""

@dataclass
class Transfer:
    TYPE = 0xD2
    CAPABILITY = "social:1"
    FIELDS = ((1, "host", "string", False, 1024, 4194304, 512), (2, "port", "uint32", False, 1024, 4194304, 512), (3, "token", "string", False, 1024, 4194304, 512), (4, "title", "string", False, 1024, 4194304, 512), (5, "reason", "string", False, 1024, 4194304, 512), )
    host: str = ""
    port: int = 0
    token: str = ""
    title: str = ""
    reason: str = ""

@dataclass
class ServerLink:
    TYPE = None
    CAPABILITY = None
    FIELDS = ((1, "label", "string", False, 1024, 4194304, 512), (2, "url", "string", False, 1024, 4194304, 512), (3, "kind", "string", False, 1024, 4194304, 512), )
    label: str = ""
    url: str = ""
    kind: str = ""

@dataclass
class ServerLinks:
    TYPE = 0xD3
    CAPABILITY = "social:1"
    FIELDS = ((1, "links", "ServerLink", True, 1024, 4194304, 512), )
    links: List["ServerLink"] = field(default_factory=list)

@dataclass
class UiNode:
    TYPE = None
    CAPABILITY = None
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "parent", "string", False, 1024, 4194304, 512), (3, "kind", "uint32", False, 1024, 4194304, 512), (4, "text", "string", False, 1024, 4194304, 512), (5, "value", "string", False, 1024, 4194304, 512), (6, "min", "sint32", False, 1024, 4194304, 512), (7, "max", "sint32", False, 1024, 4194304, 512), (8, "step", "uint32", False, 1024, 4194304, 512), (9, "asset", "bytes", False, 1024, 4194304, 512), (10, "item", "string", False, 1024, 4194304, 512), (11, "player", "string", False, 1024, 4194304, 512), (12, "color", "fixed32", False, 1024, 4194304, 512), (13, "columns", "uint32", False, 1024, 4194304, 512), (14, "disabled", "bool", False, 1024, 4194304, 512), (15, "placeholder", "string", False, 1024, 4194304, 512), (16, "options", "string", True, 1024, 4194304, 512), (17, "millis", "uint64", False, 1024, 4194304, 512), (18, "style", "uint32", False, 1024, 4194304, 512), (19, "width", "uint32", False, 1024, 4194304, 512), (20, "height", "uint32", False, 1024, 4194304, 512), (21, "tooltip", "string", False, 1024, 4194304, 512), )
    id: str = ""
    parent: str = ""
    kind: int = 0
    text: str = ""
    value: str = ""
    min: int = 0
    max: int = 0
    step: int = 0
    asset: bytes = b""
    item: str = ""
    player: str = ""
    color: int = 0
    columns: int = 0
    disabled: bool = False
    placeholder: str = ""
    options: List[str] = field(default_factory=list)
    millis: int = 0
    style: int = 0
    width: int = 0
    height: int = 0
    tooltip: str = ""

@dataclass
class UiOpen:
    TYPE = 0xE0
    CAPABILITY = "ui:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "surface", "uint32", False, 1024, 4194304, 512), (3, "title", "string", False, 1024, 4194304, 512), (4, "nodes", "UiNode", True, 1024, 4194304, 512), (5, "width", "uint32", False, 1024, 4194304, 512), (6, "closeable", "bool", False, 1024, 4194304, 512), (7, "anchor", "uint32", False, 1024, 4194304, 512), )
    id: str = ""
    surface: int = 0
    title: str = ""
    nodes: List["UiNode"] = field(default_factory=list)
    width: int = 0
    closeable: bool = False
    anchor: int = 0

@dataclass
class UiPatch:
    TYPE = 0xE1
    CAPABILITY = "ui:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "nodes", "UiNode", True, 1024, 4194304, 512), (3, "remove", "string", True, 1024, 4194304, 512), (4, "title", "string", False, 1024, 4194304, 512), )
    id: str = ""
    nodes: List["UiNode"] = field(default_factory=list)
    remove: List[str] = field(default_factory=list)
    title: str = ""

@dataclass
class UiClose:
    TYPE = 0xE2
    CAPABILITY = "ui:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), )
    id: str = ""

@dataclass
class UiEvent:
    TYPE = 0xE3
    CAPABILITY = "ui:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "node", "string", False, 1024, 4194304, 512), (3, "action", "uint32", False, 1024, 4194304, 512), (4, "value", "string", False, 1024, 4194304, 512), )
    id: str = ""
    node: str = ""
    action: int = 0
    value: str = ""

@dataclass
class InfoRequest:
    TYPE = 0xF0
    CAPABILITY = "info:1"
    FIELDS = ((1, "request_id", "uint32", False, 1024, 4194304, 512), (2, "fields", "string", True, 1024, 4194304, 512), (3, "reason", "string", False, 1024, 4194304, 512), )
    request_id: int = 0
    fields: List[str] = field(default_factory=list)
    reason: str = ""

@dataclass
class InfoReply:
    TYPE = 0xF1
    CAPABILITY = "info:1"
    FIELDS = ((1, "request_id", "uint32", False, 1024, 4194304, 512), (2, "granted", "string", True, 1024, 4194304, 512), (3, "input", "string", False, 1024, 4194304, 512), (4, "fps", "uint32", False, 1024, 4194304, 512), (5, "frame_p50", "uint32", False, 1024, 4194304, 512), (6, "frame_p99", "uint32", False, 1024, 4194304, 512), (7, "render_distance", "uint32", False, 1024, 4194304, 512), (8, "mods", "ModEntry", True, 1024, 4194304, 512), (9, "teho_modules", "string", True, 1024, 4194304, 512), (10, "locale", "string", False, 1024, 4194304, 512), (11, "gui_scale", "uint32", False, 1024, 4194304, 512), (12, "width", "uint32", False, 1024, 4194304, 512), (13, "height", "uint32", False, 1024, 4194304, 512), (14, "os", "string", False, 1024, 4194304, 512), (15, "memory_mb", "uint32", False, 1024, 4194304, 512), )
    request_id: int = 0
    granted: List[str] = field(default_factory=list)
    input: str = ""
    fps: int = 0
    frame_p50: int = 0
    frame_p99: int = 0
    render_distance: int = 0
    mods: List["ModEntry"] = field(default_factory=list)
    teho_modules: List[str] = field(default_factory=list)
    locale: str = ""
    gui_scale: int = 0
    width: int = 0
    height: int = 0
    os: str = ""
    memory_mb: int = 0

@dataclass
class AssertRequest:
    TYPE = 0x110
    CAPABILITY = "assert:1"
    FIELDS = ((1, "request_id", "uint32", False, 1024, 4194304, 512), (2, "nonce", "string", False, 1024, 4194304, 512), )
    request_id: int = 0
    nonce: str = ""

@dataclass
class AssertResult:
    TYPE = 0x111
    CAPABILITY = "assert:1"
    FIELDS = ((1, "request_id", "uint32", False, 1024, 4194304, 512), (2, "token", "string", False, 1024, 4194304, 512), (3, "error", "string", False, 1024, 4194304, 512), )
    request_id: int = 0
    token: str = ""
    error: str = ""

@dataclass
class AttestRequest:
    TYPE = 0x118
    CAPABILITY = "attest:1"
    FIELDS = ((1, "request_id", "uint32", False, 1024, 4194304, 512), (2, "nonce", "string", False, 1024, 4194304, 512), )
    request_id: int = 0
    nonce: str = ""

@dataclass
class AttestResult:
    TYPE = 0x119
    CAPABILITY = "attest:1"
    FIELDS = ((1, "request_id", "uint32", False, 1024, 4194304, 512), (2, "token", "string", False, 1024, 4194304, 512), (3, "error", "string", False, 1024, 4194304, 512), )
    request_id: int = 0
    token: str = ""
    error: str = ""

@dataclass
class PackHint:
    TYPE = 0x120
    CAPABILITY = "pack:1"
    FIELDS = ((1, "url", "string", False, 1024, 4194304, 512), (2, "sha1", "string", False, 1024, 4194304, 512), (3, "size", "uint32", False, 1024, 4194304, 512), (4, "required", "bool", False, 1024, 4194304, 512), (5, "prompt", "string", False, 1024, 4194304, 512), (6, "id", "string", False, 1024, 4194304, 512), )
    url: str = ""
    sha1: str = ""
    size: int = 0
    required: bool = False
    prompt: str = ""
    id: str = ""

@dataclass
class PackStatus:
    TYPE = 0x121
    CAPABILITY = "pack:1"
    FIELDS = ((1, "sha1", "string", False, 1024, 4194304, 512), (2, "state", "uint32", False, 1024, 4194304, 512), (3, "error", "string", False, 1024, 4194304, 512), )
    sha1: str = ""
    state: int = 0
    error: str = ""

@dataclass
class ClipMark:
    TYPE = 0x130
    CAPABILITY = "clips:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "title", "string", False, 1024, 4194304, 512), (3, "before_ms", "uint32", False, 1024, 4194304, 512), (4, "after_ms", "uint32", False, 1024, 4194304, 512), )
    id: str = ""
    title: str = ""
    before_ms: int = 0
    after_ms: int = 0

@dataclass
class ClipResult:
    TYPE = 0x131
    CAPABILITY = "clips:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "saved", "bool", False, 1024, 4194304, 512), (3, "url", "string", False, 1024, 4194304, 512), (4, "error", "string", False, 1024, 4194304, 512), )
    id: str = ""
    saved: bool = False
    url: str = ""
    error: str = ""

@dataclass
class ObserverPlayer:
    TYPE = None
    CAPABILITY = None
    FIELDS = ((1, "player", "string", False, 1024, 4194304, 512), (2, "name", "string", False, 1024, 4194304, 512), (3, "health", "uint32", False, 1024, 4194304, 512), (4, "alive", "bool", False, 1024, 4194304, 512), (5, "kills", "uint32", False, 1024, 4194304, 512), (6, "item", "string", False, 1024, 4194304, 512), )
    player: str = ""
    name: str = ""
    health: int = 0
    alive: bool = False
    kills: int = 0
    item: str = ""

@dataclass
class ObserverTeam:
    TYPE = None
    CAPABILITY = None
    FIELDS = ((1, "name", "string", False, 1024, 4194304, 512), (2, "color", "fixed32", False, 1024, 4194304, 512), (3, "score", "sint32", False, 1024, 4194304, 512), (4, "players", "ObserverPlayer", True, 1024, 4194304, 512), )
    name: str = ""
    color: int = 0
    score: int = 0
    players: List["ObserverPlayer"] = field(default_factory=list)

@dataclass
class ObserverHud:
    TYPE = 0x140
    CAPABILITY = "broadcast:1"
    FIELDS = ((1, "on", "bool", False, 1024, 4194304, 512), (2, "title", "string", False, 1024, 4194304, 512), (3, "subtitle", "string", False, 1024, 4194304, 512), (4, "teams", "ObserverTeam", True, 1024, 4194304, 512), )
    on: bool = False
    title: str = ""
    subtitle: str = ""
    teams: List["ObserverTeam"] = field(default_factory=list)

@dataclass
class CameraFollow:
    TYPE = 0x141
    CAPABILITY = "broadcast:1"
    FIELDS = ((1, "player", "string", False, 1024, 4194304, 512), (2, "mode", "uint32", False, 1024, 4194304, 512), (3, "distance", "uint32", False, 1024, 4194304, 512), (4, "smooth", "bool", False, 1024, 4194304, 512), )
    player: str = ""
    mode: int = 0
    distance: int = 0
    smooth: bool = False

@dataclass
class Statue:
    TYPE = 0x170
    CAPABILITY = "statue:1"
    FIELDS = ((1, "id", "string", False, 1024, 4194304, 512), (2, "player", "string", False, 1024, 4194304, 512), (3, "name", "string", False, 1024, 4194304, 512), (4, "x", "sint32", False, 1024, 4194304, 512), (5, "y", "sint32", False, 1024, 4194304, 512), (6, "z", "sint32", False, 1024, 4194304, 512), (7, "yaw", "sint32", False, 1024, 4194304, 512), (8, "pose", "uint32", False, 1024, 4194304, 512), (9, "scale", "uint32", False, 1024, 4194304, 512), (10, "dimension", "string", False, 1024, 4194304, 512), (11, "label", "string", False, 1024, 4194304, 512), (12, "emote", "string", False, 1024, 4194304, 512), )
    id: str = ""
    player: str = ""
    name: str = ""
    x: int = 0
    y: int = 0
    z: int = 0
    yaw: int = 0
    pose: int = 0
    scale: int = 0
    dimension: str = ""
    label: str = ""
    emote: str = ""

@dataclass
class StatueRemove:
    TYPE = 0x171
    CAPABILITY = "statue:1"
    FIELDS = ((1, "ids", "string", True, 1024, 4194304, 512), )
    ids: List[str] = field(default_factory=list)

@dataclass
class Branding:
    TYPE = 0x180
    CAPABILITY = "branding:1"
    FIELDS = ((1, "accent", "fixed32", False, 1024, 4194304, 512), (2, "logo", "bytes", False, 1024, 4194304, 512), (3, "banner", "bytes", False, 1024, 4194304, 512), (4, "background", "bytes", False, 1024, 4194304, 512), (5, "tagline", "string", False, 1024, 4194304, 512), (6, "reset", "bool", False, 1024, 4194304, 512), )
    accent: int = 0
    logo: bytes = b""
    banner: bytes = b""
    background: bytes = b""
    tagline: str = ""
    reset: bool = False

@dataclass
class ShowcaseOpen:
    TYPE = 0x190
    CAPABILITY = "showcase:1"
    FIELDS = ((1, "title", "string", False, 1024, 4194304, 512), (2, "items", "string", True, 1024, 4194304, 512), (3, "emote", "string", False, 1024, 4194304, 512), (4, "focus", "string", False, 1024, 4194304, 512), (5, "subtitle", "string", False, 1024, 4194304, 512), )
    title: str = ""
    items: List[str] = field(default_factory=list)
    emote: str = ""
    focus: str = ""
    subtitle: str = ""


MESSAGES = {cls.__name__: cls for cls in (Hello, Welcome, HelloRequest, Reset, Notify, Dismiss, WaypointSet, WaypointRemove, WaypointClear, Reply, Event, Chunk, AssetOffer, AssetWant, AssetData, ConsentResult, ModuleRules, SettingCap, SettingCaps, XrayBlock, StaffGrant, StaffRevoke, StaffState, EspEntry, EspStyle, Cooldown, CooldownClear, Timer, TimerRemove, StaffSupport, Highlight, HighlightRemove, Trail, InspectRequest, InspectRow, InspectAction, InspectCard, WheelAction, ActionWheel, Alert, TargetStats, Spectate, StaffAction, LagCell, LagMap, FreezeScreen, ScreenshotRequest, ScreenshotResult, ModListRequest, ModEntry, ModList, PrivateStatus, NpcSlot, NpcLook, NpcEmote, NpcClear, CombatRules, ThirdPartyPolicy, PolicyReport, SidebarRow, SidebarSet, SidebarClear, Bar, BarRemove, KillFeed, ScreenTint, Title, TntFuse, Beam, Hologram, Shape, WorldRemove, EntityStyle, EntityStyleClear, BorderStyle, FireColor, Nametag, NametagClear, TabEntry, TabList, Team, TeamRemove, NickHint, Keybind, Keybinds, KeyPress, CameraShake, FovPunch, LookAt, Letterbox, Keyframe, Cutscene, CutsceneStop, ScreenFade, CutsceneEnded, PlaySound, StopSound, Presence, InviteSheet, Transfer, ServerLink, ServerLinks, UiNode, UiOpen, UiPatch, UiClose, UiEvent, InfoRequest, InfoReply, AssertRequest, AssertResult, AttestRequest, AttestResult, PackHint, PackStatus, ClipMark, ClipResult, ObserverPlayer, ObserverTeam, ObserverHud, CameraFollow, Statue, StatueRemove, Branding, ShowcaseOpen)}
BY_TYPE = {cls.TYPE: cls for cls in MESSAGES.values() if cls.TYPE is not None}
