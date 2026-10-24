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

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import dev.tehoclient.api.TehoApi;
import dev.tehoclient.api.TehoListener;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.bukkit.event.TehoInspectEvent;
import dev.tehoclient.api.bukkit.event.TehoStaffActionEvent;
import dev.tehoclient.api.staff.Highlight;
import dev.tehoclient.api.staff.InspectCard;
import dev.tehoclient.api.staff.InspectResponder;
import dev.tehoclient.api.staff.LagCell;
import dev.tehoclient.api.staff.StaffAlert;
import dev.tehoclient.api.staff.StaffTool;
import dev.tehoclient.api.staff.StatRow;
import dev.tehoclient.api.staff.Target;
import dev.tehoclient.api.staff.WheelAction;

final class StaffKit implements Listener, TehoListener {
	private static final long WINDOW_MS = 1000;
	private static final long ACTIVE_MS = 5000;
	private static final int OVERLAY_RANGE = 48;
	private static final int LAG_RADIUS_CHUNKS = 16;

	private static final class Action {
		final WheelAction wheel;
		final String command;
		final boolean asStaff;
		final String builtin;

		Action(WheelAction wheel, String command, boolean asStaff, String builtin) {
			this.wheel = wheel;
			this.command = command;
			this.asStaff = asStaff;
			this.builtin = builtin;
		}
	}

	private static final class Combat {
		final Deque<Long> swings = new ArrayDeque<>();
		final Deque<long[]> reach = new ArrayDeque<>();
		long lastActive;
		int hits;
	}

	private final JavaPlugin plugin;
	private final TehoApi api;
	private final Map<String, Action> actions = new LinkedHashMap<>();
	private final Map<UUID, Deque<int[]>> trails = new ConcurrentHashMap<>();
	private final Map<UUID, Long> trailStamp = new ConcurrentHashMap<>();
	private final Map<UUID, Combat> combat = new ConcurrentHashMap<>();
	private final Set<UUID> frozen = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
	private String wheelTitle = "Staff";
	private boolean inspector = true;
	private boolean combatOverlay = true;
	private int trailSeconds = 300;
	private String freezeTitle = "";
	private String freezeBody = "";
	private int overlayTask = -1;

	StaffKit(JavaPlugin plugin, TehoApi api) {
		this.plugin = plugin;
		this.api = api;
	}

	void load(FileConfiguration config, Logger log) {
		actions.clear();
		wheelTitle = config.getString("staff.action-wheel.title", "Staff");
		inspector = config.getBoolean("staff.inspector", true);
		combatOverlay = config.getBoolean("staff.combat-overlay", true);
		trailSeconds = Math.max(10, Math.min(1800, config.getInt("staff.trail-seconds", 300)));
		freezeTitle = color(config.getString("staff.freeze.title", "&bYou are frozen"));
		freezeBody = color(config.getString("staff.freeze.body", "Do not log out. A staff member will speak to you."));
		for (Map<?, ?> entry : config.getMapList("staff.action-wheel.actions")) {
			try {
				String id = String.valueOf(entry.get("id"));
				Target.Kind kind = kind(entry.get("target"));
				String label = entry.containsKey("label") ? String.valueOf(entry.get("label")) : id;
				String item = entry.containsKey("item") ? String.valueOf(entry.get("item")) : "";
				WheelAction wheel = WheelAction.of(id, label, item, kind);
				if (entry.containsKey("color")) {
					wheel = wheel.color(Presets.hex(String.valueOf(entry.get("color"))));
				}
				String command = entry.containsKey("command") ? String.valueOf(entry.get("command")) : "";
				boolean asStaff = "staff".equalsIgnoreCase(String.valueOf(entry.get("as")));
				String builtin = entry.containsKey("builtin") ? String.valueOf(entry.get("builtin")).toLowerCase(Locale.ROOT) : "";
				actions.put(id, new Action(wheel, command, asStaff, builtin));
			} catch (RuntimeException bad) {
				log.warning("Ignoring action-wheel entry " + entry + ": " + bad.getMessage());
			}
		}
		if (overlayTask != -1) {
			Bukkit.getScheduler().cancelTask(overlayTask);
			overlayTask = -1;
		}
		if (combatOverlay && !BukkitBridge.FOLIA) {
			overlayTask = Bukkit.getScheduler().runTaskTimer(plugin, this::pushOverlays, 20L, 20L).getTaskId();
		}
	}

