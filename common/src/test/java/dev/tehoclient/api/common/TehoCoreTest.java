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

package dev.tehoclient.api.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import dev.tehoclient.api.TehoListener;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.hud.Cooldown;
import dev.tehoclient.api.modules.SettingCap;
import dev.tehoclient.api.protocol.ConsentResult;
import dev.tehoclient.api.protocol.Hello;
import dev.tehoclient.api.protocol.Message;
import dev.tehoclient.api.protocol.ModuleRules;
import dev.tehoclient.api.protocol.SettingCaps;
import dev.tehoclient.api.protocol.StaffGrant;
import dev.tehoclient.api.protocol.StaffState;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.protocol.WaypointSet;
import dev.tehoclient.api.protocol.Welcome;
import dev.tehoclient.api.staff.StaffTool;
import dev.tehoclient.api.waypoint.Waypoint;

class TehoCoreTest {
	static final class FakeBridge implements PlatformBridge {
		final List<Message> sent = new ArrayList<>();
		final List<Runnable> later = new ArrayList<>();
		boolean online = true;

		@Override
		public void send(UUID player, byte[] frame) {
			sent.add(TehoProtocol.decode(frame));
		}

		@Override
		public boolean online(UUID player) {
			return online;
		}

		@Override
		public String name(UUID player) {
			return "Tester";
		}

		@Override
		public String serverName() {
			return "Fake";
		}

		@Override
		public void runLater(UUID player, Runnable task, long delayMs) {
			later.add(task);
		}

		@Override
		public void info(String message) {
		}

		@Override
		public void warn(String message, Throwable failed) {
			throw new AssertionError(message, failed);
		}

		<T extends Message> List<T> of(Class<T> type) {
			List<T> out = new ArrayList<>();
			for (Message message : sent) {
				if (type.isInstance(message)) {
					out.add(type.cast(message));
				}
			}
			return out;
		}
	}

	private static byte[] hello(String... capabilities) {
		return TehoProtocol.encode(new Hello(2, "teho test", "1.21.11", "fabric", Arrays.asList(capabilities)));
	}

	@Test
	void handshakeNegotiatesAndFiresReadyOnce() {
		FakeBridge bridge = new FakeBridge();
		TehoCore core = new TehoCore(bridge);
		core.configure("Arena", Arrays.asList(TehoProtocol.NOTIFY, TehoProtocol.STAFF, TehoProtocol.MODULES), false);
		List<TehoPlayer> ready = new ArrayList<>();
		core.addListener(new TehoListener() {
			@Override
			public void onReady(TehoPlayer player) {
				ready.add(player);
			}
		});
		UUID id = UUID.randomUUID();
		core.onChannelRegistered(id);
		core.onMessage(id, hello(TehoProtocol.NOTIFY, TehoProtocol.STAFF, TehoProtocol.HUD));
		Welcome welcome = bridge.of(Welcome.class).get(0);
		assertEquals(Arrays.asList(TehoProtocol.NOTIFY, TehoProtocol.STAFF), welcome.enabled);
		assertEquals("Arena", welcome.serverName);
		assertEquals(2, welcome.protocol);
		bridge.later.get(0).run();
		assertTrue(bridge.of(dev.tehoclient.api.protocol.Signal.class).isEmpty());
		core.onMessage(id, hello(TehoProtocol.NOTIFY));
		assertEquals(1, ready.size());
		assertFalse(core.player(id).get().supports(TehoProtocol.STAFF));
	}

	@Test
	void staffConsentAndStateAreTracked() {
		FakeBridge bridge = new FakeBridge();
		TehoCore core = new TehoCore(bridge);
		UUID id = UUID.randomUUID();
		core.onMessage(id, hello(TehoProtocol.STAFF));
		TehoPlayer player = core.player(id).get();
		assertTrue(player.staff().grant(dev.tehoclient.api.staff.StaffGrant.of(StaffTool.XRAY, StaffTool.FREECAM).build()));
		StaffGrant grant = bridge.of(StaffGrant.class).get(0);
		assertEquals(Arrays.asList("xray", "freecam"), grant.tools);
		assertFalse(grant.xrayBlocks.isEmpty());
		List<String> events = new ArrayList<>();
		core.addListener(new TehoListener() {
			@Override
			public void onStaffTool(TehoPlayer who, StaffTool tool, boolean active) {
				events.add(tool + "=" + active);
			}
		});
		core.onMessage(id, TehoProtocol.encode(new ConsentResult("staff", true, Arrays.asList("xray", "freecam"))));
		assertEquals(EnumSet.of(StaffTool.XRAY, StaffTool.FREECAM), player.staff().accepted());
		core.onMessage(id, TehoProtocol.encode(new StaffState("freecam", true)));
		core.onMessage(id, TehoProtocol.encode(new StaffState("player_esp", true)));
		assertEquals(Collections.singletonList("FREECAM=true"), events);
		assertEquals(EnumSet.of(StaffTool.FREECAM), player.staff().active());
		player.staff().revoke(StaffTool.FREECAM);
		assertEquals(EnumSet.of(StaffTool.XRAY), player.staff().granted());
		assertTrue(player.staff().active().isEmpty());
	}

