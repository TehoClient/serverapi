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

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;
import dev.tehoclient.api.common.PlatformBridge;
import dev.tehoclient.api.common.ProxyMessage;
import dev.tehoclient.api.common.TehoProxy;
import dev.tehoclient.api.protocol.TehoProtocol;

final class VelocityBridge implements PlatformBridge, TehoProxy.Backends {
	static final MinecraftChannelIdentifier API = MinecraftChannelIdentifier.from(TehoProtocol.CHANNEL);
	static final MinecraftChannelIdentifier PROXY = MinecraftChannelIdentifier.from(ProxyMessage.CHANNEL);

	private final Object plugin;
	private final ProxyServer proxy;
	private final Logger logger;

	VelocityBridge(Object plugin, ProxyServer proxy, Logger logger) {
		this.plugin = plugin;
		this.proxy = proxy;
		this.logger = logger;
	}

	@Override
	public void send(UUID id, byte[] frame) {
		proxy.getPlayer(id).ifPresent(player -> player.sendPluginMessage(API, frame));
	}

	@Override
	public boolean online(UUID id) {
		return proxy.getPlayer(id).map(Player::isActive).orElse(false);
	}

	@Override
	public String name(UUID id) {
		return proxy.getPlayer(id).map(Player::getUsername).orElse(id.toString());
	}

	@Override
	public String serverName() {
		return proxy.getVersion().getName();
	}

	@Override
	public void runLater(UUID id, Runnable task, long delayMs) {
		proxy.getScheduler().buildTask(plugin, task).delay(Math.max(1, delayMs), TimeUnit.MILLISECONDS).schedule();
	}

	@Override
	public void info(String message) {
		logger.info(message);
	}

	@Override
	public void warn(String message, Throwable failed) {
		logger.warn(message, failed);
	}

	@Override
	public boolean connected(UUID id) {
		return backend(id).isPresent();
	}

	@Override
	public boolean sendToBackend(UUID id, byte[] message) {
		return backend(id).map(server -> server.sendPluginMessage(PROXY, message)).orElse(false);
	}

	private Optional<ServerConnection> backend(UUID id) {
		return proxy.getPlayer(id).flatMap(Player::getCurrentServer);
	}
}