	void shutdown() {
		if (overlayTask != -1 && !BukkitBridge.FOLIA) {
			Bukkit.getScheduler().cancelTask(overlayTask);
		}
		overlayTask = -1;
	}

	void sendWheel(TehoPlayer teho) {
		if (actions.isEmpty() || !teho.staff().granted().contains(StaffTool.ACTION_WHEEL)) {
			return;
		}
		List<WheelAction> wheel = new ArrayList<>();
		for (Action action : actions.values()) {
			wheel.add(action.wheel);
		}
		teho.staff().actionWheel(wheelTitle, wheel);
	}

	@Override
	public void onConsent(TehoPlayer teho, String scope, List<String> items, boolean granted) {
		if (granted && "staff".equals(scope) && items.contains(StaffTool.ACTION_WHEEL.id())) {
			sendWheel(teho);
		}
	}

	@Override
	public void onInspect(TehoPlayer staff, Target target, InspectResponder responder) {
		Bukkit.getPluginManager().callEvent(new TehoInspectEvent(staff, target, responder));
		if (responder.answered() || !inspector) {
			return;
		}
		Player viewer = Bukkit.getPlayer(staff.uniqueId());
		if (viewer == null) {
			return;
		}
		switch (target.kind()) {
			case PLAYER:
				Player player = Bukkit.getPlayer(target.id());
				if (player == null) {
					responder.nothing();
				} else {
					responder.reply(playerCard(player));
				}
				return;
			case BLOCK:
				responder.reply(blockCard(viewer.getWorld().getBlockAt(target.x(), target.y(), target.z())));
				return;
			case ENTITY:
				Entity entity = entity(viewer.getWorld(), target.id());
				if (entity == null) {
					responder.nothing();
				} else {
					responder.reply(entityCard(entity));
				}
				return;
			default:
				responder.nothing();
		}
	}

