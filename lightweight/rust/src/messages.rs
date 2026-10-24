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

use crate::wire::{Error, Reader, Writer};

pub const CHANNEL: &str = "teho:api";
pub const VERSION: u32 = 2;
pub const MAX_FRAME: usize = 32000;
pub const MAX_STREAM: usize = 4194304;
pub const SLICE: usize = 30000;
pub const CAPABILITIES: &[&str] = &["notify:1", "waypoint:1", "asset:1", "modules:1", "staff:1", "hud:1", "suspect:1", "npc:1", "world:1", "players:1", "keybind:1", "camera:1", "audio:1", "social:1", "ui:1", "info:1", "assert:1", "attest:1", "pack:1", "clips:1", "broadcast:1", "statue:1", "branding:1", "showcase:1"];
pub const RANGES: &[(u32, u32, &str)] = &[(0x10, 0x1F, "notify:1"), (0x20, 0x2F, "waypoint:1"), (0x30, 0x3F, "modules:1"), (0x40, 0x4F, "staff:1"), (0x50, 0x5F, "hud:1"), (0x60, 0x6F, "suspect:1"), (0x70, 0x7F, "npc:1"), (0x80, 0x8F, "world:1"), (0x90, 0x9F, "players:1"), (0xA0, 0xAF, "keybind:1"), (0xB0, 0xBF, "camera:1"), (0xC0, 0xCF, "audio:1"), (0xD0, 0xDF, "social:1"), (0xE0, 0xEF, "ui:1"), (0xF0, 0xFF, "info:1"), (0x110, 0x117, "assert:1"), (0x118, 0x11F, "attest:1"), (0x120, 0x12F, "pack:1"), (0x130, 0x13F, "clips:1"), (0x140, 0x14F, "broadcast:1"), (0x170, 0x17F, "statue:1"), (0x180, 0x18F, "branding:1"), (0x190, 0x19F, "showcase:1")];

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Hello {
    pub protocol: u32,
    pub client: String,
    pub minecraft: String,
    pub platform: String,
    pub capabilities: Vec<String>,
}

