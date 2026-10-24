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

package dev.tehoclient.api.bukkit;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import dev.tehoclient.api.common.PlatformBridge;
import dev.tehoclient.api.protocol.TehoProtocol;

final class BukkitBridge implements PlatformBridge {
	static final boolean FOLIA = present("io.papermc.paper.threadedregions.RegionizedServer");

	private final Plugin plugin;

	BukkitBridge(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void send(UUID id, byte[] frame) {
		Player player = Bukkit.getPlayer(id);
		if (player != null && player.isOnline()) {
			player.sendPluginMessage(plugin, TehoProtocol.CHANNEL, frame);
		}
	}

	@Override
	public boolean online(UUID id) {
		Player player = Bukkit.getPlayer(id);
		return player != null && player.isOnline();
	}

	@Override
	public String name(UUID id) {
		Player player = Bukkit.getPlayer(id);
		return player == null ? id.toString() : player.getName();
	}

	@Override
	public String serverName() {
		try {
			String name = Bukkit.getServerName();
			if (name != null && !name.trim().isEmpty()) {
				return name;
			}
		} catch (NoSuchMethodError removed) {
		}
		String motd = org.bukkit.ChatColor.stripColor(Bukkit.getMotd());
		if (motd == null || motd.trim().isEmpty()) {
			return "Minecraft Server";
		}
		String firstLine = motd.split("\n")[0].trim();
		return firstLine.length() > 32 ? firstLine.substring(0, 32) : firstLine;
	}

	@Override
	public void runLater(UUID id, Runnable task, long delayMs) {
		long ticks = Math.max(1, delayMs / 50);
		if (!FOLIA) {
			Bukkit.getScheduler().runTaskLater(plugin, task, ticks);
			return;
		}
		Player player = Bukkit.getPlayer(id);
		if (player == null) {
			return;
		}
		runOnEntity(player, task, ticks);
	}

	void runOnEntity(Player player, Runnable task, long ticks) {
		if (!FOLIA) {
			Bukkit.getScheduler().runTaskLater(plugin, task, Math.max(1, ticks));
			return;
		}
		try {
			Object scheduler = Player.class.getMethod("getScheduler").invoke(player);
			Class<?> type = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
			Method runDelayed = type.getMethod("runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class);
			Consumer<Object> body = ignored -> task.run();
			runDelayed.invoke(scheduler, plugin, body, null, Math.max(1, ticks));
		} catch (ReflectiveOperationException failed) {
			warn("Could not schedule on the Folia entity scheduler", failed);
		}
	}

	@Override
	public void info(String message) {
		plugin.getLogger().info(message);
	}

	@Override
	public void warn(String message, Throwable failed) {
		plugin.getLogger().log(Level.WARNING, message, failed);
	}

	private static boolean present(String name) {
		try {
			Class.forName(name);
			return true;
		} catch (ClassNotFoundException absent) {
			return false;
		}
	}
}
