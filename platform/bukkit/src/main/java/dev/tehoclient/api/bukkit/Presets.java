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

package dev.tehoclient.api.bukkit;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.modules.ModuleRules;
import dev.tehoclient.api.modules.SettingCap;
import dev.tehoclient.api.notify.Notification;
import dev.tehoclient.api.staff.StaffGrant;
import dev.tehoclient.api.staff.StaffTool;
import dev.tehoclient.api.staff.XrayBlock;

final class Presets {
	private final Map<String, Set<StaffTool>> staffGrants = new LinkedHashMap<>();
	private final List<XrayBlock> xrayBlocks = new ArrayList<>();
	private final List<XrayBlock> storageBlocks = new ArrayList<>();
	private final List<XrayBlock> entityTypes = new ArrayList<>();
	private final Map<String, ModuleRules> worldRules = new LinkedHashMap<>();
	private ModuleRules globalRules = ModuleRules.NONE;
	private String staffReason = "";
	private int staffRadius = 48;
	private boolean welcome;
	private String welcomeTitle = "";
	private String welcomeBody = "";
	private boolean deathWaypoint;

	void load(FileConfiguration config, java.util.logging.Logger log) {
		staffGrants.clear();
		xrayBlocks.clear();
		storageBlocks.clear();
		entityTypes.clear();
		worldRules.clear();
		welcome = config.getBoolean("welcome.enabled", false);
		welcomeTitle = color(config.getString("welcome.title", ""));
		welcomeBody = color(config.getString("welcome.body", ""));
		deathWaypoint = config.getBoolean("death-waypoint", false);
		staffReason = config.getString("staff.reason", "Staff tools");
		staffRadius = config.getInt("staff.radius", 48);
		blocks(config.getStringList("staff.xray-blocks"), xrayBlocks, log);
		blocks(config.getStringList("staff.storage-blocks"), storageBlocks, log);
		blocks(config.getStringList("staff.entity-types"), entityTypes, log);
		for (Map<?, ?> entry : config.getMapList("staff.grants")) {
			Object permission = entry.get("permission");
			Object tools = entry.get("tools");
			if (permission == null || !(tools instanceof List)) {
				continue;
			}
			Set<StaffTool> set = EnumSet.noneOf(StaffTool.class);
			for (Object tool : (List<?>) tools) {
				StaffTool parsed = StaffTool.byId(String.valueOf(tool));
				if (parsed == null) {
					log.warning("Unknown staff tool '" + tool + "' for " + permission);
				} else {
					set.add(parsed);
				}
			}
			if (!set.isEmpty()) {
				staffGrants.put(String.valueOf(permission), set);
			}
		}
		globalRules = rules(config.getConfigurationSection("rules.global"), ModuleRules.NONE, log);
		ConfigurationSection worlds = config.getConfigurationSection("rules.worlds");
		if (worlds != null) {
			for (String world : worlds.getKeys(false)) {
				worldRules.put(world, rules(worlds.getConfigurationSection(world), globalRules, log));
			}
		}
	}

	void onReady(Player player, TehoPlayer teho) {
		if (welcome && !welcomeTitle.isEmpty()) {
			teho.notify(Notification.builder("tehoapi-welcome")
					.title(welcomeTitle.replace("{player}", player.getName()))
					.body(welcomeBody.replace("{player}", player.getName()))
					.duration(8000)
					.build());
		}
		applyRules(player, teho);
		applyStaff(player, teho);
	}

	void applyRules(Player player, TehoPlayer teho) {
		ModuleRules rules = worldRules.get(player.getWorld().getName());
		teho.modules().apply(rules == null ? globalRules : rules);
	}

	void applyStaff(Player player, TehoPlayer teho) {
		Set<StaffTool> tools = EnumSet.noneOf(StaffTool.class);
		for (Map.Entry<String, Set<StaffTool>> entry : staffGrants.entrySet()) {
			if (player.hasPermission(entry.getKey())) {
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
		if (!storageBlocks.isEmpty()) {
			grant.storage(storageBlocks);
		}
		if (!entityTypes.isEmpty()) {
			grant.entities(entityTypes);
		}
		teho.staff().grant(grant.build());
	}

	boolean deathWaypoint() {
		return deathWaypoint;
	}

	private static ModuleRules rules(ConfigurationSection section, ModuleRules base, java.util.logging.Logger log) {
		if (section == null) {
			return base;
		}
		ModuleRules.Builder builder = base.toBuilder();
		builder.disable(section.getStringList("disable"));
		for (String category : section.getStringList("disable-categories")) {
			builder.disableCategory(category);
		}
		if (section.contains("competitive")) {
			if (section.getBoolean("competitive")) {
				builder.competitive(section.getStringList("allow"));
			} else {
				builder.casual();
			}
		}
		if (section.contains("reason")) {
			builder.reason(section.getString("reason"));
		}
		for (Map<?, ?> cap : section.getMapList("caps")) {
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
				log.warning("Ignoring setting cap " + cap + ": " + bad.getMessage());
			}
		}
		return builder.build();
	}

	private static void blocks(List<String> lines, List<XrayBlock> out, java.util.logging.Logger log) {
		for (String line : lines) {
			String[] parts = line.trim().split("\\s+");
			try {
				out.add(XrayBlock.of(parts[0], parts.length > 1 ? hex(parts[1]) : 0xFFFFFF));
			} catch (RuntimeException bad) {
				log.warning("Ignoring block or entity '" + line + "': " + bad.getMessage());
			}
		}
	}

	static int hex(String text) {
		String value = text.startsWith("#") ? text.substring(1) : text;
		return Integer.parseInt(value, 16) & 0xFFFFFF;
	}

	private static String color(String text) {
		return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
	}
}
