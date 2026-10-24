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
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.modules.ModuleRules;
import dev.tehoclient.api.modules.SettingCap;
import dev.tehoclient.api.notify.Notification;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.staff.StaffGrant;
import dev.tehoclient.api.staff.StaffTool;
import dev.tehoclient.api.staff.XrayBlock;

public final class NetworkPresets {
	private String serverName = "";
	private String grant = "";
	private boolean debug;
	private String secret = "";
	private final List<String> features = new ArrayList<>();
	private final Map<String, Set<StaffTool>> staffGrants = new LinkedHashMap<>();
	private final List<XrayBlock> xrayBlocks = new ArrayList<>();
	private String staffReason = "Staff tools";
	private int staffRadius = 48;
	private ModuleRules rules = ModuleRules.NONE;
	private boolean welcome;
	private String welcomeTitle = "";
	private String welcomeBody = "";

	public static NetworkPresets load(Map<?, ?> root, Consumer<String> warn) {
		NetworkPresets presets = new NetworkPresets();
		Map<?, ?> config = root == null ? Collections.emptyMap() : root;
		presets.serverName = string(config.get("server-name"), "");
		presets.debug = bool(config.get("debug"), false);
		presets.secret = string(section(config, "proxy").get("secret"), "");
		presets.grant = string(section(config, "private").get("grant"), "");
		Map<?, ?> features = section(config, "features");
		addIf(presets.features, features, "notifications", TehoProtocol.NOTIFY);
		addIf(presets.features, features, "waypoints", TehoProtocol.WAYPOINT);
		addIf(presets.features, features, "modules", TehoProtocol.MODULES);
		addIf(presets.features, features, "staff", TehoProtocol.STAFF);
		addIf(presets.features, features, "hud", TehoProtocol.HUD);
		addIf(presets.features, features, "suspect", TehoProtocol.SUSPECT);
		addIf(presets.features, features, "npcs", TehoProtocol.NPC);
		presets.features.addAll(FeatureSettings.features(config));
		Map<?, ?> welcome = section(config, "welcome");
		presets.welcome = bool(welcome.get("enabled"), false);
		presets.welcomeTitle = AdminCommand.colors(string(welcome.get("title"), ""));
		presets.welcomeBody = AdminCommand.colors(string(welcome.get("body"), ""));
		Map<?, ?> staff = section(config, "staff");
		presets.staffReason = string(staff.get("reason"), "Staff tools");
		presets.staffRadius = integer(staff.get("radius"), 48);
		for (Object line : list(staff.get("xray-blocks"))) {
			String[] parts = String.valueOf(line).trim().split("\\s+");
			try {
				presets.xrayBlocks.add(XrayBlock.of(parts[0], parts.length > 1 ? hex(parts[1]) : 0xFFFFFF));
			} catch (RuntimeException bad) {
				warn.accept("Ignoring xray block '" + line + "': " + bad.getMessage());
			}
		}
		for (Object raw : list(staff.get("grants"))) {
			if (!(raw instanceof Map)) {
				continue;
			}
			Map<?, ?> entry = (Map<?, ?>) raw;
			Object permission = entry.get("permission");
			if (permission == null) {
				continue;
			}
			Set<StaffTool> tools = EnumSet.noneOf(StaffTool.class);
			for (Object tool : list(entry.get("tools"))) {
				StaffTool parsed = StaffTool.byId(String.valueOf(tool));
				if (parsed == null) {
					warn.accept("Unknown staff tool '" + tool + "' for " + permission);
				} else {
					tools.add(parsed);
				}
			}
			if (!tools.isEmpty()) {
				presets.staffGrants.put(String.valueOf(permission), tools);
			}
		}
		presets.rules = rules(section(section(config, "rules"), "global"), warn);
		return presets;
	}

	public String grant() {
		return grant;
	}

	public String serverName() {
		return serverName;
	}

	public boolean debug() {
		return debug;
	}

	public String secret() {
		return secret;
	}

	public List<String> features() {
		return Collections.unmodifiableList(features);
	}

	public ModuleRules rules() {
		return rules;
	}

