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

import java.time.Duration;
import java.util.UUID;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.tehoclient.api.common.PlatformBridge;
import dev.tehoclient.api.protocol.TehoProtocol;

final class MinestomBridge implements PlatformBridge {
	private static final Logger LOGGER = LoggerFactory.getLogger("TehoAPI");

	private final String serverName;

	MinestomBridge(String serverName) {
		this.serverName = serverName;
	}

	static Player find(UUID id) {
		return MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(id);
	}

	@Override
	public void send(UUID id, byte[] frame) {
		Player player = find(id);
		if (player != null && player.isOnline()) {
			player.sendPluginMessage(TehoProtocol.CHANNEL, frame);
		}
	}

	@Override
	public boolean online(UUID id) {
		Player player = find(id);
		return player != null && player.isOnline();
	}

	@Override
	public String name(UUID id) {
		Player player = find(id);
		return player == null ? id.toString() : player.getUsername();
	}

	@Override
	public String serverName() {
		return serverName;
	}

	@Override
	public void runLater(UUID id, Runnable task, long delayMs) {
		MinecraftServer.getSchedulerManager().buildTask(task).delay(Duration.ofMillis(Math.max(1, delayMs))).schedule();
	}

	@Override
	public void info(String message) {
		LOGGER.info(message);
	}

	@Override
	public void warn(String message, Throwable failed) {
		LOGGER.warn(message, failed);
	}
}
