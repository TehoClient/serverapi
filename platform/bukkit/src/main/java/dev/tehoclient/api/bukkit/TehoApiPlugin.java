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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import dev.tehoclient.api.TehoListener;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.bukkit.event.TehoClientEvent;
import dev.tehoclient.api.bukkit.event.TehoConsentEvent;
import dev.tehoclient.api.bukkit.event.TehoPlayerReadyEvent;
import dev.tehoclient.api.bukkit.event.TehoStaffToolEvent;
import dev.tehoclient.api.common.ProxyKey;
import dev.tehoclient.api.common.ProxyMessage;
import dev.tehoclient.api.common.ProxyReceiver;
import dev.tehoclient.api.common.TehoCore;
import dev.tehoclient.api.hud.Cooldown;
import dev.tehoclient.api.hud.Timer;
import dev.tehoclient.api.modules.ModuleRules;
import dev.tehoclient.api.notify.Notification;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.staff.StaffGrant;
import dev.tehoclient.api.staff.StaffTool;
import dev.tehoclient.api.waypoint.Waypoint;

public final class TehoApiPlugin extends JavaPlugin implements Listener, PluginMessageListener, TehoListener {
	private BukkitBridge bridge;
	private TehoCore core;
	private ProxyReceiver proxyReceiver;
	private final Presets presets = new Presets();
	private StaffKit staffKit;
	private dev.tehoclient.api.common.FeatureSettings featureSettings = new dev.tehoclient.api.common.FeatureSettings();
	private dev.tehoclient.api.common.FeatureCommand featureCommand;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		bridge = new BukkitBridge(this);
		core = new TehoCore(bridge);
		proxyReceiver = new ProxyReceiver(core, null);
		staffKit = new StaffKit(this, core);
		featureCommand = new dev.tehoclient.api.common.FeatureCommand(core, this::targets);
		loadSettings();
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, TehoProtocol.CHANNEL);
		Bukkit.getMessenger().registerIncomingPluginChannel(this, TehoProtocol.CHANNEL, this);
		Bukkit.getMessenger().registerIncomingPluginChannel(this, ProxyMessage.CHANNEL, this);
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(staffKit, this);
		core.addListener(this);
		core.addListener(staffKit);
		core.install();
		getLogger().info("Teho Server API protocol " + TehoProtocol.VERSION + " on " + TehoProtocol.CHANNEL
				+ (BukkitBridge.FOLIA ? " (Folia)" : "") + ", features " + core.features()
				+ (proxyReceiver.enabled() ? ", accepting signed proxy handoffs" : ""));
	}

	@Override
	public void onDisable() {
		if (staffKit != null) {
			staffKit.shutdown();
		}
		if (core != null) {
			core.uninstall();
		}
		Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
		Bukkit.getMessenger().unregisterOutgoingPluginChannel(this);
	}

	private void loadSettings() {
		reloadConfig();
		List<String> features = new ArrayList<>();
		addIf(features, "features.notifications", TehoProtocol.NOTIFY);
		addIf(features, "features.waypoints", TehoProtocol.WAYPOINT);
		addIf(features, "features.modules", TehoProtocol.MODULES);
		addIf(features, "features.staff", TehoProtocol.STAFF);
		addIf(features, "features.hud", TehoProtocol.HUD);
		addIf(features, "features.suspect", TehoProtocol.SUSPECT);
		addIf(features, "features.npcs", TehoProtocol.NPC);
		java.util.Map<String, Object> tree = tree(getConfig());
		features.addAll(dev.tehoclient.api.common.FeatureSettings.features(tree));
		core.configure(getConfig().getString("server-name", ""), features, getConfig().getBoolean("debug", false));
		featureSettings = dev.tehoclient.api.common.FeatureSettings.load(core, tree, getLogger()::warning);
		presets.load(getConfig(), getLogger());
		staffKit.load(getConfig(), getLogger());
		core.grant(getConfig().getString("private.grant", ""));
		loadNpcs();
		try {
			proxyReceiver.key(ProxyKey.orNull(getConfig().getString("proxy.secret", "")));
		} catch (IllegalArgumentException weak) {
			proxyReceiver.key(null);
			getLogger().warning("Ignoring proxy.secret: " + weak.getMessage());
		}
	}

	static java.util.Map<String, Object> tree(org.bukkit.configuration.ConfigurationSection section) {
		java.util.Map<String, Object> out = new java.util.LinkedHashMap<>();
		for (String key : section.getKeys(false)) {
			Object value = section.get(key);
			out.put(key, value instanceof org.bukkit.configuration.ConfigurationSection
					? tree((org.bukkit.configuration.ConfigurationSection) value) : value);
		}
		return out;
	}

	private void addIf(List<String> features, String path, String capability) {
		if (getConfig().getBoolean(path, true)) {
			features.add(capability);
		}
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] frame) {
		if (TehoProtocol.CHANNEL.equals(channel)) {
			core.onMessage(player.getUniqueId(), frame);
		} else if (ProxyMessage.CHANNEL.equals(channel)) {
			proxyReceiver.onProxyFrame(player.getUniqueId(), frame);
		}
	}

	@EventHandler
	public void onRegister(PlayerRegisterChannelEvent event) {
		if (TehoProtocol.CHANNEL.equals(event.getChannel())) {
			core.onChannelRegistered(event.getPlayer().getUniqueId());
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		core.onQuit(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		core.player(player.getUniqueId()).ifPresent(teho -> presets.applyRules(player, teho));
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		if (!presets.deathWaypoint()) {
			return;
		}
		Player player = event.getEntity();
		Location where = player.getLocation();
		core.player(player.getUniqueId()).ifPresent(teho -> teho.waypoints().set(
				BukkitTeho.waypoint("tehoapi-death", where).name("Last death").color(0xFF5555).focus(true).build()));
	}

	@Override
	public void onReady(TehoPlayer teho) {
		Player player = Bukkit.getPlayer(teho.uniqueId());
		if (player == null) {
			return;
		}
		presets.onReady(player, teho);
		featureSettings.onReady(teho);
		Bukkit.getPluginManager().callEvent(new TehoPlayerReadyEvent(teho));
	}

	@Override
	public void onConsent(TehoPlayer teho, String scope, List<String> items, boolean granted) {
		getLogger().info(teho.name() + (granted ? " accepted " : " declined ") + scope + " " + items);
		Bukkit.getPluginManager().callEvent(new TehoConsentEvent(teho, scope, items, granted));
	}

	@Override
	public void onStaffTool(TehoPlayer teho, StaffTool tool, boolean active) {
		Bukkit.getPluginManager().callEvent(new TehoStaffToolEvent(teho, tool, active));
	}

	@Override
	public void onKey(TehoPlayer teho, String keybind, boolean pressed, long heldMs) {
		Bukkit.getPluginManager().callEvent(new dev.tehoclient.api.bukkit.event.TehoKeyEvent(teho, keybind, pressed, heldMs));
	}

	@Override
	public void onUi(TehoPlayer teho, dev.tehoclient.api.ui.UiInteraction interaction) {
		Bukkit.getPluginManager().callEvent(new dev.tehoclient.api.bukkit.event.TehoUiEvent(teho, interaction));
	}

	@Override
	public void onHologramClick(TehoPlayer teho, String hologram) {
		Bukkit.getPluginManager().callEvent(new TehoClientEvent(teho, "hologram.click", hologram, ""));
	}

	@Override
	public void onEvent(TehoPlayer teho, String kind, String target, String value) {
		Bukkit.getPluginManager().callEvent(new TehoClientEvent(teho, kind, target, value));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			help(sender, label);
			return true;
		}
		String sub = args[0].toLowerCase(Locale.ROOT);
		switch (sub) {
			case "list":
				sender.sendMessage(ChatColor.AQUA + "Teho players: " + core.players().size());
				for (TehoPlayer teho : core.players()) {
					sender.sendMessage(ChatColor.GRAY + " " + teho + " staff=" + teho.staff().granted()
							+ " accepted=" + teho.staff().accepted() + " active=" + teho.staff().active());
				}
				return true;
			case "reload":
				loadSettings();
				for (TehoPlayer teho : core.players()) {
					Player player = Bukkit.getPlayer(teho.uniqueId());
					if (player != null) {
						presets.applyRules(player, teho);
						presets.applyStaff(player, teho);
					}
				}
				sender.sendMessage(ChatColor.AQUA + "TehoAPI reloaded: " + core.features());
				return true;
			case "notify":
				return notify(sender, args);
			case "waypoint":
				return waypoint(sender, args);
			case "unwaypoint":
				if (args.length < 3) {
					return false;
				}
				for (TehoPlayer teho : targets(args[1])) {
					if ("*".equals(args[2])) {
						teho.waypoints().clear();
					} else {
						teho.waypoints().remove(args[2]);
					}
				}
				return true;
			case "staff":
				return staff(sender, args);
			case "rules":
				return rules(sender, args);
			case "cooldown":
				return cooldown(sender, args);
			case "timer":
				return timer(sender, args);
			case "npc":
				return npc(sender, args);
			case "alert":
			case "freeze":
			case "screenshot":
			case "modcheck":
			case "lagmap":
			case "trail":
			case "spectate":
			case "glow":
				return staffKit(sender, sub, args);
			default:
				if (featureCommand.run(sender::sendMessage, args)) {
					return true;
				}
				help(sender, label);
				return true;
		}
	}

	private void help(CommandSender sender, String label) {
		String p = ChatColor.AQUA + "/" + label + " ";
		sender.sendMessage(p + "list" + ChatColor.GRAY + " | " + ChatColor.AQUA + "reload");
		sender.sendMessage(p + "notify <player|*> <title> [body]");
		sender.sendMessage(p + "waypoint <player|*> <id> [name]" + ChatColor.GRAY + "  (at your position)");
		sender.sendMessage(p + "unwaypoint <player|*> <id|*>");
		sender.sendMessage(p + "staff <player> <xray|player_esp|nametags|freecam...|off>");
		sender.sendMessage(p + "rules <player|*> competitive <allowed...> | disable <modules...> | clear");
		sender.sendMessage(p + "cooldown <player|*> <id> <seconds> [item] | clear");
		sender.sendMessage(p + "timer <player|*> <id> <seconds|stopwatch|off> [label]");
		sender.sendMessage(p + dev.tehoclient.api.common.NpcCommand.USAGE + ChatColor.GRAY + " | " + ChatColor.AQUA + "npc list");
		sender.sendMessage(p + "staff <player> all" + ChatColor.GRAY + "  (every tool, incl. inspector, wheel, alerts)");
		sender.sendMessage(p + "alert <player> <info|warn|high> <title> [body]" + ChatColor.GRAY + "  (for anticheat hooks)");
		sender.sendMessage(p + "freeze <player>" + ChatColor.GRAY + " | " + ChatColor.AQUA + "screenshot <player>"
				+ ChatColor.GRAY + " | " + ChatColor.AQUA + "modcheck <player>");
		sender.sendMessage(p + "lagmap" + ChatColor.GRAY + " | " + ChatColor.AQUA + "trail <suspect>" + ChatColor.GRAY
				+ " | " + ChatColor.AQUA + "spectate <suspect|off>" + ChatColor.GRAY + " | " + ChatColor.AQUA + "glow <suspect> [label]");
		dev.tehoclient.api.common.FeatureCommand.help(sender::sendMessage, p);
	}

	private void loadNpcs() {
		if (!core.hasGrant()) {
			return;
		}
		for (java.util.Map<?, ?> entry : getConfig().getMapList("npcs")) {
			java.util.UUID npc = dev.tehoclient.api.common.NpcCommand.uuid(String.valueOf(entry.get("uuid")));
			if (npc == null) {
				getLogger().warning("Ignoring NPC preset without a valid uuid: " + entry);
				continue;
			}
			try {
				if (Boolean.TRUE.equals(entry.get("mirror"))) {
					core.npcs().mirrorViewer(npc);
				} else if (entry.get("look") instanceof List) {
					dev.tehoclient.api.npc.NpcLook.Builder look = dev.tehoclient.api.npc.NpcLook.wearing();
					for (Object key : (List<?>) entry.get("look")) {
						look.cosmetic(String.valueOf(key));
					}
					core.npcs().look(npc, look.build());
				}
				if (entry.get("emote") != null) {
					String emote = String.valueOf(entry.get("emote"));
					core.npcs().emote(npc, Boolean.FALSE.equals(entry.get("loop"))
							? dev.tehoclient.api.npc.NpcEmote.once(emote) : dev.tehoclient.api.npc.NpcEmote.loop(emote));
				}
			} catch (RuntimeException bad) {
				getLogger().warning("Ignoring NPC preset " + entry + ": " + bad.getMessage());
			}
		}
	}

	private boolean npc(CommandSender sender, String[] args) {
		if (args.length >= 2 && "list".equalsIgnoreCase(args[1])) {
			dev.tehoclient.api.common.NpcCommand.list(core.npcs(), sender::sendMessage);
			return true;
		}
		if (args.length < 3) {
			sender.sendMessage(ChatColor.AQUA + "/tehoapi " + dev.tehoclient.api.common.NpcCommand.USAGE);
			return true;
		}
		java.util.UUID npc;
		if ("near".equalsIgnoreCase(args[1])) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Only a player can target the nearest NPC");
				return true;
			}
			org.bukkit.entity.Entity found = nearestNpc((Player) sender);
			if (found == null) {
				sender.sendMessage(ChatColor.RED + "No player-model NPC within 10 blocks");
				return true;
			}
			npc = found.getUniqueId();
		} else {
			npc = dev.tehoclient.api.common.NpcCommand.uuid(args[1]);
			if (npc == null) {
				sender.sendMessage(ChatColor.RED + "Give the NPC's UUID or use near");
				return true;
			}
		}
		if (!dev.tehoclient.api.common.NpcCommand.run(core.npcs(), core.hasGrant(), npc, args, 2, sender::sendMessage)) {
			sender.sendMessage(ChatColor.AQUA + "/tehoapi " + dev.tehoclient.api.common.NpcCommand.USAGE);
		}
		return true;
	}

	private static org.bukkit.entity.Entity nearestNpc(Player viewer) {
		org.bukkit.entity.Entity best = null;
		double bestDistance = Double.MAX_VALUE;
		for (org.bukkit.entity.Entity entity : viewer.getNearbyEntities(10, 6, 10)) {
			if (!(entity instanceof Player)) {
				continue;
			}
			boolean npc = entity.hasMetadata("NPC") || Bukkit.getPlayer(entity.getUniqueId()) != entity;
			if (!npc) {
				continue;
			}
			double distance = entity.getLocation().distanceSquared(viewer.getLocation());
			if (distance < bestDistance) {
				bestDistance = distance;
				best = entity;
			}
		}
		return best;
	}

	private boolean staffKit(CommandSender sender, String sub, String[] args) {
		if ("alert".equals(sub)) {
			if (args.length < 4) {
				return false;
			}
			Player subject = Bukkit.getPlayerExact(args[1]);
			if (subject == null) {
				sender.sendMessage(ChatColor.RED + "No online player " + args[1]);
				return true;
			}
			dev.tehoclient.api.staff.StaffAlert.Severity severity;
			try {
				severity = dev.tehoclient.api.staff.StaffAlert.Severity.valueOf(args[2].toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException bad) {
				severity = dev.tehoclient.api.staff.StaffAlert.Severity.WARN;
			}
			staffKit.alert(subject, severity, ChatColor.translateAlternateColorCodes('&', args[3].replace('_', ' ')),
					ChatColor.translateAlternateColorCodes('&', join(args, 4)));
			return true;
		}
		if (args.length < 2 && !"lagmap".equals(sub)) {
			return false;
		}
		Player subject = args.length > 1 ? Bukkit.getPlayerExact(args[1]) : null;
		switch (sub) {
			case "freeze":
				if (subject == null) {
					sender.sendMessage(ChatColor.RED + "No online player " + args[1]);
				} else {
					staffKit.toggleFreeze(sender, subject);
				}
				return true;
			case "screenshot":
				if (subject == null) {
					sender.sendMessage(ChatColor.RED + "No online player " + args[1]);
				} else {
					staffKit.screenshot(sender, subject);
				}
				return true;
			case "modcheck":
				if (subject == null) {
					sender.sendMessage(ChatColor.RED + "No online player " + args[1]);
				} else {
					staffKit.modList(sender, subject);
				}
				return true;
			default:
				break;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Run it in game; it shows on your Teho Client");
			return true;
		}
		Player self = (Player) sender;
		TehoPlayer teho = core.player(self.getUniqueId()).orElse(null);
		if (teho == null) {
			sender.sendMessage(ChatColor.RED + "You are not on Teho");
			return true;
		}
		switch (sub) {
			case "lagmap":
				staffKit.lagMap(self, teho);
				return true;
			case "trail":
				if (subject == null || !staffKit.sendTrail(teho, subject)) {
					sender.sendMessage(ChatColor.RED + "No movement history for " + args[1]);
				}
				return true;
			case "spectate":
				if ("off".equalsIgnoreCase(args[1])) {
					teho.staff().stopSpectating();
				} else if (subject != null) {
					teho.staff().spectate(subject.getUniqueId(), 6);
				}
				return true;
			case "glow":
				if (subject != null) {
					staffKit.glow(teho, subject, 0xFF5555, args.length > 2 ? join(args, 2) : subject.getName());
				}
				return true;
			default:
				return false;
		}
	}

	private boolean notify(CommandSender sender, String[] args) {
		if (args.length < 3) {
			return false;
		}
		Notification notification = Notification.builder("cmd-" + System.currentTimeMillis())
				.title(ChatColor.translateAlternateColorCodes('&', args[2].replace('_', ' ')))
				.body(ChatColor.translateAlternateColorCodes('&', join(args, 3)))
				.build();
		int sent = 0;
		for (TehoPlayer teho : targets(args[1])) {
			sent += teho.notify(notification) ? 1 : 0;
		}
		sender.sendMessage(ChatColor.AQUA + "Notified " + sent + " Teho player(s)");
		return true;
	}

	private boolean waypoint(CommandSender sender, String[] args) {
		if (args.length < 3) {
			return false;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Run it in game; the waypoint goes where you stand");
			return true;
		}
		Waypoint waypoint = BukkitTeho.waypoint(args[2], ((Player) sender).getLocation())
				.name(args.length > 3 ? join(args, 3) : args[2])
				.focus(true)
				.build();
		int sent = 0;
		for (TehoPlayer teho : targets(args[1])) {
			sent += teho.waypoints().set(waypoint) ? 1 : 0;
		}
		sender.sendMessage(ChatColor.AQUA + "Waypoint sent to " + sent + " Teho player(s)");
		return true;
	}

	private boolean staff(CommandSender sender, String[] args) {
		if (args.length < 3) {
			return false;
		}
		List<TehoPlayer> targets = targets(args[1]);
		if ("all".equalsIgnoreCase(args[2])) {
			int sent = 0;
			for (TehoPlayer teho : targets) {
				sent += teho.staff().grant(StaffGrant.everything().reason("Granted by " + sender.getName()).build()) ? 1 : 0;
			}
			sender.sendMessage(ChatColor.AQUA + "Granted every staff tool to " + sent + " Teho player(s); they must accept");
			return true;
		}
		if ("off".equalsIgnoreCase(args[2])) {
			for (TehoPlayer teho : targets) {
				teho.staff().revokeAll();
			}
			sender.sendMessage(ChatColor.AQUA + "Revoked staff tools from " + targets.size() + " player(s)");
			return true;
		}
		Set<StaffTool> tools = EnumSet.noneOf(StaffTool.class);
		for (String id : Arrays.copyOfRange(args, 2, args.length)) {
			StaffTool tool = StaffTool.byId(id);
			if (tool == null) {
				sender.sendMessage(ChatColor.RED + "Unknown tool " + id);
				return true;
			}
			tools.add(tool);
		}
		int sent = 0;
		for (TehoPlayer teho : targets) {
			sent += teho.staff().grant(StaffGrant.of(tools).reason("Granted by " + sender.getName()).build()) ? 1 : 0;
		}
		sender.sendMessage(ChatColor.AQUA + "Granted " + tools + " to " + sent + " Teho player(s); they must accept");
		return true;
	}

	private boolean rules(CommandSender sender, String[] args) {
		if (args.length < 3) {
			return false;
		}
		String mode = args[2].toLowerCase(Locale.ROOT);
		String[] rest = Arrays.copyOfRange(args, 3, args.length);
		int sent = 0;
		for (TehoPlayer teho : targets(args[1])) {
			boolean ok;
			if ("clear".equals(mode)) {
				ok = teho.modules().apply(ModuleRules.NONE);
			} else if ("competitive".equals(mode)) {
				ok = teho.modules().competitive("Competitive mode", rest);
			} else if ("disable".equals(mode)) {
				ok = teho.modules().update(builder -> builder.disable(rest).reason("Disabled by staff"));
			} else {
				return false;
			}
			sent += ok ? 1 : 0;
		}
		sender.sendMessage(ChatColor.AQUA + "Rules sent to " + sent + " Teho player(s)");
		return true;
	}

	private boolean cooldown(CommandSender sender, String[] args) {
		if (args.length < 3) {
			return false;
		}
		int sent = 0;
		for (TehoPlayer teho : targets(args[1])) {
			if ("clear".equalsIgnoreCase(args[2])) {
				sent += teho.hud().clearCooldowns() ? 1 : 0;
				continue;
			}
			if (args.length < 4) {
				return false;
			}
			long millis = (long) (Double.parseDouble(args[3]) * 1000);
			Cooldown.Builder cooldown = Cooldown.builder(args[2], millis).label(args[2].replace('_', ' '));
			if (args.length > 4) {
				cooldown.item(args[4]);
			}
			sent += teho.hud().cooldown(cooldown.build()) ? 1 : 0;
		}
		sender.sendMessage(ChatColor.AQUA + "Cooldown sent to " + sent + " Teho player(s)");
		return true;
	}

	private boolean timer(CommandSender sender, String[] args) {
		if (args.length < 4) {
			return false;
		}
		String label = args.length > 4 ? join(args, 4) : args[2];
		int sent = 0;
		for (TehoPlayer teho : targets(args[1])) {
			if ("off".equalsIgnoreCase(args[3])) {
				sent += teho.hud().removeTimer(args[2]) ? 1 : 0;
			} else if ("stopwatch".equalsIgnoreCase(args[3])) {
				sent += teho.hud().timer(Timer.stopwatch(args[2], label, 0)) ? 1 : 0;
			} else {
				long millis = (long) (Double.parseDouble(args[3]) * 1000);
				sent += teho.hud().timer(Timer.countdown(args[2], label, millis)) ? 1 : 0;
			}
		}
		sender.sendMessage(ChatColor.AQUA + "Timer sent to " + sent + " Teho player(s)");
		return true;
	}

	private List<TehoPlayer> targets(String selector) {
		List<TehoPlayer> out = new ArrayList<>();
		if ("*".equals(selector)) {
			out.addAll(core.players());
			return out;
		}
		Player player = Bukkit.getPlayerExact(selector);
		if (player != null) {
			core.player(player.getUniqueId()).ifPresent(out::add);
		}
		return out;
	}

	private static String join(String[] args, int from) {
		StringBuilder out = new StringBuilder();
		for (int i = from; i < args.length; i++) {
			if (out.length() > 0) {
				out.append(' ');
			}
			out.append(args[i]);
		}
		return out.toString();
	}
}