	public void onReady(TehoPlayer teho, Predicate<String> hasPermission) {
		if (welcome && !welcomeTitle.isEmpty()) {
			teho.notify(Notification.builder("tehoapi-network-welcome")
					.title(welcomeTitle.replace("{player}", teho.name()))
					.body(welcomeBody.replace("{player}", teho.name()))
					.duration(8000)
					.build());
		}
		apply(teho, hasPermission);
	}

	public void apply(TehoPlayer teho, Predicate<String> hasPermission) {
		if (!rules.isEmpty() || !teho.modules().rules().isEmpty()) {
			teho.modules().apply(rules);
		}
		Set<StaffTool> tools = EnumSet.noneOf(StaffTool.class);
		for (Map.Entry<String, Set<StaffTool>> entry : staffGrants.entrySet()) {
			if (hasPermission.test(entry.getKey())) {
				tools.addAll(entry.getValue());
			}
		}
		if (tools.isEmpty()) {
			if (!teho.staff().granted().isEmpty()) {
				teho.staff().revokeAll();
			}
			return;
		}
		StaffGrant.Builder grant = StaffGrant.of(tools).reason(staffReason).radius(staffRadius);
		if (!xrayBlocks.isEmpty()) {
			grant.xray(xrayBlocks);
		}
		teho.staff().grant(grant.build());
	}

	private static ModuleRules rules(Map<?, ?> section, Consumer<String> warn) {
		if (section.isEmpty()) {
			return ModuleRules.NONE;
		}
		ModuleRules.Builder builder = ModuleRules.builder();
		builder.disable(strings(section.get("disable")));
		for (String category : strings(section.get("disable-categories"))) {
			builder.disableCategory(category);
		}
		if (section.containsKey("competitive")) {
			if (bool(section.get("competitive"), false)) {
				builder.competitive(strings(section.get("allow")));
			} else {
				builder.casual();
			}
		}
		if (section.containsKey("reason")) {
			builder.reason(string(section.get("reason"), ""));
		}
		for (Object raw : list(section.get("caps"))) {
			if (!(raw instanceof Map)) {
				continue;
			}
			Map<?, ?> cap = (Map<?, ?>) raw;
			try {
				String module = String.valueOf(cap.get("module"));
				String key = String.valueOf(cap.get("key"));
				if (cap.containsKey("value")) {
					builder.cap(SettingCap.pin(module, key, String.valueOf(cap.get("value"))));
				} else {
					int min = cap.containsKey("min") ? Integer.parseInt(String.valueOf(cap.get("min"))) : Integer.MIN_VALUE;
					int max = cap.containsKey("max") ? Integer.parseInt(String.valueOf(cap.get("max"))) : Integer.MAX_VALUE;
					builder.cap(SettingCap.range(module, key, min, max));
				}
			} catch (RuntimeException bad) {
				warn.accept("Ignoring setting cap " + cap + ": " + bad.getMessage());
			}
		}
		return builder.build();
	}

	private static void addIf(List<String> features, Map<?, ?> section, String key, String capability) {
		if (bool(section.get(key), true)) {
			features.add(capability);
		}
	}

	private static Map<?, ?> section(Map<?, ?> parent, String key) {
		Object value = parent.get(key);
		return value instanceof Map ? (Map<?, ?>) value : Collections.emptyMap();
	}

	private static List<?> list(Object value) {
		return value instanceof List ? (List<?>) value : Collections.emptyList();
	}

	private static List<String> strings(Object value) {
		List<String> out = new ArrayList<>();
		for (Object item : list(value)) {
			out.add(String.valueOf(item));
		}
		return out;
	}

	private static String string(Object value, String fallback) {
		return value == null ? fallback : String.valueOf(value);
	}

	private static boolean bool(Object value, boolean fallback) {
		return value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));
	}

	private static int integer(Object value, int fallback) {
		try {
			return value == null ? fallback : Integer.parseInt(String.valueOf(value));
		} catch (NumberFormatException bad) {
			return fallback;
		}
	}

	static int hex(String text) {
		String value = text.startsWith("#") ? text.substring(1) : text;
		return Integer.parseInt(value, 16) & 0xFFFFFF;
	}
}
