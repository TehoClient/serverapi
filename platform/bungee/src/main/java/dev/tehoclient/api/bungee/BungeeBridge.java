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

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import dev.tehoclient.api.common.PlatformBridge;
import dev.tehoclient.api.common.ProxyMessage;
import dev.tehoclient.api.common.TehoProxy;
import dev.tehoclient.api.protocol.TehoProtocol;

final class BungeeBridge implements PlatformBridge, TehoProxy.Backends {
	private final Plugin plugin;

	BungeeBridge(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void send(UUID id, byte[] frame) {
		ProxiedPlayer player = plugin.getProxy().getPlayer(id);
		if (player != null && player.isConnected()) {
			player.sendData(TehoProtocol.CHANNEL, frame);
		}
	}

	@Override
	public boolean online(UUID id) {
		ProxiedPlayer player = plugin.getProxy().getPlayer(id);
		return player != null && player.isConnected();
	}

	@Override
	public String name(UUID id) {
		ProxiedPlayer player = plugin.getProxy().getPlayer(id);
		return player == null ? id.toString() : player.getName();
	}

	@Override
	public String serverName() {
		return plugin.getProxy().getName();
	}

	@Override
	public void runLater(UUID id, Runnable task, long delayMs) {
		plugin.getProxy().getScheduler().schedule(plugin, task, Math.max(1, delayMs), TimeUnit.MILLISECONDS);
	}

	@Override
	public void info(String message) {
		plugin.getLogger().info(message);
	}

	@Override
	public void warn(String message, Throwable failed) {
		plugin.getLogger().log(Level.WARNING, message, failed);
	}

	@Override
	public boolean connected(UUID id) {
		return backend(id) != null;
	}

	@Override
	public boolean sendToBackend(UUID id, byte[] message) {
		Server server = backend(id);
		if (server == null) {
			return false;
		}
		server.sendData(ProxyMessage.CHANNEL, message);
		return true;
	}

	private Server backend(UUID id) {
		ProxiedPlayer player = plugin.getProxy().getPlayer(id);
		return player == null ? null : player.getServer();
	}
}
