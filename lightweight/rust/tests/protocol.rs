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

use teho_api::*;

fn hex(text: &str) -> Vec<u8> {
    let clean: String = text.chars().filter(|c| !c.is_whitespace()).collect();
    (0..clean.len()).step_by(2).map(|i| u8::from_str_radix(&clean[i..i + 2], 16).unwrap()).collect()
}

#[test]
fn hello_vector() {
    let frame = hex("01 08 01 12 01 74 1a 07 31 2e 32 31 2e 31 31 22 06 66 61 62 72 69 63 2a 08 6e 6f 74 69 66 79 3a 31");
    let hello = Hello { protocol: 1, client: "t".into(), minecraft: "1.21.11".into(), platform: "fabric".into(), capabilities: vec!["notify:1".into()] };
    assert_eq!(encode(&hello.clone().into()).unwrap(), frame);
    assert_eq!(decode(&frame).unwrap(), Some(Message::Hello(hello)));
}

#[test]
fn notify_waypoint_and_reset_vectors() {
    let notify = Notify { id: "a".into(), title: "T".into(), body: "B".into(), accent: 0x4DA9FF, duration_ms: 3000, sound: true };
    assert_eq!(encode(&notify.into()).unwrap(), hex("10 0a 01 61 12 01 54 1a 01 42 25 ff a9 4d 00 28 b8 17 30 01"));
    let frame = hex("20 0a 01 73 12 05 53 70 61 77 6e 18 01 20 80 01 28 00 32 09 6f 76 65 72 77 6f 72 6c 64 3d 00 00 ff 00 40 00");
    let waypoint = WaypointSet { id: "s".into(), name: "Spawn".into(), x: -1, y: 64, z: 0, dimension: "overworld".into(), color: 0xFF0000, focus: false };
    assert_eq!(encode(&waypoint.clone().into()).unwrap(), frame);
    assert_eq!(decode(&frame).unwrap(), Some(Message::WaypointSet(waypoint)));
    assert_eq!(encode(&Reset {}.into()).unwrap(), vec![0x04]);
}

#[test]
fn unknown_fields_and_types() {
    let frame = hex("11 0a 01 61 f8 ff 03 01 9a 01 03 61 62 63 3d 01 02 03 04 11 01 02 03 04 05 06 07 08");
    assert_eq!(decode(&frame).unwrap(), Some(Message::Dismiss(Dismiss { id: "a".into() })));
    assert_eq!(decode(&hex("ff 7f 0a 01 61")).unwrap(), None);
}

#[test]
fn limits() {
    assert!(decode(&vec![0x10; 32001]).is_err());
    let long = encode(&Dismiss { id: "x".repeat(2000) }.into()).unwrap();
    assert!(decode(&long).is_err());
    assert!(decode(&hex("11 0d 01 61")).is_err());
}

#[test]
fn corpus_matches_java_python_and_node() {
    let corpus = std::fs::read_to_string(concat!(env!("CARGO_MANIFEST_DIR"), "/../corpus.txt")).unwrap();
    let mut count = 0;
    for line in corpus.lines().filter(|l| !l.is_empty()) {
        let (name, frame) = line.split_once('\t').unwrap();
        let frame = hex(frame);
        let message = decode(&frame).unwrap().unwrap_or_else(|| panic!("{name} not decoded"));
        assert_eq!(message.name(), name);
        assert_eq!(encode(&message).unwrap(), frame, "{name}");
        count += 1;
    }
    assert_eq!(count, 108);
}

#[test]
fn corpus_values_decode_correctly() {
    let corpus = std::fs::read_to_string(concat!(env!("CARGO_MANIFEST_DIR"), "/../corpus.txt")).unwrap();
    let find = |name: &str| {
        let line = corpus.lines().find(|l| l.starts_with(&format!("{name}\t"))).unwrap();
        decode(&hex(line.split_once('\t').unwrap().1)).unwrap().unwrap()
    };
    let Message::Welcome(welcome) = find("Welcome") else { panic!() };
    assert!(welcome.session > 1 << 60);
    assert!(welcome.server_name.ends_with("\u{e9}\u{732b}"));
    let Message::Shape(shape) = find("Shape") else { panic!() };
    assert!(shape.points.iter().any(|p| *p < 0));
    assert!(shape.color > 0x8000_0000);
}

#[test]
fn chunking() {
    let png: Vec<u8> = (0..102_400u32).map(|i| i as u8).collect();
    let big = ScreenshotResult { request_id: 7, granted: true, png: png.clone(), width: 1920, height: 1080 };
    let parts = frames(&big.clone().into(), 3).unwrap();
    assert!(parts.len() > 1);
    let mut joiner = Reassembler::new();
    let mut result = None;
    for part in parts.iter().rev() {
        let Some(Message::Chunk(chunk)) = decode(part).unwrap() else { panic!() };
        result = joiner.accept(chunk).unwrap();
    }
    assert_eq!(result, Some(Message::ScreenshotResult(big)));
}

#[test]
fn session_handshake_and_gating() {
    let mut session = Session::new("Example");
    session.supported = Some(vec!["notify:1", "hud:1"]);
    let mut outbox = Vec::new();
    let hello = Hello { protocol: 2, client: "teho".into(), capabilities: vec!["notify:1".into(), "world:1".into()], ..Default::default() };
    session.receive(&encode(&hello.into()).unwrap(), &mut outbox).unwrap();
    let Some(Message::Welcome(welcome)) = decode(&outbox[0]).unwrap() else { panic!() };
    assert_eq!(welcome.enabled, vec!["notify:1".to_owned()]);
    assert_eq!(welcome.server_name, "Example");
    assert!(session.send(Notify { id: "hi".into(), ..Default::default() }).unwrap().is_some());
    assert!(session.send(Timer { id: "round".into(), ..Default::default() }).unwrap().is_none());
    assert_eq!(capability_for(NpcLook::TYPE), Some("npc:1"));
    assert_eq!(capability_for(AssetWant::TYPE), Some("asset:1"));
}
