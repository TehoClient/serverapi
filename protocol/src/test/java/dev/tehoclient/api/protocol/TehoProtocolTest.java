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

package dev.tehoclient.api.protocol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class TehoProtocolTest {
	static byte[] hex(String text) {
		String clean = text.replace(" ", "");
		byte[] out = new byte[clean.length() / 2];
		for (int i = 0; i < out.length; i++) {
			out[i] = (byte) Integer.parseInt(clean.substring(i * 2, i * 2 + 2), 16);
		}
		return out;
	}

	@Test
	void helloVector() {
		Hello hello = new Hello(1, "t", "1.21.11", "fabric", Collections.singletonList("notify:1"));
		byte[] expected = hex("01 0801 120174 1a07312e32312e3131 22066661627269 63 2a086e6f746966793a31");
		assertArrayEquals(expected, TehoProtocol.encode(hello));
		Hello back = (Hello) TehoProtocol.decode(expected);
		assertEquals(1, back.protocol);
		assertEquals("1.21.11", back.minecraft);
		assertEquals("fabric", back.platform);
		assertEquals(Collections.singletonList("notify:1"), back.capabilities);
	}

	@Test
	void notifyVector() {
		Notify notify = new Notify("a", "T", "B", 0x4DA9FF, 3000, true);
		byte[] expected = hex("10 0a0161 120154 1a0142 25ffa94d00 28b817 3001");
		assertArrayEquals(expected, TehoProtocol.encode(notify));
		Notify back = (Notify) TehoProtocol.decode(expected);
		assertEquals("a", back.id);
		assertEquals(0x4DA9FF, back.accent);
		assertEquals(3000, back.durationMs);
		assertTrue(back.sound);
	}

	@Test
	void waypointVector() {
		WaypointSet set = new WaypointSet("s", "Spawn", -1, 64, 0, "overworld", 0xFF0000, false);
		byte[] expected = hex("20 0a0173 1205537061776e 1801 208001 2800 32096f766572776f726c64 3d0000ff00 4000");
		assertArrayEquals(expected, TehoProtocol.encode(set));
		WaypointSet back = (WaypointSet) TehoProtocol.decode(expected);
		assertEquals(-1, back.x);
		assertEquals(64, back.y);
		assertEquals("overworld", back.dimension);
		assertEquals(0xFF0000, back.color);
	}

	@Test
	void roundTripsEveryMessage() {
		Message[] messages = {
				new Welcome(1, 0xFFFFFFFFFFL, Arrays.asList("notify:1", "waypoint:1"), "Lobby"),
				Signal.HELLO_REQUEST,
				Signal.RESET,
				new Dismiss("a"),
				new WaypointRemove("s"),
				Signal.WAYPOINT_CLEAR,
				new WaypointSet("n", "Neg", Integer.MIN_VALUE, -64, Integer.MAX_VALUE, "the_nether", 0x123456, true),
		};
		for (Message message : messages) {
			Message back = TehoProtocol.decode(TehoProtocol.encode(message));
			assertEquals(message.type(), back.type());
			assertEquals(message.toString(), back.toString());
		}
		assertSame(Signal.RESET, TehoProtocol.decode(new byte[] {0x04}));
	}

	@Test
	void skipsUnknownFieldsAndTypes() {
		byte[] withExtra = hex("11 0a0161 f8ff0301 9a01 03616263 3d01020304 110102030405060708");
		assertEquals("a", ((Dismiss) TehoProtocol.decode(withExtra)).id);
		assertNull(TehoProtocol.decode(hex("7f 0801")));
	}

	@Test
	void rejectsMalformedFrames() {
		assertThrows(ProtocolException.class, () -> TehoProtocol.decode(new byte[0]));
		assertThrows(ProtocolException.class, () -> TehoProtocol.decode(hex("10 0a05 61")));
		assertThrows(ProtocolException.class, () -> TehoProtocol.decode(hex("10 0a")));
		assertThrows(ProtocolException.class, () -> TehoProtocol.decode(hex("10 08")));
		assertThrows(ProtocolException.class, () -> TehoProtocol.decode(hex("10 0d")));
		assertThrows(ProtocolException.class, () -> TehoProtocol.decode(hex("10 0a ffffffff0f")));
		assertThrows(ProtocolException.class, () -> TehoProtocol.decode(new byte[TehoProtocol.MAX_FRAME + 1]));
	}

	@Test
	void roundTripsPhase2Messages() {
		Message[] messages = {
				new Reply(7, false, "nope", new byte[] {1, 2}),
				new Event("keybind", "party_menu", "press", 0),
				new AssetOffer(new byte[32], TehoProtocol.ASSET_PNG, 1234),
				new AssetWant(Arrays.asList(new byte[32], new byte[] {9})),
				new AssetData(new byte[32], TehoProtocol.ASSET_OGG, new byte[100]),
				new ConsentResult("staff", true, Arrays.asList("xray", "freecam")),
				new ModuleRules(Arrays.asList("zoom", "hitbox"), Collections.singletonList("COMBAT"), "Ranked", true, Collections.singletonList("fps_display")),
				new SettingCaps(Arrays.asList(new SettingCap("zoom", "factor", 1, 4, true, ""), new SettingCap("fov_changer", "fov", 0, 0, false, "70"))),
				new StaffGrant(Arrays.asList("xray", "player_esp"), "Moderator", Arrays.asList(new XrayBlock("minecraft:diamond_ore", 0x5FE0DC), new XrayBlock("minecraft:ancient_debris", 0xB07CFF)), 48, Collections.singletonList(new XrayBlock("minecraft:chest", 0xFFC24D)), Collections.singletonList(new XrayBlock("minecraft:zombie", 0xFF5555))),
				new StaffRevoke(Collections.<String>emptyList()),
				new StaffState("freecam", true),
				new EspStyle(Collections.singletonList(new EspEntry("139fe25e-bb2c-365b-970b-56d419e53764", 0xFF5555, "Red team"))),
				new Cooldown("pearl", "Ender Pearl", 15000, "minecraft:ender_pearl", new byte[0], 0x5FE0DC),
				Signal.COOLDOWN_CLEAR,
				new Timer("match", "Match ends", TehoProtocol.TIMER_COUNTDOWN, 600000, false, 0xFFC24D),
				new Timer("neg", "Neg", TehoProtocol.TIMER_STOPWATCH, -5, true, 0),
				new TimerRemove("match"),
		};
		for (Message message : messages) {
			Message back = TehoProtocol.decode(TehoProtocol.encode(message));
			assertEquals(message.type(), back.type());
			assertEquals(message.toString(), back.toString());
		}
		StaffGrant grant = (StaffGrant) TehoProtocol.decode(TehoProtocol.encode(messages[8]));
		assertEquals("minecraft:ancient_debris", grant.xrayBlocks.get(1).id);
		assertEquals(0xB07CFF, grant.xrayBlocks.get(1).color);
		assertEquals(48, grant.radius);
		assertEquals("70", ((SettingCaps) TehoProtocol.decode(TehoProtocol.encode(messages[7]))).caps.get(1).pinned);
	}

	@Test
	void chunksLargeMessagesAndReassembles() {
		byte[] big = new byte[100_000];
		for (int i = 0; i < big.length; i++) {
			big[i] = (byte) (i * 31);
		}
		AssetData asset = new AssetData(new byte[32], TehoProtocol.ASSET_PNG, big);
		List<byte[]> frames = Chunking.frames(asset, 3);
		assertEquals(4, frames.size());
		Chunking receiver = new Chunking();
		byte[] whole = null;
		for (int i = frames.size() - 1; i >= 0; i--) {
			assertTrue(frames.get(i).length <= TehoProtocol.MAX_FRAME);
			Chunk chunk = (Chunk) TehoProtocol.decode(frames.get(i));
			byte[] done = receiver.accept(chunk, 0);
			if (i > 0) {
				assertNull(done);
			} else {
				whole = done;
			}
		}
		AssetData back = (AssetData) Chunking.decodeWhole(whole);
		assertArrayEquals(big, back.data);
		assertEquals(1, Chunking.frames(new Dismiss("x"), 1).size());
		assertThrows(ProtocolException.class, () -> new Chunking().accept(new Chunk(1, 5, 2, new byte[1]), 0));
	}

	@Test
	void roundTripsStaffToolkitAndSuspectMessages() {
		Message[] messages = {
				new StaffSupport(Arrays.asList("xray", "tracers", "inspector")),
				new Highlight("vanish:1", TehoProtocol.HIGHLIGHT_ENTITY, "139fe25e-bb2c-365b-970b-56d419e53764", -5, 64, 9, 0, 0, 0, 0x80B07CFF, "Vanished: Mod", TehoProtocol.STYLE_BOTH, true, 0, "overworld"),
				new HighlightRemove(Collections.<String>emptyList()),
				new Trail("139fe25e-bb2c-365b-970b-56d419e53764", Arrays.asList(1, 64, -3, 2, 64, -4, -100000, 320, 99999), 0xFF5555, "Suspect", 60000, "overworld"),
				new InspectRequest(9, TehoProtocol.TARGET_BLOCK, "", 10, -60, 22),
				new InspectCard(9, "Chest", "10 -60 22", Arrays.asList(new InspectRow("Placed by", "Steve", 0), new InspectRow("Opened", "3m ago by Alex", 0xFFC24D)), Collections.singletonList(new InspectAction("rollback", "Roll back")), 15000, ""),
				new ActionWheel(Arrays.asList(new WheelAction("freeze", "Freeze", "minecraft:ice", 0x5FE0DC, TehoProtocol.TARGET_PLAYER), new WheelAction("vanish", "Vanish", "minecraft:ender_eye", 0, TehoProtocol.TARGET_NONE)), "Staff"),
				new Alert("ac-1", TehoProtocol.SEVERITY_HIGH, "Reach (Grim)", "VL 12, 3.41 blocks", "139fe25e-bb2c-365b-970b-56d419e53764", 1, 2, 3, "overworld", 30000),
				new TargetStats("139fe25e-bb2c-365b-970b-56d419e53764", Arrays.asList(new InspectRow("CPS", "17", 0xFF5555), new InspectRow("Reach", "3.4", 0)), 2000),
				new Spectate("139fe25e-bb2c-365b-970b-56d419e53764", 6),
				new StaffAction("freeze", TehoProtocol.TARGET_PLAYER, "139fe25e-bb2c-365b-970b-56d419e53764", 0, 0, 0),
				new LagMap(Arrays.asList(new LagCell(-3, 7, 95, "412 entities"), new LagCell(0, 0, 10, "")), "overworld", 60000),
				new FreezeScreen(true, "You are frozen", "Join the screenshare channel"),
				new ScreenshotRequest(4, "Screenshare", 1280),
				new ScreenshotResult(4, true, new byte[] {(byte) 0x89, 'P', 'N', 'G'}, 1280, 720),
				new ModListRequest(5, "Mod check"),
				new ModList(5, true, Collections.singletonList(new ModEntry("sodium", "Sodium", "0.6.0")), Arrays.asList("zoom", "fps_display")),
		};
		for (Message message : messages) {
			Message back = TehoProtocol.decode(TehoProtocol.encode(message));
			assertEquals(message.type(), back.type());
			assertEquals(message.toString(), back.toString());
		}
		assertEquals(TehoProtocol.STAFF, TehoProtocol.capabilityFor(TehoProtocol.LAG_MAP));
		assertEquals(TehoProtocol.SUSPECT, TehoProtocol.capabilityFor(TehoProtocol.MOD_LIST));
		Trail trail = (Trail) TehoProtocol.decode(TehoProtocol.encode(messages[3]));
		assertEquals(-100000, (int) trail.points.get(6));
		StaffGrant grant = new StaffGrant(Collections.singletonList("storage_esp"), "", Collections.<XrayBlock>emptyList(), 32,
				Collections.singletonList(new XrayBlock("minecraft:chest", 1)), Collections.<XrayBlock>emptyList());
		assertEquals("minecraft:chest", ((StaffGrant) TehoProtocol.decode(TehoProtocol.encode(grant))).storageBlocks.get(0).id);
	}

	@Test
	void roundTripsPrivateGrantAndNpcMessages() {
		Message[] messages = {
				new Welcome(2, 9, Arrays.asList("npc:1"), "Hub", "eyJibG9i"),
				new PrivateStatus(Collections.singletonList("npc:1"), "", "hypix"),
				new NpcLook("00000000-0000-2000-8000-000000000001", TehoProtocol.NPC_MODE_EXPLICIT,
						Arrays.asList(new NpcSlot("cape", "founder_cape"), new NpcSlot("wings", "founder_wings"))),
				new NpcLook("00000000-0000-2000-8000-000000000001", TehoProtocol.NPC_MODE_MIRROR, Collections.<NpcSlot>emptyList()),
				new NpcEmote("00000000-0000-2000-8000-000000000001", TehoProtocol.NPC_EMOTE_START, "wave", true),
				new NpcEmote("00000000-0000-2000-8000-000000000001", TehoProtocol.NPC_EMOTE_START, "bow", false),
				new NpcClear(Collections.<String>emptyList()),
		};
		for (Message message : messages) {
			Message back = TehoProtocol.decode(TehoProtocol.encode(message));
			assertEquals(message.type(), back.type());
			assertEquals(message.toString(), back.toString());
		}
		assertEquals("eyJibG9i", ((Welcome) TehoProtocol.decode(TehoProtocol.encode(messages[0]))).grant);
		assertEquals("", ((Welcome) TehoProtocol.decode(TehoProtocol.encode(new Welcome(1, 1, Collections.<String>emptyList(), "x")))).grant);
		assertEquals(TehoProtocol.NPC, TehoProtocol.capabilityFor(TehoProtocol.NPC_EMOTE));
		assertEquals("founder_wings", ((NpcLook) TehoProtocol.decode(TehoProtocol.encode(messages[2]))).slots.get(1).id);
	}
}
