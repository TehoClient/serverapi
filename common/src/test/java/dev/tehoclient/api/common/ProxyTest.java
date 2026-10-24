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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import dev.tehoclient.api.TehoListener;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.protocol.Hello;
import dev.tehoclient.api.protocol.Signal;
import dev.tehoclient.api.protocol.StaffGrant;
import dev.tehoclient.api.protocol.StaffState;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.protocol.Welcome;
import dev.tehoclient.api.staff.StaffTool;

class ProxyTest {
	private static final String SECRET = "correct horse battery staple";
	private static final Hello HELLO = new Hello(2, "teho test", "1.21.11", "fabric",
			Arrays.asList(TehoProtocol.NOTIFY, TehoProtocol.STAFF, TehoProtocol.HUD));

	private static ProxyMessage handoff(UUID player, long now) {
		return ProxyMessage.handoff(player, now, new java.util.Random().nextLong(), 2, 42L,
				Arrays.asList(TehoProtocol.NOTIFY, TehoProtocol.STAFF, TehoProtocol.HUD), "Network", HELLO);
	}

	@Test
	void sealedHandoffRoundTrips() throws Exception {
		ProxyKey key = ProxyKey.of(SECRET);
		UUID player = UUID.randomUUID();
		long now = System.currentTimeMillis();
		ProxyMessage opened = key.open(key.seal(handoff(player, now)), player, now);
		assertEquals(ProxyMessage.HANDOFF, opened.kind);
		assertEquals(player, opened.player);
		assertEquals(42L, opened.session);
		assertEquals(2, opened.protocol);
		assertEquals("teho test", opened.client);
		assertEquals(HELLO.capabilities, opened.advertised);
		assertEquals("Network", opened.network);
	}

	@Test
	void relayCarriesTheFrame() throws Exception {
		ProxyKey key = ProxyKey.of(SECRET);
		UUID player = UUID.randomUUID();
		long now = System.currentTimeMillis();
		byte[] frame = TehoProtocol.encode(new StaffState("xray", true));
		ProxyMessage opened = key.open(key.seal(ProxyMessage.relay(player, now, 7L, frame)), player, now);
		assertEquals(ProxyMessage.RELAY, opened.kind);
		assertArrayEquals(frame, opened.frame);
	}

	@Test
	void tamperedMessageIsRejected() {
		ProxyKey key = ProxyKey.of(SECRET);
		UUID player = UUID.randomUUID();
		long now = System.currentTimeMillis();
		byte[] sealed = key.seal(handoff(player, now));
		sealed[sealed.length / 2] ^= 0x01;
		ProxyKey.ProxyRejected rejected = assertThrows(ProxyKey.ProxyRejected.class, () -> key.open(sealed, player, now));
		assertEquals("bad signature", rejected.getMessage());
	}

	@Test
	void wrongSecretIsRejected() {
		UUID player = UUID.randomUUID();
		long now = System.currentTimeMillis();
		byte[] sealed = ProxyKey.of("an attacker guessed this").seal(handoff(player, now));
		assertThrows(ProxyKey.ProxyRejected.class, () -> ProxyKey.of(SECRET).open(sealed, player, now));
	}

	@Test
	void truncatedOrForgedMacIsRejected() {
		ProxyKey key = ProxyKey.of(SECRET);
		UUID player = UUID.randomUUID();
		long now = System.currentTimeMillis();
		byte[] sealed = key.seal(handoff(player, now));
		assertThrows(ProxyKey.ProxyRejected.class, () -> key.open(Arrays.copyOf(sealed, sealed.length - 1), player, now));
		assertThrows(ProxyKey.ProxyRejected.class, () -> key.open(new byte[ProxyKey.MAC_BYTES], player, now));
		byte[] unsigned = handoff(player, now).encode();
		assertThrows(ProxyKey.ProxyRejected.class,
				() -> key.open(Arrays.copyOf(unsigned, unsigned.length + ProxyKey.MAC_BYTES), player, now));
	}

	@Test
	void messageForAnotherPlayerIsRejected() {
		ProxyKey key = ProxyKey.of(SECRET);
		long now = System.currentTimeMillis();
		byte[] sealed = key.seal(handoff(UUID.randomUUID(), now));
		ProxyKey.ProxyRejected rejected = assertThrows(ProxyKey.ProxyRejected.class,
				() -> key.open(sealed, UUID.randomUUID(), now));
		assertEquals("signed for another player", rejected.getMessage());
	}

	@Test
	void staleMessageIsRejected() {
		ProxyKey key = ProxyKey.of(SECRET);
		UUID player = UUID.randomUUID();
		long now = System.currentTimeMillis();
		byte[] sealed = key.seal(handoff(player, now - ProxyKey.MAX_SKEW_MS - 1));
		ProxyKey.ProxyRejected rejected = assertThrows(ProxyKey.ProxyRejected.class, () -> key.open(sealed, player, now));
		assertEquals("expired", rejected.getMessage());
	}

