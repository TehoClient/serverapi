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

mod messages;
mod wire;

use std::collections::HashMap;
use std::time::{Duration, Instant};

pub use messages::*;
pub use wire::{Error, Reader, Writer};

use wire::error;

fn whole(message: &Message) -> Vec<u8> {
    let mut writer = Writer::new();
    writer.varint(u64::from(message.type_id()));
    let mut out = writer.into_bytes();
    out.extend_from_slice(&message.encode_body());
    out
}

pub fn encode(message: &Message) -> Result<Vec<u8>, Error> {
    let frame = whole(message);
    if frame.len() > MAX_FRAME {
        return error(format!("frame {} over {}", frame.len(), MAX_FRAME));
    }
    Ok(frame)
}

fn decode_any(frame: &[u8]) -> Result<Option<Message>, Error> {
    if frame.is_empty() {
        return error("empty frame");
    }
    let mut reader = Reader::new(frame);
    let type_id = reader.varint()?;
    let Ok(type_id) = u32::try_from(type_id) else {
        return Ok(None);
    };
    Message::decode_body(type_id, &frame[reader.position()..])
}

pub fn decode(frame: &[u8]) -> Result<Option<Message>, Error> {
    if frame.len() > MAX_FRAME {
        return error(format!("frame {} over {}", frame.len(), MAX_FRAME));
    }
    decode_any(frame)
}

pub fn frames(message: &Message, stream: u32) -> Result<Vec<Vec<u8>>, Error> {
    let all = whole(message);
    if all.len() <= MAX_FRAME {
        return Ok(vec![all]);
    }
    if all.len() > MAX_STREAM {
        return error(format!("message {} over {}", all.len(), MAX_STREAM));
    }
    let total = all.len().div_ceil(SLICE) as u32;
    all.chunks(SLICE)
        .enumerate()
        .map(|(index, data)| encode(&Message::Chunk(Chunk { stream, index: index as u32, total, data: data.to_vec() })))
        .collect()
}

pub fn capability_for(type_id: u32) -> Option<&'static str> {
    if type_id == AssetOffer::TYPE || type_id == AssetWant::TYPE || type_id == AssetData::TYPE {
        return Some("asset:1");
    }
    RANGES.iter().find(|(low, high, _)| (*low..=*high).contains(&type_id)).map(|(_, _, capability)| *capability)
}

pub struct WelcomeOptions<'a> {
    pub server_name: &'a str,
    pub supported: Option<&'a [&'a str]>,
    pub session: u64,
    pub grant: &'a str,
    pub verified: &'a str,
}

pub fn welcome(hello: &Hello, options: &WelcomeOptions<'_>) -> Welcome {
    let wanted = options.supported.unwrap_or(CAPABILITIES);
    Welcome {
        protocol: VERSION.min(hello.protocol.max(1)),
        session: options.session,
        enabled: wanted.iter().filter(|c| hello.capabilities.iter().any(|o| o == *c)).map(|c| (*c).to_owned()).collect(),
        server_name: options.server_name.to_owned(),
        grant: options.grant.to_owned(),
        verified: options.verified.to_owned(),
    }
}

struct Partial {
    parts: Vec<Option<Vec<u8>>>,
    bytes: usize,
    received: usize,
    touched: Instant,
}

#[derive(Default)]
pub struct Reassembler {
    open: HashMap<u32, Partial>,
}

impl Reassembler {
    pub fn new() -> Self {
        Self::default()
    }

    pub fn accept(&mut self, chunk: Chunk) -> Result<Option<Message>, Error> {
        self.accept_at(chunk, Instant::now())
    }