	@Override
	public void onStaffAction(TehoPlayer staff, String id, Target target) {
		TehoStaffActionEvent event = new TehoStaffActionEvent(staff, id, target);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}
		Player viewer = Bukkit.getPlayer(staff.uniqueId());
		if (viewer == null) {
			return;
		}
		Action action = actions.get(id);
		String builtin = action == null ? id.toLowerCase(Locale.ROOT) : action.builtin;
		if (action != null && !action.command.isEmpty()) {
			String command = fill(action.command, viewer, target);
			if (command == null) {
				viewer.sendMessage(ChatColor.RED + "That action needs a " + action.wheel.target().name().toLowerCase(Locale.ROOT) + " target");
				return;
			}
			Bukkit.dispatchCommand(action.asStaff ? viewer : Bukkit.getConsoleSender(), command);
			if (builtin.isEmpty()) {
				return;
			}
		}
		builtin(viewer, staff, builtin, target);
	}

	private void builtin(Player viewer, TehoPlayer staff, String builtin, Target target) {
		Player subject = target.kind() == Target.Kind.PLAYER ? Bukkit.getPlayer(target.id()) : null;
		switch (builtin) {
			case "teleport":
				if (subject != null) {
					viewer.teleport(subject.getLocation());
				} else if (target.kind() == Target.Kind.BLOCK) {
					viewer.teleport(new Location(viewer.getWorld(), target.x() + 0.5, target.y() + 1, target.z() + 0.5,
							viewer.getLocation().getYaw(), viewer.getLocation().getPitch()));
				}
				return;
			case "spectate":
				if (subject != null) {
					staff.staff().spectate(subject.getUniqueId(), 6);
				}
				return;
			case "freeze":
				if (subject != null) {
					toggleFreeze(viewer, subject);
				}
				return;
			case "invsee":
				if (subject != null) {
					viewer.openInventory(subject.getInventory());
				}
				return;
			case "screenshot":
				if (subject != null) {
					screenshot(viewer, subject);
				}
				return;
			case "modlist":
				if (subject != null) {
					modList(viewer, subject);
				}
				return;
			case "trail":
				if (subject != null) {
					sendTrail(staff, subject);
				}
				return;
			case "lagmap":
				lagMap(viewer, staff);
				return;
			case "inspect":
				if (subject != null) {
					viewer.sendMessage(ChatColor.AQUA + subject.getName() + ChatColor.GRAY + " " + subject.getUniqueId());
				}
				return;
			default:
		}
	}

	void toggleFreeze(org.bukkit.command.CommandSender by, Player subject) {
		if (frozen.remove(subject.getUniqueId())) {
			api.player(subject.getUniqueId()).ifPresent(teho -> teho.suspect().unfreeze());
			subject.sendMessage(ChatColor.GREEN + "You are no longer frozen.");
			by.sendMessage(ChatColor.AQUA + "Unfroze " + subject.getName());
			return;
		}
		frozen.add(subject.getUniqueId());
		boolean screen = api.player(subject.getUniqueId()).map(teho -> teho.suspect().freeze(freezeTitle, freezeBody)).orElse(false);
		if (!screen) {
			subject.sendMessage(freezeTitle + ChatColor.RESET + " " + freezeBody);
		}
		by.sendMessage(ChatColor.AQUA + "Froze " + subject.getName() + (screen ? " (Teho freeze screen shown)" : ""));
	}

	void screenshot(org.bukkit.command.CommandSender by, Player subject) {
		TehoPlayer teho = api.player(subject.getUniqueId()).orElse(null);
		if (teho == null) {
			by.sendMessage(ChatColor.RED + subject.getName() + " is not on Teho");
			return;
		}
		by.sendMessage(ChatColor.AQUA + "Asked " + subject.getName() + " for a screenshot...");
		teho.suspect().screenshot("Requested by " + by.getName(), 1280).whenComplete((shot, failed) -> {
			if (failed != null) {
				by.sendMessage(ChatColor.RED + "Screenshot failed: " + failed.getMessage());
				return;
			}
			if (!shot.granted()) {
				by.sendMessage(ChatColor.YELLOW + subject.getName() + " declined the screenshot request");
				return;
			}
			try {
				File folder = new File(plugin.getDataFolder(), "screenshots");
				Files.createDirectories(folder.toPath());
				String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(new Date());
				File file = new File(folder, subject.getName() + "-" + stamp + ".png");
				Files.write(file.toPath(), shot.png());
				by.sendMessage(ChatColor.GREEN + "Screenshot " + shot.width() + "x" + shot.height() + " saved to " + file.getPath());
			} catch (Exception io) {
				by.sendMessage(ChatColor.RED + "Could not save the screenshot: " + io.getMessage());
			}
		});
	}

	void modList(org.bukkit.command.CommandSender by, Player subject) {
		TehoPlayer teho = api.player(subject.getUniqueId()).orElse(null);
		if (teho == null) {
			by.sendMessage(ChatColor.RED + subject.getName() + " is not on Teho");
			return;
		}
		teho.suspect().modList("Requested by " + by.getName()).whenComplete((result, failed) -> {
			if (failed != null) {
				by.sendMessage(ChatColor.RED + "Mod check failed: " + failed.getMessage());
			} else if (!result.granted()) {
				by.sendMessage(ChatColor.YELLOW + subject.getName() + " declined the mod check");
			} else {
				by.sendMessage(ChatColor.AQUA + subject.getName() + " mods (" + result.mods().size() + "): " + ChatColor.GRAY + result.mods());
				by.sendMessage(ChatColor.AQUA + "Teho modules on: " + ChatColor.GRAY + result.tehoModules());
			}
		});
	}

	boolean sendTrail(TehoPlayer staff, Player subject) {
		Deque<int[]> points = trails.get(subject.getUniqueId());
		if (points == null || points.isEmpty()) {
			return false;
		}
		List<int[]> copy;
		synchronized (points) {
			copy = new ArrayList<>(points);
		}
		return staff.staff().trail(subject.getUniqueId(), copy, 0xFF5555, subject.getName(),
				BukkitTeho.dimensionOf(subject.getWorld()), 120000);
	}

	boolean lagMap(Player viewer, TehoPlayer staff) {
		if (BukkitBridge.FOLIA) {
			viewer.sendMessage(ChatColor.RED + "The built-in lag map is not available on Folia");
			return false;
		}
		World world = viewer.getWorld();
		int centerX = viewer.getLocation().getBlockX() >> 4;
		int centerZ = viewer.getLocation().getBlockZ() >> 4;
		List<int[]> raw = new ArrayList<>();
		int max = 1;
		for (Chunk chunk : world.getLoadedChunks()) {
			if (Math.abs(chunk.getX() - centerX) > LAG_RADIUS_CHUNKS || Math.abs(chunk.getZ() - centerZ) > LAG_RADIUS_CHUNKS) {
				continue;
			}
			int entities = chunk.getEntities().length;
			int tiles = chunk.getTileEntities().length;
			int weight = entities + tiles * 2;
			if (weight < 8) {
				continue;
			}
			raw.add(new int[] {chunk.getX(), chunk.getZ(), weight, entities, tiles});
			max = Math.max(max, weight);
		}
		raw.sort((a, b) -> Integer.compare(b[2], a[2]));
		List<LagCell> cells = new ArrayList<>();
		for (int i = 0; i < raw.size() && i < 256; i++) {
			int[] cell = raw.get(i);
			cells.add(LagCell.of(cell[0], cell[1], (int) Math.round(100.0 * cell[2] / max),
					cell[3] + " entities, " + cell[4] + " tile entities"));
		}
		viewer.sendMessage(ChatColor.AQUA + "Lag map: " + cells.size() + " busy chunks within " + LAG_RADIUS_CHUNKS + " chunks");
		return staff.staff().lagMap(BukkitTeho.dimensionOf(world), cells, 120000);
	}

	void alert(Player subject, StaffAlert.Severity severity, String title, String body) {
		Location at = subject.getLocation();
		StaffAlert alert = StaffAlert.builder("alert-" + System.nanoTime(), title)
				.severity(severity)
				.body(subject.getName() + (body.isEmpty() ? "" : " — " + body))
				.player(subject.getUniqueId())
				.at(at.getBlockX(), at.getBlockY(), at.getBlockZ(), BukkitTeho.dimensionOf(at.getWorld()))
				.build();
		for (TehoPlayer teho : api.players()) {
			if (teho.staff().accepted().contains(StaffTool.ALERTS)) {
				teho.staff().alert(alert);
			}
		}
	}

	boolean glow(TehoPlayer staff, Player subject, int rgb, String label) {
		return staff.staff().highlight(Highlight.entity("player:" + subject.getUniqueId(), subject.getUniqueId())
				.color(rgb).label(label).tracer(true).style(Highlight.Style.BOTH).build());
	}

	@EventHandler(ignoreCancelled = true)
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location to = event.getTo();
		if (to == null) {
			return;
		}
		if (frozen.contains(player.getUniqueId())) {
			Location from = event.getFrom();
			if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
				Location back = from.clone();
				back.setYaw(to.getYaw());
				back.setPitch(to.getPitch());
				event.setTo(back);
			}
			return;
		}
		long now = System.currentTimeMillis();
		Long last = trailStamp.get(player.getUniqueId());
		if (last != null && now - last < 1000) {
			return;
		}
		trailStamp.put(player.getUniqueId(), now);
		Deque<int[]> points = trails.computeIfAbsent(player.getUniqueId(), ignored -> new ArrayDeque<>());
		synchronized (points) {
			points.addLast(new int[] {to.getBlockX(), to.getBlockY(), to.getBlockZ()});
			while (points.size() > trailSeconds) {
				points.pollFirst();
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSwing(PlayerAnimationEvent event) {
		if (!combatOverlay) {
			return;
		}
		Combat stats = combat.computeIfAbsent(event.getPlayer().getUniqueId(), ignored -> new Combat());
		long now = System.currentTimeMillis();
		synchronized (stats) {
			stats.swings.addLast(now);
			stats.lastActive = now;
			while (!stats.swings.isEmpty() && now - stats.swings.peekFirst() > WINDOW_MS) {
				stats.swings.pollFirst();
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onHit(EntityDamageByEntityEvent event) {
		if (!combatOverlay || !(event.getDamager() instanceof Player)) {
			return;
		}
		Player attacker = (Player) event.getDamager();
		Location eye = attacker.getEyeLocation();
		Location victim = event.getEntity().getLocation();
		if (eye.getWorld() != victim.getWorld()) {
			return;
		}
		double dx = victim.getX() - eye.getX();
		double dz = victim.getZ() - eye.getZ();
		double horizontal = Math.max(0, Math.sqrt(dx * dx + dz * dz) - 0.3);
		Combat stats = combat.computeIfAbsent(attacker.getUniqueId(), ignored -> new Combat());
		long now = System.currentTimeMillis();
		synchronized (stats) {
			stats.reach.addLast(new long[] {now, Math.round(horizontal * 100)});
			stats.hits++;
			stats.lastActive = now;
			while (!stats.reach.isEmpty() && now - stats.reach.peekFirst()[0] > ACTIVE_MS) {
				stats.reach.pollFirst();
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		UUID id = event.getPlayer().getUniqueId();
		trails.remove(id);
		trailStamp.remove(id);
		combat.remove(id);
	}

	private void pushOverlays() {
		long now = System.currentTimeMillis();
		for (TehoPlayer teho : api.players()) {
			if (!teho.staff().active().contains(StaffTool.COMBAT_OVERLAY)) {
				continue;
			}
			Player viewer = Bukkit.getPlayer(teho.uniqueId());
			if (viewer == null) {
				continue;
			}
			for (Player other : viewer.getWorld().getPlayers()) {
				if (other == viewer || other.getLocation().distanceSquared(viewer.getLocation()) > OVERLAY_RANGE * OVERLAY_RANGE) {
					continue;
				}
				Combat stats = combat.get(other.getUniqueId());
				if (stats == null) {
					continue;
				}
				List<StatRow> rows = new ArrayList<>();
				synchronized (stats) {
					if (now - stats.lastActive > ACTIVE_MS) {
						continue;
					}
					while (!stats.swings.isEmpty() && now - stats.swings.peekFirst() > WINDOW_MS) {
						stats.swings.pollFirst();
					}
					int cps = stats.swings.size();
					long best = 0;
					for (long[] sample : stats.reach) {
						best = Math.max(best, sample[1]);
					}
					rows.add(StatRow.of("CPS", cps, cps >= 16 ? 0xFF5555 : cps >= 12 ? 0xFFC24D : 0x7CDB8A));
					rows.add(StatRow.of("Reach", String.format(Locale.ROOT, "%.2f", best / 100.0),
							best > 310 ? 0xFF5555 : best > 300 ? 0xFFC24D : 0x7CDB8A));
					rows.add(StatRow.of("Hits", stats.hits));
				}
				teho.staff().targetStats(other.getUniqueId(), rows, 2500);
			}
		}
	}

	private InspectCard playerCard(Player player) {
		Location at = player.getLocation();
		InspectCard.Builder card = InspectCard.builder(player.getName())
				.subtitle(player.getUniqueId().toString())
				.row("Health", String.format(Locale.ROOT, "%.1f / %.1f", player.getHealth(), player.getMaxHealth()))
				.row("Game mode", player.getGameMode().name().toLowerCase(Locale.ROOT))
				.row("Location", at.getWorld().getName() + " " + at.getBlockX() + " " + at.getBlockY() + " " + at.getBlockZ())
				.row("Ping", ping(player) + " ms")
				.row("First joined", new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(new Date(player.getFirstPlayed())))
				.row("Flying", player.isFlying() ? "yes" : "no")
				.row("Operator", player.isOp() ? "yes" : "no", player.isOp() ? 0xFFC24D : 0);
		api.player(player.getUniqueId()).ifPresent(teho -> card.row("Client", teho.clientVersion() + " (" + teho.minecraftVersion() + ")", 0x4DA9FF));
		if (frozen.contains(player.getUniqueId())) {
			card.row("Frozen", "yes", 0x5FE0DC);
		}
		for (Action action : actions.values()) {
			if (action.wheel.target() == Target.Kind.PLAYER) {
				card.action(action.wheel.id(), action.wheel.label());
			}
		}
		return card.build();
	}

	private static InspectCard blockCard(Block block) {
		InspectCard.Builder card = InspectCard.builder(block.getType().name().toLowerCase(Locale.ROOT))
				.subtitle(block.getWorld().getName() + " " + block.getX() + " " + block.getY() + " " + block.getZ())
				.row("Biome", block.getBiome().name().toLowerCase(Locale.ROOT))
				.row("Light", block.getLightLevel());
		BlockState state = block.getState();
		if (state instanceof InventoryHolder) {
			int filled = 0;
			int items = 0;
			for (org.bukkit.inventory.ItemStack stack : ((InventoryHolder) state).getInventory().getContents()) {
				if (stack != null && stack.getAmount() > 0) {
					filled++;
					items += stack.getAmount();
				}
			}
			card.row("Contents", items + " items in " + filled + " slots", 0xFFC24D);
		}
		return card.action("teleport", "Teleport here").build();
	}

	private static InspectCard entityCard(Entity entity) {
		Location at = entity.getLocation();
		InspectCard.Builder card = InspectCard.builder(entity.getType().name().toLowerCase(Locale.ROOT))
				.subtitle(entity.getUniqueId().toString())
				.row("Location", at.getWorld().getName() + " " + at.getBlockX() + " " + at.getBlockY() + " " + at.getBlockZ())
				.row("Age", entity.getTicksLived() / 20 + " s");
		if (entity instanceof LivingEntity) {
			LivingEntity living = (LivingEntity) entity;
			card.row("Health", String.format(Locale.ROOT, "%.1f / %.1f", living.getHealth(), living.getMaxHealth()));
			if (living.getCustomName() != null) {
				card.row("Name", living.getCustomName());
			}
		}
		return card.build();
	}

	private static Entity entity(World world, UUID id) {
		try {
			Object found = Bukkit.class.getMethod("getEntity", UUID.class).invoke(null, id);
			if (found instanceof Entity) {
				return (Entity) found;
			}
		} catch (ReflectiveOperationException | RuntimeException absent) {
			for (Entity entity : world.getEntities()) {
				if (entity.getUniqueId().equals(id)) {
					return entity;
				}
			}
		}
		return null;
	}

	private static int ping(Player player) {
		try {
			return (Integer) player.getClass().getMethod("getPing").invoke(player);
		} catch (ReflectiveOperationException | RuntimeException modern) {
			try {
				Object handle = player.getClass().getMethod("getHandle").invoke(player);
				return handle.getClass().getField("ping").getInt(handle);
			} catch (ReflectiveOperationException | RuntimeException legacy) {
				return -1;
			}
		}
	}

	private static String fill(String command, Player staff, Target target) {
		String out = command.replace("{staff}", staff.getName());
		if (out.contains("{target}")) {
			if (target.kind() == Target.Kind.PLAYER) {
				Player player = Bukkit.getPlayer(target.id());
				if (player == null) {
					return null;
				}
				out = out.replace("{target}", player.getName()).replace("{uuid}", player.getUniqueId().toString());
			} else if (target.kind() == Target.Kind.ENTITY) {
				out = out.replace("{target}", target.id().toString());
			} else {
				return null;
			}
		}
		if (out.contains("{x}") || out.contains("{y}") || out.contains("{z}")) {
			if (target.kind() != Target.Kind.BLOCK) {
				return null;
			}
			out = out.replace("{x}", Integer.toString(target.x())).replace("{y}", Integer.toString(target.y()))
					.replace("{z}", Integer.toString(target.z()));
		}
		return out.startsWith("/") ? out.substring(1) : out;
	}

	private static Target.Kind kind(Object raw) {
		String value = raw == null ? "none" : String.valueOf(raw).toLowerCase(Locale.ROOT);
		switch (value) {
			case "player":
				return Target.Kind.PLAYER;
			case "block":
				return Target.Kind.BLOCK;
			case "entity":
				return Target.Kind.ENTITY;
			default:
				return Target.Kind.NONE;
		}
	}

	private static String color(String text) {
		return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
	}
}
