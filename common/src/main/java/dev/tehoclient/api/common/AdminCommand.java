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

package dev.tehoclient.api.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.hud.Cooldown;
import dev.tehoclient.api.hud.Timer;
import dev.tehoclient.api.modules.ModuleRules;
import dev.tehoclient.api.notify.Notification;
import dev.tehoclient.api.staff.StaffGrant;
import dev.tehoclient.api.staff.StaffTool;
import dev.tehoclient.api.waypoint.Waypoint;

public final class AdminCommand {
	private static final String AQUA = "§b";
	private static final String GRAY = "§7";
	private static final String RED = "§c";
	private static final List<String> SUBCOMMANDS = Arrays.asList("list", "reload", "notify", "waypoint", "unwaypoint",
			"staff", "rules", "cooldown", "timer", "npc", "title", "bar", "tint", "shake", "fade", "letterbox", "presence", "transfer",
			"verify", "integrity", "info", "clip", "showcase", "debug");

	public interface Sender {
		String name();

		void reply(String legacyText);

		Waypoint.Builder here(String waypointId);
	}

	private final TehoCore core;
	private final Function<String, UUID> lookup;
	private final Runnable reload;
	private final FeatureCommand features;

	public AdminCommand(TehoCore core, Function<String, UUID> lookup, Runnable reload) {
		this.core = core;
		this.lookup = lookup;
		this.reload = reload;
		this.features = new FeatureCommand(core, this::targets);
	}

	public void execute(Sender sender, String label, String[] args) {
		if (args.length == 0) {
			help(sender, label);
			return;
		}
		boolean done;
		try {
			done = run(sender, args);
		} catch (NumberFormatException bad) {
			sender.reply(RED + "Not a number: " + bad.getMessage());
			return;
		}
		if (!done) {
			help(sender, label);
		}
	}

	public List<String> suggest(String[] args, Collection<String> names) {
		List<String> out = new ArrayList<>();
		if (args.length <= 1) {
			String prefix = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
			for (String sub : SUBCOMMANDS) {
				if (sub.startsWith(prefix)) {
					out.add(sub);
				}
			}
		} else if (args.length == 2 && !"list".equalsIgnoreCase(args[0]) && !"reload".equalsIgnoreCase(args[0])) {
			String prefix = args[1].toLowerCase(Locale.ROOT);
			if ("*".startsWith(prefix)) {
				out.add("*");
			}
			for (String name : names) {
				if (name.toLowerCase(Locale.ROOT).startsWith(prefix)) {
					out.add(name);
				}
			}
		}
		return out;
	}

	private boolean run(Sender sender, String[] args) {
		switch (args[0].toLowerCase(Locale.ROOT)) {
			case "list":
				sender.reply(AQUA + "Teho players: " + core.players().size());
				for (TehoPlayer teho : core.players()) {
					sender.reply(GRAY + " " + teho + " staff=" + teho.staff().granted() + " accepted="
							+ teho.staff().accepted() + " active=" + teho.staff().active());
				}
				return true;
			case "reload":
				reload.run();
				sender.reply(AQUA + "TehoAPI reloaded: " + core.features());
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
				if (args.length >= 2 && "list".equalsIgnoreCase(args[1])) {
					NpcCommand.list(core.npcs(), sender::reply);
					return true;
				}
				if (args.length < 3) {
					return false;
				}
				UUID npc = NpcCommand.uuid(args[1]);
				if (npc == null) {
					sender.reply(RED + "Give the NPC's UUID");
					return true;
				}
				return NpcCommand.run(core.npcs(), core.hasGrant(), npc, args, 2, sender::reply);
			default:
				return features.run(sender::reply, args);
		}
	}

	private void help(Sender sender, String label) {
		String p = AQUA + "/" + label + " ";
		sender.reply(p + "list" + GRAY + " | " + AQUA + "reload");
		sender.reply(p + "notify <player|*> <title> [body]");
		sender.reply(p + "waypoint <player|*> <id> [name]" + GRAY + "  (at your position)");
		sender.reply(p + "unwaypoint <player|*> <id|*>");
		sender.reply(p + "staff [player] [all|off|xray|player_esp|nametags|freecam...]" + GRAY + "  (no arguments: every tool for you)");
		sender.reply(p + "rules <player|*> competitive <allowed...> | disable <modules...> | clear");
		sender.reply(p + "cooldown <player|*> <id> <seconds> [item] | clear");
		sender.reply(p + "timer <player|*> <id> <seconds|stopwatch|off> [label]");
		sender.reply(p + NpcCommand.USAGE.replace("<uuid|near>", "<uuid>") + GRAY + " | " + AQUA + "npc list");
		FeatureCommand.help(sender::reply, p);
	}

	private boolean notify(Sender sender, String[] args) {
		if (args.length < 3) {
			return false;
		}
		Notification notification = Notification.builder("cmd-" + System.currentTimeMillis())
				.title(colors(args[2].replace('_', ' ')))
				.body(colors(join(args, 3)))
				.build();
		int sent = 0;
		for (TehoPlayer teho : targets(args[1])) {
			sent += teho.notify(notification) ? 1 : 0;
		}
		sender.reply(AQUA + "Notified " + sent + " Teho player(s)");
		return true;
	}

