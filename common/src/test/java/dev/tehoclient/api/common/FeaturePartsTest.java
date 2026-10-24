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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import dev.tehoclient.api.TehoListener;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.asset.Asset;
import dev.tehoclient.api.hud.Sidebar;
import dev.tehoclient.api.info.ClientInfo;
import dev.tehoclient.api.keybind.Keybind;
import dev.tehoclient.api.players.TabList;
import dev.tehoclient.api.protocol.AssertRequest;
import dev.tehoclient.api.protocol.AssertResult;
import dev.tehoclient.api.protocol.AssetData;
import dev.tehoclient.api.protocol.AssetOffer;
import dev.tehoclient.api.protocol.AssetWant;
import dev.tehoclient.api.protocol.Event;
import dev.tehoclient.api.protocol.Hello;
import dev.tehoclient.api.protocol.InfoReply;
import dev.tehoclient.api.protocol.InfoRequest;
import dev.tehoclient.api.protocol.KeyPress;
import dev.tehoclient.api.protocol.Message;
import dev.tehoclient.api.protocol.ModEntry;
import dev.tehoclient.api.protocol.Signal;
import dev.tehoclient.api.protocol.SidebarSet;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.protocol.UiEvent;
import dev.tehoclient.api.protocol.UiOpen;
import dev.tehoclient.api.protocol.UiPatch;
import dev.tehoclient.api.ui.Node;
import dev.tehoclient.api.ui.Screen;
import dev.tehoclient.api.ui.UiInteraction;
import dev.tehoclient.api.verify.Proof;
import dev.tehoclient.api.world.Beam;
import dev.tehoclient.api.world.Hologram;

