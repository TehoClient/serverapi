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

package dev.tehoclient.api.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.PlayerChannelRegisterEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import dev.tehoclient.api.TehoListener;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.common.AdminCommand;
import dev.tehoclient.api.common.NetworkPresets;
import dev.tehoclient.api.common.ProxyKey;
import dev.tehoclient.api.common.TehoCore;
import dev.tehoclient.api.common.TehoProxy;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.staff.StaffTool;
import dev.tehoclient.api.velocity.event.TehoClientEvent;
import dev.tehoclient.api.velocity.event.TehoConsentEvent;
import dev.tehoclient.api.velocity.event.TehoPlayerReadyEvent;
import dev.tehoclient.api.velocity.event.TehoStaffToolEvent;
import dev.tehoclient.api.waypoint.Waypoint;

public final class TehoVelocityPlugin implements TehoListener {
	private final ProxyServer server;
	private final Logger logger;
	private final Path dataDirectory;
	private VelocityBridge bridge;
	private TehoCore core;
	private TehoProxy proxy;
	private AdminCommand admin;
	private volatile NetworkPresets presets;

	@Inject
	public TehoVelocityPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
		this.server = server;
		this.logger = logger;
		this.dataDirectory = dataDirectory;
	}

	private dev.tehoclient.api.common.FeatureSettings featureSettings;

	@Subscribe
	public void onInitialize(ProxyInitializeEvent event) {
		bridge = new VelocityBridge(this, server, logger);
		core = new TehoCore(bridge);
		proxy = new TehoProxy(core, null, bridge);
		loadSettings();
		server.getChannelRegistrar().register(VelocityBridge.API, VelocityBridge.PROXY);
		core.addListener(this);
		core.install();
		admin = new AdminCommand(core, name -> server.getPlayer(name).map(Player::getUniqueId).orElse(null), this::reload);
		server.getCommandManager().register(server.getCommandManager().metaBuilder("tehoapi").plugin(this).build(),
				new Command());
		logger.info("Teho Server API protocol " + TehoProtocol.VERSION + " on " + TehoProtocol.CHANNEL + " (proxy), features "
				+ core.features() + (proxy.signing() ? ", signing backend handoffs" : ""));
	}

	@Subscribe
	public void onShutdown(ProxyShutdownEvent event) {
		if (core != null) {
			core.uninstall();
		}
	}

	private void loadSettings() {
		Map<?, ?> tree = readConfig();
		NetworkPresets next = NetworkPresets.load(tree, logger::warn);
		ProxyKey key = null;
		try {
			key = ProxyKey.orNull(next.secret());
		} catch (IllegalArgumentException weak) {
			logger.warn("Ignoring proxy.secret: " + weak.getMessage());
		}
		if (key == null) {
			logger.warn("proxy.secret is not set, so backends will not learn which players are on Teho. "
					+ "Set the same secret here and in each backend's TehoAPI config.");
		}
		core.configure(next.serverName(), next.features(), next.debug());
		core.grant(next.grant());
		proxy.configure(key, next.serverName());
		presets = next;
		featureSettings = dev.tehoclient.api.common.FeatureSettings.load(core, tree, logger::warn);
	}

	private void reload() {
		loadSettings();
		for (TehoPlayer teho : core.players()) {
			server.getPlayer(teho.uniqueId()).ifPresent(player -> presets.apply(teho, player::hasPermission));
		}
	}

	private Map<?, ?> readConfig() {
		Path file = dataDirectory.resolve("config.yml");
		try {
			if (!Files.exists(file)) {
				Files.createDirectories(dataDirectory);
				try (InputStream defaults = getClass().getResourceAsStream("/config.yml")) {
					Files.copy(defaults, file);
				}
			}
			try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
				Object loaded = new Yaml().load(reader);
				return loaded instanceof Map ? (Map<?, ?>) loaded : null;
			}
		} catch (IOException | RuntimeException failed) {
			logger.warn("Could not read " + file + ", using defaults", failed);
			return null;
		}
	}

	@Subscribe
	public void onPluginMessage(PluginMessageEvent event) {
		String channel = event.getIdentifier().getId();
		if (VelocityBridge.PROXY.getId().equals(channel)) {
			event.setResult(PluginMessageEvent.ForwardResult.handled());
			return;
		}
		if (!VelocityBridge.API.getId().equals(channel)) {
			return;
		}
		if (event.getSource() instanceof Player) {
			event.setResult(PluginMessageEvent.ForwardResult.handled());
			proxy.onClientFrame(((Player) event.getSource()).getUniqueId(), event.getData());
		} else if (event.getSource() instanceof ServerConnection) {
			proxy.onBackendFrame(((ServerConnection) event.getSource()).getPlayer().getUniqueId(), event.getData());
		}
	}

	@Subscribe
	public void onChannelRegister(PlayerChannelRegisterEvent event) {
		for (ChannelIdentifier channel : event.getChannels()) {
			if (VelocityBridge.API.getId().equals(channel.getId())) {
				core.onChannelRegistered(event.getPlayer().getUniqueId());
				return;
			}
		}
	}

	@Subscribe
	public void onServerConnected(ServerPostConnectEvent event) {
		proxy.onBackendConnected(event.getPlayer().getUniqueId(), event.getPreviousServer() != null);
	}

	@Subscribe
	public void onDisconnect(DisconnectEvent event) {
		proxy.onQuit(event.getPlayer().getUniqueId());
	}

	@Override
	public void onReady(TehoPlayer teho) {
		Optional<Player> player = server.getPlayer(teho.uniqueId());
		if (!player.isPresent()) {
			return;
		}
		presets.onReady(teho, player.get()::hasPermission);
		featureSettings.onReady(teho);
		server.getEventManager().fireAndForget(new TehoPlayerReadyEvent(teho, player.get()));
	}

	@Override
	public void onConsent(TehoPlayer teho, String scope, List<String> items, boolean granted) {
		logger.info(teho.name() + (granted ? " accepted " : " declined ") + scope + " " + items);
		server.getPlayer(teho.uniqueId()).ifPresent(player ->
				server.getEventManager().fireAndForget(new TehoConsentEvent(teho, player, scope, items, granted)));
	}

	@Override
	public void onStaffTool(TehoPlayer teho, StaffTool tool, boolean active) {
		server.getPlayer(teho.uniqueId()).ifPresent(player ->
				server.getEventManager().fireAndForget(new TehoStaffToolEvent(teho, player, tool, active)));
	}

	@Override
	public void onEvent(TehoPlayer teho, String kind, String target, String value) {
		server.getPlayer(teho.uniqueId()).ifPresent(player ->
				server.getEventManager().fireAndForget(new TehoClientEvent(teho, player, kind, target, value)));
	}

	private final class Command implements SimpleCommand {
		@Override
		public void execute(Invocation invocation) {
			CommandSource source = invocation.source();
			admin.execute(new AdminCommand.Sender() {
				@Override
				public String name() {
					return source instanceof Player ? ((Player) source).getUsername() : "Console";
				}

				@Override
				public void reply(String legacyText) {
					source.sendMessage(LegacyComponentSerializer.legacySection().deserialize(legacyText));
				}

				@Override
				public Waypoint.Builder here(String waypointId) {
					return null;
				}
			}, invocation.alias(), invocation.arguments());
		}

		@Override
		public List<String> suggest(Invocation invocation) {
			List<String> names = new ArrayList<>();
			for (Player player : server.getAllPlayers()) {
				names.add(player.getUsername());
			}
			return admin.suggest(invocation.arguments(), names);
		}

		@Override
		public boolean hasPermission(Invocation invocation) {
			return invocation.source().hasPermission("tehoapi.admin");
		}
	}
}
