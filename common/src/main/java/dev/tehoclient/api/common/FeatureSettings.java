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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.modules.CombatRules;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.social.Presence;

public final class FeatureSettings {
	private static final String[][] FEATURES = {
			{"world", TehoProtocol.WORLD}, {"players", TehoProtocol.PLAYERS}, {"keybinds", TehoProtocol.KEYBIND},
			{"camera", TehoProtocol.CAMERA}, {"audio", TehoProtocol.AUDIO}, {"social", TehoProtocol.SOCIAL}, {"ui", TehoProtocol.UI},
			{"info", TehoProtocol.INFO}, {"identity", TehoProtocol.ASSERT},
			{"integrity", TehoProtocol.ATTEST}, {"pack", TehoProtocol.PACK}, {"clips", TehoProtocol.CLIPS},
			{"broadcast", TehoProtocol.BROADCAST}, {"statues", TehoProtocol.STATUE},
			{"branding", TehoProtocol.BRANDING}, {"showcase", TehoProtocol.SHOWCASE}, {"assets", TehoProtocol.ASSET}};

	private String presenceGame = "";
	private String presenceState = "";
	private final Map<String, String> links = new LinkedHashMap<>();
	private String packUrl = "";
	private String packSha1 = "";
	private boolean packRequired;
	private boolean legacyCombat;

	public static List<String> features(Map<?, ?> config) {
		Map<?, ?> features = section(config, "features");
		List<String> out = new ArrayList<>();
		for (String[] feature : FEATURES) {
			if (bool(features.get(feature[0]), true)) {
				out.add(feature[1]);
			}
		}
		return out;
	}

	public static FeatureSettings load(TehoCore core, Map<?, ?> root, Consumer<String> warn) {
		Map<?, ?> config = root == null ? Collections.emptyMap() : root;
		FeatureSettings settings = new FeatureSettings();
		Map<?, ?> backend = section(config, "backend");
		try {
			core.backend().configure(string(backend.get("url"), ""), string(backend.get("key"), ""));
		} catch (IllegalArgumentException bad) {
			warn.accept("Ignoring backend.url: " + bad.getMessage());
		}
		String certificate = string(section(config, "verified").get("certificate"), "");
		core.verified(certificate);
		if (certificate.isEmpty() && core.backend().hasKey()) {
			core.backend().get("/v1/servers/certificate").thenAccept(response -> {
				String fetched = Json.string(response.body, "certificate");
				if (response.ok() && !fetched.isEmpty()) {
					core.verified(fetched);
				}
			});
		}
		Map<?, ?> presence = section(config, "presence");
		settings.presenceGame = string(presence.get("game"), "");
		settings.presenceState = string(presence.get("state"), "");
		Map<?, ?> links = section(config, "links");
		for (Map.Entry<?, ?> link : links.entrySet()) {
			String url = string(link.getValue(), "");
			if (url.startsWith("https://")) {
				settings.links.put(String.valueOf(link.getKey()), url);
			} else if (!url.isEmpty()) {
				warn.accept("Ignoring link " + link.getKey() + ": links must be https");
			}
		}
		Map<?, ?> pack = section(config, "resource-pack");
		settings.packUrl = string(pack.get("url"), "");
		settings.packSha1 = string(pack.get("sha1"), "");
		settings.packRequired = bool(pack.get("required"), false);
		if (!settings.packUrl.isEmpty() && (!settings.packUrl.startsWith("https://") || !settings.packSha1.matches("[0-9a-fA-F]{40}"))) {
			warn.accept("Ignoring resource-pack: it needs an https url and a 40-character sha1");
			settings.packUrl = "";
		}
		settings.legacyCombat = bool(section(config, "combat").get("legacy-pvp"), false);
		return settings;
	}

	public void onReady(TehoPlayer player) {
		if (!presenceGame.isEmpty()) {
			player.social().presence(Presence.builder(presenceGame).state(presenceState).startedNow().build());
		}
		if (!links.isEmpty()) {
			player.social().links(links);
		}
		if (!packUrl.isEmpty()) {
			player.pack().hint("server", packUrl, packSha1, 0, packRequired, "");
		}
		if (legacyCombat) {
			player.modules().combat(CombatRules.LEGACY_PVP);
		}
	}

	static Map<?, ?> section(Map<?, ?> config, String key) {
		Object value = config.get(key);
		return value instanceof Map ? (Map<?, ?>) value : Collections.emptyMap();
	}

	static List<?> list(Object value) {
		return value instanceof List ? (List<?>) value : Collections.emptyList();
	}

	static String string(Object value, String fallback) {
		return value == null ? fallback : String.valueOf(value).trim();
	}

	static boolean bool(Object value, boolean fallback) {
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));
	}

	static int integer(Object value, int fallback) {
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		try {
			return value == null ? fallback : Integer.parseInt(String.valueOf(value).trim());
		} catch (NumberFormatException bad) {
			return fallback;
		}
	}
}
