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

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import dev.tehoclient.api.protocol.ProtocolException;

public final class ProxyKey {
	public static final int MIN_SECRET = 16;
	public static final long MAX_SKEW_MS = 30_000;
	public static final int MAC_BYTES = 32;
	private static final int MAX_REMEMBERED = 65536;
	private static final byte[] DOMAIN = "teho:proxy/1\0".getBytes(StandardCharsets.UTF_8);

	private final SecretKeySpec key;
	private final Map<Long, Long> seen = new LinkedHashMap<>();

	private ProxyKey(byte[] secret) {
		this.key = new SecretKeySpec(secret, "HmacSHA256");
	}

	public static ProxyKey of(String secret) {
		if (secret == null || secret.trim().length() < MIN_SECRET) {
			throw new IllegalArgumentException("the proxy secret must be at least " + MIN_SECRET + " characters");
		}
		return new ProxyKey(secret.trim().getBytes(StandardCharsets.UTF_8));
	}

	public static ProxyKey orNull(String secret) {
		return secret == null || secret.trim().isEmpty() ? null : of(secret);
	}

	public byte[] seal(ProxyMessage message) {
		byte[] body = message.encode();
		byte[] out = Arrays.copyOf(body, body.length + MAC_BYTES);
		System.arraycopy(mac(body, body.length), 0, out, body.length, MAC_BYTES);
		return out;
	}

	public ProxyMessage open(byte[] sealed, UUID expected, long now) throws ProxyRejected {
		if (sealed == null || sealed.length <= MAC_BYTES || sealed.length > ProxyMessage.MAX_BYTES) {
			throw new ProxyRejected("bad length");
		}
		int length = sealed.length - MAC_BYTES;
		byte[] claimed = Arrays.copyOfRange(sealed, length, sealed.length);
		if (!MessageDigest.isEqual(claimed, mac(sealed, length))) {
			throw new ProxyRejected("bad signature");
		}
		ProxyMessage message;
		try {
			message = ProxyMessage.decode(Arrays.copyOf(sealed, length));
		} catch (ProtocolException malformed) {
			throw new ProxyRejected("malformed: " + malformed.getMessage());
		}
		if (!message.player.equals(expected)) {
			throw new ProxyRejected("signed for another player");
		}
		if (Math.abs(now - message.issuedAt) > MAX_SKEW_MS) {
			throw new ProxyRejected("expired");
		}
		if (!remember(message.nonce, now)) {
			throw new ProxyRejected("replayed");
		}
		return message;
	}

	private synchronized boolean remember(long nonce, long now) {
		Iterator<Long> expiries = seen.values().iterator();
		while (expiries.hasNext() && (expiries.next() < now || seen.size() >= MAX_REMEMBERED)) {
			expiries.remove();
		}
		if (seen.containsKey(nonce)) {
			return false;
		}
		seen.put(nonce, now + 2 * MAX_SKEW_MS);
		return true;
	}

	private byte[] mac(byte[] data, int length) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(key);
			mac.update(DOMAIN);
			mac.update(data, 0, length);
			return mac.doFinal();
		} catch (GeneralSecurityException missing) {
			throw new IllegalStateException("HmacSHA256 is unavailable", missing);
		}
	}

	public static final class ProxyRejected extends Exception {
		private static final long serialVersionUID = 1L;

		public ProxyRejected(String reason) {
			super(reason, null, false, false);
		}
	}
}
