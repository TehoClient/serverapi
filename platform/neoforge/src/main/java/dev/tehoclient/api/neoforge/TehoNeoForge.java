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

package dev.tehoclient.api.neoforge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import dev.tehoclient.api.TehoApi;
import dev.tehoclient.api.TehoListener;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.common.AdminCommand;
import dev.tehoclient.api.common.ServerHost;
import dev.tehoclient.api.staff.StaffTool;
import dev.tehoclient.api.waypoint.Waypoint;

@Mod(value = "teho_api", dist = Dist.DEDICATED_SERVER)
public final class TehoNeoForge implements TehoListener {
	private static TehoNeoForge instance;

	private final NeoForgeBridge bridge = new NeoForgeBridge();
	private final ServerHost host;

	public TehoNeoForge(IEventBus modBus) {
		instance = this;
		host = new ServerHost(bridge, FMLPaths.CONFIGDIR.get().resolve("teho-api.json"), this::lookup, this::hasPermission);
		modBus.addListener(RegisterPayloadHandlersEvent.class, this::registerPayloads);
		NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, event -> {
			bridge.server(event.getServer());
			host.start(this);
		});
		NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class, event -> {
			host.stop();
			bridge.shutdown();
		});
		NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
			if (event.getEntity() instanceof ServerPlayer player) {
				UUID id = player.getUUID();
				bridge.runLater(id, () -> {
					ServerPlayer online = bridge.find(id);
					if (online != null && online.connection.hasChannel(FramePayload.API)) {
						host.onChannelsRegistered(id, List.of(FramePayload.API.id().toString()));
					}
				}, 1500);
			}
		});
		NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, event -> host.onQuit(event.getEntity().getUUID()));
		NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, this::registerCommand);
	}

	public static Optional<TehoPlayer> player(ServerPlayer player) {
		return player == null ? Optional.<TehoPlayer>empty() : TehoApi.get().player(player.getUUID());
	}

	public static boolean isTeho(ServerPlayer player) {
		return player(player).isPresent();
	}

	public static Waypoint.Builder waypoint(String id, ServerPlayer player) {
		return Waypoint.builder(id).at(player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ())
				.dimension(dimensionOf(player));
	}

	public static String dimensionOf(ServerPlayer player) {
		Identifier id = player.level().dimension().identifier();
		return "minecraft".equals(id.getNamespace()) ? id.getPath() : id.toString();
	}

	public static ServerHost host() {
		return instance == null ? null : instance.host;
	}

	private void registerPayloads(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1").optional();
		registrar.playBidirectional(FramePayload.API, FramePayload.codec(FramePayload.API),
				(payload, context) -> host.onFrame(context.player().getUUID(), payload.data()));
		registrar.playToServer(FramePayload.PROXY, FramePayload.codec(FramePayload.PROXY),
				(payload, context) -> host.onProxyFrame(context.player().getUUID(), payload.data()));
	}

	private UUID lookup(String name) {
		MinecraftServer server = bridge.server();
		ServerPlayer player = server == null ? null : server.getPlayerList().getPlayerByName(name);
		return player == null ? null : player.getUUID();
	}

	private boolean hasPermission(UUID id, String node) {
		ServerPlayer player = bridge.find(id);
		if (player == null) {
			return false;
		}
		if (host.granted(player.getScoreboardName(), id, node)) {
			return true;
		}
		Permission needed = switch (host.operatorLevel()) {
			case 0 -> null;
			case 1 -> Permissions.COMMANDS_MODERATOR;
			case 2 -> Permissions.COMMANDS_GAMEMASTER;
			case 3 -> Permissions.COMMANDS_ADMIN;
			default -> Permissions.COMMANDS_OWNER;
		};
		return needed == null || player.permissions().hasPermission(needed);
	}

	private void registerCommand(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("tehoapi")
				.requires(source -> !source.isPlayer() || hasPermission(source.getPlayer().getUUID(), "tehoapi.admin"))
				.executes(context -> run(context.getSource(), new String[0]))
				.then(Commands.argument("args", StringArgumentType.greedyString())
						.suggests((context, builder) -> {
							String input = builder.getRemaining();
							String[] args = input.split(" ", -1);
							String prefix = input.substring(0, input.lastIndexOf(' ') + 1);
							List<String> names = new ArrayList<>();
							for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
								names.add(player.getScoreboardName());
							}
							for (String suggestion : host.admin().suggest(args, names)) {
								builder.suggest(prefix + suggestion);
							}
							return builder.buildFuture();
						})
						.executes(context -> run(context.getSource(), StringArgumentType.getString(context, "args").trim().split("\\s+")))));
	}

	private int run(CommandSourceStack source, String[] args) {
		host.admin().execute(new AdminCommand.Sender() {
			@Override
			public String name() {
				return source.getTextName();
			}

			@Override
			public void reply(String legacyText) {
				source.sendSystemMessage(Component.literal(legacyText));
			}

			@Override
			public Waypoint.Builder here(String waypointId) {
				return source.isPlayer() ? waypoint(waypointId, source.getPlayer()) : null;
			}
		}, "tehoapi", args);
		return 1;
	}

	@Override
	public void onReady(TehoPlayer teho) {
		ServerPlayer player = bridge.find(teho.uniqueId());
		if (player != null) {
			NeoForge.EVENT_BUS.post(new TehoNeoForgeEvent.Ready(teho, player));
		}
	}

	@Override
	public void onConsent(TehoPlayer teho, String scope, List<String> items, boolean granted) {
		bridge.info(teho.name() + (granted ? " accepted " : " declined ") + scope + " " + items);
		ServerPlayer player = bridge.find(teho.uniqueId());
		if (player != null) {
			NeoForge.EVENT_BUS.post(new TehoNeoForgeEvent.Consent(teho, player, scope, items, granted));
		}
	}

	@Override
	public void onStaffTool(TehoPlayer teho, StaffTool tool, boolean active) {
		ServerPlayer player = bridge.find(teho.uniqueId());
		if (player != null) {
			NeoForge.EVENT_BUS.post(new TehoNeoForgeEvent.StaffToolChanged(teho, player, tool, active));
		}
	}

	@Override
	public void onEvent(TehoPlayer teho, String kind, String target, String value) {
		ServerPlayer player = bridge.find(teho.uniqueId());
		if (player != null) {
			NeoForge.EVENT_BUS.post(new TehoNeoForgeEvent.ClientEvent(teho, player, kind, target, value));
		}
	}
}
