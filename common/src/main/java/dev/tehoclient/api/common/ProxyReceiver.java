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

import java.util.UUID;

public final class ProxyReceiver {
	private static final long WARN_EVERY_MS = 10_000;

	private final TehoCore core;
	private volatile ProxyKey key;
	private volatile long lastWarning;

	public ProxyReceiver(TehoCore core, ProxyKey key) {
		this.core = core;
		this.key = key;
	}

	public void key(ProxyKey key) {
		this.key = key;
	}

	public boolean enabled() {
		return key != null;
	}

	public boolean onProxyFrame(UUID player, byte[] sealed) {
		ProxyKey current = key;
		if (current == null) {
			return false;
		}
		long now = System.currentTimeMillis();
		ProxyMessage message;
		try {
			message = current.open(sealed, player, now);
		} catch (ProxyKey.ProxyRejected rejected) {
			if (core.debug() || now - lastWarning > WARN_EVERY_MS) {
				lastWarning = now;
				core.bridge().info("Rejected a " + ProxyMessage.CHANNEL + " message for " + core.bridge().name(player)
						+ ": " + rejected.getMessage() + ". Check that the proxy and this server share the same secret.");
			}
			return false;
		}
		if (core.debug()) {
			core.bridge().info("<= proxy " + message);
		}
		if (message.kind == ProxyMessage.HANDOFF) {
			core.attach(player, message.hello(), message.enabled, message.session, message.protocol);
		} else {
			core.onRelayed(player, message.frame);
		}
		return true;
	}
}
