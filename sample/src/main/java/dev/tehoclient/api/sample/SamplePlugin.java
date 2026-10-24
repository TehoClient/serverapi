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

package dev.tehoclient.api.sample;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.bukkit.BukkitTeho;
import dev.tehoclient.api.bukkit.event.TehoPlayerReadyEvent;
import dev.tehoclient.api.bukkit.event.TehoStaffToolEvent;
import dev.tehoclient.api.hud.Cooldown;
import dev.tehoclient.api.hud.Timer;
import dev.tehoclient.api.notify.Notification;
import dev.tehoclient.api.staff.StaffTool;

public final class SamplePlugin extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		getCommand("tehotest").setExecutor(new TestCommand(this));
	}

	@EventHandler
	public void onClientEvent(dev.tehoclient.api.bukkit.event.TehoClientEvent event) {
		if ("hologram.click".equals(event.getKind()) && "test_holo".equals(event.getTarget())) {
			TestCommand.clicked(event.getTehoPlayer(), event.getPlayer());
		}
	}

	@EventHandler
	public void onKey(dev.tehoclient.api.bukkit.event.TehoKeyEvent event) {
		if (event.isPressed()) {
			event.getPlayer().sendMessage("§bKeybind pressed: §f" + event.getKeybind());
		}
	}

	@EventHandler
	public void onReady(TehoPlayerReadyEvent event) {
		TehoPlayer teho = event.getTehoPlayer();
		Player player = event.getPlayer();
		teho.notify(Notification.builder("welcome")
				.title("Welcome, " + player.getName())
				.body("This server speaks Teho")
				.accent(0x4DA9FF)
				.duration(8000)
				.build());
		Location spawn = player.getWorld().getSpawnLocation();
		teho.waypoints().set(BukkitTeho.waypoint("spawn", spawn).name("Spawn").color(0x7CDB8A).focus(true).build());
		teho.hud().timer(Timer.stopwatch("session", "Session", 0).color(0xFFC24D));
	}

	@EventHandler
	public void onPearl(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if (event.getItem() == null || event.getItem().getType() != Material.ENDER_PEARL) {
			return;
		}
		BukkitTeho.player(event.getPlayer()).ifPresent(teho -> teho.hud().cooldown(
				Cooldown.builder("ender_pearl", 15000).label("Ender Pearl").item("ender_pearl").color(0x5FE0DC).build()));
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		Location where = player.getLocation();
		BukkitTeho.player(player).ifPresent(teho -> {
			teho.waypoints().set(BukkitTeho.waypoint("death", where).name("Last death").color(0xFF5555).focus(true).build());
			teho.notify(Notification.builder("death")
					.title("You died")
					.body("Marked at " + where.getBlockX() + " " + where.getBlockY() + " " + where.getBlockZ())
					.accent(0xFF5555)
					.build());
		});
	}

	@EventHandler
	public void onStaffTool(TehoStaffToolEvent event) {
		if (event.getTool() == StaffTool.FREECAM) {
			getLogger().info(event.getPlayer().getName() + (event.isActive() ? " entered" : " left") + " freecam");
		}
	}
}