class FeaturePartsTest {
	private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 0, 0, 0, 13};

	private static byte[] hello(String... capabilities) {
		return TehoProtocol.encode(new Hello(2, "teho test", "1.21.11", "fabric", Arrays.asList(capabilities)));
	}

	private static byte[] frame(Message message) {
		return TehoProtocol.encode(message);
	}

	@Test
	void worldStateSurvivesARefreshAndOnlyClickableHologramsFire() {
		TehoCoreTest.FakeBridge bridge = new TehoCoreTest.FakeBridge();
		TehoCore core = new TehoCore(bridge);
		UUID id = UUID.randomUUID();
		core.onMessage(id, hello(TehoProtocol.WORLD, TehoProtocol.ASSET));
		TehoPlayer player = core.player(id).get();
		List<String> clicks = new ArrayList<>();
		core.addListener(new TehoListener() {
			@Override
			public void onHologramClick(TehoPlayer who, String hologram) {
				clicks.add(hologram);
			}
		});
		Asset image = core.assets().png(PNG);
		assertTrue(player.world().beam(Beam.at("spawn", 0.5, 64, 0.5).color(0xFF5555).pulse(true).build()));
		assertTrue(player.world().hologram(Hologram.at("shop", 1, 65.25, 1).lines("Shop", "Click me").image(image).clickable(true).build()));
		assertTrue(player.world().hologram(Hologram.at("sign", 2, 65, 2).lines("Read only").build()));
		assertEquals(1, bridge.of(AssetOffer.class).size());
		assertEquals(6525, bridge.of(dev.tehoclient.api.protocol.Hologram.class).get(0).y);
		core.onMessage(id, frame(new Event("hologram.click", "shop", "", 0)));
		core.onMessage(id, frame(new Event("hologram.click", "sign", "", 0)));
		assertEquals(Collections.singletonList("shop"), clicks);
		bridge.sent.clear();
		core.refresh(id);
		assertEquals(1, bridge.of(dev.tehoclient.api.protocol.Beam.class).size());
		assertEquals(2, bridge.of(dev.tehoclient.api.protocol.Hologram.class).size());
		assertTrue(player.world().removeHolograms("shop"));
		bridge.sent.clear();
		core.refresh(id);
		assertEquals(1, bridge.of(dev.tehoclient.api.protocol.Hologram.class).size());
	}

	@Test
	void assetsAreOfferedOnceAndServedOnlyWhenOffered() {
		TehoCoreTest.FakeBridge bridge = new TehoCoreTest.FakeBridge();
		TehoCore core = new TehoCore(bridge);
		UUID id = UUID.randomUUID();
		core.onMessage(id, hello(TehoProtocol.WORLD, TehoProtocol.ASSET));
		TehoPlayer player = core.player(id).get();
		Asset image = core.assets().png(PNG);
		Asset other = core.assets().png(new byte[] {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3, 4, 5});
		player.world().hologram(Hologram.at("a", 0, 0, 0).image(image).build());
		player.world().hologram(Hologram.at("b", 0, 0, 0).image(image).build());
		assertEquals(1, bridge.of(AssetOffer.class).size());
		core.onMessage(id, frame(new AssetWant(Arrays.asList(image.hash(), other.hash()))));
		List<AssetData> data = bridge.of(AssetData.class);
		assertEquals(1, data.size());
		assertArrayEquals(image.data(), data.get(0).data);
	}

	@Test
	void uiScreensPatchRouteInteractionsAndIgnoreDisabledNodes() {
		TehoCoreTest.FakeBridge bridge = new TehoCoreTest.FakeBridge();
		TehoCore core = new TehoCore(bridge);
		UUID id = UUID.randomUUID();
		core.onMessage(id, hello(TehoProtocol.UI));
		TehoPlayer player = core.player(id).get();
		List<UiInteraction> seen = new ArrayList<>();
		Screen shop = Screen.modal("shop", "Kit shop").width(420)
				.add(Node.text("intro", "Pick a kit"), Node.grid("kits", 2, Node.button("warrior", "Warrior").icon("iron_sword"),
						Node.button("archer", "Archer").disabled(true)))
				.onInteract((who, interaction) -> seen.add(interaction)).build();
		assertTrue(player.ui().open(shop));
		UiOpen open = bridge.of(UiOpen.class).get(0);
		assertEquals(4, open.nodes.size());
		assertEquals("kits", open.nodes.get(3).parent);
		core.onMessage(id, frame(new UiEvent("shop", "warrior", TehoProtocol.UI_CLICK, "")));
		core.onMessage(id, frame(new UiEvent("shop", "archer", TehoProtocol.UI_CLICK, "")));
		core.onMessage(id, frame(new UiEvent("shop", "ghost", TehoProtocol.UI_CLICK, "")));
		assertEquals(1, seen.size());
		assertTrue(seen.get(0).clicked("warrior"));
		assertTrue(player.ui().patch("shop", Node.grid("kits", 1, Node.button("mage", "Mage"))));
		UiPatch patch = bridge.of(UiPatch.class).get(0);
		assertEquals(2, patch.nodes.size());
		bridge.sent.clear();
		core.refresh(id);
		List<String> ids = new ArrayList<>();
		for (dev.tehoclient.api.protocol.UiNode node : bridge.of(UiOpen.class).get(0).nodes) {
			ids.add(node.id);
		}
		assertEquals(Arrays.asList("intro", "kits", "mage"), ids);
		core.onMessage(id, frame(new UiEvent("shop", "", TehoProtocol.UI_CLOSED, "")));
		assertTrue(player.ui().openScreens().isEmpty());
		assertThrows(IllegalArgumentException.class, () -> Screen.modal("x", "x").add(Node.text("a", "1"), Node.text("a", "2")).build());
	}

	@Test
	void keybindPressesAreFilteredToRegisteredBindsAndRateLimited() {
		TehoCoreTest.FakeBridge bridge = new TehoCoreTest.FakeBridge();
		TehoCore core = new TehoCore(bridge);
		UUID id = UUID.randomUUID();
		core.onMessage(id, hello(TehoProtocol.KEYBIND));
		TehoPlayer player = core.player(id).get();
		List<String> presses = new ArrayList<>();
		core.addListener(new TehoListener() {
			@Override
			public void onKey(TehoPlayer who, String keybind, boolean pressed, long heldMs) {
				presses.add(keybind + ":" + pressed);
			}
		});
		player.keybinds().register("Bedwars", Arrays.asList(Keybind.builder("party", "Open party").key('P').build()));
		core.onMessage(id, frame(new KeyPress("party", true, 0)));
		core.onMessage(id, frame(new KeyPress("party", true, 0)));
		core.onMessage(id, frame(new KeyPress("party", false, 120)));
		core.onMessage(id, frame(new KeyPress("unknown", true, 0)));
		assertEquals(Arrays.asList("party:true", "party:false"), presses);
	}

	@Test
	void infoRepliesCompleteTheRequest() throws Exception {
		TehoCoreTest.FakeBridge bridge = new TehoCoreTest.FakeBridge();
		TehoCore core = new TehoCore(bridge);
		UUID id = UUID.randomUUID();
		core.onMessage(id, hello(TehoProtocol.INFO));
		CompletableFuture<ClientInfo> future = core.player(id).get().info().request("Matchmaking", ClientInfo.Field.INPUT, ClientInfo.Field.MODS);
		InfoRequest request = bridge.of(InfoRequest.class).get(0);
		assertEquals(Arrays.asList("input", "mods"), request.fields);
		core.onMessage(id, frame(new InfoReply(request.requestId, Arrays.asList("input"), "controller", 0, 0, 0, 0,
				Collections.<ModEntry>emptyList(), Collections.<String>emptyList(), "", 0, 0, 0, "", 0)));
		ClientInfo info = future.get(1, TimeUnit.SECONDS);
		assertEquals(ClientInfo.Input.CONTROLLER, info.input());
		assertTrue(info.has(ClientInfo.Field.INPUT));
		assertFalse(info.has(ClientInfo.Field.MODS));
	}

	@Test
	void identityProofsAreCheckedAgainstPlayerNonceAndSignature() throws Exception {
		TehoCoreTest.FakeBridge bridge = new TehoCoreTest.FakeBridge();
		TehoCore core = new TehoCore(bridge);
		KeyPair pair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
		core.verifier().trust("test", Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
		UUID id = UUID.randomUUID();
		core.onMessage(id, hello(TehoProtocol.ASSERT));
		CompletableFuture<Proof> good = core.player(id).get().verify().identity();
		AssertRequest request = bridge.of(AssertRequest.class).get(0);
		core.onMessage(id, frame(new AssertResult(request.requestId, token(pair, id, request.nonce, "teho-assert"), "")));
		Proof proof = good.get(1, TimeUnit.SECONDS);
		assertTrue(proof.valid(), proof.toString());
		assertEquals("Tester", proof.name());

		CompletableFuture<Proof> replay = core.player(id).get().verify().identity();
		AssertRequest second = bridge.of(AssertRequest.class).get(1);
		core.onMessage(id, frame(new AssertResult(second.requestId, token(pair, id, request.nonce, "teho-assert"), "")));
		assertEquals("nonce mismatch", replay.get(1, TimeUnit.SECONDS).error());

		CompletableFuture<Proof> stranger = core.player(id).get().verify().identity();
		AssertRequest third = bridge.of(AssertRequest.class).get(2);
		core.onMessage(id, frame(new AssertResult(third.requestId, token(pair, UUID.randomUUID(), third.nonce, "teho-assert"), "")));
		assertEquals("token is for another player", stranger.get(1, TimeUnit.SECONDS).error());

		CompletableFuture<Proof> forged = core.player(id).get().verify().identity();
		AssertRequest fourth = bridge.of(AssertRequest.class).get(3);
		KeyPair other = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
		core.onMessage(id, frame(new AssertResult(fourth.requestId, token(other, id, fourth.nonce, "teho-assert"), "")));
		assertEquals("bad signature", forged.get(1, TimeUnit.SECONDS).error());
	}

	private static String token(KeyPair pair, UUID player, String nonce, String purpose) throws Exception {
		long now = System.currentTimeMillis() / 1000;
		String claims = "{\"purpose\":\"" + purpose + "\",\"uuid\":\"" + player + "\",\"name\":\"Tester\",\"nonce\":\"" + nonce
				+ "\",\"iat\":" + now + ",\"exp\":" + (now + 300) + ",\"kid\":\"test\"}";
		String blob = Base64.getEncoder().encodeToString(claims.getBytes(StandardCharsets.UTF_8));
		Signature signer = Signature.getInstance("Ed25519");
		signer.initSign(pair.getPrivate());
		signer.update(blob.getBytes(StandardCharsets.US_ASCII));
		String sig = Base64.getEncoder().encodeToString(signer.sign());
		String wrapper = "{\"blob\":\"" + blob + "\",\"sig\":\"" + sig + "\",\"kid\":\"test\"}";
		return Base64.getUrlEncoder().withoutPadding().encodeToString(wrapper.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void tabListsAndSidebarsMergeUpdatesAndReplayAsOneReplace() {
		TehoCoreTest.FakeBridge bridge = new TehoCoreTest.FakeBridge();
		TehoCore core = new TehoCore(bridge);
		UUID id = UUID.randomUUID();
		core.onMessage(id, hello(TehoProtocol.PLAYERS, TehoProtocol.HUD));
		TehoPlayer player = core.player(id).get();
		UUID a = UUID.randomUUID();
		UUID b = UUID.randomUUID();
		player.players().tabList(TabList.replace().header("Bedwars").entry(TabList.Entry.of(a).display("Red | A")).build());
		player.players().tabList(TabList.update().entry(TabList.Entry.of(b).display("Blue | B")).remove(a).build());
		player.hud().sidebar(Sidebar.replace("BEDWARS").row(Sidebar.Row.of("bed", "Red bed").value("alive")).build());
		player.hud().sidebar(Sidebar.update().row(Sidebar.Row.of("kills", "Kills").value("3")).build());
		bridge.sent.clear();
		core.refresh(id);
		dev.tehoclient.api.protocol.TabList tab = bridge.of(dev.tehoclient.api.protocol.TabList.class).get(0);
		assertTrue(tab.replace);
		assertEquals(1, tab.entries.size());
		assertEquals(b.toString(), tab.entries.get(0).player);
		assertEquals(Collections.singletonList("Bedwars"), tab.header);
		SidebarSet sidebar = bridge.of(SidebarSet.class).get(0);
		assertEquals("BEDWARS", sidebar.title);
		assertEquals(2, sidebar.rows.size());
		player.hud().clearSidebar();
		bridge.sent.clear();
		core.refresh(id);
		assertTrue(bridge.of(SidebarSet.class).isEmpty());
		assertTrue(bridge.sent.contains(Signal.RESET));
	}

	@Test
	void featuresTheClientLacksAreNotSent() {
		TehoCoreTest.FakeBridge bridge = new TehoCoreTest.FakeBridge();
		TehoCore core = new TehoCore(bridge);
		UUID id = UUID.randomUUID();
		core.onMessage(id, hello(TehoProtocol.NOTIFY));
		TehoPlayer player = core.player(id).get();
		assertFalse(player.world().beam(Beam.at("x", 0, 0, 0).build()));
		assertFalse(player.camera().shake(1, 500, 20));
		assertFalse(player.ui().open(Screen.modal("m", "M").add(Node.text("t", "hi")).build()));
		assertFalse(player.verify().identity().join().valid());
	}
}