	@Test
	void replayIsRejected() throws Exception {
		ProxyKey key = ProxyKey.of(SECRET);
		UUID player = UUID.randomUUID();
		long now = System.currentTimeMillis();
		byte[] sealed = key.seal(handoff(player, now));
		key.open(sealed, player, now);
		ProxyKey.ProxyRejected rejected = assertThrows(ProxyKey.ProxyRejected.class, () -> key.open(sealed, player, now + 5));
		assertEquals("replayed", rejected.getMessage());
	}

	@Test
	void weakSecretIsRefused() {
		assertThrows(IllegalArgumentException.class, () -> ProxyKey.of("short"));
		assertEquals(null, ProxyKey.orNull("  "));
	}

	@Test
	void receiverRejectsBadSignatureWithoutCreatingASession() {
		TehoCoreTest.FakeBridge bridge = new TehoCoreTest.FakeBridge();
		TehoCore backend = new TehoCore(bridge);
		ProxyReceiver receiver = new ProxyReceiver(backend, ProxyKey.of(SECRET));
		UUID player = UUID.randomUUID();
		byte[] forged = ProxyKey.of("someone else's secret!").seal(handoff(player, System.currentTimeMillis()));
		assertFalse(receiver.onProxyFrame(player, forged));
		assertFalse(backend.player(player).isPresent());
		assertTrue(bridge.sent.isEmpty());
	}

	@Test
	void receiverIgnoresEverythingWithoutASecret() {
		TehoCore backend = new TehoCore(new TehoCoreTest.FakeBridge());
		ProxyReceiver receiver = new ProxyReceiver(backend, null);
		UUID player = UUID.randomUUID();
		assertFalse(receiver.onProxyFrame(player, ProxyKey.of(SECRET).seal(handoff(player, System.currentTimeMillis()))));
		assertFalse(backend.player(player).isPresent());
	}

	@Test
	void handoffCreatesSessionWithoutWelcome() {
		TehoCoreTest.FakeBridge bridge = new TehoCoreTest.FakeBridge();
		TehoCore backend = new TehoCore(bridge);
		backend.configure("Lobby", Arrays.asList(TehoProtocol.NOTIFY, TehoProtocol.STAFF), false);
		List<TehoPlayer> ready = new ArrayList<>();
		backend.addListener(new TehoListener() {
			@Override
			public void onReady(TehoPlayer player) {
				ready.add(player);
			}
		});
		ProxyKey key = ProxyKey.of(SECRET);
		ProxyReceiver receiver = new ProxyReceiver(backend, key);
		UUID player = UUID.randomUUID();
		assertTrue(receiver.onProxyFrame(player, key.seal(handoff(player, System.currentTimeMillis()))));
		TehoPlayer teho = backend.player(player).get();
		assertEquals(Arrays.asList(TehoProtocol.NOTIFY, TehoProtocol.STAFF), new ArrayList<>(teho.capabilities()));
		assertEquals(42L, teho.session());
		assertEquals("teho test", teho.clientVersion());
		assertTrue(bridge.of(Welcome.class).isEmpty());
		assertEquals(1, ready.size());
		assertTrue(receiver.onProxyFrame(player, key.seal(handoff(player, System.currentTimeMillis()))));
		assertEquals(1, ready.size());
	}

	@Test
	void relayedHelloIsIgnoredButOtherFramesArrive() {
		TehoCoreTest.FakeBridge bridge = new TehoCoreTest.FakeBridge();
		TehoCore backend = new TehoCore(bridge);
		ProxyKey key = ProxyKey.of(SECRET);
		ProxyReceiver receiver = new ProxyReceiver(backend, key);
		UUID player = UUID.randomUUID();
		long now = System.currentTimeMillis();
		assertTrue(receiver.onProxyFrame(player, key.seal(ProxyMessage.relay(player, now, 1L, TehoProtocol.encode(HELLO)))));
		assertFalse(backend.player(player).isPresent());
		assertTrue(bridge.sent.isEmpty());
		receiver.onProxyFrame(player, key.seal(handoff(player, now)));
		TehoPlayer teho = backend.player(player).get();
		teho.staff().grant(dev.tehoclient.api.staff.StaffGrant.of(StaffTool.XRAY).build());
		List<StaffTool> toggled = new ArrayList<>();
		backend.addListener(new TehoListener() {
			@Override
			public void onStaffTool(TehoPlayer who, StaffTool tool, boolean active) {
				toggled.add(tool);
			}
		});
		receiver.onProxyFrame(player, key.seal(ProxyMessage.relay(player, now, 2L,
				TehoProtocol.encode(new StaffState("xray", true)))));
		assertEquals(Arrays.asList(StaffTool.XRAY), toggled);
		assertEquals(42L, teho.session());
	}