	@Test
	void stateIsReappliedAfterReHandshake() {
		FakeBridge bridge = new FakeBridge();
		TehoCore core = new TehoCore(bridge);
		UUID id = UUID.randomUUID();
		core.onMessage(id, hello(TehoProtocol.WAYPOINT, TehoProtocol.MODULES, TehoProtocol.HUD));
		TehoPlayer player = core.player(id).get();
		player.waypoints().set(Waypoint.builder("spawn").name("Spawn").at(1, 2, 3).focus(true).build());
		player.modules().update(rules -> rules.disable("zoom").cap(SettingCap.atMost("zoom", "factor", 4)));
		player.hud().cooldown(Cooldown.builder("pearl", 60000).item("ender_pearl").build());
		bridge.sent.clear();
		core.onMessage(id, hello(TehoProtocol.WAYPOINT, TehoProtocol.MODULES, TehoProtocol.HUD));
		assertEquals(1, bridge.of(Welcome.class).size());
		WaypointSet waypoint = bridge.of(WaypointSet.class).get(0);
		assertEquals("spawn", waypoint.id);
		assertFalse(waypoint.focus);
		assertEquals(Collections.singletonList("zoom"), bridge.of(ModuleRules.class).get(0).disabled);
		assertEquals(4, bridge.of(SettingCaps.class).get(0).caps.get(0).max);
		dev.tehoclient.api.protocol.Cooldown cooldown = bridge.of(dev.tehoclient.api.protocol.Cooldown.class).get(0);
		assertTrue(cooldown.durationMs > 59000 && cooldown.durationMs <= 60000);
		assertEquals("minecraft:ender_pearl", cooldown.item);
	}

	@Test
	void unsupportedFeaturesAreNotSent() {
		FakeBridge bridge = new FakeBridge();
		TehoCore core = new TehoCore(bridge);
		UUID id = UUID.randomUUID();
		core.onMessage(id, hello(TehoProtocol.NOTIFY));
		bridge.sent.clear();
		TehoPlayer player = core.player(id).get();
		assertFalse(player.staff().grant(dev.tehoclient.api.staff.StaffGrant.of(StaffTool.XRAY).build()));
		assertFalse(player.modules().disable("zoom"));
		assertTrue(bridge.sent.isEmpty());
	}

	@Test
	void inspectorActionsAndSuspectRequestsRoundTrip() throws Exception {
		FakeBridge bridge = new FakeBridge();
		TehoCore core = new TehoCore(bridge);
		UUID id = UUID.randomUUID();
		core.onMessage(id, hello(TehoProtocol.STAFF, TehoProtocol.SUSPECT));
		TehoPlayer staff = core.player(id).get();
		List<String> actions = new ArrayList<>();
		core.addListener(new TehoListener() {
			@Override
			public void onInspect(TehoPlayer who, dev.tehoclient.api.staff.Target target, dev.tehoclient.api.staff.InspectResponder responder) {
				responder.reply(dev.tehoclient.api.staff.InspectCard.builder("Block").row("At", target.x() + " " + target.y() + " " + target.z())
						.action("teleport", "Teleport").build());
			}

			@Override
			public void onStaffAction(TehoPlayer who, String action, dev.tehoclient.api.staff.Target target) {
				actions.add(action + "@" + target);
			}
		});
		core.onMessage(id, TehoProtocol.encode(new dev.tehoclient.api.protocol.InspectRequest(3, TehoProtocol.TARGET_BLOCK, "", 1, 2, 3)));
		assertTrue(bridge.of(dev.tehoclient.api.protocol.InspectCard.class).isEmpty());
		staff.staff().grant(dev.tehoclient.api.staff.StaffGrant.of(StaffTool.INSPECTOR, StaffTool.ACTION_WHEEL).build());
		core.onMessage(id, TehoProtocol.encode(new dev.tehoclient.api.protocol.StaffAction("freeze", TehoProtocol.TARGET_PLAYER, id.toString(), 0, 0, 0)));
		assertTrue(actions.isEmpty());
		core.onMessage(id, TehoProtocol.encode(new ConsentResult("staff", true, Arrays.asList("inspector", "action_wheel"))));
		core.onMessage(id, TehoProtocol.encode(new dev.tehoclient.api.protocol.InspectRequest(3, TehoProtocol.TARGET_BLOCK, "", 1, 2, 3)));
		dev.tehoclient.api.protocol.InspectCard card = bridge.of(dev.tehoclient.api.protocol.InspectCard.class).get(0);
		assertEquals(3, card.requestId);
		assertEquals("1 2 3", card.rows.get(0).value);
		assertEquals("teleport", card.actions.get(0).id);
		core.onMessage(id, TehoProtocol.encode(new dev.tehoclient.api.protocol.StaffAction("freeze", TehoProtocol.TARGET_PLAYER, id.toString(), 0, 0, 0)));
		assertEquals(Collections.singletonList("freeze@player " + id), actions);
		core.onMessage(id, TehoProtocol.encode(new dev.tehoclient.api.protocol.StaffSupport(Arrays.asList("xray", "inspector", "bogus"))));
		assertEquals(EnumSet.of(StaffTool.XRAY, StaffTool.INSPECTOR), staff.staff().supported());

		java.util.concurrent.CompletableFuture<dev.tehoclient.api.suspect.Screenshot> shot = staff.suspect().screenshot("check", 1280);
		dev.tehoclient.api.protocol.ScreenshotRequest request = bridge.of(dev.tehoclient.api.protocol.ScreenshotRequest.class).get(0);
		assertFalse(shot.isDone());
		core.onMessage(id, TehoProtocol.encode(new dev.tehoclient.api.protocol.ScreenshotResult(request.requestId, true, new byte[] {1, 2, 3}, 4, 5)));
		assertEquals(3, shot.get().png().length);
		java.util.concurrent.CompletableFuture<dev.tehoclient.api.suspect.ModListResult> mods = staff.suspect().modList("check");
		core.onQuit(id);
		assertTrue(mods.isCompletedExceptionally());
		assertTrue(staff.suspect().freeze("Frozen", "Wait") || !bridge.online);
	}

