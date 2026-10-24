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
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class TokenVerifier {
	public static final class Claims {
		public final boolean valid;
		public final Map<String, Object> claims;
		public final String error;

		Claims(boolean valid, Map<String, Object> claims, String error) {
			this.valid = valid;
			this.claims = claims;
			this.error = error;
		}

		static Claims bad(String error) {
			return new Claims(false, new LinkedHashMap<String, Object>(), error);
		}
	}

	private static final Map<String, String> BAKED = new LinkedHashMap<>();

	static {
		BAKED.put("ab58118d", "MCowBQYDK2VwAyEA+v4np3GirIAFztYptygktdweagxZhTlerFAqJtW4L4o=");
	}

	private final Backend backend;
	private final Map<String, String> keys = new LinkedHashMap<>(BAKED);
	private volatile long fetched;

	public TokenVerifier(Backend backend) {
		this.backend = backend;
	}

	public void trust(String kid, String spki) {
		synchronized (keys) {
			keys.put(kid, spki);
		}
	}

	public static boolean offlineCapable() {
		try {
			KeyFactory.getInstance("Ed25519");
			return true;
		} catch (Exception unavailable) {
			return false;
		}
	}

	public CompletableFuture<Claims> verify(final String token, final String purpose) {
		final Map<String, Object> wrapper;
		final Map<String, Object> claims;
		try {
			wrapper = Json.object(new String(Base64.getUrlDecoder().decode(strip(token)), StandardCharsets.UTF_8));
			claims = Json.object(new String(Base64.getDecoder().decode(Json.string(wrapper, "blob")), StandardCharsets.UTF_8));
		} catch (IllegalArgumentException malformed) {
			return CompletableFuture.completedFuture(Claims.bad("malformed token"));
		}
		if (!purpose.equals(Json.string(claims, "purpose"))) {
			return CompletableFuture.completedFuture(Claims.bad("wrong purpose"));
		}
		long now = System.currentTimeMillis() / 1000L;
		if (Json.number(claims, "exp") < now) {
			return CompletableFuture.completedFuture(Claims.bad("expired"));
		}
		if (Json.number(claims, "iat") > now + 120) {
			return CompletableFuture.completedFuture(Claims.bad("issued in the future"));
		}
		if (!offlineCapable()) {
			return online(token, purpose, claims);
		}
		final String kid = Json.string(wrapper, "kid");
		return keyFor(kid).thenApply(spki -> {
			if (spki == null) {
				return Claims.bad("unknown signing key " + kid);
			}
			try {
				PublicKey key = KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(spki)));
				Signature signature = Signature.getInstance("Ed25519");
				signature.initVerify(key);
				signature.update(Json.string(wrapper, "blob").getBytes(StandardCharsets.US_ASCII));
				boolean ok = signature.verify(Base64.getDecoder().decode(Json.string(wrapper, "sig")));
				return ok ? new Claims(true, claims, "") : Claims.bad("bad signature");
			} catch (Exception failed) {
				return Claims.bad("could not verify: " + failed.getClass().getSimpleName());
			}
		});
	}

	private static String strip(String token) {
		return token == null ? "" : token.trim();
	}

	private CompletableFuture<String> keyFor(String kid) {
		synchronized (keys) {
			String known = keys.get(kid);
			if (known != null || System.currentTimeMillis() - fetched < 300_000L) {
				return CompletableFuture.completedFuture(known);
			}
			fetched = System.currentTimeMillis();
		}
		return backend.get("/v1/api/keys").handle((response, failure) -> {
			if (response != null && response.ok()) {
				Object list = response.body.get("keys");
				if (list instanceof java.util.List) {
					synchronized (keys) {
						for (Object entry : (java.util.List<?>) list) {
							if (entry instanceof Map) {
								@SuppressWarnings("unchecked")
								Map<String, Object> key = (Map<String, Object>) entry;
								String id = Json.string(key, "kid");
								String spki = Json.string(key, "spki");
								if (!id.isEmpty() && !spki.isEmpty()) {
									keys.put(id, spki);
								}
							}
						}
					}
				}
			}
			synchronized (keys) {
				return keys.get(kid);
			}
		});
	}

	private CompletableFuture<Claims> online(String token, String purpose, Map<String, Object> claims) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("token", token);
		body.put("purpose", purpose);
		return backend.post("/v1/api/verify", body).handle((response, failure) -> {
			if (response == null) {
				return Claims.bad("verification service unreachable");
			}
			if (!response.ok() || !Json.bool(response.body, "valid")) {
				return Claims.bad(response.error());
			}
			return new Claims(true, claims, "");
		});
	}
}
