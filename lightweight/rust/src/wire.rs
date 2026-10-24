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

use std::fmt;

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct Error(pub String);

impl fmt::Display for Error {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.write_str(&self.0)
    }
}

impl std::error::Error for Error {}

pub(crate) fn error<T>(message: impl Into<String>) -> Result<T, Error> {
    Err(Error(message.into()))
}

#[derive(Default)]
pub struct Writer {
    out: Vec<u8>,
}

impl Writer {
    pub fn new() -> Self {
        Self { out: Vec::with_capacity(64) }
    }

    pub fn varint(&mut self, mut value: u64) {
        while value > 0x7F {
            self.out.push((value as u8 & 0x7F) | 0x80);
            value >>= 7;
        }
        self.out.push(value as u8);
    }

    fn tag(&mut self, number: u32, wire: u8) {
        self.varint((u64::from(number) << 3) | u64::from(wire));
    }

    pub fn uint(&mut self, number: u32, value: u64) {
        self.tag(number, 0);
        self.varint(value);
    }

    pub fn sint(&mut self, number: u32, value: i32) {
        self.tag(number, 0);
        self.varint(u64::from(((value << 1) ^ (value >> 31)) as u32));
    }

    pub fn fixed32(&mut self, number: u32, value: u32) {
        self.tag(number, 5);
        self.out.extend_from_slice(&value.to_le_bytes());
    }

    pub fn bytes(&mut self, number: u32, value: &[u8]) {
        self.tag(number, 2);
        self.varint(value.len() as u64);
        self.out.extend_from_slice(value);
    }

    pub fn string(&mut self, number: u32, value: &str) {
        self.bytes(number, value.as_bytes());
    }

    pub fn into_bytes(self) -> Vec<u8> {
        self.out
    }
}

pub struct Reader<'a> {
    data: &'a [u8],
    pos: usize,
    wire: u8,
}

impl<'a> Reader<'a> {
    pub fn new(data: &'a [u8]) -> Self {
        Self { data, pos: 0, wire: 255 }
    }

    pub fn position(&self) -> usize {
        self.pos
    }

    pub fn has_next(&self) -> bool {
        self.pos < self.data.len()
    }

    pub fn varint(&mut self) -> Result<u64, Error> {
        let mut result = 0u64;
        let mut shift = 0u32;
        while shift < 64 {
            let Some(&byte) = self.data.get(self.pos) else {
                return error("truncated varint");
            };
            self.pos += 1;
            result |= u64::from(byte & 0x7F) << shift;
            if byte & 0x80 == 0 {
                return Ok(result);
            }
            shift += 7;
        }
        error("varint too long")
    }

    pub fn next_field(&mut self) -> Result<u32, Error> {
        let tag = self.varint()?;
        self.wire = (tag & 7) as u8;
        let number = tag >> 3;
        if !(1..=0x1FFF_FFFF).contains(&number) {
            return error("field number");
        }
        Ok(number as u32)
    }

    pub fn expect(&self, wire: u8) -> Result<(), Error> {
        if self.wire != wire {
            return error(format!("wire type {}, expected {}", self.wire, wire));
        }
        Ok(())
    }

    fn take(&mut self, count: usize) -> Result<&'a [u8], Error> {
        let end = self.pos.checked_add(count).filter(|end| *end <= self.data.len());
        let Some(end) = end else {
            return error("truncated field");
        };
        let out = &self.data[self.pos..end];
        self.pos = end;
        Ok(out)
    }

    pub fn bytes(&mut self, limit: usize) -> Result<&'a [u8], Error> {
        self.expect(2)?;
        let size = self.varint()?;
        if size > limit as u64 {
            return error(format!("field of {size} bytes over {limit}"));
        }
        self.take(size as usize)
    }

    pub fn string(&mut self, limit: usize) -> Result<String, Error> {
        match std::str::from_utf8(self.bytes(limit)?) {
            Ok(text) => Ok(text.to_owned()),
            Err(_) => error("invalid utf-8"),
        }
    }

    pub fn sint(&mut self) -> Result<i32, Error> {
        let raw = self.varint()? as u32;
        Ok(((raw >> 1) as i32) ^ -((raw & 1) as i32))
    }

    pub fn fixed32(&mut self) -> Result<u32, Error> {
        let raw = self.take(4)?;
        Ok(u32::from_le_bytes([raw[0], raw[1], raw[2], raw[3]]))
    }

    pub fn skip(&mut self) -> Result<(), Error> {
        match self.wire {
            0 => {
                self.varint()?;
            }
            1 => {
                self.take(8)?;
            }
            2 => {
                let size = self.varint()?;
                self.take(usize::try_from(size).unwrap_or(usize::MAX))?;
            }
            5 => {
                self.take(4)?;
            }
            other => return error(format!("wire type {other}")),
        }
        Ok(())
    }
}