    pub fn accept_at(&mut self, chunk: Chunk, now: Instant) -> Result<Option<Message>, Error> {
        self.open.retain(|_, partial| now.saturating_duration_since(partial.touched) <= Duration::from_secs(30));
        let total = chunk.total as usize;
        if total < 1 || chunk.index >= chunk.total || total.saturating_mul(SLICE) > MAX_STREAM + SLICE {
            return error("chunk bounds");
        }
        let fresh = self.open.get(&chunk.stream).map_or(true, |p| p.parts.len() != total);
        if fresh {
            if !self.open.contains_key(&chunk.stream) && self.open.len() >= 8 {
                return error("too many open streams");
            }
            self.open.insert(chunk.stream, Partial { parts: vec![None; total], bytes: 0, received: 0, touched: now });
        }
        let partial = self.open.get_mut(&chunk.stream).expect("stream just inserted");
        let index = chunk.index as usize;
        if partial.parts[index].is_none() {
            partial.bytes += chunk.data.len();
            if partial.bytes > MAX_STREAM {
                self.open.remove(&chunk.stream);
                return error(format!("stream over {MAX_STREAM}"));
            }
            partial.parts[index] = Some(chunk.data);
            partial.received += 1;
        }
        partial.touched = now;
        if partial.received < total {
            return Ok(None);
        }
        let partial = self.open.remove(&chunk.stream).expect("stream present");
        let joined: Vec<u8> = partial.parts.into_iter().flatten().flatten().collect();
        if Reader::new(&joined).varint()? == u64::from(Chunk::TYPE) {
            return error("nested chunk");
        }
        decode_any(&joined)
    }

    pub fn clear(&mut self) {
        self.open.clear();
    }
}

pub struct Session {
    pub server_name: String,
    pub supported: Option<Vec<&'static str>>,
    pub grant: String,
    pub verified: String,
    pub hello: Option<Hello>,
    pub enabled: Vec<String>,
    reassembler: Reassembler,
    next_stream: u32,
    next_request: u32,
}

impl Session {
    pub fn new(server_name: impl Into<String>) -> Self {
        Self {
            server_name: server_name.into(),
            supported: None,
            grant: String::new(),
            verified: String::new(),
            hello: None,
            enabled: Vec::new(),
            reassembler: Reassembler::new(),
            next_stream: 1,
            next_request: 1,
        }
    }

    pub fn has(&self, capability: &str) -> bool {
        self.enabled.iter().any(|c| c == capability)
    }

    pub fn receive(&mut self, frame: &[u8], outbox: &mut Vec<Vec<u8>>) -> Result<Option<Message>, Error> {
        let mut message = decode(frame)?;
        if let Some(Message::Chunk(chunk)) = message {
            message = self.reassembler.accept(chunk)?;
        }
        if let Some(Message::Hello(hello)) = &message {
            let supported = self.supported.clone();
            let reply = welcome(
                hello,
                &WelcomeOptions {
                    server_name: &self.server_name,
                    supported: supported.as_deref(),
                    session: rand_session(),
                    grant: &self.grant,
                    verified: &self.verified,
                },
            );
            self.enabled = reply.enabled.clone();
            self.hello = Some(hello.clone());
            self.reassembler.clear();
            outbox.push(encode(&Message::Welcome(reply))?);
        }
        Ok(message)
    }

    pub fn send(&mut self, message: impl Into<Message>) -> Result<Option<Vec<Vec<u8>>>, Error> {
        let message = message.into();
        let ungated = matches!(message, Message::Welcome(_) | Message::HelloRequest(_) | Message::Reset(_));
        if !ungated {
            if let Some(capability) = capability_for(message.type_id()) {
                if !self.has(capability) {
                    return Ok(None);
                }
            }
        }
        let stream = self.next_stream & 0x7FFF_FFFF;
        self.next_stream = self.next_stream.wrapping_add(1);
        frames(&message, stream).map(Some)
    }

    pub fn next_request_id(&mut self) -> u32 {
        let id = self.next_request & 0x7FFF_FFFF;
        self.next_request = self.next_request.wrapping_add(1).max(1);
        id
    }
}

fn rand_session() -> u64 {
    use std::collections::hash_map::RandomState;
    use std::hash::{BuildHasher, Hasher};
    let mut hasher = RandomState::new().build_hasher();
    let nanos = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).map_or(0, |d| d.as_nanos());
    hasher.write_u128(nanos ^ u128::from(std::process::id()));
    hasher.finish()
}
