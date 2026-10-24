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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class FeatureMessagesTest {
	@Test
	void roundTripsEveryFeatureMessage() {
		Message[] messages = {
				new CombatRules(false, true, false, true, false),
				new ThirdPartyPolicy(Arrays.asList("a3", "b3"), "reason-é-4", false),
				new PolicyReport(false, Arrays.asList(new ModEntry("id-é-5", "name-é-6", "version-é-7"), new ModEntry("id-é-6", "name-é-7", "version-é-8"))),
				new SidebarSet("title-é-3", Arrays.asList(new SidebarRow("id-é-5", "text-é-6", "value-é-7", "item-é-8", 0x80B07CFF, 1370, new byte[] {1, 2, (byte) 0xFF, 11}, -1413), new SidebarRow("id-é-6", "text-é-7", "value-é-8", "item-é-9", 0x80B07CFF, 1407, new byte[] {1, 2, (byte) 0xFF, 12}, -502)), Arrays.asList("a5", "b5"), true, 0x80B07CFF, "footer-é-8"),
				new Bar("id-é-3", "text-é-4", 1185, 0x80B07CFF, 1259, 1296, "item-é-9"),
				new BarRemove(Arrays.asList("a3", "b3")),
				new KillFeed("killer-é-3", "victim-é-4", "weapon-é-5", "text-é-6", 0x80B07CFF, "killer_id-é-8", "victim_id-é-9", 1370),
				new ScreenTint(0x80B07CFF, true, 1185, 1222, 1259),
				new Title("title-é-3", "subtitle-é-4", 1185, 1222, 1259, 1296, 0x80B07CFF, 1370),
				new TntFuse(1111),
				new Beam("id-é-3", -8701, -7790, -6879, "entity-é-7", 0x80B07CFF, 1333, 1370, false, "dimension-é-12", 1481),
				new Hologram("id-é-3", -8701, -7790, -6879, "entity-é-7", Arrays.asList("a8", "b8"), "item-é-9", new byte[] {1, 2, (byte) 0xFF, 10}, 1407, 0x80B07CFF, false, true, "dimension-é-15", 1592, 1629),
				new Shape("id-é-3", 1148, Arrays.asList(1, -2, 300000, -5), 1222, 1259, 0x80B07CFF, 0x80B07CFF, 1370, false, "label-é-12", "dimension-é-13", 1518),
				new WorldRemove(1111, Arrays.asList("a4", "b4")),
				new EntityStyle("entity-é-3", 0x80B07CFF, "name-é-5", 1222, false, Arrays.asList("a8", "b8"), 1333, true),
				new EntityStyleClear(Arrays.asList("a3", "b3")),
				new BorderStyle(0x80B07CFF, true, false),
				new FireColor("entity-é-3", 0x80B07CFF, false),
				new Nametag("player-é-3", Arrays.asList("a4", "b4"), Arrays.asList("a5", "b5"), "prefix-é-6", "suffix-é-7", new byte[] {1, 2, (byte) 0xFF, 8}, false, "badge_item-é-10"),
				new NametagClear(Arrays.asList("a3", "b3")),
				new TabList(Arrays.asList("a3", "b3"), Arrays.asList("a4", "b4"), Arrays.asList(new TabEntry("player-é-6", "display-é-7", "group-é-8", new byte[] {1, 2, (byte) 0xFF, 9}, "ping-é-10", -2324, 0x80B07CFF), new TabEntry("player-é-7", "display-é-8", "group-é-9", new byte[] {1, 2, (byte) 0xFF, 10}, "ping-é-11", -1413, 0x80B07CFF)), true, false, Arrays.asList("a8", "b8"), false),
				new Team("id-é-3", "name-é-4", 0x80B07CFF, Arrays.asList("a6", "b6"), false, true, false, true),
				new TeamRemove(Arrays.asList("a3", "b3")),
				new NickHint("nick-é-3", "player-é-4"),
				new Keybinds(Arrays.asList(new Keybind("id-é-4", "label-é-5", -6879, "button-é-7", true, "description-é-9"), new Keybind("id-é-5", "label-é-6", -5968, "button-é-8", false, "description-é-10")), "category-é-4"),
				new KeyPress("id-é-3", true, 1185),
				new CameraShake(1111, 1148, 1185),
				new FovPunch(-9612, 1148),
				new LookAt(-9612, -8701, -7790, "entity-é-6", 1259, 1296),
				new Letterbox(false, 1148, 1185),
				new Cutscene("id-é-3", Arrays.asList(new Keyframe(-7790, -6879, -5968, -5057, -4146, -3235, 1407, 1444, 1481), new Keyframe(-6879, -5968, -5057, -4146, -3235, -2324, 1444, 1481, 1518)), false, true, false, true, "dimension-é-9"),
				new CutsceneStop("id-é-3"),
				new ScreenFade(0x80B07CFF, 1148, 1185, 1222),
				new CutsceneEnded("id-é-3", true),
				new PlaySound("id-é-3", new byte[] {1, 2, (byte) 0xFF, 4}, "sound-é-5", -6879, -5968, -5057, "entity-é-9", true, 1407, 1444, false, 1518, false),
				new StopSound(Arrays.asList("a3", "b3"), 1148),
				new Presence("game-é-3", "map-é-4", "state-é-5", 1793000000000L, 1259, 1296, false, true),
				new InviteSheet("reason-é-3", "party-é-4", "address-é-5"),
				new Transfer("host-é-3", 1148, "token-é-5", "title-é-6", "reason-é-7"),
				new ServerLinks(Arrays.asList(new ServerLink("label-é-4", "url-é-5", "kind-é-6"), new ServerLink("label-é-5", "url-é-6", "kind-é-7"))),
				new UiOpen("id-é-3", 1148, "title-é-5", Arrays.asList(new UiNode("id-é-7", "parent-é-8", 1333, "text-é-10", "value-é-11", -1413, -502, 1518, new byte[] {1, 2, (byte) 0xFF, 15}, "item-é-16", "player-é-17", 0x80B07CFF, 1703, true, "placeholder-é-21", Arrays.asList("a22", "b22"), 1793000000000L, 1888, 1925, 1962, "tooltip-é-27"), new UiNode("id-é-8", "parent-é-9", 1370, "text-é-11", "value-é-12", -502, 409, 1555, new byte[] {1, 2, (byte) 0xFF, 16}, "item-é-17", "player-é-18", 0x80B07CFF, 1740, false, "placeholder-é-22", Arrays.asList("a23", "b23"), 1793000000000L, 1925, 1962, 1999, "tooltip-é-28")), 1259, true, 1333),
				new UiPatch("id-é-3", Arrays.asList(new UiNode("id-é-5", "parent-é-6", 1259, "text-é-8", "value-é-9", -3235, -2324, 1444, new byte[] {1, 2, (byte) 0xFF, 13}, "item-é-14", "player-é-15", 0x80B07CFF, 1629, true, "placeholder-é-19", Arrays.asList("a20", "b20"), 1793000000000L, 1814, 1851, 1888, "tooltip-é-25"), new UiNode("id-é-6", "parent-é-7", 1296, "text-é-9", "value-é-10", -2324, -1413, 1481, new byte[] {1, 2, (byte) 0xFF, 14}, "item-é-15", "player-é-16", 0x80B07CFF, 1666, false, "placeholder-é-20", Arrays.asList("a21", "b21"), 1793000000000L, 1851, 1888, 1925, "tooltip-é-26")), Arrays.asList("a5", "b5"), "title-é-6"),
				new UiClose("id-é-3"),
				new UiEvent("id-é-3", "node-é-4", 1185, "value-é-6"),
				new InfoRequest(1111, Arrays.asList("a4", "b4"), "reason-é-5"),
				new InfoReply(1111, Arrays.asList("a4", "b4"), "input-é-5", 1222, 1259, 1296, 1333, Arrays.asList(new ModEntry("id-é-11", "name-é-12", "version-é-13"), new ModEntry("id-é-12", "name-é-13", "version-é-14")), Arrays.asList("a11", "b11"), "locale-é-12", 1481, 1518, 1555, "os-é-16", 1629),
				new AssertRequest(1111, "nonce-é-4"),
				new AssertResult(1111, "token-é-4", "error-é-5"),
				new AttestRequest(1111, "nonce-é-4"),
				new AttestResult(1111, "token-é-4", "error-é-5"),
				new PackHint("url-é-3", "sha1-é-4", 1185, true, "prompt-é-7", "id-é-8"),
				new PackStatus("sha1-é-3", 1148, "error-é-5"),
				new ClipMark("id-é-3", "title-é-4", 1185, 1222),
				new ClipResult("id-é-3", true, "url-é-5", "error-é-6"),
				new ObserverHud(false, "title-é-4", "subtitle-é-5", Arrays.asList(new ObserverTeam("name-é-7", 0x80B07CFF, -4146, Arrays.asList(new ObserverPlayer("player-é-11", "name-é-12", 1481, true, 1555, "item-é-16"), new ObserverPlayer("player-é-12", "name-é-13", 1518, false, 1592, "item-é-17"))), new ObserverTeam("name-é-8", 0x80B07CFF, -3235, Arrays.asList(new ObserverPlayer("player-é-12", "name-é-13", 1518, false, 1592, "item-é-17"), new ObserverPlayer("player-é-13", "name-é-14", 1555, true, 1629, "item-é-18"))))),
				new CameraFollow("player-é-3", 1148, 1185, true),
				new Statue("id-é-3", "player-é-4", "name-é-5", -6879, -5968, -5057, -4146, 1370, 1407, "dimension-é-12", "label-é-13", "emote-é-14"),
				new StatueRemove(Arrays.asList("a3", "b3")),
				new Branding(0x80B07CFF, new byte[] {1, 2, (byte) 0xFF, 4}, new byte[] {1, 2, (byte) 0xFF, 5}, new byte[] {1, 2, (byte) 0xFF, 6}, "tagline-é-7", true),
				new ShowcaseOpen("title-é-3", Arrays.asList("a4", "b4"), "emote-é-5", "focus-é-6", "subtitle-é-7"),
		};
		for (Message message : messages) {
			byte[] frame = TehoProtocol.encode(message);
			Message back = TehoProtocol.decode(frame);
			assertNotNull(back, message.toString());
			assertEquals(message.type(), back.type());
			assertEquals(message.toString(), back.toString());
			assertArrayEquals(frame, TehoProtocol.encode(back));
			assertNotNull(TehoProtocol.capabilityFor(message.type()), message.toString());
		}
	}

	@Test
	void mapsEveryFeatureRangeToItsCapability() {
		assertEquals("world:1", TehoProtocol.capabilityFor(0x80));
		assertEquals("world:1", TehoProtocol.capabilityFor(0x8F));
		assertEquals("players:1", TehoProtocol.capabilityFor(0x90));
		assertEquals("players:1", TehoProtocol.capabilityFor(0x9F));
		assertEquals("keybind:1", TehoProtocol.capabilityFor(0xA0));
		assertEquals("keybind:1", TehoProtocol.capabilityFor(0xAF));
		assertEquals("camera:1", TehoProtocol.capabilityFor(0xB0));
		assertEquals("camera:1", TehoProtocol.capabilityFor(0xBF));
		assertEquals("audio:1", TehoProtocol.capabilityFor(0xC0));
		assertEquals("audio:1", TehoProtocol.capabilityFor(0xCF));
		assertEquals("social:1", TehoProtocol.capabilityFor(0xD0));
		assertEquals("social:1", TehoProtocol.capabilityFor(0xDF));
		assertEquals("ui:1", TehoProtocol.capabilityFor(0xE0));
		assertEquals("ui:1", TehoProtocol.capabilityFor(0xEF));
		assertEquals("info:1", TehoProtocol.capabilityFor(0xF0));
		assertEquals("info:1", TehoProtocol.capabilityFor(0xFF));
		assertEquals("assert:1", TehoProtocol.capabilityFor(0x110));
		assertEquals("assert:1", TehoProtocol.capabilityFor(0x117));
		assertEquals("attest:1", TehoProtocol.capabilityFor(0x118));
		assertEquals("attest:1", TehoProtocol.capabilityFor(0x11F));
		assertEquals("pack:1", TehoProtocol.capabilityFor(0x120));
		assertEquals("pack:1", TehoProtocol.capabilityFor(0x12F));
		assertEquals("clips:1", TehoProtocol.capabilityFor(0x130));
		assertEquals("clips:1", TehoProtocol.capabilityFor(0x13F));
		assertEquals("broadcast:1", TehoProtocol.capabilityFor(0x140));
		assertEquals("broadcast:1", TehoProtocol.capabilityFor(0x14F));
		assertEquals("statue:1", TehoProtocol.capabilityFor(0x170));
		assertEquals("statue:1", TehoProtocol.capabilityFor(0x17F));
		assertEquals("branding:1", TehoProtocol.capabilityFor(0x180));
		assertEquals("branding:1", TehoProtocol.capabilityFor(0x18F));
		assertEquals("showcase:1", TehoProtocol.capabilityFor(0x190));
		assertEquals("showcase:1", TehoProtocol.capabilityFor(0x19F));
		assertEquals(TehoProtocol.HUD, TehoProtocol.capabilityFor(TehoProtocol.SIDEBAR_CLEAR));
		assertEquals(Signal.SIDEBAR_CLEAR, TehoProtocol.decode(TehoProtocol.encode(Signal.SIDEBAR_CLEAR)));
	}

	@Test
	void welcomeCarriesTheVerifiedCertificate() {
		Welcome welcome = new Welcome(2, 7, Arrays.asList("ui:1"), "Hub", "grant", "cert");
		Welcome back = (Welcome) TehoProtocol.decode(TehoProtocol.encode(welcome));
		assertEquals("cert", back.verified);
		assertEquals("grant", back.grant);
		assertEquals("", ((Welcome) TehoProtocol.decode(TehoProtocol.encode(new Welcome(2, 7, Arrays.asList("ui:1"), "Hub")))).verified);
	}
}
