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

package dev.tehoclient.api.sponge;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterChannelEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.network.ServerConnectionState;
import org.spongepowered.api.network.channel.raw.RawDataChannel;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import dev.tehoclient.api.TehoApi;
import dev.tehoclient.api.TehoListener;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.common.AdminCommand;
import dev.tehoclient.api.common.PlatformBridge;
import dev.tehoclient.api.common.ProxyMessage;
import dev.tehoclient.api.common.ServerHost;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.staff.StaffTool;
import dev.tehoclient.api.waypoint.Waypoint;

@Plugin("teho-api")
public final class TehoSponge implements TehoListener, PlatformBridge {
	private static TehoSponge instance;

	private final PluginContainer plugin;
	private final Logger logger;
	private final ServerHost host;
	private RawDataChannel api;

	@Inject
	public TehoSponge(PluginContainer plugin, Logger logger, @ConfigDir(sharedRoot = false) Path configDir) {
		instance = this;
		this.plugin = plugin;
		this.logger = logger;
		this.host = new ServerHost(this, configDir.resolve("config.json"), this::lookup, this::hasPermission);
	}

	public static Optional<TehoPlayer> player(ServerPlayer player) {
		return player == null ? Optional.<TehoPlayer>empty() : TehoApi.get().player(player.uniqueId());
	}

	public static boolean isTeho(ServerPlayer player) {
		return player(player).isPresent();
	}

	public static Waypoint.Builder waypoint(String id, ServerPlayer player) {
		return Waypoint.builder(id).at(player.position().floorX(), player.position().floorY(), player.position().floorZ())
				.dimension(dimensionOf(player));
	}

	public static String dimensionOf(ServerPlayer player) {
		ResourceKey key = player.world().key();
		return "minecraft".equals(key.namespace()) ? key.value() : key.asString();
	}

	public static ServerHost host() {
		return instance == null ? null : instance.host;
	}

	@Listener
	public void onRegisterChannels(RegisterChannelEvent event) {
		api = event.register(ResourceKey.resolve(TehoProtocol.CHANNEL), RawDataChannel.class);
		RawDataChannel proxy = event.register(ResourceKey.resolve(ProxyMessage.CHANNEL), RawDataChannel.class);
		api.play().addHandler(ServerConnectionState.Game.class,
				(buf, state) -> host.onFrame(state.player().uniqueId(), buf.readBytes(buf.available())));
		proxy.play().addHandler(ServerConnectionState.Game.class,
				(buf, state) -> host.onProxyFrame(state.player().uniqueId(), buf.readBytes(buf.available())));
	}

	@Listener
	public void onStarted(StartedEngineEvent<Server> event) {
		host.start(this);
	}

	@Listener
	public void onStopping(StoppingEngineEvent<Server> event) {
		host.stop();
	}

	@Listener
	public void onJoin(ServerSideConnectionEvent.Join event) {
		UUID id = event.player().uniqueId();
		runLater(id, () -> Sponge.server().player(id).ifPresent(player -> {
			if (api != null && api.play().isSupportedBy(player.connection())) {
				host.onChannelsRegistered(id, List.of(TehoProtocol.CHANNEL));
			}
		}), 1500);
	}

	@Listener
	public void onLeave(ServerSideConnectionEvent.Leave event) {
		host.onQuit(event.player().uniqueId());
	}

