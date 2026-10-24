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

package dev.tehoclient.api.minestom;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.instance.Instance;
import dev.tehoclient.api.TehoApi;
import dev.tehoclient.api.TehoListener;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.common.AdminCommand;
import dev.tehoclient.api.common.ProxyKey;
import dev.tehoclient.api.common.ProxyMessage;
import dev.tehoclient.api.common.ProxyReceiver;
import dev.tehoclient.api.common.TehoCore;
import dev.tehoclient.api.minestom.event.TehoClientEvent;
import dev.tehoclient.api.minestom.event.TehoConsentEvent;
import dev.tehoclient.api.minestom.event.TehoPlayerReadyEvent;
import dev.tehoclient.api.minestom.event.TehoStaffToolEvent;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.staff.StaffTool;
import dev.tehoclient.api.waypoint.Waypoint;

public final class TehoMinestom implements TehoListener {
	private final MinestomBridge bridge;
	private final TehoCore core;
	private final ProxyReceiver receiver;
	private final EventNode<Event> node = EventNode.all("teho-api");
	private final Command command;

	private TehoMinestom(Builder builder) {
		bridge = new MinestomBridge(builder.serverName.isEmpty() ? "Minestom" : builder.serverName);
		core = new TehoCore(bridge);
		core.configure(builder.serverName, builder.features, builder.debug);
		core.grant(builder.grant);
		core.backend().configure(builder.backendUrl, builder.backendKey);
		core.verified(builder.certificate);
		receiver = new ProxyReceiver(core, ProxyKey.orNull(builder.proxySecret));
		command = builder.command ? new AdminCommandHandler(builder.admin) : null;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Optional<TehoPlayer> player(Player player) {
		return player == null ? Optional.<TehoPlayer>empty() : TehoApi.get().player(player.getUuid());
	}

	public static boolean isTeho(Player player) {
		return player(player).isPresent();
	}

	public static Waypoint.Builder waypoint(String id, Player player) {
		return waypoint(id, player.getPosition(), player.getInstance());
	}

	public static Waypoint.Builder waypoint(String id, Point point, Instance instance) {
		return Waypoint.builder(id).at(point.blockX(), point.blockY(), point.blockZ()).dimension(dimensionOf(instance));
	}

	public static String dimensionOf(Instance instance) {
		if (instance == null) {
			return "";
		}
		String name = instance.getDimensionName();
		return name.startsWith("minecraft:") ? name.substring("minecraft:".length()) : name;
	}

	public TehoCore core() {
		return core;
	}

	public boolean proxied() {
		return receiver.enabled();
	}

	private TehoMinestom start() {
		node.addListener(PlayerPluginMessageEvent.class, this::onPluginMessage);
		node.addListener(PlayerDisconnectEvent.class, event -> core.onQuit(event.getPlayer().getUuid()));
		MinecraftServer.getGlobalEventHandler().addChild(node);
		if (command != null) {
			MinecraftServer.getCommandManager().register(command);
		}
		core.addListener(this);
		core.install();
		bridge.info("Teho Server API protocol " + TehoProtocol.VERSION + " on " + TehoProtocol.CHANNEL + ", features "
				+ core.features() + (receiver.enabled() ? ", accepting signed proxy handoffs" : ""));
		return this;
	}

	public void uninstall() {
		MinecraftServer.getGlobalEventHandler().removeChild(node);
		if (command != null) {
			MinecraftServer.getCommandManager().unregister(command);
		}
		core.removeListener(this);
		core.uninstall();
	}

	private void onPluginMessage(PlayerPluginMessageEvent event) {
		String channel = event.getIdentifier();
		Player player = event.getPlayer();
		if (TehoProtocol.CHANNEL.equals(channel)) {
			core.onMessage(player.getUuid(), event.getMessage());
		} else if (ProxyMessage.CHANNEL.equals(channel)) {
			receiver.onProxyFrame(player.getUuid(), event.getMessage());
		} else if ("minecraft:register".equals(channel)) {
			for (String registered : new String(event.getMessage(), StandardCharsets.UTF_8).split("\0")) {
				if (TehoProtocol.CHANNEL.equals(registered)) {
					core.onChannelRegistered(player.getUuid());
					return;
				}
			}
		}
	}

	@Override
	public void onReady(TehoPlayer teho) {
		Player player = MinestomBridge.find(teho.uniqueId());
		if (player != null) {
			EventDispatcher.call(new TehoPlayerReadyEvent(teho, player));
		}
	}

	@Override
	public void onConsent(TehoPlayer teho, String scope, List<String> items, boolean granted) {
		Player player = MinestomBridge.find(teho.uniqueId());
		if (player != null) {
			EventDispatcher.call(new TehoConsentEvent(teho, player, scope, items, granted));
		}
	}

	@Override
	public void onStaffTool(TehoPlayer teho, StaffTool tool, boolean active) {
		Player player = MinestomBridge.find(teho.uniqueId());
		if (player != null) {
			EventDispatcher.call(new TehoStaffToolEvent(teho, player, tool, active));
		}
	}

	@Override
	public void onEvent(TehoPlayer teho, String kind, String target, String value) {
		Player player = MinestomBridge.find(teho.uniqueId());
		if (player != null) {
			EventDispatcher.call(new TehoClientEvent(teho, player, kind, target, value));
		}
	}

	private final class AdminCommandHandler extends Command {
		private final AdminCommand admin = new AdminCommand(core, name -> {
			Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(name);
			return player == null ? null : player.getUuid();
		}, () -> { });

		AdminCommandHandler(Predicate<CommandSender> allowed) {
			super("tehoapi");
			setCondition((sender, input) -> allowed.test(sender));
			setDefaultExecutor((sender, context) -> admin.execute(adapt(sender), "tehoapi", new String[0]));
			ArgumentStringArray args = ArgumentType.StringArray("args");
			addSyntax((sender, context) -> admin.execute(adapt(sender), "tehoapi", context.get(args)), args);
		}

		private AdminCommand.Sender adapt(CommandSender sender) {
			return new AdminCommand.Sender() {
				@Override
				public String name() {
					return sender instanceof Player ? ((Player) sender).getUsername() : "Console";
				}

				@Override
				public void reply(String legacyText) {
					sender.sendMessage(LegacyComponentSerializer.legacySection().deserialize(legacyText));
				}

				@Override
				public Waypoint.Builder here(String waypointId) {
					return sender instanceof Player ? waypoint(waypointId, (Player) sender) : null;
				}
			};
		}
	}

	public static final class Builder {
		private String serverName = "";
		private String grant = "";
		private final Set<String> features = new LinkedHashSet<>(TehoProtocol.known());
		private boolean debug;
		private String proxySecret = "";
		private boolean command = true;
		private Predicate<CommandSender> admin = sender -> sender instanceof ConsoleSender;
		private String backendUrl = "";
		private String backendKey = "";
		private String certificate = "";

		private Builder() {}

		public Builder privateGrant(String token) {
			this.grant = token == null ? "" : token.trim();
			return this;
		}

		public Builder serverName(String serverName) {
			this.serverName = serverName == null ? "" : serverName;
			return this;
		}

		public Builder features(String... capabilities) {
			return features(Arrays.asList(capabilities));
		}

		public Builder features(Collection<String> capabilities) {
			features.clear();
			features.addAll(capabilities);
			return this;
		}

		public Builder debug(boolean debug) {
			this.debug = debug;
			return this;
		}

		public Builder proxySecret(String secret) {
			this.proxySecret = secret == null ? "" : secret;
			return this;
		}

		public Builder command(boolean register) {
			this.command = register;
			return this;
		}

		public Builder admin(Predicate<CommandSender> allowed) {
			this.admin = allowed;
			return this;
		}

		public Builder backend(String url, String serverKey) {
			this.backendUrl = url == null ? "" : url;
			this.backendKey = serverKey == null ? "" : serverKey;
			return this;
		}

		public Builder verifiedCertificate(String certificate) {
			this.certificate = certificate == null ? "" : certificate.trim();
			return this;
		}

		public TehoMinestom install() {
			return new TehoMinestom(this).start();
		}
	}
}
