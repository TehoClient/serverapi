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

package dev.tehoclient.api.bungee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.event.EventHandler;
import org.yaml.snakeyaml.Yaml;
import dev.tehoclient.api.TehoListener;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.bungee.event.TehoClientEvent;
import dev.tehoclient.api.bungee.event.TehoConsentEvent;
import dev.tehoclient.api.bungee.event.TehoPlayerReadyEvent;
import dev.tehoclient.api.bungee.event.TehoStaffToolEvent;
import dev.tehoclient.api.common.AdminCommand;
import dev.tehoclient.api.common.NetworkPresets;
import dev.tehoclient.api.common.ProxyKey;
import dev.tehoclient.api.common.ProxyMessage;
import dev.tehoclient.api.common.TehoCore;
import dev.tehoclient.api.common.TehoProxy;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.staff.StaffTool;
import dev.tehoclient.api.waypoint.Waypoint;

public final class TehoBungeePlugin extends Plugin implements Listener, TehoListener {
	private BungeeBridge bridge;
	private TehoCore core;
	private TehoProxy proxy;
	private AdminCommand admin;
	private volatile NetworkPresets presets;

	@Override
	public void onEnable() {
		bridge = new BungeeBridge(this);
		core = new TehoCore(bridge);
		proxy = new TehoProxy(core, null, bridge);
		loadSettings();
		getProxy().registerChannel(TehoProtocol.CHANNEL);
		getProxy().registerChannel(ProxyMessage.CHANNEL);
		getProxy().getPluginManager().registerListener(this, this);
		core.addListener(this);
		core.install();
		admin = new AdminCommand(core, name -> {
			ProxiedPlayer player = getProxy().getPlayer(name);
			return player == null ? null : player.getUniqueId();
		}, this::reload);
		getProxy().getPluginManager().registerCommand(this, new AdminCommandHandler());
		getLogger().info("Teho Server API protocol " + TehoProtocol.VERSION + " on " + TehoProtocol.CHANNEL
				+ " (proxy), features " + core.features() + (proxy.signing() ? ", signing backend handoffs" : ""));
	}

	@Override
	public void onDisable() {
		if (core != null) {
			core.uninstall();
		}
		getProxy().unregisterChannel(TehoProtocol.CHANNEL);
		getProxy().unregisterChannel(ProxyMessage.CHANNEL);
	}

	private dev.tehoclient.api.common.FeatureSettings featureSettings;

	private void loadSettings() {
		java.util.Map<?, ?> tree = readConfig();
		NetworkPresets next = NetworkPresets.load(tree, getLogger()::warning);
		ProxyKey key = null;
		try {
			key = ProxyKey.orNull(next.secret());
		} catch (IllegalArgumentException weak) {
			getLogger().warning("Ignoring proxy.secret: " + weak.getMessage());
		}
		if (key == null) {
			getLogger().warning("proxy.secret is not set, so backends will not learn which players are on Teho. "
					+ "Set the same secret here and in each backend's TehoAPI config.");
		}
		core.configure(next.serverName(), next.features(), next.debug());
		core.grant(next.grant());
		proxy.configure(key, next.serverName());
		presets = next;
		featureSettings = dev.tehoclient.api.common.FeatureSettings.load(core, tree, getLogger()::warning);
	}

	private void reload() {
		loadSettings();
		for (TehoPlayer teho : core.players()) {
			ProxiedPlayer player = getProxy().getPlayer(teho.uniqueId());
			if (player != null) {
				presets.apply(teho, player::hasPermission);
			}
		}
	}