	private boolean waypoint(Sender sender, String[] args) {
		if (args.length < 3) {
			return false;
		}
		Waypoint.Builder here = sender.here(args[2]);
		if (here == null) {
			sender.reply(RED + "Run it in game on a server with a world; the waypoint goes where you stand");
			return true;
		}
		Waypoint waypoint = here.name(args.length > 3 ? join(args, 3) : args[2]).focus(true).build();
		int sent = 0;
		for (TehoPlayer teho : targets(args[1])) {
			sent += teho.waypoints().set(waypoint) ? 1 : 0;
		}
		sender.reply(AQUA + "Waypoint sent to " + sent + " Teho player(s)");
		return true;
	}

	private boolean staff(Sender sender, String[] args) {
		if (args.length == 1) {
			args = new String[] {args[0], sender.name(), "all"};
		} else if (args.length == 2) {
			boolean toolWord = "all".equalsIgnoreCase(args[1]) || "off".equalsIgnoreCase(args[1]) || StaffTool.byId(args[1]) != null;
			args = toolWord ? new String[] {args[0], sender.name(), args[1]} : new String[] {args[0], args[1], "all"};
		}
		List<TehoPlayer> targets = targets(args[1]);
		if (targets.isEmpty()) {
			sender.reply(RED + "No Teho player called " + args[1] + " is online");
			return true;
		}
		if ("off".equalsIgnoreCase(args[2])) {
			for (TehoPlayer teho : targets) {
				teho.staff().revokeAll();
			}
			sender.reply(AQUA + "Revoked staff tools from " + targets.size() + " player(s)");
			return true;
		}
		Set<StaffTool> tools = EnumSet.noneOf(StaffTool.class);
		for (String id : Arrays.copyOfRange(args, 2, args.length)) {
			if ("all".equalsIgnoreCase(id)) {
				tools.addAll(EnumSet.allOf(StaffTool.class));
				continue;
			}
			StaffTool tool = StaffTool.byId(id);
			if (tool == null) {
				sender.reply(RED + "Unknown tool " + id);
				return true;
			}
			tools.add(tool);
		}
		int sent = 0;
		for (TehoPlayer teho : targets) {
			sent += teho.staff().grant(StaffGrant.of(tools).reason("Granted by " + sender.name()).build()) ? 1 : 0;
		}
		sender.reply(AQUA + "Granted " + tools + " to " + sent + " Teho player(s); they must accept");
		return true;
	}

	private boolean rules(Sender sender, String[] args) {
		if (args.length < 3) {
			return false;
		}
		String mode = args[2].toLowerCase(Locale.ROOT);
		String[] rest = Arrays.copyOfRange(args, 3, args.length);
		if (!"clear".equals(mode) && !"competitive".equals(mode) && !"disable".equals(mode)) {
			return false;
		}
		int sent = 0;
		for (TehoPlayer teho : targets(args[1])) {
			boolean ok;
			if ("clear".equals(mode)) {
				ok = teho.modules().apply(ModuleRules.NONE);
			} else if ("competitive".equals(mode)) {
				ok = teho.modules().competitive("Competitive mode", rest);
			} else {
				ok = teho.modules().update(builder -> builder.disable(rest).reason("Disabled by staff"));
			}
			sent += ok ? 1 : 0;
		}
		sender.reply(AQUA + "Rules sent to " + sent + " Teho player(s)");
		return true;
	}

	private boolean cooldown(Sender sender, String[] args) {
		if (args.length < 3) {
			return false;
		}
		boolean clear = "clear".equalsIgnoreCase(args[2]);
		if (!clear && args.length < 4) {
			return false;
		}
		int sent = 0;
		for (TehoPlayer teho : targets(args[1])) {
			if (clear) {
				sent += teho.hud().clearCooldowns() ? 1 : 0;
				continue;
			}
			long millis = (long) (Double.parseDouble(args[3]) * 1000);
			Cooldown.Builder cooldown = Cooldown.builder(args[2], millis).label(args[2].replace('_', ' '));
			if (args.length > 4) {
				cooldown.item(args[4]);
			}
			sent += teho.hud().cooldown(cooldown.build()) ? 1 : 0;
		}
		sender.reply(AQUA + "Cooldown sent to " + sent + " Teho player(s)");
		return true;
	}

	private boolean timer(Sender sender, String[] args) {
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
		sender.reply(AQUA + "Timer sent to " + sent + " Teho player(s)");
		return true;
	}

	private List<TehoPlayer> targets(String selector) {
		List<TehoPlayer> out = new ArrayList<>();
		if ("*".equals(selector)) {
			out.addAll(core.players());
			return out;
		}
		UUID id = lookup.apply(selector);
		if (id != null) {
			core.player(id).ifPresent(out::add);
		}
		return out;
	}

	static String colors(String text) {
		char[] chars = text.toCharArray();
		for (int i = 0; i < chars.length - 1; i++) {
			if (chars[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(chars[i + 1]) >= 0) {
				chars[i] = '§';
				chars[i + 1] = Character.toLowerCase(chars[i + 1]);
			}
		}
		return new String(chars);
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