	@Test
	void proxyOwnsHandshakeAndHandsOffToBackend() {
		TehoCoreTest.FakeBridge client = new TehoCoreTest.FakeBridge();
		TehoCore network = new TehoCore(client);
		network.configure("Network", TehoProtocol.known(), false);
		Wire wire = new Wire();
		ProxyKey key = ProxyKey.of(SECRET);
		TehoProxy proxy = new TehoProxy(network, key, wire);

		TehoCoreTest.FakeBridge backendClient = new TehoCoreTest.FakeBridge();
		TehoCore backend = new TehoCore(backendClient);
		backend.configure("Lobby", Arrays.asList(TehoProtocol.NOTIFY, TehoProtocol.HUD), false);
		wire.receiver = new ProxyReceiver(backend, ProxyKey.of(SECRET));

		UUID player = UUID.randomUUID();
		proxy.onClientFrame(player, TehoProtocol.encode(HELLO));
		Welcome welcome = client.of(Welcome.class).get(0);
		assertEquals("Network", welcome.serverName);
		assertEquals(Arrays.asList(TehoProtocol.NOTIFY, TehoProtocol.STAFF, TehoProtocol.HUD), welcome.enabled);
		assertEquals(1, wire.delivered);
		TehoPlayer onBackend = backend.player(player).get();
		assertEquals(welcome.session, onBackend.session());
		assertEquals(Arrays.asList(TehoProtocol.NOTIFY, TehoProtocol.HUD), new ArrayList<>(onBackend.capabilities()));
		assertTrue(backendClient.sent.isEmpty());

		proxy.onClientFrame(player, TehoProtocol.encode(new StaffState("xray", true)));
		assertEquals(2, wire.delivered);
		assertEquals(0, wire.rejected);
	}

	@Test
	void backendSwitchResetsAndKeepsNetworkState() {
		TehoCoreTest.FakeBridge client = new TehoCoreTest.FakeBridge();
		TehoCore network = new TehoCore(client);
		Wire wire = new Wire();
		TehoProxy proxy = new TehoProxy(network, ProxyKey.of(SECRET), wire);
		UUID player = UUID.randomUUID();
		proxy.onClientFrame(player, TehoProtocol.encode(HELLO));
		network.player(player).get().staff().grant(dev.tehoclient.api.staff.StaffGrant.of(StaffTool.XRAY).build());
		client.sent.clear();
		int before = wire.handoffs;

		proxy.onBackendConnected(player, true);
		assertTrue(client.sent.get(0) == Signal.RESET);
		assertEquals(1, client.of(StaffGrant.class).size());
		assertEquals(before + 1, wire.handoffs);

		client.sent.clear();
		proxy.onBackendFrame(player, TehoProtocol.encode(Signal.RESET));
		assertEquals(1, client.later.size());
		client.later.get(0).run();
		assertEquals(1, client.of(StaffGrant.class).size());

		client.sent.clear();
		client.later.clear();
		proxy.onBackendFrame(player, TehoProtocol.encode(new dev.tehoclient.api.protocol.Notify("n", "t", "b", 0, 0, false)));
		assertTrue(client.later.isEmpty());
	}

	@Test
	void unsignedProxyStillHandshakesButSendsNothingToBackends() {
		TehoCoreTest.FakeBridge client = new TehoCoreTest.FakeBridge();
		TehoCore network = new TehoCore(client);
		Wire wire = new Wire();
		TehoProxy proxy = new TehoProxy(network, null, wire);
		UUID player = UUID.randomUUID();
		proxy.onClientFrame(player, TehoProtocol.encode(HELLO));
		assertEquals(1, client.of(Welcome.class).size());
		assertEquals(0, wire.sent);
	}

	private static final class Wire implements TehoProxy.Backends {
		ProxyReceiver receiver;
		int sent;
		int handoffs;
		int delivered;
		int rejected;

		@Override
		public boolean connected(UUID player) {
			return true;
		}

		@Override
		public boolean sendToBackend(UUID player, byte[] message) {
			sent++;
			try {
				if (ProxyKey.of(SECRET).open(message, player, System.currentTimeMillis()).kind == ProxyMessage.HANDOFF) {
					handoffs++;
				}
			} catch (ProxyKey.ProxyRejected bad) {
				throw new AssertionError(bad);
			}
			if (receiver != null) {
				if (receiver.onProxyFrame(player, message)) {
					delivered++;
				} else {
					rejected++;
				}
			}
			return true;
		}
	}
}
