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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import dev.tehoclient.api.TehoListener;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.protocol.TehoProtocol;

public final class ServerHost {
	public interface Permissions {
		boolean has(UUID player, String node);
	}

	private final PlatformBridge bridge;
	private final Path configFile;
	private final Permissions permissions;
	private final TehoCore core;
	private final ProxyReceiver receiver;
	private final AdminCommand admin;
	private volatile NetworkPresets presets;
	private volatile FeatureSettings settings;
	private volatile Map<String, List<String>> grantedNodes = Collections.emptyMap();
	private volatile int operatorLevel = 2;

	public ServerHost(PlatformBridge bridge, Path configFile, Function<String, UUID> lookup, Permissions permissions) {
		this.bridge = bridge;
		this.configFile = configFile;
		this.permissions = permissions;
		this.core = new TehoCore(bridge);
		this.receiver = new ProxyReceiver(core, null);
		this.admin = new AdminCommand(core, lookup, this::reload);
	}

	public TehoCore core() {
		return core;
	}

	public AdminCommand admin() {
		return admin;
	}

	public int operatorLevel() {
		return operatorLevel;
	}

	public boolean granted(String playerName, UUID player, String node) {
		Map<String, List<String>> nodes = grantedNodes;
		for (String key : new String[] {player.toString(), playerName == null ? "" : playerName.toLowerCase(java.util.Locale.ROOT)}) {
			List<String> list = nodes.get(key);
			if (list != null && (list.contains(node) || list.contains("*") || list.contains("tehoapi.*"))) {
				return true;
			}
		}
		return false;
	}

	public void start(TehoListener listener) {
		load();
		core.addListener(new TehoListener() {
			@Override
			public void onReady(TehoPlayer teho) {
				NetworkPresets current = presets;
				Predicate<String> check = node -> permissions.has(teho.uniqueId(), node);
				if (current != null) {
					current.onReady(teho, check);
				}
				FeatureSettings features = settings;
				if (features != null) {
					features.onReady(teho);
				}
			}
		});
		if (listener != null) {
			core.addListener(listener);
		}
		core.install();
		bridge.info("Teho Server API protocol " + TehoProtocol.VERSION + " on " + TehoProtocol.CHANNEL + ", features " + core.features()
				+ (receiver.enabled() ? ", accepting signed proxy handoffs" : ""));
	}

	public void stop() {
		core.uninstall();
	}

	public void reload() {
		load();
		NetworkPresets current = presets;
		for (TehoPlayer teho : core.players()) {
			current.apply(teho, node -> permissions.has(teho.uniqueId(), node));
		}
	}

	public void onChannelsRegistered(UUID player, Collection<String> channels) {
		if (channels.contains(TehoProtocol.CHANNEL)) {
			core.onChannelRegistered(player);
		}
	}

	public void onFrame(UUID player, byte[] frame) {
		core.onMessage(player, frame);
	}

	public void onProxyFrame(UUID player, byte[] sealed) {
		receiver.onProxyFrame(player, sealed);
	}

	public void onQuit(UUID player) {
		core.onQuit(player);
	}

	private void load() {
		Map<?, ?> tree = read();
		NetworkPresets next = NetworkPresets.load(tree, message -> bridge.warn(message, null));
		try {
			receiver.key(ProxyKey.orNull(next.secret()));
		} catch (IllegalArgumentException weak) {
			bridge.warn("Ignoring proxy.secret: " + weak.getMessage(), null);
			receiver.key(null);
		}
		core.configure(next.serverName(), next.features(), next.debug());
		core.grant(next.grant());
		presets = next;
		readPermissions(tree);
		FeatureSettings loaded = FeatureSettings.load(core, tree, message -> bridge.warn(message, null));
		settings = loaded;
	}

	private void readPermissions(Map<?, ?> tree) {
		Map<String, List<String>> nodes = new LinkedHashMap<>();
		Object section = tree == null ? null : tree.get("permissions");
		if (section instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) section;
			Object level = map.get("operator-level");
			if (level instanceof Number) {
				operatorLevel = Math.max(0, Math.min(4, ((Number) level).intValue()));
			}
			Object players = map.get("players");
			if (players instanceof Map) {
				for (Map.Entry<?, ?> entry : ((Map<?, ?>) players).entrySet()) {
					List<String> list = new ArrayList<>();
					if (entry.getValue() instanceof Collection) {
						for (Object node : (Collection<?>) entry.getValue()) {
							list.add(String.valueOf(node));
						}
					}
					nodes.put(String.valueOf(entry.getKey()).toLowerCase(java.util.Locale.ROOT), list);
				}
			}
		}
		grantedNodes = nodes;
	}

	private Map<?, ?> read() {
		try {
			if (!Files.exists(configFile)) {
				Path parent = configFile.getParent();
				if (parent != null) {
					Files.createDirectories(parent);
				}
				try (InputStream defaults = ServerHost.class.getResourceAsStream("/teho-api-default.json")) {
					if (defaults != null) {
						Files.copy(defaults, configFile);
					}
				}
			}
			if (!Files.exists(configFile)) {
				return null;
			}
			return Json.object(new String(Files.readAllBytes(configFile), StandardCharsets.UTF_8));
		} catch (IOException | RuntimeException failed) {
			bridge.warn("Could not read " + configFile + ", using defaults", failed);
			return null;
		}
	}
}