	@Test
	void privateGrantTravelsInWelcomeAndNpcStateReplays() {
		FakeBridge bridge = new FakeBridge();
		TehoCore core = new TehoCore(bridge);
		core.grant("signed-token");
		UUID npc = UUID.fromString("00000000-0000-2000-8000-000000000001");
		core.npcs().look(npc, dev.tehoclient.api.npc.NpcLook.wearing().cape("founder_cape").headwear("founder_crown").build());
		core.npcs().emote(npc, dev.tehoclient.api.npc.NpcEmote.loop("wave"));
		core.npcs().emote(UUID.randomUUID(), dev.tehoclient.api.npc.NpcEmote.once("bow"));
		assertEquals(1, core.npcs().emotes().size());
		UUID viewer = UUID.randomUUID();
		core.onMessage(viewer, hello(TehoProtocol.NOTIFY, TehoProtocol.NPC));
		assertEquals("signed-token", bridge.of(Welcome.class).get(0).grant);
		dev.tehoclient.api.protocol.NpcLook look = bridge.of(dev.tehoclient.api.protocol.NpcLook.class).get(0);
		assertEquals(npc.toString(), look.npc);
		assertEquals(TehoProtocol.NPC_MODE_EXPLICIT, look.mode);
		assertEquals("founder_cape", look.slots.get(0).id);
		dev.tehoclient.api.protocol.NpcEmote emote = bridge.of(dev.tehoclient.api.protocol.NpcEmote.class).get(0);
		assertTrue(emote.loop);
		assertEquals("wave", emote.emote);
		core.onMessage(viewer, TehoProtocol.encode(new dev.tehoclient.api.protocol.PrivateStatus(Collections.singletonList("npc:1"), "", "test")));
		TehoPlayer player = core.player(viewer).get();
		assertEquals(Collections.singleton("npc:1"), player.privileges());
		bridge.sent.clear();
		core.npcs().mirrorViewer(npc);
		assertEquals(TehoProtocol.NPC_MODE_MIRROR, bridge.of(dev.tehoclient.api.protocol.NpcLook.class).get(0).mode);
		core.npcs().remove(npc);
		assertEquals(Collections.singletonList(npc.toString()), bridge.of(dev.tehoclient.api.protocol.NpcClear.class).get(0).npcs);
		assertTrue(core.npcs().looks().isEmpty());
		UUID other = UUID.randomUUID();
		core.onMessage(other, hello(TehoProtocol.NOTIFY));
		bridge.sent.clear();
		core.npcs().playOnce(npc, "bow");
		assertEquals(1, bridge.of(dev.tehoclient.api.protocol.NpcEmote.class).size());
		assertFalse(bridge.of(dev.tehoclient.api.protocol.NpcEmote.class).get(0).loop);
		org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
				() -> dev.tehoclient.api.npc.NpcLook.wearing().cosmetic("badge", "staff"));
	}
}
