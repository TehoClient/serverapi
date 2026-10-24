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

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.tehoclient.api.common.PlatformBridge;

final class NeoForgeBridge implements PlatformBridge {
	private static final Logger LOGGER = LoggerFactory.getLogger("TehoAPI");

	private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(task -> {
		Thread thread = new Thread(task, "TehoAPI timer");
		thread.setDaemon(true);
		return thread;
	});
	private volatile MinecraftServer server;

	void server(MinecraftServer server) {
		this.server = server;
	}

	MinecraftServer server() {
		return server;
	}

	ServerPlayer find(UUID id) {
		MinecraftServer current = server;
		return current == null ? null : current.getPlayerList().getPlayer(id);
	}

	void shutdown() {
		timer.shutdownNow();
	}

	@Override
	public void send(UUID id, byte[] frame) {
		ServerPlayer player = find(id);
		if (player != null && player.connection.hasChannel(FramePayload.API)) {
			PacketDistributor.sendToPlayer(player, new FramePayload(FramePayload.API, frame));
		}
	}

	@Override
	public boolean online(UUID id) {
		return find(id) != null;
	}

	@Override
	public String name(UUID id) {
		ServerPlayer player = find(id);
		return player == null ? id.toString() : player.getScoreboardName();
	}

	@Override
	public String serverName() {
		MinecraftServer current = server;
		return current == null ? "NeoForge" : current.getMotd();
	}

	@Override
	public void runLater(UUID id, Runnable task, long delayMs) {
		timer.schedule(() -> {
			MinecraftServer current = server;
			if (current != null) {
				current.execute(task);
			}
		}, Math.max(1, delayMs), TimeUnit.MILLISECONDS);
	}

	@Override
	public void info(String message) {
		LOGGER.info(message);
	}

	@Override
	public void warn(String message, Throwable failed) {
		if (failed == null) {
			LOGGER.warn(message);
		} else {
			LOGGER.warn(message, failed);
		}
	}
}