	@Listener
	public void onRegisterCommands(RegisterCommandEvent<Command.Parameterized> event) {
		Parameter.Value<String> args = Parameter.remainingJoinedStrings().key("args").optional()
				.completer((context, input) -> {
					String[] parts = input.split(" ", -1);
					String prefix = input.substring(0, input.lastIndexOf(' ') + 1);
					List<String> names = new ArrayList<>();
					for (ServerPlayer player : Sponge.server().onlinePlayers()) {
						names.add(player.name());
					}
					List<CommandCompletion> out = new ArrayList<>();
					for (String suggestion : host.admin().suggest(parts, names)) {
						out.add(CommandCompletion.of(prefix + suggestion));
					}
					return out;
				}).build();
		Command.Parameterized command = Command.builder()
				.permission("tehoapi.admin")
				.addParameter(args)
				.executor(context -> {
					String joined = context.one(args).orElse("").trim();
					String[] split = joined.isEmpty() ? new String[0] : joined.split("\\s+");
					Object root = context.cause().root();
					host.admin().execute(new AdminCommand.Sender() {
						@Override
						public String name() {
							return root instanceof ServerPlayer ? ((ServerPlayer) root).name() : "Console";
						}

						@Override
						public void reply(String legacyText) {
							context.sendMessage(LegacyComponentSerializer.legacySection().deserialize(legacyText));
						}

						@Override
						public Waypoint.Builder here(String waypointId) {
							return root instanceof ServerPlayer ? waypoint(waypointId, (ServerPlayer) root) : null;
						}
					}, "tehoapi", split);
					return CommandResult.success();
				})
				.build();
		event.register(plugin, command, "tehoapi");
	}

	private UUID lookup(String name) {
		return Sponge.server().player(name).map(ServerPlayer::uniqueId).orElse(null);
	}

	private boolean hasPermission(UUID id, String node) {
		Optional<ServerPlayer> player = Sponge.server().player(id);
		return player.isPresent() && (host.granted(player.get().name(), id, node) || player.get().hasPermission(node));
	}

	@Override
	public void send(UUID id, byte[] frame) {
		if (api == null || !Sponge.isServerAvailable()) {
			return;
		}
		Sponge.server().player(id).ifPresent(player -> {
			if (api.play().isSupportedBy(player.connection())) {
				api.play().sendTo(player, buf -> buf.writeBytes(frame));
			}
		});
	}

	@Override
	public boolean online(UUID id) {
		return Sponge.isServerAvailable() && Sponge.server().player(id).isPresent();
	}

	@Override
	public String name(UUID id) {
		return Sponge.isServerAvailable() ? Sponge.server().player(id).map(ServerPlayer::name).orElse(id.toString()) : id.toString();
	}

	@Override
	public String serverName() {
		return Sponge.isServerAvailable() ? PlainTextComponentSerializer.plainText().serialize(Sponge.server().motd()) : "Sponge";
	}

	@Override
	public void runLater(UUID id, Runnable task, long delayMs) {
		Sponge.server().scheduler().submit(Task.builder().plugin(plugin).execute(task).delay(Math.max(1, delayMs), TimeUnit.MILLISECONDS).build());
	}

	@Override
	public void info(String message) {
		logger.info(message);
	}

	@Override
	public void warn(String message, Throwable failed) {
		if (failed == null) {
			logger.warn(message);
		} else {
			logger.warn(message, failed);
		}
	}

	@Override
	public void onReady(TehoPlayer teho) {
		Sponge.server().player(teho.uniqueId()).ifPresent(player -> post(new TehoSpongeEvent.Ready(teho, player)));
	}

	@Override
	public void onConsent(TehoPlayer teho, String scope, List<String> items, boolean granted) {
		info(teho.name() + (granted ? " accepted " : " declined ") + scope + " " + items);
		Sponge.server().player(teho.uniqueId()).ifPresent(player -> post(new TehoSpongeEvent.Consent(teho, player, scope, items, granted)));
	}

	@Override
	public void onStaffTool(TehoPlayer teho, StaffTool tool, boolean active) {
		Sponge.server().player(teho.uniqueId()).ifPresent(player -> post(new TehoSpongeEvent.StaffToolChanged(teho, player, tool, active)));
	}

	@Override
	public void onEvent(TehoPlayer teho, String kind, String target, String value) {
		Sponge.server().player(teho.uniqueId()).ifPresent(player -> post(new TehoSpongeEvent.ClientEvent(teho, player, kind, target, value)));
	}

	private void post(TehoSpongeEvent event) {
		Sponge.eventManager().post(event);
	}
}
