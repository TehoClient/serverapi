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

import java.security.SecureRandom;
import java.util.UUID;

import dev.tehoclient.api.protocol.ProtocolException;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.protocol.WireReader;

public final class TehoProxy {
	public static final long RESET_REAPPLY_MS = 100;

	public interface Backends {
		boolean connected(UUID player);

		boolean sendToBackend(UUID player, byte[] message);
	}

	private final TehoCore core;
	private final Backends backends;
	private final SecureRandom random = new SecureRandom();
	private volatile ProxyKey key;
	private volatile String network = "";

	public TehoProxy(TehoCore core, ProxyKey key, Backends backends) {
		this.core = core;
		this.key = key;
		this.backends = backends;
		core.addHandshakeHook(this::handoff);
	}

	public TehoCore core() {
		return core;
	}

	public void configure(ProxyKey key, String network) {
		this.key = key;
		this.network = network == null ? "" : network;
	}

	public boolean signing() {
		return key != null;
	}

	public void onClientFrame(UUID player, byte[] frame) {
		core.onMessage(player, frame);
		ProxyKey current = key;
		if (current == null || type(frame) == TehoProtocol.HELLO || !core.session(player).isPresent()
				|| !backends.connected(player)) {
			return;
		}
		backends.sendToBackend(player, current.seal(ProxyMessage.relay(player, System.currentTimeMillis(), random.nextLong(), frame)));
	}

	public void onBackendFrame(UUID player, byte[] frame) {
		if (type(frame) == TehoProtocol.RESET && core.session(player).isPresent()) {
			core.bridge().runLater(player, () -> core.reapply(player), RESET_REAPPLY_MS);
		}
	}

	public void onBackendConnected(UUID player, boolean switched) {
		if (switched) {
			core.refresh(player);
		}
		core.session(player).ifPresent(this::handoff);
	}

	public void onQuit(UUID player) {
		core.onQuit(player);
	}

	void handoff(TehoSession session) {
		ProxyKey current = key;
		UUID player = session.uniqueId();
		if (current == null || !backends.connected(player)) {
			return;
		}
		ProxyMessage message = ProxyMessage.handoff(player, System.currentTimeMillis(), random.nextLong(),
				session.protocol(), session.session(), session.capabilities(), network, session.hello());
		if (core.debug()) {
			core.bridge().info("=> backend " + message);
		}
		backends.sendToBackend(player, current.seal(message));
	}

	static int type(byte[] frame) {
		if (frame == null || frame.length == 0) {
			return -1;
		}
		try {
			long type = new WireReader(frame).rawVarint();
			return type < 0 || type > Integer.MAX_VALUE ? -1 : (int) type;
		} catch (ProtocolException malformed) {
			return -1;
		}
	}
}
