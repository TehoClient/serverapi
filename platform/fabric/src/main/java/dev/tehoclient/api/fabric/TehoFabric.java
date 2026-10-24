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

package dev.tehoclient.api.fabric;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ClientboundPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.permission.v1.PermissionContextOwner;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import dev.tehoclient.api.TehoApi;
import dev.tehoclient.api.TehoListener;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.common.AdminCommand;
import dev.tehoclient.api.common.ServerHost;
import dev.tehoclient.api.staff.StaffTool;
import dev.tehoclient.api.waypoint.Waypoint;

public final class TehoFabric implements DedicatedServerModInitializer, TehoListener {
	private static TehoFabric instance;

	private final FabricBridge bridge = new FabricBridge();
	private ServerHost host;

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

	@Override
	public void onInitializeServer() {
		instance = this;
		PayloadTypeRegistry.serverboundPlay().register(FramePayload.API, FramePayload.codec(FramePayload.API));
		PayloadTypeRegistry.serverboundPlay().register(FramePayload.PROXY, FramePayload.codec(FramePayload.PROXY));
		PayloadTypeRegistry.clientboundPlay().register(FramePayload.API, FramePayload.codec(FramePayload.API));
		host = new ServerHost(bridge, FabricLoader.getInstance().getConfigDir().resolve("teho-api.json"), this::lookup, this::hasPermission);
		ServerPlayNetworking.registerGlobalReceiver(FramePayload.API, (payload, context) -> host.onFrame(context.player().getUUID(), payload.data()));
		ServerPlayNetworking.registerGlobalReceiver(FramePayload.PROXY,
				(payload, context) -> host.onProxyFrame(context.player().getUUID(), payload.data()));
		ClientboundPlayChannelEvents.REGISTER.register((handler, sender, server, channels) -> {
			List<String> names = new ArrayList<>();
			for (Identifier channel : channels) {
				names.add(channel.toString());
			}
			host.onChannelsRegistered(handler.getPlayer().getUUID(), names);
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> host.onQuit(handler.getPlayer().getUUID()));
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			bridge.server(server);
			host.start(this);
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			host.stop();
			bridge.shutdown();
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, registries, environment) -> registerCommand(dispatcher));
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
		return check((PermissionContextOwner) (Object) player, node);
	}

	private boolean check(PermissionContextOwner owner, String node) {
		int dot = node.indexOf('.');
		Identifier id = dot > 0 ? Identifier.tryBuild(node.substring(0, dot), node.substring(dot + 1)) : null;
		PermissionLevel level = PermissionLevel.byId(host.operatorLevel());
		if (id == null) {
			return owner.checkPermission(Identifier.fromNamespaceAndPath("tehoapi", "admin"), level);
		}
		return owner.checkPermission(id, level);
	}

	private void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("tehoapi")
				.requires(source -> !source.isPlayer() || hasPermission(source.getPlayer().getUUID(), "tehoapi.admin"))
				.executes(context -> run(context.getSource(), new String[0]))
				.then(Commands.argument("args", StringArgumentType.greedyString())
						.suggests((context, builder) -> {
							String input = builder.getRemaining();
							String[] args = input.split(" ", -1);
							String prefix = input.substring(0, input.lastIndexOf(' ') + 1);
							List<String> names = new ArrayList<>();
							MinecraftServer server = context.getSource().getServer();
							for (ServerPlayer player : server.getPlayerList().getPlayers()) {
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
			TehoFabricEvents.READY.invoker().onReady(teho, player);
		}
	}

	@Override
	public void onConsent(TehoPlayer teho, String scope, List<String> items, boolean granted) {
		bridge.info(teho.name() + (granted ? " accepted " : " declined ") + scope + " " + items);
		ServerPlayer player = bridge.find(teho.uniqueId());
		if (player != null) {
			TehoFabricEvents.CONSENT.invoker().onConsent(teho, player, scope, items, granted);
		}
	}

	@Override
	public void onStaffTool(TehoPlayer teho, StaffTool tool, boolean active) {
		ServerPlayer player = bridge.find(teho.uniqueId());
		if (player != null) {
			TehoFabricEvents.STAFF_TOOL.invoker().onStaffTool(teho, player, tool, active);
		}
	}

	@Override
	public void onEvent(TehoPlayer teho, String kind, String target, String value) {
		ServerPlayer player = bridge.find(teho.uniqueId());
		if (player != null) {
			TehoFabricEvents.CLIENT_EVENT.invoker().onClientEvent(teho, player, kind, target, value);
		}
	}
}