impl Hello {
    pub const TYPE: u32 = 0x1;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.protocol));
        w.string(2, &self.client);
        w.string(3, &self.minecraft);
        w.string(4, &self.platform);
        for item in &self.capabilities {
            w.string(5, item);
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.protocol = r.varint()? as u32
                }
                2 => {
                    r.expect(2)?;
                    out.client = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.minecraft = r.string(1024)?
                }
                4 => {
                    r.expect(2)?;
                    out.platform = r.string(1024)?
                }
                5 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.capabilities.len() < 512 {
                        out.capabilities.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Welcome {
    pub protocol: u32,
    pub session: u64,
    pub enabled: Vec<String>,
    pub server_name: String,
    pub grant: String,
    pub verified: String,
}

impl Welcome {
    pub const TYPE: u32 = 0x2;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.protocol));
        w.uint(2, self.session);
        for item in &self.enabled {
            w.string(3, item);
        }
        w.string(4, &self.server_name);
        w.string(5, &self.grant);
        w.string(6, &self.verified);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.protocol = r.varint()? as u32
                }
                2 => {
                    r.expect(0)?;
                    out.session = r.varint()?
                }
                3 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.enabled.len() < 512 {
                        out.enabled.push(item);
                    }
                }
                4 => {
                    r.expect(2)?;
                    out.server_name = r.string(1024)?
                }
                5 => {
                    r.expect(2)?;
                    out.grant = r.string(8192)?
                }
                6 => {
                    r.expect(2)?;
                    out.verified = r.string(8192)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct HelloRequest {}

impl HelloRequest {
    pub const TYPE: u32 = 0x3;

    pub fn encode_body(&self) -> Vec<u8> {
        Vec::new()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            let _ = number;
            r.skip()?;
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Reset {}

impl Reset {
    pub const TYPE: u32 = 0x4;

    pub fn encode_body(&self) -> Vec<u8> {
        Vec::new()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            let _ = number;
            r.skip()?;
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Notify {
    pub id: String,
    pub title: String,
    pub body: String,
    pub accent: u32,
    pub duration_ms: u32,
    pub sound: bool,
}

impl Notify {
    pub const TYPE: u32 = 0x10;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.string(2, &self.title);
        w.string(3, &self.body);
        w.fixed32(4, self.accent);
        w.uint(5, u64::from(self.duration_ms));
        w.uint(6, u64::from(self.sound));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.title = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.body = r.string(1024)?
                }
                4 => {
                    r.expect(5)?;
                    out.accent = r.fixed32()?
                }
                5 => {
                    r.expect(0)?;
                    out.duration_ms = r.varint()? as u32
                }
                6 => {
                    r.expect(0)?;
                    out.sound = r.varint()? != 0
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Dismiss {
    pub id: String,
}

impl Dismiss {
    pub const TYPE: u32 = 0x11;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct WaypointSet {
    pub id: String,
    pub name: String,
    pub x: i32,
    pub y: i32,
    pub z: i32,
    pub dimension: String,
    pub color: u32,
    pub focus: bool,
}

impl WaypointSet {
    pub const TYPE: u32 = 0x20;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.string(2, &self.name);
        w.sint(3, self.x);
        w.sint(4, self.y);
        w.sint(5, self.z);
        w.string(6, &self.dimension);
        w.fixed32(7, self.color);
        w.uint(8, u64::from(self.focus));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.name = r.string(1024)?
                }
                3 => {
                    r.expect(0)?;
                    out.x = r.sint()?
                }
                4 => {
                    r.expect(0)?;
                    out.y = r.sint()?
                }
                5 => {
                    r.expect(0)?;
                    out.z = r.sint()?
                }
                6 => {
                    r.expect(2)?;
                    out.dimension = r.string(1024)?
                }
                7 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                8 => {
                    r.expect(0)?;
                    out.focus = r.varint()? != 0
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct WaypointRemove {
    pub id: String,
}

impl WaypointRemove {
    pub const TYPE: u32 = 0x21;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct WaypointClear {}

impl WaypointClear {
    pub const TYPE: u32 = 0x22;

    pub fn encode_body(&self) -> Vec<u8> {
        Vec::new()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            let _ = number;
            r.skip()?;
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Reply {
    pub request_id: u32,
    pub ok: bool,
    pub error: String,
    pub data: Vec<u8>,
}

impl Reply {
    pub const TYPE: u32 = 0x5;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.request_id));
        w.uint(2, u64::from(self.ok));
        w.string(3, &self.error);
        w.bytes(4, &self.data);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.request_id = r.varint()? as u32
                }
                2 => {
                    r.expect(0)?;
                    out.ok = r.varint()? != 0
                }
                3 => {
                    r.expect(2)?;
                    out.error = r.string(1024)?
                }
                4 => {
                    r.expect(2)?;
                    out.data = r.bytes(4194304)?.to_vec()
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Event {
    pub kind: String,
    pub target: String,
    pub value: String,
    pub request_id: u32,
}

impl Event {
    pub const TYPE: u32 = 0x6;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.kind);
        w.string(2, &self.target);
        w.string(3, &self.value);
        w.uint(4, u64::from(self.request_id));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.kind = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.target = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.value = r.string(1024)?
                }
                4 => {
                    r.expect(0)?;
                    out.request_id = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Chunk {
    pub stream: u32,
    pub index: u32,
    pub total: u32,
    pub data: Vec<u8>,
}

impl Chunk {
    pub const TYPE: u32 = 0x7;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.stream));
        w.uint(2, u64::from(self.index));
        w.uint(3, u64::from(self.total));
        w.bytes(4, &self.data);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.stream = r.varint()? as u32
                }
                2 => {
                    r.expect(0)?;
                    out.index = r.varint()? as u32
                }
                3 => {
                    r.expect(0)?;
                    out.total = r.varint()? as u32
                }
                4 => {
                    r.expect(2)?;
                    out.data = r.bytes(4194304)?.to_vec()
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct AssetOffer {
    pub hash: Vec<u8>,
    pub kind: u32,
    pub size: u32,
}

impl AssetOffer {
    pub const TYPE: u32 = 0x8;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.bytes(1, &self.hash);
        w.uint(2, u64::from(self.kind));
        w.uint(3, u64::from(self.size));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.hash = r.bytes(4194304)?.to_vec()
                }
                2 => {
                    r.expect(0)?;
                    out.kind = r.varint()? as u32
                }
                3 => {
                    r.expect(0)?;
                    out.size = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct AssetWant {
    pub hashes: Vec<Vec<u8>>,
}

impl AssetWant {
    pub const TYPE: u32 = 0x9;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.hashes {
            w.bytes(1, item);
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = r.bytes(1024)?.to_vec();
                    if out.hashes.len() < 512 {
                        out.hashes.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct AssetData {
    pub hash: Vec<u8>,
    pub kind: u32,
    pub data: Vec<u8>,
}

impl AssetData {
    pub const TYPE: u32 = 0xC;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.bytes(1, &self.hash);
        w.uint(2, u64::from(self.kind));
        w.bytes(3, &self.data);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.hash = r.bytes(4194304)?.to_vec()
                }
                2 => {
                    r.expect(0)?;
                    out.kind = r.varint()? as u32
                }
                3 => {
                    r.expect(2)?;
                    out.data = r.bytes(4194304)?.to_vec()
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ConsentResult {
    pub scope: String,
    pub granted: bool,
    pub items: Vec<String>,
}

impl ConsentResult {
    pub const TYPE: u32 = 0xD;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.scope);
        w.uint(2, u64::from(self.granted));
        for item in &self.items {
            w.string(3, item);
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.scope = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.granted = r.varint()? != 0
                }
                3 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.items.len() < 512 {
                        out.items.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ModuleRules {
    pub disabled: Vec<String>,
    pub disabled_categories: Vec<String>,
    pub reason: String,
    pub competitive: bool,
    pub allowed: Vec<String>,
}

impl ModuleRules {
    pub const TYPE: u32 = 0x30;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.disabled {
            w.string(1, item);
        }
        for item in &self.disabled_categories {
            w.string(2, item);
        }
        w.string(3, &self.reason);
        w.uint(4, u64::from(self.competitive));
        for item in &self.allowed {
            w.string(5, item);
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.disabled.len() < 512 {
                        out.disabled.push(item);
                    }
                }
                2 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.disabled_categories.len() < 512 {
                        out.disabled_categories.push(item);
                    }
                }
                3 => {
                    r.expect(2)?;
                    out.reason = r.string(1024)?
                }
                4 => {
                    r.expect(0)?;
                    out.competitive = r.varint()? != 0
                }
                5 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.allowed.len() < 512 {
                        out.allowed.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct SettingCap {
    pub module: String,
    pub key: String,
    pub min: i32,
    pub max: i32,
    pub bounded: bool,
    pub pinned: String,
}

impl SettingCap {
    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.module);
        w.string(2, &self.key);
        w.sint(3, self.min);
        w.sint(4, self.max);
        w.uint(5, u64::from(self.bounded));
        w.string(6, &self.pinned);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.module = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.key = r.string(1024)?
                }
                3 => {
                    r.expect(0)?;
                    out.min = r.sint()?
                }
                4 => {
                    r.expect(0)?;
                    out.max = r.sint()?
                }
                5 => {
                    r.expect(0)?;
                    out.bounded = r.varint()? != 0
                }
                6 => {
                    r.expect(2)?;
                    out.pinned = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct SettingCaps {
    pub caps: Vec<SettingCap>,
}

impl SettingCaps {
    pub const TYPE: u32 = 0x31;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.caps {
            w.bytes(1, &item.encode_body());
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = SettingCap::decode_body(r.bytes(4194304)?)?;
                    if out.caps.len() < 512 {
                        out.caps.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct XrayBlock {
    pub id: String,
    pub color: u32,
}

impl XrayBlock {
    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.fixed32(2, self.color);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct StaffGrant {
    pub tools: Vec<String>,
    pub reason: String,
    pub xray_blocks: Vec<XrayBlock>,
    pub radius: u32,
    pub storage_blocks: Vec<XrayBlock>,
    pub entity_types: Vec<XrayBlock>,
}

impl StaffGrant {
    pub const TYPE: u32 = 0x40;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.tools {
            w.string(1, item);
        }
        w.string(2, &self.reason);
        for item in &self.xray_blocks {
            w.bytes(3, &item.encode_body());
        }
        w.uint(4, u64::from(self.radius));
        for item in &self.storage_blocks {
            w.bytes(5, &item.encode_body());
        }
        for item in &self.entity_types {
            w.bytes(6, &item.encode_body());
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.tools.len() < 512 {
                        out.tools.push(item);
                    }
                }
                2 => {
                    r.expect(2)?;
                    out.reason = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    let item = XrayBlock::decode_body(r.bytes(4194304)?)?;
                    if out.xray_blocks.len() < 512 {
                        out.xray_blocks.push(item);
                    }
                }
                4 => {
                    r.expect(0)?;
                    out.radius = r.varint()? as u32
                }
                5 => {
                    r.expect(2)?;
                    let item = XrayBlock::decode_body(r.bytes(4194304)?)?;
                    if out.storage_blocks.len() < 512 {
                        out.storage_blocks.push(item);
                    }
                }
                6 => {
                    r.expect(2)?;
                    let item = XrayBlock::decode_body(r.bytes(4194304)?)?;
                    if out.entity_types.len() < 512 {
                        out.entity_types.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct StaffRevoke {
    pub tools: Vec<String>,
}

impl StaffRevoke {
    pub const TYPE: u32 = 0x41;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.tools {
            w.string(1, item);
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.tools.len() < 512 {
                        out.tools.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct StaffState {
    pub tool: String,
    pub active: bool,
}

impl StaffState {
    pub const TYPE: u32 = 0x42;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.tool);
        w.uint(2, u64::from(self.active));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.tool = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.active = r.varint()? != 0
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct EspEntry {
    pub player: String,
    pub color: u32,
    pub label: String,
}

impl EspEntry {
    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.player);
        w.fixed32(2, self.color);
        w.string(3, &self.label);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.player = r.string(1024)?
                }
                2 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                3 => {
                    r.expect(2)?;
                    out.label = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct EspStyle {
    pub entries: Vec<EspEntry>,
}

impl EspStyle {
    pub const TYPE: u32 = 0x43;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.entries {
            w.bytes(1, &item.encode_body());
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = EspEntry::decode_body(r.bytes(4194304)?)?;
                    if out.entries.len() < 512 {
                        out.entries.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Cooldown {
    pub id: String,
    pub label: String,
    pub duration_ms: u32,
    pub item: String,
    pub asset: Vec<u8>,
    pub color: u32,
}

impl Cooldown {
    pub const TYPE: u32 = 0x50;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.string(2, &self.label);
        w.uint(3, u64::from(self.duration_ms));
        w.string(4, &self.item);
        w.bytes(5, &self.asset);
        w.fixed32(6, self.color);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.label = r.string(1024)?
                }
                3 => {
                    r.expect(0)?;
                    out.duration_ms = r.varint()? as u32
                }
                4 => {
                    r.expect(2)?;
                    out.item = r.string(1024)?
                }
                5 => {
                    r.expect(2)?;
                    out.asset = r.bytes(4194304)?.to_vec()
                }
                6 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct CooldownClear {}

impl CooldownClear {
    pub const TYPE: u32 = 0x51;

    pub fn encode_body(&self) -> Vec<u8> {
        Vec::new()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            let _ = number;
            r.skip()?;
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Timer {
    pub id: String,
    pub label: String,
    pub mode: u32,
    pub millis: i32,
    pub paused: bool,
    pub color: u32,
}

impl Timer {
    pub const TYPE: u32 = 0x52;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.string(2, &self.label);
        w.uint(3, u64::from(self.mode));
        w.sint(4, self.millis);
        w.uint(5, u64::from(self.paused));
        w.fixed32(6, self.color);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.label = r.string(1024)?
                }
                3 => {
                    r.expect(0)?;
                    out.mode = r.varint()? as u32
                }
                4 => {
                    r.expect(0)?;
                    out.millis = r.sint()?
                }
                5 => {
                    r.expect(0)?;
                    out.paused = r.varint()? != 0
                }
                6 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct TimerRemove {
    pub id: String,
}

impl TimerRemove {
    pub const TYPE: u32 = 0x53;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct StaffSupport {
    pub tools: Vec<String>,
}

impl StaffSupport {
    pub const TYPE: u32 = 0x44;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.tools {
            w.string(1, item);
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.tools.len() < 512 {
                        out.tools.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Highlight {
    pub id: String,
    pub kind: u32,
    pub entity: String,
    pub x: i32,
    pub y: i32,
    pub z: i32,
    pub x2: i32,
    pub y2: i32,
    pub z2: i32,
    pub color: u32,
    pub label: String,
    pub style: u32,
    pub tracer: bool,
    pub ttl_ms: u32,
    pub dimension: String,
}

impl Highlight {
    pub const TYPE: u32 = 0x45;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.uint(2, u64::from(self.kind));
        w.string(3, &self.entity);
        w.sint(4, self.x);
        w.sint(5, self.y);
        w.sint(6, self.z);
        w.sint(7, self.x2);
        w.sint(8, self.y2);
        w.sint(9, self.z2);
        w.fixed32(10, self.color);
        w.string(11, &self.label);
        w.uint(12, u64::from(self.style));
        w.uint(13, u64::from(self.tracer));
        w.uint(14, u64::from(self.ttl_ms));
        w.string(15, &self.dimension);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.kind = r.varint()? as u32
                }
                3 => {
                    r.expect(2)?;
                    out.entity = r.string(1024)?
                }
                4 => {
                    r.expect(0)?;
                    out.x = r.sint()?
                }
                5 => {
                    r.expect(0)?;
                    out.y = r.sint()?
                }
                6 => {
                    r.expect(0)?;
                    out.z = r.sint()?
                }
                7 => {
                    r.expect(0)?;
                    out.x2 = r.sint()?
                }
                8 => {
                    r.expect(0)?;
                    out.y2 = r.sint()?
                }
                9 => {
                    r.expect(0)?;
                    out.z2 = r.sint()?
                }
                10 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                11 => {
                    r.expect(2)?;
                    out.label = r.string(1024)?
                }
                12 => {
                    r.expect(0)?;
                    out.style = r.varint()? as u32
                }
                13 => {
                    r.expect(0)?;
                    out.tracer = r.varint()? != 0
                }
                14 => {
                    r.expect(0)?;
                    out.ttl_ms = r.varint()? as u32
                }
                15 => {
                    r.expect(2)?;
                    out.dimension = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct HighlightRemove {
    pub ids: Vec<String>,
}

impl HighlightRemove {
    pub const TYPE: u32 = 0x46;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.ids {
            w.string(1, item);
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.ids.len() < 512 {
                        out.ids.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Trail {
    pub player: String,
    pub points: Vec<i32>,
    pub color: u32,
    pub label: String,
    pub ttl_ms: u32,
    pub dimension: String,
}

impl Trail {
    pub const TYPE: u32 = 0x47;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.player);
        for item in &self.points {
            w.sint(2, *item);
        }
        w.fixed32(3, self.color);
        w.string(4, &self.label);
        w.uint(5, u64::from(self.ttl_ms));
        w.string(6, &self.dimension);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.player = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    let item = r.sint()?;
                    if out.points.len() < 3072 {
                        out.points.push(item);
                    }
                }
                3 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                4 => {
                    r.expect(2)?;
                    out.label = r.string(1024)?
                }
                5 => {
                    r.expect(0)?;
                    out.ttl_ms = r.varint()? as u32
                }
                6 => {
                    r.expect(2)?;
                    out.dimension = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct InspectRequest {
    pub request_id: u32,
    pub kind: u32,
    pub target: String,
    pub x: i32,
    pub y: i32,
    pub z: i32,
}

impl InspectRequest {
    pub const TYPE: u32 = 0x48;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.request_id));
        w.uint(2, u64::from(self.kind));
        w.string(3, &self.target);
        w.sint(4, self.x);
        w.sint(5, self.y);
        w.sint(6, self.z);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.request_id = r.varint()? as u32
                }
                2 => {
                    r.expect(0)?;
                    out.kind = r.varint()? as u32
                }
                3 => {
                    r.expect(2)?;
                    out.target = r.string(1024)?
                }
                4 => {
                    r.expect(0)?;
                    out.x = r.sint()?
                }
                5 => {
                    r.expect(0)?;
                    out.y = r.sint()?
                }
                6 => {
                    r.expect(0)?;
                    out.z = r.sint()?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct InspectRow {
    pub label: String,
    pub value: String,
    pub color: u32,
}

impl InspectRow {
    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.label);
        w.string(2, &self.value);
        w.fixed32(3, self.color);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.label = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.value = r.string(1024)?
                }
                3 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct InspectAction {
    pub id: String,
    pub label: String,
}

impl InspectAction {
    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.string(2, &self.label);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.label = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct InspectCard {
    pub request_id: u32,
    pub title: String,
    pub subtitle: String,
    pub rows: Vec<InspectRow>,
    pub actions: Vec<InspectAction>,
    pub ttl_ms: u32,
    pub target: String,
}

impl InspectCard {
    pub const TYPE: u32 = 0x49;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.request_id));
        w.string(2, &self.title);
        w.string(3, &self.subtitle);
        for item in &self.rows {
            w.bytes(4, &item.encode_body());
        }
        for item in &self.actions {
            w.bytes(5, &item.encode_body());
        }
        w.uint(6, u64::from(self.ttl_ms));
        w.string(7, &self.target);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.request_id = r.varint()? as u32
                }
                2 => {
                    r.expect(2)?;
                    out.title = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.subtitle = r.string(1024)?
                }
                4 => {
                    r.expect(2)?;
                    let item = InspectRow::decode_body(r.bytes(4194304)?)?;
                    if out.rows.len() < 512 {
                        out.rows.push(item);
                    }
                }
                5 => {
                    r.expect(2)?;
                    let item = InspectAction::decode_body(r.bytes(4194304)?)?;
                    if out.actions.len() < 512 {
                        out.actions.push(item);
                    }
                }
                6 => {
                    r.expect(0)?;
                    out.ttl_ms = r.varint()? as u32
                }
                7 => {
                    r.expect(2)?;
                    out.target = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct WheelAction {
    pub id: String,
    pub label: String,
    pub item: String,
    pub color: u32,
    pub target: u32,
}

impl WheelAction {
    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.string(2, &self.label);
        w.string(3, &self.item);
        w.fixed32(4, self.color);
        w.uint(5, u64::from(self.target));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.label = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.item = r.string(1024)?
                }
                4 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                5 => {
                    r.expect(0)?;
                    out.target = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ActionWheel {
    pub actions: Vec<WheelAction>,
    pub title: String,
}

impl ActionWheel {
    pub const TYPE: u32 = 0x4A;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.actions {
            w.bytes(1, &item.encode_body());
        }
        w.string(2, &self.title);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = WheelAction::decode_body(r.bytes(4194304)?)?;
                    if out.actions.len() < 512 {
                        out.actions.push(item);
                    }
                }
                2 => {
                    r.expect(2)?;
                    out.title = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Alert {
    pub id: String,
    pub severity: u32,
    pub title: String,
    pub body: String,
    pub player: String,
    pub x: i32,
    pub y: i32,
    pub z: i32,
    pub dimension: String,
    pub ttl_ms: u32,
}

impl Alert {
    pub const TYPE: u32 = 0x4B;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.uint(2, u64::from(self.severity));
        w.string(3, &self.title);
        w.string(4, &self.body);
        w.string(5, &self.player);
        w.sint(6, self.x);
        w.sint(7, self.y);
        w.sint(8, self.z);
        w.string(9, &self.dimension);
        w.uint(10, u64::from(self.ttl_ms));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.severity = r.varint()? as u32
                }
                3 => {
                    r.expect(2)?;
                    out.title = r.string(1024)?
                }
                4 => {
                    r.expect(2)?;
                    out.body = r.string(1024)?
                }
                5 => {
                    r.expect(2)?;
                    out.player = r.string(1024)?
                }
                6 => {
                    r.expect(0)?;
                    out.x = r.sint()?
                }
                7 => {
                    r.expect(0)?;
                    out.y = r.sint()?
                }
                8 => {
                    r.expect(0)?;
                    out.z = r.sint()?
                }
                9 => {
                    r.expect(2)?;
                    out.dimension = r.string(1024)?
                }
                10 => {
                    r.expect(0)?;
                    out.ttl_ms = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct TargetStats {
    pub player: String,
    pub rows: Vec<InspectRow>,
    pub ttl_ms: u32,
}

impl TargetStats {
    pub const TYPE: u32 = 0x4C;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.player);
        for item in &self.rows {
            w.bytes(2, &item.encode_body());
        }
        w.uint(3, u64::from(self.ttl_ms));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.player = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    let item = InspectRow::decode_body(r.bytes(4194304)?)?;
                    if out.rows.len() < 512 {
                        out.rows.push(item);
                    }
                }
                3 => {
                    r.expect(0)?;
                    out.ttl_ms = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Spectate {
    pub player: String,
    pub distance: u32,
}

impl Spectate {
    pub const TYPE: u32 = 0x4D;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.player);
        w.uint(2, u64::from(self.distance));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.player = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.distance = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct StaffAction {
    pub action: String,
    pub target_kind: u32,
    pub target: String,
    pub x: i32,
    pub y: i32,
    pub z: i32,
}

impl StaffAction {
    pub const TYPE: u32 = 0x4E;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.action);
        w.uint(2, u64::from(self.target_kind));
        w.string(3, &self.target);
        w.sint(4, self.x);
        w.sint(5, self.y);
        w.sint(6, self.z);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.action = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.target_kind = r.varint()? as u32
                }
                3 => {
                    r.expect(2)?;
                    out.target = r.string(1024)?
                }
                4 => {
                    r.expect(0)?;
                    out.x = r.sint()?
                }
                5 => {
                    r.expect(0)?;
                    out.y = r.sint()?
                }
                6 => {
                    r.expect(0)?;
                    out.z = r.sint()?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct LagCell {
    pub chunk_x: i32,
    pub chunk_z: i32,
    pub score: u32,
    pub label: String,
}

impl LagCell {
    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.sint(1, self.chunk_x);
        w.sint(2, self.chunk_z);
        w.uint(3, u64::from(self.score));
        w.string(4, &self.label);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.chunk_x = r.sint()?
                }
                2 => {
                    r.expect(0)?;
                    out.chunk_z = r.sint()?
                }
                3 => {
                    r.expect(0)?;
                    out.score = r.varint()? as u32
                }
                4 => {
                    r.expect(2)?;
                    out.label = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct LagMap {
    pub cells: Vec<LagCell>,
    pub dimension: String,
    pub ttl_ms: u32,
}

impl LagMap {
    pub const TYPE: u32 = 0x4F;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.cells {
            w.bytes(1, &item.encode_body());
        }
        w.string(2, &self.dimension);
        w.uint(3, u64::from(self.ttl_ms));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = LagCell::decode_body(r.bytes(4194304)?)?;
                    if out.cells.len() < 512 {
                        out.cells.push(item);
                    }
                }
                2 => {
                    r.expect(2)?;
                    out.dimension = r.string(1024)?
                }
                3 => {
                    r.expect(0)?;
                    out.ttl_ms = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct FreezeScreen {
    pub on: bool,
    pub title: String,
    pub body: String,
}

impl FreezeScreen {
    pub const TYPE: u32 = 0x60;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.on));
        w.string(2, &self.title);
        w.string(3, &self.body);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.on = r.varint()? != 0
                }
                2 => {
                    r.expect(2)?;
                    out.title = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.body = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ScreenshotRequest {
    pub request_id: u32,
    pub reason: String,
    pub max_width: u32,
}

impl ScreenshotRequest {
    pub const TYPE: u32 = 0x61;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.request_id));
        w.string(2, &self.reason);
        w.uint(3, u64::from(self.max_width));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.request_id = r.varint()? as u32
                }
                2 => {
                    r.expect(2)?;
                    out.reason = r.string(1024)?
                }
                3 => {
                    r.expect(0)?;
                    out.max_width = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ScreenshotResult {
    pub request_id: u32,
    pub granted: bool,
    pub png: Vec<u8>,
    pub width: u32,
    pub height: u32,
}

impl ScreenshotResult {
    pub const TYPE: u32 = 0x62;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.request_id));
        w.uint(2, u64::from(self.granted));
        w.bytes(3, &self.png);
        w.uint(4, u64::from(self.width));
        w.uint(5, u64::from(self.height));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.request_id = r.varint()? as u32
                }
                2 => {
                    r.expect(0)?;
                    out.granted = r.varint()? != 0
                }
                3 => {
                    r.expect(2)?;
                    out.png = r.bytes(4194304)?.to_vec()
                }
                4 => {
                    r.expect(0)?;
                    out.width = r.varint()? as u32
                }
                5 => {
                    r.expect(0)?;
                    out.height = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ModListRequest {
    pub request_id: u32,
    pub reason: String,
}

impl ModListRequest {
    pub const TYPE: u32 = 0x63;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.request_id));
        w.string(2, &self.reason);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.request_id = r.varint()? as u32
                }
                2 => {
                    r.expect(2)?;
                    out.reason = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ModEntry {
    pub id: String,
    pub name: String,
    pub version: String,
}

impl ModEntry {
    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.string(2, &self.name);
        w.string(3, &self.version);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.name = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.version = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ModList {
    pub request_id: u32,
    pub granted: bool,
    pub mods: Vec<ModEntry>,
    pub teho_modules: Vec<String>,
}

impl ModList {
    pub const TYPE: u32 = 0x64;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.request_id));
        w.uint(2, u64::from(self.granted));
        for item in &self.mods {
            w.bytes(3, &item.encode_body());
        }
        for item in &self.teho_modules {
            w.string(4, item);
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.request_id = r.varint()? as u32
                }
                2 => {
                    r.expect(0)?;
                    out.granted = r.varint()? != 0
                }
                3 => {
                    r.expect(2)?;
                    let item = ModEntry::decode_body(r.bytes(4194304)?)?;
                    if out.mods.len() < 512 {
                        out.mods.push(item);
                    }
                }
                4 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.teho_modules.len() < 512 {
                        out.teho_modules.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct PrivateStatus {
    pub capabilities: Vec<String>,
    pub error: String,
    pub server: String,
}

impl PrivateStatus {
    pub const TYPE: u32 = 0xE;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.capabilities {
            w.string(1, item);
        }
        w.string(2, &self.error);
        w.string(3, &self.server);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.capabilities.len() < 512 {
                        out.capabilities.push(item);
                    }
                }
                2 => {
                    r.expect(2)?;
                    out.error = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.server = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct NpcSlot {
    pub slot: String,
    pub id: String,
}

impl NpcSlot {
    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.slot);
        w.string(2, &self.id);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.slot = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct NpcLook {
    pub npc: String,
    pub mode: u32,
    pub slots: Vec<NpcSlot>,
}

impl NpcLook {
    pub const TYPE: u32 = 0x70;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.npc);
        w.uint(2, u64::from(self.mode));
        for item in &self.slots {
            w.bytes(3, &item.encode_body());
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.npc = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.mode = r.varint()? as u32
                }
                3 => {
                    r.expect(2)?;
                    let item = NpcSlot::decode_body(r.bytes(4194304)?)?;
                    if out.slots.len() < 512 {
                        out.slots.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct NpcEmote {
    pub npc: String,
    pub action: u32,
    pub emote: String,
    pub r#loop: bool,
}

impl NpcEmote {
    pub const TYPE: u32 = 0x71;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.npc);
        w.uint(2, u64::from(self.action));
        w.string(3, &self.emote);
        w.uint(4, u64::from(self.r#loop));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.npc = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.action = r.varint()? as u32
                }
                3 => {
                    r.expect(2)?;
                    out.emote = r.string(1024)?
                }
                4 => {
                    r.expect(0)?;
                    out.r#loop = r.varint()? != 0
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct NpcClear {
    pub npcs: Vec<String>,
}

impl NpcClear {
    pub const TYPE: u32 = 0x72;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.npcs {
            w.string(1, item);
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.npcs.len() < 512 {
                        out.npcs.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct CombatRules {
    pub legacy_swing: bool,
    pub legacy_blockhit: bool,
    pub no_hit_delay_visual: bool,
    pub miss_penalty_visual: bool,
    pub legacy_hit_color: bool,
}

impl CombatRules {
    pub const TYPE: u32 = 0x32;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.legacy_swing));
        w.uint(2, u64::from(self.legacy_blockhit));
        w.uint(3, u64::from(self.no_hit_delay_visual));
        w.uint(4, u64::from(self.miss_penalty_visual));
        w.uint(5, u64::from(self.legacy_hit_color));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.legacy_swing = r.varint()? != 0
                }
                2 => {
                    r.expect(0)?;
                    out.legacy_blockhit = r.varint()? != 0
                }
                3 => {
                    r.expect(0)?;
                    out.no_hit_delay_visual = r.varint()? != 0
                }
                4 => {
                    r.expect(0)?;
                    out.miss_penalty_visual = r.varint()? != 0
                }
                5 => {
                    r.expect(0)?;
                    out.legacy_hit_color = r.varint()? != 0
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ThirdPartyPolicy {
    pub deny: Vec<String>,
    pub reason: String,
    pub report: bool,
}

impl ThirdPartyPolicy {
    pub const TYPE: u32 = 0x33;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.deny {
            w.string(1, item);
        }
        w.string(2, &self.reason);
        w.uint(3, u64::from(self.report));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.deny.len() < 512 {
                        out.deny.push(item);
                    }
                }
                2 => {
                    r.expect(2)?;
                    out.reason = r.string(1024)?
                }
                3 => {
                    r.expect(0)?;
                    out.report = r.varint()? != 0
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct PolicyReport {
    pub granted: bool,
    pub violations: Vec<ModEntry>,
}

impl PolicyReport {
    pub const TYPE: u32 = 0x34;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.granted));
        for item in &self.violations {
            w.bytes(2, &item.encode_body());
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.granted = r.varint()? != 0
                }
                2 => {
                    r.expect(2)?;
                    let item = ModEntry::decode_body(r.bytes(4194304)?)?;
                    if out.violations.len() < 512 {
                        out.violations.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct SidebarRow {
    pub id: String,
    pub text: String,
    pub value: String,
    pub item: String,
    pub color: u32,
    pub progress: u32,
    pub asset: Vec<u8>,
    pub order: i32,
}

impl SidebarRow {
    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.string(2, &self.text);
        w.string(3, &self.value);
        w.string(4, &self.item);
        w.fixed32(5, self.color);
        w.uint(6, u64::from(self.progress));
        w.bytes(7, &self.asset);
        w.sint(8, self.order);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.text = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.value = r.string(1024)?
                }
                4 => {
                    r.expect(2)?;
                    out.item = r.string(1024)?
                }
                5 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                6 => {
                    r.expect(0)?;
                    out.progress = r.varint()? as u32
                }
                7 => {
                    r.expect(2)?;
                    out.asset = r.bytes(4194304)?.to_vec()
                }
                8 => {
                    r.expect(0)?;
                    out.order = r.sint()?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct SidebarSet {
    pub title: String,
    pub rows: Vec<SidebarRow>,
    pub remove: Vec<String>,
    pub replace: bool,
    pub accent: u32,
    pub footer: String,
}

impl SidebarSet {
    pub const TYPE: u32 = 0x54;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.title);
        for item in &self.rows {
            w.bytes(2, &item.encode_body());
        }
        for item in &self.remove {
            w.string(3, item);
        }
        w.uint(4, u64::from(self.replace));
        w.fixed32(5, self.accent);
        w.string(6, &self.footer);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.title = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    let item = SidebarRow::decode_body(r.bytes(4194304)?)?;
                    if out.rows.len() < 512 {
                        out.rows.push(item);
                    }
                }
                3 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.remove.len() < 512 {
                        out.remove.push(item);
                    }
                }
                4 => {
                    r.expect(0)?;
                    out.replace = r.varint()? != 0
                }
                5 => {
                    r.expect(5)?;
                    out.accent = r.fixed32()?
                }
                6 => {
                    r.expect(2)?;
                    out.footer = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct SidebarClear {}

impl SidebarClear {
    pub const TYPE: u32 = 0x55;

    pub fn encode_body(&self) -> Vec<u8> {
        Vec::new()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            let _ = number;
            r.skip()?;
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Bar {
    pub id: String,
    pub text: String,
    pub progress: u32,
    pub color: u32,
    pub style: u32,
    pub ttl_ms: u32,
    pub item: String,
}

impl Bar {
    pub const TYPE: u32 = 0x56;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.string(2, &self.text);
        w.uint(3, u64::from(self.progress));
        w.fixed32(4, self.color);
        w.uint(5, u64::from(self.style));
        w.uint(6, u64::from(self.ttl_ms));
        w.string(7, &self.item);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.text = r.string(1024)?
                }
                3 => {
                    r.expect(0)?;
                    out.progress = r.varint()? as u32
                }
                4 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                5 => {
                    r.expect(0)?;
                    out.style = r.varint()? as u32
                }
                6 => {
                    r.expect(0)?;
                    out.ttl_ms = r.varint()? as u32
                }
                7 => {
                    r.expect(2)?;
                    out.item = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct BarRemove {
    pub ids: Vec<String>,
}

impl BarRemove {
    pub const TYPE: u32 = 0x57;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.ids {
            w.string(1, item);
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.ids.len() < 512 {
                        out.ids.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct KillFeed {
    pub killer: String,
    pub victim: String,
    pub weapon: String,
    pub text: String,
    pub color: u32,
    pub killer_id: String,
    pub victim_id: String,
    pub ttl_ms: u32,
}

impl KillFeed {
    pub const TYPE: u32 = 0x58;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.killer);
        w.string(2, &self.victim);
        w.string(3, &self.weapon);
        w.string(4, &self.text);
        w.fixed32(5, self.color);
        w.string(6, &self.killer_id);
        w.string(7, &self.victim_id);
        w.uint(8, u64::from(self.ttl_ms));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.killer = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.victim = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.weapon = r.string(1024)?
                }
                4 => {
                    r.expect(2)?;
                    out.text = r.string(1024)?
                }
                5 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                6 => {
                    r.expect(2)?;
                    out.killer_id = r.string(1024)?
                }
                7 => {
                    r.expect(2)?;
                    out.victim_id = r.string(1024)?
                }
                8 => {
                    r.expect(0)?;
                    out.ttl_ms = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ScreenTint {
    pub color: u32,
    pub vignette: bool,
    pub fade_ms: u32,
    pub ttl_ms: u32,
    pub strength: u32,
}

impl ScreenTint {
    pub const TYPE: u32 = 0x59;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.fixed32(1, self.color);
        w.uint(2, u64::from(self.vignette));
        w.uint(3, u64::from(self.fade_ms));
        w.uint(4, u64::from(self.ttl_ms));
        w.uint(5, u64::from(self.strength));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                2 => {
                    r.expect(0)?;
                    out.vignette = r.varint()? != 0
                }
                3 => {
                    r.expect(0)?;
                    out.fade_ms = r.varint()? as u32
                }
                4 => {
                    r.expect(0)?;
                    out.ttl_ms = r.varint()? as u32
                }
                5 => {
                    r.expect(0)?;
                    out.strength = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Title {
    pub title: String,
    pub subtitle: String,
    pub animation: u32,
    pub fade_in_ms: u32,
    pub stay_ms: u32,
    pub fade_out_ms: u32,
    pub color: u32,
    pub scale: u32,
}

impl Title {
    pub const TYPE: u32 = 0x5A;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.title);
        w.string(2, &self.subtitle);
        w.uint(3, u64::from(self.animation));
        w.uint(4, u64::from(self.fade_in_ms));
        w.uint(5, u64::from(self.stay_ms));
        w.uint(6, u64::from(self.fade_out_ms));
        w.fixed32(7, self.color);
        w.uint(8, u64::from(self.scale));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.title = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.subtitle = r.string(1024)?
                }
                3 => {
                    r.expect(0)?;
                    out.animation = r.varint()? as u32
                }
                4 => {
                    r.expect(0)?;
                    out.fade_in_ms = r.varint()? as u32
                }
                5 => {
                    r.expect(0)?;
                    out.stay_ms = r.varint()? as u32
                }
                6 => {
                    r.expect(0)?;
                    out.fade_out_ms = r.varint()? as u32
                }
                7 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                8 => {
                    r.expect(0)?;
                    out.scale = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct TntFuse {
    pub fuse_ticks: u32,
}

impl TntFuse {
    pub const TYPE: u32 = 0x5B;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.fuse_ticks));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.fuse_ticks = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Beam {
    pub id: String,
    pub x: i32,
    pub y: i32,
    pub z: i32,
    pub entity: String,
    pub color: u32,
    pub height: u32,
    pub width: u32,
    pub pulse: bool,
    pub dimension: String,
    pub ttl_ms: u32,
}

impl Beam {
    pub const TYPE: u32 = 0x80;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.sint(2, self.x);
        w.sint(3, self.y);
        w.sint(4, self.z);
        w.string(5, &self.entity);
        w.fixed32(6, self.color);
        w.uint(7, u64::from(self.height));
        w.uint(8, u64::from(self.width));
        w.uint(9, u64::from(self.pulse));
        w.string(10, &self.dimension);
        w.uint(11, u64::from(self.ttl_ms));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.x = r.sint()?
                }
                3 => {
                    r.expect(0)?;
                    out.y = r.sint()?
                }
                4 => {
                    r.expect(0)?;
                    out.z = r.sint()?
                }
                5 => {
                    r.expect(2)?;
                    out.entity = r.string(1024)?
                }
                6 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                7 => {
                    r.expect(0)?;
                    out.height = r.varint()? as u32
                }
                8 => {
                    r.expect(0)?;
                    out.width = r.varint()? as u32
                }
                9 => {
                    r.expect(0)?;
                    out.pulse = r.varint()? != 0
                }
                10 => {
                    r.expect(2)?;
                    out.dimension = r.string(1024)?
                }
                11 => {
                    r.expect(0)?;
                    out.ttl_ms = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Hologram {
    pub id: String,
    pub x: i32,
    pub y: i32,
    pub z: i32,
    pub entity: String,
    pub lines: Vec<String>,
    pub item: String,
    pub asset: Vec<u8>,
    pub scale: u32,
    pub background: u32,
    pub clickable: bool,
    pub through_walls: bool,
    pub dimension: String,
    pub ttl_ms: u32,
    pub range: u32,
}

impl Hologram {
    pub const TYPE: u32 = 0x81;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.sint(2, self.x);
        w.sint(3, self.y);
        w.sint(4, self.z);
        w.string(5, &self.entity);
        for item in &self.lines {
            w.string(6, item);
        }
        w.string(7, &self.item);
        w.bytes(8, &self.asset);
        w.uint(9, u64::from(self.scale));
        w.fixed32(10, self.background);
        w.uint(11, u64::from(self.clickable));
        w.uint(12, u64::from(self.through_walls));
        w.string(13, &self.dimension);
        w.uint(14, u64::from(self.ttl_ms));
        w.uint(15, u64::from(self.range));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.x = r.sint()?
                }
                3 => {
                    r.expect(0)?;
                    out.y = r.sint()?
                }
                4 => {
                    r.expect(0)?;
                    out.z = r.sint()?
                }
                5 => {
                    r.expect(2)?;
                    out.entity = r.string(1024)?
                }
                6 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.lines.len() < 512 {
                        out.lines.push(item);
                    }
                }
                7 => {
                    r.expect(2)?;
                    out.item = r.string(1024)?
                }
                8 => {
                    r.expect(2)?;
                    out.asset = r.bytes(4194304)?.to_vec()
                }
                9 => {
                    r.expect(0)?;
                    out.scale = r.varint()? as u32
                }
                10 => {
                    r.expect(5)?;
                    out.background = r.fixed32()?
                }
                11 => {
                    r.expect(0)?;
                    out.clickable = r.varint()? != 0
                }
                12 => {
                    r.expect(0)?;
                    out.through_walls = r.varint()? != 0
                }
                13 => {
                    r.expect(2)?;
                    out.dimension = r.string(1024)?
                }
                14 => {
                    r.expect(0)?;
                    out.ttl_ms = r.varint()? as u32
                }
                15 => {
                    r.expect(0)?;
                    out.range = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Shape {
    pub id: String,
    pub kind: u32,
    pub points: Vec<i32>,
    pub radius: u32,
    pub height: u32,
    pub color: u32,
    pub fill: u32,
    pub width: u32,
    pub through_walls: bool,
    pub label: String,
    pub dimension: String,
    pub ttl_ms: u32,
}

impl Shape {
    pub const TYPE: u32 = 0x82;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.uint(2, u64::from(self.kind));
        for item in &self.points {
            w.sint(3, *item);
        }
        w.uint(4, u64::from(self.radius));
        w.uint(5, u64::from(self.height));
        w.fixed32(6, self.color);
        w.fixed32(7, self.fill);
        w.uint(8, u64::from(self.width));
        w.uint(9, u64::from(self.through_walls));
        w.string(10, &self.label);
        w.string(11, &self.dimension);
        w.uint(12, u64::from(self.ttl_ms));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.kind = r.varint()? as u32
                }
                3 => {
                    r.expect(0)?;
                    let item = r.sint()?;
                    if out.points.len() < 3072 {
                        out.points.push(item);
                    }
                }
                4 => {
                    r.expect(0)?;
                    out.radius = r.varint()? as u32
                }
                5 => {
                    r.expect(0)?;
                    out.height = r.varint()? as u32
                }
                6 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                7 => {
                    r.expect(5)?;
                    out.fill = r.fixed32()?
                }
                8 => {
                    r.expect(0)?;
                    out.width = r.varint()? as u32
                }
                9 => {
                    r.expect(0)?;
                    out.through_walls = r.varint()? != 0
                }
                10 => {
                    r.expect(2)?;
                    out.label = r.string(1024)?
                }
                11 => {
                    r.expect(2)?;
                    out.dimension = r.string(1024)?
                }
                12 => {
                    r.expect(0)?;
                    out.ttl_ms = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct WorldRemove {
    pub kind: u32,
    pub ids: Vec<String>,
}

impl WorldRemove {
    pub const TYPE: u32 = 0x83;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.kind));
        for item in &self.ids {
            w.string(2, item);
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.kind = r.varint()? as u32
                }
                2 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.ids.len() < 512 {
                        out.ids.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct EntityStyle {
    pub entity: String,
    pub glow: u32,
    pub name: String,
    pub scale: u32,
    pub rainbow: bool,
    pub hide: Vec<String>,
    pub ttl_ms: u32,
    pub hide_name: bool,
}

impl EntityStyle {
    pub const TYPE: u32 = 0x84;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.entity);
        w.fixed32(2, self.glow);
        w.string(3, &self.name);
        w.uint(4, u64::from(self.scale));
        w.uint(5, u64::from(self.rainbow));
        for item in &self.hide {
            w.string(6, item);
        }
        w.uint(7, u64::from(self.ttl_ms));
        w.uint(8, u64::from(self.hide_name));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.entity = r.string(1024)?
                }
                2 => {
                    r.expect(5)?;
                    out.glow = r.fixed32()?
                }
                3 => {
                    r.expect(2)?;
                    out.name = r.string(1024)?
                }
                4 => {
                    r.expect(0)?;
                    out.scale = r.varint()? as u32
                }
                5 => {
                    r.expect(0)?;
                    out.rainbow = r.varint()? != 0
                }
                6 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.hide.len() < 512 {
                        out.hide.push(item);
                    }
                }
                7 => {
                    r.expect(0)?;
                    out.ttl_ms = r.varint()? as u32
                }
                8 => {
                    r.expect(0)?;
                    out.hide_name = r.varint()? != 0
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct EntityStyleClear {
    pub entities: Vec<String>,
}

impl EntityStyleClear {
    pub const TYPE: u32 = 0x85;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.entities {
            w.string(1, item);
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.entities.len() < 512 {
                        out.entities.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct BorderStyle {
    pub color: u32,
    pub hide_wall: bool,
    pub reset: bool,
}

impl BorderStyle {
    pub const TYPE: u32 = 0x86;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.fixed32(1, self.color);
        w.uint(2, u64::from(self.hide_wall));
        w.uint(3, u64::from(self.reset));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                2 => {
                    r.expect(0)?;
                    out.hide_wall = r.varint()? != 0
                }
                3 => {
                    r.expect(0)?;
                    out.reset = r.varint()? != 0
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct FireColor {
    pub entity: String,
    pub color: u32,
    pub reset: bool,
}

impl FireColor {
    pub const TYPE: u32 = 0x87;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.entity);
        w.fixed32(2, self.color);
        w.uint(3, u64::from(self.reset));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.entity = r.string(1024)?
                }
                2 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                3 => {
                    r.expect(0)?;
                    out.reset = r.varint()? != 0
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Nametag {
    pub player: String,
    pub above: Vec<String>,
    pub below: Vec<String>,
    pub prefix: String,
    pub suffix: String,
    pub badge: Vec<u8>,
    pub hide: bool,
    pub badge_item: String,
}

impl Nametag {
    pub const TYPE: u32 = 0x90;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.player);
        for item in &self.above {
            w.string(2, item);
        }
        for item in &self.below {
            w.string(3, item);
        }
        w.string(4, &self.prefix);
        w.string(5, &self.suffix);
        w.bytes(6, &self.badge);
        w.uint(7, u64::from(self.hide));
        w.string(8, &self.badge_item);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.player = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.above.len() < 512 {
                        out.above.push(item);
                    }
                }
                3 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.below.len() < 512 {
                        out.below.push(item);
                    }
                }
                4 => {
                    r.expect(2)?;
                    out.prefix = r.string(1024)?
                }
                5 => {
                    r.expect(2)?;
                    out.suffix = r.string(1024)?
                }
                6 => {
                    r.expect(2)?;
                    out.badge = r.bytes(4194304)?.to_vec()
                }
                7 => {
                    r.expect(0)?;
                    out.hide = r.varint()? != 0
                }
                8 => {
                    r.expect(2)?;
                    out.badge_item = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct NametagClear {
    pub players: Vec<String>,
}

impl NametagClear {
    pub const TYPE: u32 = 0x91;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.players {
            w.string(1, item);
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.players.len() < 512 {
                        out.players.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct TabEntry {
    pub player: String,
    pub display: String,
    pub group: String,
    pub icon: Vec<u8>,
    pub ping: String,
    pub order: i32,
    pub color: u32,
}

impl TabEntry {
    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.player);
        w.string(2, &self.display);
        w.string(3, &self.group);
        w.bytes(4, &self.icon);
        w.string(5, &self.ping);
        w.sint(6, self.order);
        w.fixed32(7, self.color);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.player = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.display = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.group = r.string(1024)?
                }
                4 => {
                    r.expect(2)?;
                    out.icon = r.bytes(4194304)?.to_vec()
                }
                5 => {
                    r.expect(2)?;
                    out.ping = r.string(1024)?
                }
                6 => {
                    r.expect(0)?;
                    out.order = r.sint()?
                }
                7 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct TabList {
    pub header: Vec<String>,
    pub footer: Vec<String>,
    pub entries: Vec<TabEntry>,
    pub hide_ping: bool,
    pub replace: bool,
    pub remove: Vec<String>,
    pub reset: bool,
}

impl TabList {
    pub const TYPE: u32 = 0x92;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.header {
            w.string(1, item);
        }
        for item in &self.footer {
            w.string(2, item);
        }
        for item in &self.entries {
            w.bytes(3, &item.encode_body());
        }
        w.uint(4, u64::from(self.hide_ping));
        w.uint(5, u64::from(self.replace));
        for item in &self.remove {
            w.string(6, item);
        }
        w.uint(7, u64::from(self.reset));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.header.len() < 512 {
                        out.header.push(item);
                    }
                }
                2 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.footer.len() < 512 {
                        out.footer.push(item);
                    }
                }
                3 => {
                    r.expect(2)?;
                    let item = TabEntry::decode_body(r.bytes(4194304)?)?;
                    if out.entries.len() < 512 {
                        out.entries.push(item);
                    }
                }
                4 => {
                    r.expect(0)?;
                    out.hide_ping = r.varint()? != 0
                }
                5 => {
                    r.expect(0)?;
                    out.replace = r.varint()? != 0
                }
                6 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.remove.len() < 512 {
                        out.remove.push(item);
                    }
                }
                7 => {
                    r.expect(0)?;
                    out.reset = r.varint()? != 0
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Team {
    pub id: String,
    pub name: String,
    pub color: u32,
    pub members: Vec<String>,
    pub markers: bool,
    pub through_walls: bool,
    pub health: bool,
    pub compass: bool,
}

impl Team {
    pub const TYPE: u32 = 0x93;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.string(2, &self.name);
        w.fixed32(3, self.color);
        for item in &self.members {
            w.string(4, item);
        }
        w.uint(5, u64::from(self.markers));
        w.uint(6, u64::from(self.through_walls));
        w.uint(7, u64::from(self.health));
        w.uint(8, u64::from(self.compass));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.name = r.string(1024)?
                }
                3 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                4 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.members.len() < 512 {
                        out.members.push(item);
                    }
                }
                5 => {
                    r.expect(0)?;
                    out.markers = r.varint()? != 0
                }
                6 => {
                    r.expect(0)?;
                    out.through_walls = r.varint()? != 0
                }
                7 => {
                    r.expect(0)?;
                    out.health = r.varint()? != 0
                }
                8 => {
                    r.expect(0)?;
                    out.compass = r.varint()? != 0
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct TeamRemove {
    pub ids: Vec<String>,
}

impl TeamRemove {
    pub const TYPE: u32 = 0x94;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.ids {
            w.string(1, item);
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.ids.len() < 512 {
                        out.ids.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct NickHint {
    pub nick: String,
    pub player: String,
}

impl NickHint {
    pub const TYPE: u32 = 0x95;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.nick);
        w.string(2, &self.player);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.nick = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.player = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Keybind {
    pub id: String,
    pub label: String,
    pub key: i32,
    pub button: String,
    pub hold: bool,
    pub description: String,
}

impl Keybind {
    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.string(2, &self.label);
        w.sint(3, self.key);
        w.string(4, &self.button);
        w.uint(5, u64::from(self.hold));
        w.string(6, &self.description);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.label = r.string(1024)?
                }
                3 => {
                    r.expect(0)?;
                    out.key = r.sint()?
                }
                4 => {
                    r.expect(2)?;
                    out.button = r.string(1024)?
                }
                5 => {
                    r.expect(0)?;
                    out.hold = r.varint()? != 0
                }
                6 => {
                    r.expect(2)?;
                    out.description = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Keybinds {
    pub binds: Vec<Keybind>,
    pub category: String,
}

impl Keybinds {
    pub const TYPE: u32 = 0xA0;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.binds {
            w.bytes(1, &item.encode_body());
        }
        w.string(2, &self.category);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = Keybind::decode_body(r.bytes(4194304)?)?;
                    if out.binds.len() < 512 {
                        out.binds.push(item);
                    }
                }
                2 => {
                    r.expect(2)?;
                    out.category = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct KeyPress {
    pub id: String,
    pub pressed: bool,
    pub held_ms: u32,
}

impl KeyPress {
    pub const TYPE: u32 = 0xA1;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.uint(2, u64::from(self.pressed));
        w.uint(3, u64::from(self.held_ms));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.pressed = r.varint()? != 0
                }
                3 => {
                    r.expect(0)?;
                    out.held_ms = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct CameraShake {
    pub intensity: u32,
    pub duration_ms: u32,
    pub frequency: u32,
}

impl CameraShake {
    pub const TYPE: u32 = 0xB0;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.intensity));
        w.uint(2, u64::from(self.duration_ms));
        w.uint(3, u64::from(self.frequency));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.intensity = r.varint()? as u32
                }
                2 => {
                    r.expect(0)?;
                    out.duration_ms = r.varint()? as u32
                }
                3 => {
                    r.expect(0)?;
                    out.frequency = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct FovPunch {
    pub amount: i32,
    pub duration_ms: u32,
}

impl FovPunch {
    pub const TYPE: u32 = 0xB1;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.sint(1, self.amount);
        w.uint(2, u64::from(self.duration_ms));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.amount = r.sint()?
                }
                2 => {
                    r.expect(0)?;
                    out.duration_ms = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct LookAt {
    pub x: i32,
    pub y: i32,
    pub z: i32,
    pub entity: String,
    pub duration_ms: u32,
    pub hold_ms: u32,
}

impl LookAt {
    pub const TYPE: u32 = 0xB2;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.sint(1, self.x);
        w.sint(2, self.y);
        w.sint(3, self.z);
        w.string(4, &self.entity);
        w.uint(5, u64::from(self.duration_ms));
        w.uint(6, u64::from(self.hold_ms));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.x = r.sint()?
                }
                2 => {
                    r.expect(0)?;
                    out.y = r.sint()?
                }
                3 => {
                    r.expect(0)?;
                    out.z = r.sint()?
                }
                4 => {
                    r.expect(2)?;
                    out.entity = r.string(1024)?
                }
                5 => {
                    r.expect(0)?;
                    out.duration_ms = r.varint()? as u32
                }
                6 => {
                    r.expect(0)?;
                    out.hold_ms = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Letterbox {
    pub on: bool,
    pub size: u32,
    pub fade_ms: u32,
}

impl Letterbox {
    pub const TYPE: u32 = 0xB3;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.on));
        w.uint(2, u64::from(self.size));
        w.uint(3, u64::from(self.fade_ms));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.on = r.varint()? != 0
                }
                2 => {
                    r.expect(0)?;
                    out.size = r.varint()? as u32
                }
                3 => {
                    r.expect(0)?;
                    out.fade_ms = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Keyframe {
    pub x: i32,
    pub y: i32,
    pub z: i32,
    pub yaw: i32,
    pub pitch: i32,
    pub roll: i32,
    pub fov: u32,
    pub at_ms: u32,
    pub ease: u32,
}

impl Keyframe {
    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.sint(1, self.x);
        w.sint(2, self.y);
        w.sint(3, self.z);
        w.sint(4, self.yaw);
        w.sint(5, self.pitch);
        w.sint(6, self.roll);
        w.uint(7, u64::from(self.fov));
        w.uint(8, u64::from(self.at_ms));
        w.uint(9, u64::from(self.ease));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.x = r.sint()?
                }
                2 => {
                    r.expect(0)?;
                    out.y = r.sint()?
                }
                3 => {
                    r.expect(0)?;
                    out.z = r.sint()?
                }
                4 => {
                    r.expect(0)?;
                    out.yaw = r.sint()?
                }
                5 => {
                    r.expect(0)?;
                    out.pitch = r.sint()?
                }
                6 => {
                    r.expect(0)?;
                    out.roll = r.sint()?
                }
                7 => {
                    r.expect(0)?;
                    out.fov = r.varint()? as u32
                }
                8 => {
                    r.expect(0)?;
                    out.at_ms = r.varint()? as u32
                }
                9 => {
                    r.expect(0)?;
                    out.ease = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Cutscene {
    pub id: String,
    pub frames: Vec<Keyframe>,
    pub skippable: bool,
    pub hide_hud: bool,
    pub letterbox: bool,
    pub r#loop: bool,
    pub dimension: String,
}

impl Cutscene {
    pub const TYPE: u32 = 0xB4;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        for item in &self.frames {
            w.bytes(2, &item.encode_body());
        }
        w.uint(3, u64::from(self.skippable));
        w.uint(4, u64::from(self.hide_hud));
        w.uint(5, u64::from(self.letterbox));
        w.uint(6, u64::from(self.r#loop));
        w.string(7, &self.dimension);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    let item = Keyframe::decode_body(r.bytes(4194304)?)?;
                    if out.frames.len() < 512 {
                        out.frames.push(item);
                    }
                }
                3 => {
                    r.expect(0)?;
                    out.skippable = r.varint()? != 0
                }
                4 => {
                    r.expect(0)?;
                    out.hide_hud = r.varint()? != 0
                }
                5 => {
                    r.expect(0)?;
                    out.letterbox = r.varint()? != 0
                }
                6 => {
                    r.expect(0)?;
                    out.r#loop = r.varint()? != 0
                }
                7 => {
                    r.expect(2)?;
                    out.dimension = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct CutsceneStop {
    pub id: String,
}

impl CutsceneStop {
    pub const TYPE: u32 = 0xB5;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ScreenFade {
    pub color: u32,
    pub in_ms: u32,
    pub hold_ms: u32,
    pub out_ms: u32,
}

impl ScreenFade {
    pub const TYPE: u32 = 0xB6;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.fixed32(1, self.color);
        w.uint(2, u64::from(self.in_ms));
        w.uint(3, u64::from(self.hold_ms));
        w.uint(4, u64::from(self.out_ms));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                2 => {
                    r.expect(0)?;
                    out.in_ms = r.varint()? as u32
                }
                3 => {
                    r.expect(0)?;
                    out.hold_ms = r.varint()? as u32
                }
                4 => {
                    r.expect(0)?;
                    out.out_ms = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct CutsceneEnded {
    pub id: String,
    pub skipped: bool,
}

impl CutsceneEnded {
    pub const TYPE: u32 = 0xB7;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.uint(2, u64::from(self.skipped));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.skipped = r.varint()? != 0
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct PlaySound {
    pub id: String,
    pub asset: Vec<u8>,
    pub sound: String,
    pub x: i32,
    pub y: i32,
    pub z: i32,
    pub entity: String,
    pub positional: bool,
    pub volume: u32,
    pub pitch: u32,
    pub r#loop: bool,
    pub fade_in_ms: u32,
    pub music: bool,
}

impl PlaySound {
    pub const TYPE: u32 = 0xC0;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.bytes(2, &self.asset);
        w.string(3, &self.sound);
        w.sint(4, self.x);
        w.sint(5, self.y);
        w.sint(6, self.z);
        w.string(7, &self.entity);
        w.uint(8, u64::from(self.positional));
        w.uint(9, u64::from(self.volume));
        w.uint(10, u64::from(self.pitch));
        w.uint(11, u64::from(self.r#loop));
        w.uint(12, u64::from(self.fade_in_ms));
        w.uint(13, u64::from(self.music));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.asset = r.bytes(4194304)?.to_vec()
                }
                3 => {
                    r.expect(2)?;
                    out.sound = r.string(1024)?
                }
                4 => {
                    r.expect(0)?;
                    out.x = r.sint()?
                }
                5 => {
                    r.expect(0)?;
                    out.y = r.sint()?
                }
                6 => {
                    r.expect(0)?;
                    out.z = r.sint()?
                }
                7 => {
                    r.expect(2)?;
                    out.entity = r.string(1024)?
                }
                8 => {
                    r.expect(0)?;
                    out.positional = r.varint()? != 0
                }
                9 => {
                    r.expect(0)?;
                    out.volume = r.varint()? as u32
                }
                10 => {
                    r.expect(0)?;
                    out.pitch = r.varint()? as u32
                }
                11 => {
                    r.expect(0)?;
                    out.r#loop = r.varint()? != 0
                }
                12 => {
                    r.expect(0)?;
                    out.fade_in_ms = r.varint()? as u32
                }
                13 => {
                    r.expect(0)?;
                    out.music = r.varint()? != 0
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct StopSound {
    pub ids: Vec<String>,
    pub fade_out_ms: u32,
}

impl StopSound {
    pub const TYPE: u32 = 0xC1;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.ids {
            w.string(1, item);
        }
        w.uint(2, u64::from(self.fade_out_ms));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.ids.len() < 512 {
                        out.ids.push(item);
                    }
                }
                2 => {
                    r.expect(0)?;
                    out.fade_out_ms = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Presence {
    pub game: String,
    pub map: String,
    pub state: String,
    pub started: u64,
    pub party_size: u32,
    pub party_max: u32,
    pub joinable: bool,
    pub clear: bool,
}

impl Presence {
    pub const TYPE: u32 = 0xD0;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.game);
        w.string(2, &self.map);
        w.string(3, &self.state);
        w.uint(4, self.started);
        w.uint(5, u64::from(self.party_size));
        w.uint(6, u64::from(self.party_max));
        w.uint(7, u64::from(self.joinable));
        w.uint(8, u64::from(self.clear));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.game = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.map = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.state = r.string(1024)?
                }
                4 => {
                    r.expect(0)?;
                    out.started = r.varint()?
                }
                5 => {
                    r.expect(0)?;
                    out.party_size = r.varint()? as u32
                }
                6 => {
                    r.expect(0)?;
                    out.party_max = r.varint()? as u32
                }
                7 => {
                    r.expect(0)?;
                    out.joinable = r.varint()? != 0
                }
                8 => {
                    r.expect(0)?;
                    out.clear = r.varint()? != 0
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct InviteSheet {
    pub reason: String,
    pub party: String,
    pub address: String,
}

impl InviteSheet {
    pub const TYPE: u32 = 0xD1;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.reason);
        w.string(2, &self.party);
        w.string(3, &self.address);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.reason = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.party = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.address = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Transfer {
    pub host: String,
    pub port: u32,
    pub token: String,
    pub title: String,
    pub reason: String,
}

impl Transfer {
    pub const TYPE: u32 = 0xD2;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.host);
        w.uint(2, u64::from(self.port));
        w.string(3, &self.token);
        w.string(4, &self.title);
        w.string(5, &self.reason);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.host = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.port = r.varint()? as u32
                }
                3 => {
                    r.expect(2)?;
                    out.token = r.string(1024)?
                }
                4 => {
                    r.expect(2)?;
                    out.title = r.string(1024)?
                }
                5 => {
                    r.expect(2)?;
                    out.reason = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ServerLink {
    pub label: String,
    pub url: String,
    pub kind: String,
}

impl ServerLink {
    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.label);
        w.string(2, &self.url);
        w.string(3, &self.kind);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.label = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.url = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.kind = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ServerLinks {
    pub links: Vec<ServerLink>,
}

impl ServerLinks {
    pub const TYPE: u32 = 0xD3;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.links {
            w.bytes(1, &item.encode_body());
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = ServerLink::decode_body(r.bytes(4194304)?)?;
                    if out.links.len() < 512 {
                        out.links.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct UiNode {
    pub id: String,
    pub parent: String,
    pub kind: u32,
    pub text: String,
    pub value: String,
    pub min: i32,
    pub max: i32,
    pub step: u32,
    pub asset: Vec<u8>,
    pub item: String,
    pub player: String,
    pub color: u32,
    pub columns: u32,
    pub disabled: bool,
    pub placeholder: String,
    pub options: Vec<String>,
    pub millis: u64,
    pub style: u32,
    pub width: u32,
    pub height: u32,
    pub tooltip: String,
}

impl UiNode {
    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.string(2, &self.parent);
        w.uint(3, u64::from(self.kind));
        w.string(4, &self.text);
        w.string(5, &self.value);
        w.sint(6, self.min);
        w.sint(7, self.max);
        w.uint(8, u64::from(self.step));
        w.bytes(9, &self.asset);
        w.string(10, &self.item);
        w.string(11, &self.player);
        w.fixed32(12, self.color);
        w.uint(13, u64::from(self.columns));
        w.uint(14, u64::from(self.disabled));
        w.string(15, &self.placeholder);
        for item in &self.options {
            w.string(16, item);
        }
        w.uint(17, self.millis);
        w.uint(18, u64::from(self.style));
        w.uint(19, u64::from(self.width));
        w.uint(20, u64::from(self.height));
        w.string(21, &self.tooltip);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.parent = r.string(1024)?
                }
                3 => {
                    r.expect(0)?;
                    out.kind = r.varint()? as u32
                }
                4 => {
                    r.expect(2)?;
                    out.text = r.string(1024)?
                }
                5 => {
                    r.expect(2)?;
                    out.value = r.string(1024)?
                }
                6 => {
                    r.expect(0)?;
                    out.min = r.sint()?
                }
                7 => {
                    r.expect(0)?;
                    out.max = r.sint()?
                }
                8 => {
                    r.expect(0)?;
                    out.step = r.varint()? as u32
                }
                9 => {
                    r.expect(2)?;
                    out.asset = r.bytes(4194304)?.to_vec()
                }
                10 => {
                    r.expect(2)?;
                    out.item = r.string(1024)?
                }
                11 => {
                    r.expect(2)?;
                    out.player = r.string(1024)?
                }
                12 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                13 => {
                    r.expect(0)?;
                    out.columns = r.varint()? as u32
                }
                14 => {
                    r.expect(0)?;
                    out.disabled = r.varint()? != 0
                }
                15 => {
                    r.expect(2)?;
                    out.placeholder = r.string(1024)?
                }
                16 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.options.len() < 512 {
                        out.options.push(item);
                    }
                }
                17 => {
                    r.expect(0)?;
                    out.millis = r.varint()?
                }
                18 => {
                    r.expect(0)?;
                    out.style = r.varint()? as u32
                }
                19 => {
                    r.expect(0)?;
                    out.width = r.varint()? as u32
                }
                20 => {
                    r.expect(0)?;
                    out.height = r.varint()? as u32
                }
                21 => {
                    r.expect(2)?;
                    out.tooltip = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct UiOpen {
    pub id: String,
    pub surface: u32,
    pub title: String,
    pub nodes: Vec<UiNode>,
    pub width: u32,
    pub closeable: bool,
    pub anchor: u32,
}

impl UiOpen {
    pub const TYPE: u32 = 0xE0;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.uint(2, u64::from(self.surface));
        w.string(3, &self.title);
        for item in &self.nodes {
            w.bytes(4, &item.encode_body());
        }
        w.uint(5, u64::from(self.width));
        w.uint(6, u64::from(self.closeable));
        w.uint(7, u64::from(self.anchor));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.surface = r.varint()? as u32
                }
                3 => {
                    r.expect(2)?;
                    out.title = r.string(1024)?
                }
                4 => {
                    r.expect(2)?;
                    let item = UiNode::decode_body(r.bytes(4194304)?)?;
                    if out.nodes.len() < 512 {
                        out.nodes.push(item);
                    }
                }
                5 => {
                    r.expect(0)?;
                    out.width = r.varint()? as u32
                }
                6 => {
                    r.expect(0)?;
                    out.closeable = r.varint()? != 0
                }
                7 => {
                    r.expect(0)?;
                    out.anchor = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct UiPatch {
    pub id: String,
    pub nodes: Vec<UiNode>,
    pub remove: Vec<String>,
    pub title: String,
}

impl UiPatch {
    pub const TYPE: u32 = 0xE1;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        for item in &self.nodes {
            w.bytes(2, &item.encode_body());
        }
        for item in &self.remove {
            w.string(3, item);
        }
        w.string(4, &self.title);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    let item = UiNode::decode_body(r.bytes(4194304)?)?;
                    if out.nodes.len() < 512 {
                        out.nodes.push(item);
                    }
                }
                3 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.remove.len() < 512 {
                        out.remove.push(item);
                    }
                }
                4 => {
                    r.expect(2)?;
                    out.title = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct UiClose {
    pub id: String,
}

impl UiClose {
    pub const TYPE: u32 = 0xE2;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct UiEvent {
    pub id: String,
    pub node: String,
    pub action: u32,
    pub value: String,
}

impl UiEvent {
    pub const TYPE: u32 = 0xE3;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.string(2, &self.node);
        w.uint(3, u64::from(self.action));
        w.string(4, &self.value);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.node = r.string(1024)?
                }
                3 => {
                    r.expect(0)?;
                    out.action = r.varint()? as u32
                }
                4 => {
                    r.expect(2)?;
                    out.value = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct InfoRequest {
    pub request_id: u32,
    pub fields: Vec<String>,
    pub reason: String,
}

impl InfoRequest {
    pub const TYPE: u32 = 0xF0;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.request_id));
        for item in &self.fields {
            w.string(2, item);
        }
        w.string(3, &self.reason);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.request_id = r.varint()? as u32
                }
                2 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.fields.len() < 512 {
                        out.fields.push(item);
                    }
                }
                3 => {
                    r.expect(2)?;
                    out.reason = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct InfoReply {
    pub request_id: u32,
    pub granted: Vec<String>,
    pub input: String,
    pub fps: u32,
    pub frame_p50: u32,
    pub frame_p99: u32,
    pub render_distance: u32,
    pub mods: Vec<ModEntry>,
    pub teho_modules: Vec<String>,
    pub locale: String,
    pub gui_scale: u32,
    pub width: u32,
    pub height: u32,
    pub os: String,
    pub memory_mb: u32,
}

impl InfoReply {
    pub const TYPE: u32 = 0xF1;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.request_id));
        for item in &self.granted {
            w.string(2, item);
        }
        w.string(3, &self.input);
        w.uint(4, u64::from(self.fps));
        w.uint(5, u64::from(self.frame_p50));
        w.uint(6, u64::from(self.frame_p99));
        w.uint(7, u64::from(self.render_distance));
        for item in &self.mods {
            w.bytes(8, &item.encode_body());
        }
        for item in &self.teho_modules {
            w.string(9, item);
        }
        w.string(10, &self.locale);
        w.uint(11, u64::from(self.gui_scale));
        w.uint(12, u64::from(self.width));
        w.uint(13, u64::from(self.height));
        w.string(14, &self.os);
        w.uint(15, u64::from(self.memory_mb));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.request_id = r.varint()? as u32
                }
                2 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.granted.len() < 512 {
                        out.granted.push(item);
                    }
                }
                3 => {
                    r.expect(2)?;
                    out.input = r.string(1024)?
                }
                4 => {
                    r.expect(0)?;
                    out.fps = r.varint()? as u32
                }
                5 => {
                    r.expect(0)?;
                    out.frame_p50 = r.varint()? as u32
                }
                6 => {
                    r.expect(0)?;
                    out.frame_p99 = r.varint()? as u32
                }
                7 => {
                    r.expect(0)?;
                    out.render_distance = r.varint()? as u32
                }
                8 => {
                    r.expect(2)?;
                    let item = ModEntry::decode_body(r.bytes(4194304)?)?;
                    if out.mods.len() < 512 {
                        out.mods.push(item);
                    }
                }
                9 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.teho_modules.len() < 512 {
                        out.teho_modules.push(item);
                    }
                }
                10 => {
                    r.expect(2)?;
                    out.locale = r.string(1024)?
                }
                11 => {
                    r.expect(0)?;
                    out.gui_scale = r.varint()? as u32
                }
                12 => {
                    r.expect(0)?;
                    out.width = r.varint()? as u32
                }
                13 => {
                    r.expect(0)?;
                    out.height = r.varint()? as u32
                }
                14 => {
                    r.expect(2)?;
                    out.os = r.string(1024)?
                }
                15 => {
                    r.expect(0)?;
                    out.memory_mb = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct AssertRequest {
    pub request_id: u32,
    pub nonce: String,
}

impl AssertRequest {
    pub const TYPE: u32 = 0x110;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.request_id));
        w.string(2, &self.nonce);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.request_id = r.varint()? as u32
                }
                2 => {
                    r.expect(2)?;
                    out.nonce = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct AssertResult {
    pub request_id: u32,
    pub token: String,
    pub error: String,
}

impl AssertResult {
    pub const TYPE: u32 = 0x111;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.request_id));
        w.string(2, &self.token);
        w.string(3, &self.error);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.request_id = r.varint()? as u32
                }
                2 => {
                    r.expect(2)?;
                    out.token = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.error = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct AttestRequest {
    pub request_id: u32,
    pub nonce: String,
}

impl AttestRequest {
    pub const TYPE: u32 = 0x118;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.request_id));
        w.string(2, &self.nonce);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.request_id = r.varint()? as u32
                }
                2 => {
                    r.expect(2)?;
                    out.nonce = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct AttestResult {
    pub request_id: u32,
    pub token: String,
    pub error: String,
}

impl AttestResult {
    pub const TYPE: u32 = 0x119;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.request_id));
        w.string(2, &self.token);
        w.string(3, &self.error);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.request_id = r.varint()? as u32
                }
                2 => {
                    r.expect(2)?;
                    out.token = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.error = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct PackHint {
    pub url: String,
    pub sha1: String,
    pub size: u32,
    pub required: bool,
    pub prompt: String,
    pub id: String,
}

impl PackHint {
    pub const TYPE: u32 = 0x120;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.url);
        w.string(2, &self.sha1);
        w.uint(3, u64::from(self.size));
        w.uint(4, u64::from(self.required));
        w.string(5, &self.prompt);
        w.string(6, &self.id);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.url = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.sha1 = r.string(1024)?
                }
                3 => {
                    r.expect(0)?;
                    out.size = r.varint()? as u32
                }
                4 => {
                    r.expect(0)?;
                    out.required = r.varint()? != 0
                }
                5 => {
                    r.expect(2)?;
                    out.prompt = r.string(1024)?
                }
                6 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct PackStatus {
    pub sha1: String,
    pub state: u32,
    pub error: String,
}

impl PackStatus {
    pub const TYPE: u32 = 0x121;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.sha1);
        w.uint(2, u64::from(self.state));
        w.string(3, &self.error);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.sha1 = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.state = r.varint()? as u32
                }
                3 => {
                    r.expect(2)?;
                    out.error = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ClipMark {
    pub id: String,
    pub title: String,
    pub before_ms: u32,
    pub after_ms: u32,
}

impl ClipMark {
    pub const TYPE: u32 = 0x130;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.string(2, &self.title);
        w.uint(3, u64::from(self.before_ms));
        w.uint(4, u64::from(self.after_ms));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.title = r.string(1024)?
                }
                3 => {
                    r.expect(0)?;
                    out.before_ms = r.varint()? as u32
                }
                4 => {
                    r.expect(0)?;
                    out.after_ms = r.varint()? as u32
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ClipResult {
    pub id: String,
    pub saved: bool,
    pub url: String,
    pub error: String,
}

impl ClipResult {
    pub const TYPE: u32 = 0x131;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.uint(2, u64::from(self.saved));
        w.string(3, &self.url);
        w.string(4, &self.error);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.saved = r.varint()? != 0
                }
                3 => {
                    r.expect(2)?;
                    out.url = r.string(1024)?
                }
                4 => {
                    r.expect(2)?;
                    out.error = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ObserverPlayer {
    pub player: String,
    pub name: String,
    pub health: u32,
    pub alive: bool,
    pub kills: u32,
    pub item: String,
}

impl ObserverPlayer {
    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.player);
        w.string(2, &self.name);
        w.uint(3, u64::from(self.health));
        w.uint(4, u64::from(self.alive));
        w.uint(5, u64::from(self.kills));
        w.string(6, &self.item);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.player = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.name = r.string(1024)?
                }
                3 => {
                    r.expect(0)?;
                    out.health = r.varint()? as u32
                }
                4 => {
                    r.expect(0)?;
                    out.alive = r.varint()? != 0
                }
                5 => {
                    r.expect(0)?;
                    out.kills = r.varint()? as u32
                }
                6 => {
                    r.expect(2)?;
                    out.item = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ObserverTeam {
    pub name: String,
    pub color: u32,
    pub score: i32,
    pub players: Vec<ObserverPlayer>,
}

impl ObserverTeam {
    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.name);
        w.fixed32(2, self.color);
        w.sint(3, self.score);
        for item in &self.players {
            w.bytes(4, &item.encode_body());
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.name = r.string(1024)?
                }
                2 => {
                    r.expect(5)?;
                    out.color = r.fixed32()?
                }
                3 => {
                    r.expect(0)?;
                    out.score = r.sint()?
                }
                4 => {
                    r.expect(2)?;
                    let item = ObserverPlayer::decode_body(r.bytes(4194304)?)?;
                    if out.players.len() < 512 {
                        out.players.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ObserverHud {
    pub on: bool,
    pub title: String,
    pub subtitle: String,
    pub teams: Vec<ObserverTeam>,
}

impl ObserverHud {
    pub const TYPE: u32 = 0x140;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.uint(1, u64::from(self.on));
        w.string(2, &self.title);
        w.string(3, &self.subtitle);
        for item in &self.teams {
            w.bytes(4, &item.encode_body());
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(0)?;
                    out.on = r.varint()? != 0
                }
                2 => {
                    r.expect(2)?;
                    out.title = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.subtitle = r.string(1024)?
                }
                4 => {
                    r.expect(2)?;
                    let item = ObserverTeam::decode_body(r.bytes(4194304)?)?;
                    if out.teams.len() < 512 {
                        out.teams.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct CameraFollow {
    pub player: String,
    pub mode: u32,
    pub distance: u32,
    pub smooth: bool,
}

impl CameraFollow {
    pub const TYPE: u32 = 0x141;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.player);
        w.uint(2, u64::from(self.mode));
        w.uint(3, u64::from(self.distance));
        w.uint(4, u64::from(self.smooth));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.player = r.string(1024)?
                }
                2 => {
                    r.expect(0)?;
                    out.mode = r.varint()? as u32
                }
                3 => {
                    r.expect(0)?;
                    out.distance = r.varint()? as u32
                }
                4 => {
                    r.expect(0)?;
                    out.smooth = r.varint()? != 0
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Statue {
    pub id: String,
    pub player: String,
    pub name: String,
    pub x: i32,
    pub y: i32,
    pub z: i32,
    pub yaw: i32,
    pub pose: u32,
    pub scale: u32,
    pub dimension: String,
    pub label: String,
    pub emote: String,
}

impl Statue {
    pub const TYPE: u32 = 0x170;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.id);
        w.string(2, &self.player);
        w.string(3, &self.name);
        w.sint(4, self.x);
        w.sint(5, self.y);
        w.sint(6, self.z);
        w.sint(7, self.yaw);
        w.uint(8, u64::from(self.pose));
        w.uint(9, u64::from(self.scale));
        w.string(10, &self.dimension);
        w.string(11, &self.label);
        w.string(12, &self.emote);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.id = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    out.player = r.string(1024)?
                }
                3 => {
                    r.expect(2)?;
                    out.name = r.string(1024)?
                }
                4 => {
                    r.expect(0)?;
                    out.x = r.sint()?
                }
                5 => {
                    r.expect(0)?;
                    out.y = r.sint()?
                }
                6 => {
                    r.expect(0)?;
                    out.z = r.sint()?
                }
                7 => {
                    r.expect(0)?;
                    out.yaw = r.sint()?
                }
                8 => {
                    r.expect(0)?;
                    out.pose = r.varint()? as u32
                }
                9 => {
                    r.expect(0)?;
                    out.scale = r.varint()? as u32
                }
                10 => {
                    r.expect(2)?;
                    out.dimension = r.string(1024)?
                }
                11 => {
                    r.expect(2)?;
                    out.label = r.string(1024)?
                }
                12 => {
                    r.expect(2)?;
                    out.emote = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct StatueRemove {
    pub ids: Vec<String>,
}

impl StatueRemove {
    pub const TYPE: u32 = 0x171;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        for item in &self.ids {
            w.string(1, item);
        }
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.ids.len() < 512 {
                        out.ids.push(item);
                    }
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct Branding {
    pub accent: u32,
    pub logo: Vec<u8>,
    pub banner: Vec<u8>,
    pub background: Vec<u8>,
    pub tagline: String,
    pub reset: bool,
}

impl Branding {
    pub const TYPE: u32 = 0x180;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.fixed32(1, self.accent);
        w.bytes(2, &self.logo);
        w.bytes(3, &self.banner);
        w.bytes(4, &self.background);
        w.string(5, &self.tagline);
        w.uint(6, u64::from(self.reset));
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(5)?;
                    out.accent = r.fixed32()?
                }
                2 => {
                    r.expect(2)?;
                    out.logo = r.bytes(4194304)?.to_vec()
                }
                3 => {
                    r.expect(2)?;
                    out.banner = r.bytes(4194304)?.to_vec()
                }
                4 => {
                    r.expect(2)?;
                    out.background = r.bytes(4194304)?.to_vec()
                }
                5 => {
                    r.expect(2)?;
                    out.tagline = r.string(1024)?
                }
                6 => {
                    r.expect(0)?;
                    out.reset = r.varint()? != 0
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct ShowcaseOpen {
    pub title: String,
    pub items: Vec<String>,
    pub emote: String,
    pub focus: String,
    pub subtitle: String,
}

impl ShowcaseOpen {
    pub const TYPE: u32 = 0x190;

    pub fn encode_body(&self) -> Vec<u8> {
        let mut w = Writer::new();
        w.string(1, &self.title);
        for item in &self.items {
            w.string(2, item);
        }
        w.string(3, &self.emote);
        w.string(4, &self.focus);
        w.string(5, &self.subtitle);
        w.into_bytes()
    }

    pub fn decode_body(bytes: &[u8]) -> Result<Self, Error> {
        let mut r = Reader::new(bytes);
        let mut out = Self::default();
        while r.has_next() {
            let number = r.next_field()?;
            match number {
                1 => {
                    r.expect(2)?;
                    out.title = r.string(1024)?
                }
                2 => {
                    r.expect(2)?;
                    let item = r.string(1024)?;
                    if out.items.len() < 512 {
                        out.items.push(item);
                    }
                }
                3 => {
                    r.expect(2)?;
                    out.emote = r.string(1024)?
                }
                4 => {
                    r.expect(2)?;
                    out.focus = r.string(1024)?
                }
                5 => {
                    r.expect(2)?;
                    out.subtitle = r.string(1024)?
                }
                _ => r.skip()?,
            }
        }
        Ok(out)
    }
}

#[derive(Debug, Clone, PartialEq)]
pub enum Message {
    Hello(Hello),
    Welcome(Welcome),
    HelloRequest(HelloRequest),
    Reset(Reset),
    Notify(Notify),
    Dismiss(Dismiss),
    WaypointSet(WaypointSet),
    WaypointRemove(WaypointRemove),
    WaypointClear(WaypointClear),
    Reply(Reply),
    Event(Event),
    Chunk(Chunk),
    AssetOffer(AssetOffer),
    AssetWant(AssetWant),
    AssetData(AssetData),
    ConsentResult(ConsentResult),
    ModuleRules(ModuleRules),
    SettingCaps(SettingCaps),
    StaffGrant(StaffGrant),
    StaffRevoke(StaffRevoke),
    StaffState(StaffState),
    EspStyle(EspStyle),
    Cooldown(Cooldown),
    CooldownClear(CooldownClear),
    Timer(Timer),
    TimerRemove(TimerRemove),
    StaffSupport(StaffSupport),
    Highlight(Highlight),
    HighlightRemove(HighlightRemove),
    Trail(Trail),
    InspectRequest(InspectRequest),
    InspectCard(InspectCard),
    ActionWheel(ActionWheel),
    Alert(Alert),
    TargetStats(TargetStats),
    Spectate(Spectate),
    StaffAction(StaffAction),
    LagMap(LagMap),
    FreezeScreen(FreezeScreen),
    ScreenshotRequest(ScreenshotRequest),
    ScreenshotResult(ScreenshotResult),
    ModListRequest(ModListRequest),
    ModList(ModList),
    PrivateStatus(PrivateStatus),
    NpcLook(NpcLook),
    NpcEmote(NpcEmote),
    NpcClear(NpcClear),
    CombatRules(CombatRules),
    ThirdPartyPolicy(ThirdPartyPolicy),
    PolicyReport(PolicyReport),
    SidebarSet(SidebarSet),
    SidebarClear(SidebarClear),
    Bar(Bar),
    BarRemove(BarRemove),
    KillFeed(KillFeed),
    ScreenTint(ScreenTint),
    Title(Title),
    TntFuse(TntFuse),
    Beam(Beam),
    Hologram(Hologram),
    Shape(Shape),
    WorldRemove(WorldRemove),
    EntityStyle(EntityStyle),
    EntityStyleClear(EntityStyleClear),
    BorderStyle(BorderStyle),
    FireColor(FireColor),
    Nametag(Nametag),
    NametagClear(NametagClear),
    TabList(TabList),
    Team(Team),
    TeamRemove(TeamRemove),
    NickHint(NickHint),
    Keybinds(Keybinds),
    KeyPress(KeyPress),
    CameraShake(CameraShake),
    FovPunch(FovPunch),
    LookAt(LookAt),
    Letterbox(Letterbox),
    Cutscene(Cutscene),
    CutsceneStop(CutsceneStop),
    ScreenFade(ScreenFade),
    CutsceneEnded(CutsceneEnded),
    PlaySound(PlaySound),
    StopSound(StopSound),
    Presence(Presence),
    InviteSheet(InviteSheet),
    Transfer(Transfer),
    ServerLinks(ServerLinks),
    UiOpen(UiOpen),
    UiPatch(UiPatch),
    UiClose(UiClose),
    UiEvent(UiEvent),
    InfoRequest(InfoRequest),
    InfoReply(InfoReply),
    AssertRequest(AssertRequest),
    AssertResult(AssertResult),
    AttestRequest(AttestRequest),
    AttestResult(AttestResult),
    PackHint(PackHint),
    PackStatus(PackStatus),
    ClipMark(ClipMark),
    ClipResult(ClipResult),
    ObserverHud(ObserverHud),
    CameraFollow(CameraFollow),
    Statue(Statue),
    StatueRemove(StatueRemove),
    Branding(Branding),
    ShowcaseOpen(ShowcaseOpen),
}

impl Message {
    pub fn type_id(&self) -> u32 {
        match self {
            Message::Hello(_) => Hello::TYPE,
            Message::Welcome(_) => Welcome::TYPE,
            Message::HelloRequest(_) => HelloRequest::TYPE,
            Message::Reset(_) => Reset::TYPE,
            Message::Notify(_) => Notify::TYPE,
            Message::Dismiss(_) => Dismiss::TYPE,
            Message::WaypointSet(_) => WaypointSet::TYPE,
            Message::WaypointRemove(_) => WaypointRemove::TYPE,
            Message::WaypointClear(_) => WaypointClear::TYPE,
            Message::Reply(_) => Reply::TYPE,
            Message::Event(_) => Event::TYPE,
            Message::Chunk(_) => Chunk::TYPE,
            Message::AssetOffer(_) => AssetOffer::TYPE,
            Message::AssetWant(_) => AssetWant::TYPE,
            Message::AssetData(_) => AssetData::TYPE,
            Message::ConsentResult(_) => ConsentResult::TYPE,
            Message::ModuleRules(_) => ModuleRules::TYPE,
            Message::SettingCaps(_) => SettingCaps::TYPE,
            Message::StaffGrant(_) => StaffGrant::TYPE,
            Message::StaffRevoke(_) => StaffRevoke::TYPE,
            Message::StaffState(_) => StaffState::TYPE,
            Message::EspStyle(_) => EspStyle::TYPE,
            Message::Cooldown(_) => Cooldown::TYPE,
            Message::CooldownClear(_) => CooldownClear::TYPE,
            Message::Timer(_) => Timer::TYPE,
            Message::TimerRemove(_) => TimerRemove::TYPE,
            Message::StaffSupport(_) => StaffSupport::TYPE,
            Message::Highlight(_) => Highlight::TYPE,
            Message::HighlightRemove(_) => HighlightRemove::TYPE,
            Message::Trail(_) => Trail::TYPE,
            Message::InspectRequest(_) => InspectRequest::TYPE,
            Message::InspectCard(_) => InspectCard::TYPE,
            Message::ActionWheel(_) => ActionWheel::TYPE,
            Message::Alert(_) => Alert::TYPE,
            Message::TargetStats(_) => TargetStats::TYPE,
            Message::Spectate(_) => Spectate::TYPE,
            Message::StaffAction(_) => StaffAction::TYPE,
            Message::LagMap(_) => LagMap::TYPE,
            Message::FreezeScreen(_) => FreezeScreen::TYPE,
            Message::ScreenshotRequest(_) => ScreenshotRequest::TYPE,
            Message::ScreenshotResult(_) => ScreenshotResult::TYPE,
            Message::ModListRequest(_) => ModListRequest::TYPE,
            Message::ModList(_) => ModList::TYPE,
            Message::PrivateStatus(_) => PrivateStatus::TYPE,
            Message::NpcLook(_) => NpcLook::TYPE,
            Message::NpcEmote(_) => NpcEmote::TYPE,
            Message::NpcClear(_) => NpcClear::TYPE,
            Message::CombatRules(_) => CombatRules::TYPE,
            Message::ThirdPartyPolicy(_) => ThirdPartyPolicy::TYPE,
            Message::PolicyReport(_) => PolicyReport::TYPE,
            Message::SidebarSet(_) => SidebarSet::TYPE,
            Message::SidebarClear(_) => SidebarClear::TYPE,
            Message::Bar(_) => Bar::TYPE,
            Message::BarRemove(_) => BarRemove::TYPE,
            Message::KillFeed(_) => KillFeed::TYPE,
            Message::ScreenTint(_) => ScreenTint::TYPE,
            Message::Title(_) => Title::TYPE,
            Message::TntFuse(_) => TntFuse::TYPE,
            Message::Beam(_) => Beam::TYPE,
            Message::Hologram(_) => Hologram::TYPE,
            Message::Shape(_) => Shape::TYPE,
            Message::WorldRemove(_) => WorldRemove::TYPE,
            Message::EntityStyle(_) => EntityStyle::TYPE,
            Message::EntityStyleClear(_) => EntityStyleClear::TYPE,
            Message::BorderStyle(_) => BorderStyle::TYPE,
            Message::FireColor(_) => FireColor::TYPE,
            Message::Nametag(_) => Nametag::TYPE,
            Message::NametagClear(_) => NametagClear::TYPE,
            Message::TabList(_) => TabList::TYPE,
            Message::Team(_) => Team::TYPE,
            Message::TeamRemove(_) => TeamRemove::TYPE,
            Message::NickHint(_) => NickHint::TYPE,
            Message::Keybinds(_) => Keybinds::TYPE,
            Message::KeyPress(_) => KeyPress::TYPE,
            Message::CameraShake(_) => CameraShake::TYPE,
            Message::FovPunch(_) => FovPunch::TYPE,
            Message::LookAt(_) => LookAt::TYPE,
            Message::Letterbox(_) => Letterbox::TYPE,
            Message::Cutscene(_) => Cutscene::TYPE,
            Message::CutsceneStop(_) => CutsceneStop::TYPE,
            Message::ScreenFade(_) => ScreenFade::TYPE,
            Message::CutsceneEnded(_) => CutsceneEnded::TYPE,
            Message::PlaySound(_) => PlaySound::TYPE,
            Message::StopSound(_) => StopSound::TYPE,
            Message::Presence(_) => Presence::TYPE,
            Message::InviteSheet(_) => InviteSheet::TYPE,
            Message::Transfer(_) => Transfer::TYPE,
            Message::ServerLinks(_) => ServerLinks::TYPE,
            Message::UiOpen(_) => UiOpen::TYPE,
            Message::UiPatch(_) => UiPatch::TYPE,
            Message::UiClose(_) => UiClose::TYPE,
            Message::UiEvent(_) => UiEvent::TYPE,
            Message::InfoRequest(_) => InfoRequest::TYPE,
            Message::InfoReply(_) => InfoReply::TYPE,
            Message::AssertRequest(_) => AssertRequest::TYPE,
            Message::AssertResult(_) => AssertResult::TYPE,
            Message::AttestRequest(_) => AttestRequest::TYPE,
            Message::AttestResult(_) => AttestResult::TYPE,
            Message::PackHint(_) => PackHint::TYPE,
            Message::PackStatus(_) => PackStatus::TYPE,
            Message::ClipMark(_) => ClipMark::TYPE,
            Message::ClipResult(_) => ClipResult::TYPE,
            Message::ObserverHud(_) => ObserverHud::TYPE,
            Message::CameraFollow(_) => CameraFollow::TYPE,
            Message::Statue(_) => Statue::TYPE,
            Message::StatueRemove(_) => StatueRemove::TYPE,
            Message::Branding(_) => Branding::TYPE,
            Message::ShowcaseOpen(_) => ShowcaseOpen::TYPE,
        }
    }

    pub fn name(&self) -> &'static str {
        match self {
            Message::Hello(_) => "Hello",
            Message::Welcome(_) => "Welcome",
            Message::HelloRequest(_) => "HelloRequest",
            Message::Reset(_) => "Reset",
            Message::Notify(_) => "Notify",
            Message::Dismiss(_) => "Dismiss",
            Message::WaypointSet(_) => "WaypointSet",
            Message::WaypointRemove(_) => "WaypointRemove",
            Message::WaypointClear(_) => "WaypointClear",
            Message::Reply(_) => "Reply",
            Message::Event(_) => "Event",
            Message::Chunk(_) => "Chunk",
            Message::AssetOffer(_) => "AssetOffer",
            Message::AssetWant(_) => "AssetWant",
            Message::AssetData(_) => "AssetData",
            Message::ConsentResult(_) => "ConsentResult",
            Message::ModuleRules(_) => "ModuleRules",
            Message::SettingCaps(_) => "SettingCaps",
            Message::StaffGrant(_) => "StaffGrant",
            Message::StaffRevoke(_) => "StaffRevoke",
            Message::StaffState(_) => "StaffState",
            Message::EspStyle(_) => "EspStyle",
            Message::Cooldown(_) => "Cooldown",
            Message::CooldownClear(_) => "CooldownClear",
            Message::Timer(_) => "Timer",
            Message::TimerRemove(_) => "TimerRemove",
            Message::StaffSupport(_) => "StaffSupport",
            Message::Highlight(_) => "Highlight",
            Message::HighlightRemove(_) => "HighlightRemove",
            Message::Trail(_) => "Trail",
            Message::InspectRequest(_) => "InspectRequest",
            Message::InspectCard(_) => "InspectCard",
            Message::ActionWheel(_) => "ActionWheel",
            Message::Alert(_) => "Alert",
            Message::TargetStats(_) => "TargetStats",
            Message::Spectate(_) => "Spectate",
            Message::StaffAction(_) => "StaffAction",
            Message::LagMap(_) => "LagMap",
            Message::FreezeScreen(_) => "FreezeScreen",
            Message::ScreenshotRequest(_) => "ScreenshotRequest",
            Message::ScreenshotResult(_) => "ScreenshotResult",
            Message::ModListRequest(_) => "ModListRequest",
            Message::ModList(_) => "ModList",
            Message::PrivateStatus(_) => "PrivateStatus",
            Message::NpcLook(_) => "NpcLook",
            Message::NpcEmote(_) => "NpcEmote",
            Message::NpcClear(_) => "NpcClear",
            Message::CombatRules(_) => "CombatRules",
            Message::ThirdPartyPolicy(_) => "ThirdPartyPolicy",
            Message::PolicyReport(_) => "PolicyReport",
            Message::SidebarSet(_) => "SidebarSet",
            Message::SidebarClear(_) => "SidebarClear",
            Message::Bar(_) => "Bar",
            Message::BarRemove(_) => "BarRemove",
            Message::KillFeed(_) => "KillFeed",
            Message::ScreenTint(_) => "ScreenTint",
            Message::Title(_) => "Title",
            Message::TntFuse(_) => "TntFuse",
            Message::Beam(_) => "Beam",
            Message::Hologram(_) => "Hologram",
            Message::Shape(_) => "Shape",
            Message::WorldRemove(_) => "WorldRemove",
            Message::EntityStyle(_) => "EntityStyle",
            Message::EntityStyleClear(_) => "EntityStyleClear",
            Message::BorderStyle(_) => "BorderStyle",
            Message::FireColor(_) => "FireColor",
            Message::Nametag(_) => "Nametag",
            Message::NametagClear(_) => "NametagClear",
            Message::TabList(_) => "TabList",
            Message::Team(_) => "Team",
            Message::TeamRemove(_) => "TeamRemove",
            Message::NickHint(_) => "NickHint",
            Message::Keybinds(_) => "Keybinds",
            Message::KeyPress(_) => "KeyPress",
            Message::CameraShake(_) => "CameraShake",
            Message::FovPunch(_) => "FovPunch",
            Message::LookAt(_) => "LookAt",
            Message::Letterbox(_) => "Letterbox",
            Message::Cutscene(_) => "Cutscene",
            Message::CutsceneStop(_) => "CutsceneStop",
            Message::ScreenFade(_) => "ScreenFade",
            Message::CutsceneEnded(_) => "CutsceneEnded",
            Message::PlaySound(_) => "PlaySound",
            Message::StopSound(_) => "StopSound",
            Message::Presence(_) => "Presence",
            Message::InviteSheet(_) => "InviteSheet",
            Message::Transfer(_) => "Transfer",
            Message::ServerLinks(_) => "ServerLinks",
            Message::UiOpen(_) => "UiOpen",
            Message::UiPatch(_) => "UiPatch",
            Message::UiClose(_) => "UiClose",
            Message::UiEvent(_) => "UiEvent",
            Message::InfoRequest(_) => "InfoRequest",
            Message::InfoReply(_) => "InfoReply",
            Message::AssertRequest(_) => "AssertRequest",
            Message::AssertResult(_) => "AssertResult",
            Message::AttestRequest(_) => "AttestRequest",
            Message::AttestResult(_) => "AttestResult",
            Message::PackHint(_) => "PackHint",
            Message::PackStatus(_) => "PackStatus",
            Message::ClipMark(_) => "ClipMark",
            Message::ClipResult(_) => "ClipResult",
            Message::ObserverHud(_) => "ObserverHud",
            Message::CameraFollow(_) => "CameraFollow",
            Message::Statue(_) => "Statue",
            Message::StatueRemove(_) => "StatueRemove",
            Message::Branding(_) => "Branding",
            Message::ShowcaseOpen(_) => "ShowcaseOpen",
        }
    }

    pub fn encode_body(&self) -> Vec<u8> {
        match self {
            Message::Hello(m) => m.encode_body(),
            Message::Welcome(m) => m.encode_body(),
            Message::HelloRequest(m) => m.encode_body(),
            Message::Reset(m) => m.encode_body(),
            Message::Notify(m) => m.encode_body(),
            Message::Dismiss(m) => m.encode_body(),
            Message::WaypointSet(m) => m.encode_body(),
            Message::WaypointRemove(m) => m.encode_body(),
            Message::WaypointClear(m) => m.encode_body(),
            Message::Reply(m) => m.encode_body(),
            Message::Event(m) => m.encode_body(),
            Message::Chunk(m) => m.encode_body(),
            Message::AssetOffer(m) => m.encode_body(),
            Message::AssetWant(m) => m.encode_body(),
            Message::AssetData(m) => m.encode_body(),
            Message::ConsentResult(m) => m.encode_body(),
            Message::ModuleRules(m) => m.encode_body(),
            Message::SettingCaps(m) => m.encode_body(),
            Message::StaffGrant(m) => m.encode_body(),
            Message::StaffRevoke(m) => m.encode_body(),
            Message::StaffState(m) => m.encode_body(),
            Message::EspStyle(m) => m.encode_body(),
            Message::Cooldown(m) => m.encode_body(),
            Message::CooldownClear(m) => m.encode_body(),
            Message::Timer(m) => m.encode_body(),
            Message::TimerRemove(m) => m.encode_body(),
            Message::StaffSupport(m) => m.encode_body(),
            Message::Highlight(m) => m.encode_body(),
            Message::HighlightRemove(m) => m.encode_body(),
            Message::Trail(m) => m.encode_body(),
            Message::InspectRequest(m) => m.encode_body(),
            Message::InspectCard(m) => m.encode_body(),
            Message::ActionWheel(m) => m.encode_body(),
            Message::Alert(m) => m.encode_body(),
            Message::TargetStats(m) => m.encode_body(),
            Message::Spectate(m) => m.encode_body(),
            Message::StaffAction(m) => m.encode_body(),
            Message::LagMap(m) => m.encode_body(),
            Message::FreezeScreen(m) => m.encode_body(),
            Message::ScreenshotRequest(m) => m.encode_body(),
            Message::ScreenshotResult(m) => m.encode_body(),
            Message::ModListRequest(m) => m.encode_body(),
            Message::ModList(m) => m.encode_body(),
            Message::PrivateStatus(m) => m.encode_body(),
            Message::NpcLook(m) => m.encode_body(),
            Message::NpcEmote(m) => m.encode_body(),
            Message::NpcClear(m) => m.encode_body(),
            Message::CombatRules(m) => m.encode_body(),
            Message::ThirdPartyPolicy(m) => m.encode_body(),
            Message::PolicyReport(m) => m.encode_body(),
            Message::SidebarSet(m) => m.encode_body(),
            Message::SidebarClear(m) => m.encode_body(),
            Message::Bar(m) => m.encode_body(),
            Message::BarRemove(m) => m.encode_body(),
            Message::KillFeed(m) => m.encode_body(),
            Message::ScreenTint(m) => m.encode_body(),
            Message::Title(m) => m.encode_body(),
            Message::TntFuse(m) => m.encode_body(),
            Message::Beam(m) => m.encode_body(),
            Message::Hologram(m) => m.encode_body(),
            Message::Shape(m) => m.encode_body(),
            Message::WorldRemove(m) => m.encode_body(),
            Message::EntityStyle(m) => m.encode_body(),
            Message::EntityStyleClear(m) => m.encode_body(),
            Message::BorderStyle(m) => m.encode_body(),
            Message::FireColor(m) => m.encode_body(),
            Message::Nametag(m) => m.encode_body(),
            Message::NametagClear(m) => m.encode_body(),
            Message::TabList(m) => m.encode_body(),
            Message::Team(m) => m.encode_body(),
            Message::TeamRemove(m) => m.encode_body(),
            Message::NickHint(m) => m.encode_body(),
            Message::Keybinds(m) => m.encode_body(),
            Message::KeyPress(m) => m.encode_body(),
            Message::CameraShake(m) => m.encode_body(),
            Message::FovPunch(m) => m.encode_body(),
            Message::LookAt(m) => m.encode_body(),
            Message::Letterbox(m) => m.encode_body(),
            Message::Cutscene(m) => m.encode_body(),
            Message::CutsceneStop(m) => m.encode_body(),
            Message::ScreenFade(m) => m.encode_body(),
            Message::CutsceneEnded(m) => m.encode_body(),
            Message::PlaySound(m) => m.encode_body(),
            Message::StopSound(m) => m.encode_body(),
            Message::Presence(m) => m.encode_body(),
            Message::InviteSheet(m) => m.encode_body(),
            Message::Transfer(m) => m.encode_body(),
            Message::ServerLinks(m) => m.encode_body(),
            Message::UiOpen(m) => m.encode_body(),
            Message::UiPatch(m) => m.encode_body(),
            Message::UiClose(m) => m.encode_body(),
            Message::UiEvent(m) => m.encode_body(),
            Message::InfoRequest(m) => m.encode_body(),
            Message::InfoReply(m) => m.encode_body(),
            Message::AssertRequest(m) => m.encode_body(),
            Message::AssertResult(m) => m.encode_body(),
            Message::AttestRequest(m) => m.encode_body(),
            Message::AttestResult(m) => m.encode_body(),
            Message::PackHint(m) => m.encode_body(),
            Message::PackStatus(m) => m.encode_body(),
            Message::ClipMark(m) => m.encode_body(),
            Message::ClipResult(m) => m.encode_body(),
            Message::ObserverHud(m) => m.encode_body(),
            Message::CameraFollow(m) => m.encode_body(),
            Message::Statue(m) => m.encode_body(),
            Message::StatueRemove(m) => m.encode_body(),
            Message::Branding(m) => m.encode_body(),
            Message::ShowcaseOpen(m) => m.encode_body(),
        }
    }

    pub fn decode_body(type_id: u32, body: &[u8]) -> Result<Option<Message>, Error> {
        Ok(Some(match type_id {
            0x1 => Message::Hello(Hello::decode_body(body)?),
            0x2 => Message::Welcome(Welcome::decode_body(body)?),
            0x3 => Message::HelloRequest(HelloRequest::decode_body(body)?),
            0x4 => Message::Reset(Reset::decode_body(body)?),
            0x10 => Message::Notify(Notify::decode_body(body)?),
            0x11 => Message::Dismiss(Dismiss::decode_body(body)?),
            0x20 => Message::WaypointSet(WaypointSet::decode_body(body)?),
            0x21 => Message::WaypointRemove(WaypointRemove::decode_body(body)?),
            0x22 => Message::WaypointClear(WaypointClear::decode_body(body)?),
            0x5 => Message::Reply(Reply::decode_body(body)?),
            0x6 => Message::Event(Event::decode_body(body)?),
            0x7 => Message::Chunk(Chunk::decode_body(body)?),
            0x8 => Message::AssetOffer(AssetOffer::decode_body(body)?),
            0x9 => Message::AssetWant(AssetWant::decode_body(body)?),
            0xC => Message::AssetData(AssetData::decode_body(body)?),
            0xD => Message::ConsentResult(ConsentResult::decode_body(body)?),
            0x30 => Message::ModuleRules(ModuleRules::decode_body(body)?),
            0x31 => Message::SettingCaps(SettingCaps::decode_body(body)?),
            0x40 => Message::StaffGrant(StaffGrant::decode_body(body)?),
            0x41 => Message::StaffRevoke(StaffRevoke::decode_body(body)?),
            0x42 => Message::StaffState(StaffState::decode_body(body)?),
            0x43 => Message::EspStyle(EspStyle::decode_body(body)?),
            0x50 => Message::Cooldown(Cooldown::decode_body(body)?),
            0x51 => Message::CooldownClear(CooldownClear::decode_body(body)?),
            0x52 => Message::Timer(Timer::decode_body(body)?),
            0x53 => Message::TimerRemove(TimerRemove::decode_body(body)?),
            0x44 => Message::StaffSupport(StaffSupport::decode_body(body)?),
            0x45 => Message::Highlight(Highlight::decode_body(body)?),
            0x46 => Message::HighlightRemove(HighlightRemove::decode_body(body)?),
            0x47 => Message::Trail(Trail::decode_body(body)?),
            0x48 => Message::InspectRequest(InspectRequest::decode_body(body)?),
            0x49 => Message::InspectCard(InspectCard::decode_body(body)?),
            0x4A => Message::ActionWheel(ActionWheel::decode_body(body)?),
            0x4B => Message::Alert(Alert::decode_body(body)?),
            0x4C => Message::TargetStats(TargetStats::decode_body(body)?),
            0x4D => Message::Spectate(Spectate::decode_body(body)?),
            0x4E => Message::StaffAction(StaffAction::decode_body(body)?),
            0x4F => Message::LagMap(LagMap::decode_body(body)?),
            0x60 => Message::FreezeScreen(FreezeScreen::decode_body(body)?),
            0x61 => Message::ScreenshotRequest(ScreenshotRequest::decode_body(body)?),
            0x62 => Message::ScreenshotResult(ScreenshotResult::decode_body(body)?),
            0x63 => Message::ModListRequest(ModListRequest::decode_body(body)?),
            0x64 => Message::ModList(ModList::decode_body(body)?),
            0xE => Message::PrivateStatus(PrivateStatus::decode_body(body)?),
            0x70 => Message::NpcLook(NpcLook::decode_body(body)?),
            0x71 => Message::NpcEmote(NpcEmote::decode_body(body)?),
            0x72 => Message::NpcClear(NpcClear::decode_body(body)?),
            0x32 => Message::CombatRules(CombatRules::decode_body(body)?),
            0x33 => Message::ThirdPartyPolicy(ThirdPartyPolicy::decode_body(body)?),
            0x34 => Message::PolicyReport(PolicyReport::decode_body(body)?),
            0x54 => Message::SidebarSet(SidebarSet::decode_body(body)?),
            0x55 => Message::SidebarClear(SidebarClear::decode_body(body)?),
            0x56 => Message::Bar(Bar::decode_body(body)?),
            0x57 => Message::BarRemove(BarRemove::decode_body(body)?),
            0x58 => Message::KillFeed(KillFeed::decode_body(body)?),
            0x59 => Message::ScreenTint(ScreenTint::decode_body(body)?),
            0x5A => Message::Title(Title::decode_body(body)?),
            0x5B => Message::TntFuse(TntFuse::decode_body(body)?),
            0x80 => Message::Beam(Beam::decode_body(body)?),
            0x81 => Message::Hologram(Hologram::decode_body(body)?),
            0x82 => Message::Shape(Shape::decode_body(body)?),
            0x83 => Message::WorldRemove(WorldRemove::decode_body(body)?),
            0x84 => Message::EntityStyle(EntityStyle::decode_body(body)?),
            0x85 => Message::EntityStyleClear(EntityStyleClear::decode_body(body)?),
            0x86 => Message::BorderStyle(BorderStyle::decode_body(body)?),
            0x87 => Message::FireColor(FireColor::decode_body(body)?),
            0x90 => Message::Nametag(Nametag::decode_body(body)?),
            0x91 => Message::NametagClear(NametagClear::decode_body(body)?),
            0x92 => Message::TabList(TabList::decode_body(body)?),
            0x93 => Message::Team(Team::decode_body(body)?),
            0x94 => Message::TeamRemove(TeamRemove::decode_body(body)?),
            0x95 => Message::NickHint(NickHint::decode_body(body)?),
            0xA0 => Message::Keybinds(Keybinds::decode_body(body)?),
            0xA1 => Message::KeyPress(KeyPress::decode_body(body)?),
            0xB0 => Message::CameraShake(CameraShake::decode_body(body)?),
            0xB1 => Message::FovPunch(FovPunch::decode_body(body)?),
            0xB2 => Message::LookAt(LookAt::decode_body(body)?),
            0xB3 => Message::Letterbox(Letterbox::decode_body(body)?),
            0xB4 => Message::Cutscene(Cutscene::decode_body(body)?),
            0xB5 => Message::CutsceneStop(CutsceneStop::decode_body(body)?),
            0xB6 => Message::ScreenFade(ScreenFade::decode_body(body)?),
            0xB7 => Message::CutsceneEnded(CutsceneEnded::decode_body(body)?),
            0xC0 => Message::PlaySound(PlaySound::decode_body(body)?),
            0xC1 => Message::StopSound(StopSound::decode_body(body)?),
            0xD0 => Message::Presence(Presence::decode_body(body)?),
            0xD1 => Message::InviteSheet(InviteSheet::decode_body(body)?),
            0xD2 => Message::Transfer(Transfer::decode_body(body)?),
            0xD3 => Message::ServerLinks(ServerLinks::decode_body(body)?),
            0xE0 => Message::UiOpen(UiOpen::decode_body(body)?),
            0xE1 => Message::UiPatch(UiPatch::decode_body(body)?),
            0xE2 => Message::UiClose(UiClose::decode_body(body)?),
            0xE3 => Message::UiEvent(UiEvent::decode_body(body)?),
            0xF0 => Message::InfoRequest(InfoRequest::decode_body(body)?),
            0xF1 => Message::InfoReply(InfoReply::decode_body(body)?),
            0x110 => Message::AssertRequest(AssertRequest::decode_body(body)?),
            0x111 => Message::AssertResult(AssertResult::decode_body(body)?),
            0x118 => Message::AttestRequest(AttestRequest::decode_body(body)?),
            0x119 => Message::AttestResult(AttestResult::decode_body(body)?),
            0x120 => Message::PackHint(PackHint::decode_body(body)?),
            0x121 => Message::PackStatus(PackStatus::decode_body(body)?),
            0x130 => Message::ClipMark(ClipMark::decode_body(body)?),
            0x131 => Message::ClipResult(ClipResult::decode_body(body)?),
            0x140 => Message::ObserverHud(ObserverHud::decode_body(body)?),
            0x141 => Message::CameraFollow(CameraFollow::decode_body(body)?),
            0x170 => Message::Statue(Statue::decode_body(body)?),
            0x171 => Message::StatueRemove(StatueRemove::decode_body(body)?),
            0x180 => Message::Branding(Branding::decode_body(body)?),
            0x190 => Message::ShowcaseOpen(ShowcaseOpen::decode_body(body)?),
            _ => return Ok(None),
        }))
    }
}

impl From<Hello> for Message {
    fn from(message: Hello) -> Self {
        Message::Hello(message)
    }
}

impl From<Welcome> for Message {
    fn from(message: Welcome) -> Self {
        Message::Welcome(message)
    }
}

impl From<HelloRequest> for Message {
    fn from(message: HelloRequest) -> Self {
        Message::HelloRequest(message)
    }
}

impl From<Reset> for Message {
    fn from(message: Reset) -> Self {
        Message::Reset(message)
    }
}

impl From<Notify> for Message {
    fn from(message: Notify) -> Self {
        Message::Notify(message)
    }
}

impl From<Dismiss> for Message {
    fn from(message: Dismiss) -> Self {
        Message::Dismiss(message)
    }
}

impl From<WaypointSet> for Message {
    fn from(message: WaypointSet) -> Self {
        Message::WaypointSet(message)
    }
}

impl From<WaypointRemove> for Message {
    fn from(message: WaypointRemove) -> Self {
        Message::WaypointRemove(message)
    }
}

impl From<WaypointClear> for Message {
    fn from(message: WaypointClear) -> Self {
        Message::WaypointClear(message)
    }
}

impl From<Reply> for Message {
    fn from(message: Reply) -> Self {
        Message::Reply(message)
    }
}

impl From<Event> for Message {
    fn from(message: Event) -> Self {
        Message::Event(message)
    }
}

impl From<Chunk> for Message {
    fn from(message: Chunk) -> Self {
        Message::Chunk(message)
    }
}

impl From<AssetOffer> for Message {
    fn from(message: AssetOffer) -> Self {
        Message::AssetOffer(message)
    }
}

impl From<AssetWant> for Message {
    fn from(message: AssetWant) -> Self {
        Message::AssetWant(message)
    }
}

impl From<AssetData> for Message {
    fn from(message: AssetData) -> Self {
        Message::AssetData(message)
    }
}

impl From<ConsentResult> for Message {
    fn from(message: ConsentResult) -> Self {
        Message::ConsentResult(message)
    }
}

impl From<ModuleRules> for Message {
    fn from(message: ModuleRules) -> Self {
        Message::ModuleRules(message)
    }
}

impl From<SettingCaps> for Message {
    fn from(message: SettingCaps) -> Self {
        Message::SettingCaps(message)
    }
}

impl From<StaffGrant> for Message {
    fn from(message: StaffGrant) -> Self {
        Message::StaffGrant(message)
    }
}

impl From<StaffRevoke> for Message {
    fn from(message: StaffRevoke) -> Self {
        Message::StaffRevoke(message)
    }
}

impl From<StaffState> for Message {
    fn from(message: StaffState) -> Self {
        Message::StaffState(message)
    }
}

impl From<EspStyle> for Message {
    fn from(message: EspStyle) -> Self {
        Message::EspStyle(message)
    }
}

impl From<Cooldown> for Message {
    fn from(message: Cooldown) -> Self {
        Message::Cooldown(message)
    }
}

impl From<CooldownClear> for Message {
    fn from(message: CooldownClear) -> Self {
        Message::CooldownClear(message)
    }
}

impl From<Timer> for Message {
    fn from(message: Timer) -> Self {
        Message::Timer(message)
    }
}

impl From<TimerRemove> for Message {
    fn from(message: TimerRemove) -> Self {
        Message::TimerRemove(message)
    }
}

impl From<StaffSupport> for Message {
    fn from(message: StaffSupport) -> Self {
        Message::StaffSupport(message)
    }
}

impl From<Highlight> for Message {
    fn from(message: Highlight) -> Self {
        Message::Highlight(message)
    }
}

impl From<HighlightRemove> for Message {
    fn from(message: HighlightRemove) -> Self {
        Message::HighlightRemove(message)
    }
}

impl From<Trail> for Message {
    fn from(message: Trail) -> Self {
        Message::Trail(message)
    }
}

impl From<InspectRequest> for Message {
    fn from(message: InspectRequest) -> Self {
        Message::InspectRequest(message)
    }
}

impl From<InspectCard> for Message {
    fn from(message: InspectCard) -> Self {
        Message::InspectCard(message)
    }
}

impl From<ActionWheel> for Message {
    fn from(message: ActionWheel) -> Self {
        Message::ActionWheel(message)
    }
}

impl From<Alert> for Message {
    fn from(message: Alert) -> Self {
        Message::Alert(message)
    }
}

impl From<TargetStats> for Message {
    fn from(message: TargetStats) -> Self {
        Message::TargetStats(message)
    }
}

impl From<Spectate> for Message {
    fn from(message: Spectate) -> Self {
        Message::Spectate(message)
    }
}

impl From<StaffAction> for Message {
    fn from(message: StaffAction) -> Self {
        Message::StaffAction(message)
    }
}

impl From<LagMap> for Message {
    fn from(message: LagMap) -> Self {
        Message::LagMap(message)
    }
}

impl From<FreezeScreen> for Message {
    fn from(message: FreezeScreen) -> Self {
        Message::FreezeScreen(message)
    }
}

impl From<ScreenshotRequest> for Message {
    fn from(message: ScreenshotRequest) -> Self {
        Message::ScreenshotRequest(message)
    }
}

impl From<ScreenshotResult> for Message {
    fn from(message: ScreenshotResult) -> Self {
        Message::ScreenshotResult(message)
    }
}

impl From<ModListRequest> for Message {
    fn from(message: ModListRequest) -> Self {
        Message::ModListRequest(message)
    }
}

impl From<ModList> for Message {
    fn from(message: ModList) -> Self {
        Message::ModList(message)
    }
}

impl From<PrivateStatus> for Message {
    fn from(message: PrivateStatus) -> Self {
        Message::PrivateStatus(message)
    }
}

impl From<NpcLook> for Message {
    fn from(message: NpcLook) -> Self {
        Message::NpcLook(message)
    }
}

impl From<NpcEmote> for Message {
    fn from(message: NpcEmote) -> Self {
        Message::NpcEmote(message)
    }
}

impl From<NpcClear> for Message {
    fn from(message: NpcClear) -> Self {
        Message::NpcClear(message)
    }
}

impl From<CombatRules> for Message {
    fn from(message: CombatRules) -> Self {
        Message::CombatRules(message)
    }
}

impl From<ThirdPartyPolicy> for Message {
    fn from(message: ThirdPartyPolicy) -> Self {
        Message::ThirdPartyPolicy(message)
    }
}

impl From<PolicyReport> for Message {
    fn from(message: PolicyReport) -> Self {
        Message::PolicyReport(message)
    }
}

impl From<SidebarSet> for Message {
    fn from(message: SidebarSet) -> Self {
        Message::SidebarSet(message)
    }
}

impl From<SidebarClear> for Message {
    fn from(message: SidebarClear) -> Self {
        Message::SidebarClear(message)
    }
}

impl From<Bar> for Message {
    fn from(message: Bar) -> Self {
        Message::Bar(message)
    }
}

impl From<BarRemove> for Message {
    fn from(message: BarRemove) -> Self {
        Message::BarRemove(message)
    }
}

impl From<KillFeed> for Message {
    fn from(message: KillFeed) -> Self {
        Message::KillFeed(message)
    }
}

impl From<ScreenTint> for Message {
    fn from(message: ScreenTint) -> Self {
        Message::ScreenTint(message)
    }
}

impl From<Title> for Message {
    fn from(message: Title) -> Self {
        Message::Title(message)
    }
}

impl From<TntFuse> for Message {
    fn from(message: TntFuse) -> Self {
        Message::TntFuse(message)
    }
}

impl From<Beam> for Message {
    fn from(message: Beam) -> Self {
        Message::Beam(message)
    }
}

impl From<Hologram> for Message {
    fn from(message: Hologram) -> Self {
        Message::Hologram(message)
    }
}

impl From<Shape> for Message {
    fn from(message: Shape) -> Self {
        Message::Shape(message)
    }
}

impl From<WorldRemove> for Message {
    fn from(message: WorldRemove) -> Self {
        Message::WorldRemove(message)
    }
}

impl From<EntityStyle> for Message {
    fn from(message: EntityStyle) -> Self {
        Message::EntityStyle(message)
    }
}

impl From<EntityStyleClear> for Message {
    fn from(message: EntityStyleClear) -> Self {
        Message::EntityStyleClear(message)
    }
}

impl From<BorderStyle> for Message {
    fn from(message: BorderStyle) -> Self {
        Message::BorderStyle(message)
    }
}

impl From<FireColor> for Message {
    fn from(message: FireColor) -> Self {
        Message::FireColor(message)
    }
}

impl From<Nametag> for Message {
    fn from(message: Nametag) -> Self {
        Message::Nametag(message)
    }
}

impl From<NametagClear> for Message {
    fn from(message: NametagClear) -> Self {
        Message::NametagClear(message)
    }
}

impl From<TabList> for Message {
    fn from(message: TabList) -> Self {
        Message::TabList(message)
    }
}

impl From<Team> for Message {
    fn from(message: Team) -> Self {
        Message::Team(message)
    }
}

impl From<TeamRemove> for Message {
    fn from(message: TeamRemove) -> Self {
        Message::TeamRemove(message)
    }
}

impl From<NickHint> for Message {
    fn from(message: NickHint) -> Self {
        Message::NickHint(message)
    }
}

impl From<Keybinds> for Message {
    fn from(message: Keybinds) -> Self {
        Message::Keybinds(message)
    }
}

impl From<KeyPress> for Message {
    fn from(message: KeyPress) -> Self {
        Message::KeyPress(message)
    }
}

impl From<CameraShake> for Message {
    fn from(message: CameraShake) -> Self {
        Message::CameraShake(message)
    }
}

impl From<FovPunch> for Message {
    fn from(message: FovPunch) -> Self {
        Message::FovPunch(message)
    }
}

impl From<LookAt> for Message {
    fn from(message: LookAt) -> Self {
        Message::LookAt(message)
    }
}

impl From<Letterbox> for Message {
    fn from(message: Letterbox) -> Self {
        Message::Letterbox(message)
    }
}

impl From<Cutscene> for Message {
    fn from(message: Cutscene) -> Self {
        Message::Cutscene(message)
    }
}

impl From<CutsceneStop> for Message {
    fn from(message: CutsceneStop) -> Self {
        Message::CutsceneStop(message)
    }
}

impl From<ScreenFade> for Message {
    fn from(message: ScreenFade) -> Self {
        Message::ScreenFade(message)
    }
}

impl From<CutsceneEnded> for Message {
    fn from(message: CutsceneEnded) -> Self {
        Message::CutsceneEnded(message)
    }
}

impl From<PlaySound> for Message {
    fn from(message: PlaySound) -> Self {
        Message::PlaySound(message)
    }
}

impl From<StopSound> for Message {
    fn from(message: StopSound) -> Self {
        Message::StopSound(message)
    }
}

impl From<Presence> for Message {
    fn from(message: Presence) -> Self {
        Message::Presence(message)
    }
}

impl From<InviteSheet> for Message {
    fn from(message: InviteSheet) -> Self {
        Message::InviteSheet(message)
    }
}

impl From<Transfer> for Message {
    fn from(message: Transfer) -> Self {
        Message::Transfer(message)
    }
}

impl From<ServerLinks> for Message {
    fn from(message: ServerLinks) -> Self {
        Message::ServerLinks(message)
    }
}

impl From<UiOpen> for Message {
    fn from(message: UiOpen) -> Self {
        Message::UiOpen(message)
    }
}

impl From<UiPatch> for Message {
    fn from(message: UiPatch) -> Self {
        Message::UiPatch(message)
    }
}

impl From<UiClose> for Message {
    fn from(message: UiClose) -> Self {
        Message::UiClose(message)
    }
}

impl From<UiEvent> for Message {
    fn from(message: UiEvent) -> Self {
        Message::UiEvent(message)
    }
}

impl From<InfoRequest> for Message {
    fn from(message: InfoRequest) -> Self {
        Message::InfoRequest(message)
    }
}

impl From<InfoReply> for Message {
    fn from(message: InfoReply) -> Self {
        Message::InfoReply(message)
    }
}

impl From<AssertRequest> for Message {
    fn from(message: AssertRequest) -> Self {
        Message::AssertRequest(message)
    }
}

impl From<AssertResult> for Message {
    fn from(message: AssertResult) -> Self {
        Message::AssertResult(message)
    }
}

impl From<AttestRequest> for Message {
    fn from(message: AttestRequest) -> Self {
        Message::AttestRequest(message)
    }
}

impl From<AttestResult> for Message {
    fn from(message: AttestResult) -> Self {
        Message::AttestResult(message)
    }
}

impl From<PackHint> for Message {
    fn from(message: PackHint) -> Self {
        Message::PackHint(message)
    }
}

impl From<PackStatus> for Message {
    fn from(message: PackStatus) -> Self {
        Message::PackStatus(message)
    }
}

impl From<ClipMark> for Message {
    fn from(message: ClipMark) -> Self {
        Message::ClipMark(message)
    }
}

impl From<ClipResult> for Message {
    fn from(message: ClipResult) -> Self {
        Message::ClipResult(message)
    }
}

impl From<ObserverHud> for Message {
    fn from(message: ObserverHud) -> Self {
        Message::ObserverHud(message)
    }
}

impl From<CameraFollow> for Message {
    fn from(message: CameraFollow) -> Self {
        Message::CameraFollow(message)
    }
}

impl From<Statue> for Message {
    fn from(message: Statue) -> Self {
        Message::Statue(message)
    }
}

impl From<StatueRemove> for Message {
    fn from(message: StatueRemove) -> Self {
        Message::StatueRemove(message)
    }
}

impl From<Branding> for Message {
    fn from(message: Branding) -> Self {
        Message::Branding(message)
    }
}

impl From<ShowcaseOpen> for Message {
    fn from(message: ShowcaseOpen) -> Self {
        Message::ShowcaseOpen(message)
    }
}