	private Map<?, ?> readConfig() {
		File file = new File(getDataFolder(), "config.yml");
		try {
			if (!file.exists()) {
				Files.createDirectories(getDataFolder().toPath());
				try (InputStream defaults = getResourceAsStream("config.yml")) {
					Files.copy(defaults, file.toPath());
				}
			}
			try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
				Object loaded = new Yaml().load(reader);
				return loaded instanceof Map ? (Map<?, ?>) loaded : null;
			}
		} catch (IOException | RuntimeException failed) {
			getLogger().warning("Could not read " + file + ", using defaults: " + failed);
			return null;
		}
	}

	@EventHandler
	public void onPluginMessage(PluginMessageEvent event) {
		String tag = event.getTag();
		if (ProxyMessage.CHANNEL.equals(tag)) {
			event.setCancelled(true);
			return;
		}
		if (TehoProtocol.CHANNEL.equals(tag)) {
			if (event.getSender() instanceof ProxiedPlayer) {
				event.setCancelled(true);
				proxy.onClientFrame(((ProxiedPlayer) event.getSender()).getUniqueId(), event.getData());
			} else if (event.getSender() instanceof Server && event.getReceiver() instanceof ProxiedPlayer) {
				proxy.onBackendFrame(((ProxiedPlayer) event.getReceiver()).getUniqueId(), event.getData());
			}
			return;
		}
		if (("minecraft:register".equals(tag) || "REGISTER".equals(tag)) && event.getSender() instanceof ProxiedPlayer) {
			for (String channel : new String(event.getData(), StandardCharsets.UTF_8).split("\0")) {
				if (TehoProtocol.CHANNEL.equals(channel)) {
					core.onChannelRegistered(((ProxiedPlayer) event.getSender()).getUniqueId());
					return;
				}
			}
		}
	}

	@EventHandler
	public void onSwitch(ServerSwitchEvent event) {
		proxy.onBackendConnected(event.getPlayer().getUniqueId(), event.getFrom() != null);
	}

	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent event) {
		proxy.onQuit(event.getPlayer().getUniqueId());
	}

	@Override
	public void onReady(TehoPlayer teho) {
		ProxiedPlayer player = getProxy().getPlayer(teho.uniqueId());
		if (player == null) {
			return;
		}
		presets.onReady(teho, player::hasPermission);
		featureSettings.onReady(teho);
		getProxy().getPluginManager().callEvent(new TehoPlayerReadyEvent(teho, player));
	}

	@Override
	public void onConsent(TehoPlayer teho, String scope, List<String> items, boolean granted) {
		getLogger().info(teho.name() + (granted ? " accepted " : " declined ") + scope + " " + items);
		ProxiedPlayer player = getProxy().getPlayer(teho.uniqueId());
		if (player != null) {
			getProxy().getPluginManager().callEvent(new TehoConsentEvent(teho, player, scope, items, granted));
		}
	}

	@Override
	public void onStaffTool(TehoPlayer teho, StaffTool tool, boolean active) {
		ProxiedPlayer player = getProxy().getPlayer(teho.uniqueId());
		if (player != null) {
			getProxy().getPluginManager().callEvent(new TehoStaffToolEvent(teho, player, tool, active));
		}
	}

	@Override
	public void onEvent(TehoPlayer teho, String kind, String target, String value) {
		ProxiedPlayer player = getProxy().getPlayer(teho.uniqueId());
		if (player != null) {
			getProxy().getPluginManager().callEvent(new TehoClientEvent(teho, player, kind, target, value));
		}
	}

	private final class AdminCommandHandler extends Command implements TabExecutor {
		AdminCommandHandler() {
			super("tehoapi", "tehoapi.admin");
		}

		@Override
		public void execute(CommandSender sender, String[] args) {
			admin.execute(new AdminCommand.Sender() {
				@Override
				public String name() {
					return sender.getName();
				}

				@Override
				@SuppressWarnings("deprecation")
				public void reply(String legacyText) {
					sender.sendMessage(TextComponent.fromLegacyText(legacyText));
				}

				@Override
				public Waypoint.Builder here(String waypointId) {
					return null;
				}
			}, "tehoapi", args);
		}

		@Override
		public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
			List<String> names = new ArrayList<>();
			for (ProxiedPlayer player : getProxy().getPlayers()) {
				names.add(player.getName());
			}
			return admin.suggest(args, names);
		}
	}
}
