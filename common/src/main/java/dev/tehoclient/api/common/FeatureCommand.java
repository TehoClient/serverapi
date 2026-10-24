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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.hud.Bar;
import dev.tehoclient.api.hud.Title;
import dev.tehoclient.api.info.ClientInfo;
import dev.tehoclient.api.social.Presence;

public final class FeatureCommand {
	public static final List<String> SUBCOMMANDS = Collections.unmodifiableList(Arrays.asList("title", "bar", "tint", "shake", "fade",
			"letterbox", "presence", "transfer", "verify", "integrity", "info", "clip", "showcase", "debug"));

	private static final String AQUA = "§b";
	private static final String GRAY = "§7";
	private static final String RED = "§c";

	private final TehoCore core;
	private final Function<String, List<TehoPlayer>> targets;

	public FeatureCommand(TehoCore core, Function<String, List<TehoPlayer>> targets) {
		this.core = core;
		this.targets = targets;
	}

	public static void help(Consumer<String> reply, String prefix) {
		reply.accept(prefix + "title <player|*> <title> [| subtitle]" + GRAY + "  (animated title)");
		reply.accept(prefix + "bar <player|*> <id> <0-100|off> [text]" + GRAY + " | " + AQUA + "tint <player|*> <#rrggbb|off>");
		reply.accept(prefix + "shake <player|*> [intensity] [ms]" + GRAY + " | " + AQUA + "fade <player|*> [#rrggbb]"
				+ GRAY + " | " + AQUA + "letterbox <player|*> <on|off>");
		reply.accept(prefix + "presence <player|*> <game> [| state]" + GRAY + " | " + AQUA + "transfer <player> <host[:port]>");
		reply.accept(prefix + "verify <player>" + GRAY + " | " + AQUA + "integrity <player>" + GRAY + " | " + AQUA + "info <player>");
		reply.accept(prefix + "clip <player|*> <title>" + GRAY + "  (save a highlight)");
		reply.accept(prefix + "showcase <player> <slot:id...>" + GRAY + " | " + AQUA + "debug" + GRAY + "  (log every frame)");
	}

	public boolean run(Consumer<String> reply, String[] args) {
		if (args.length == 0) {
			return false;
		}
		String sub = args[0].toLowerCase(Locale.ROOT);
		if (!SUBCOMMANDS.contains(sub)) {
			return false;
		}
		if ("debug".equals(sub)) {
			core.debug(!core.debug());
			reply.accept(AQUA + "Frame logging " + (core.debug() ? "on" : "off"));
			return true;
		}
		if (args.length < 2) {
			reply.accept(RED + "Name a player, or * for everyone on Teho");
			return true;
		}
		List<TehoPlayer> players = targets.apply(args[1]);
		if (players.isEmpty()) {
			reply.accept(RED + "No Teho player matches " + args[1]);
			return true;
		}
		String rest = join(args, 2);
		int delivered = 0;
		for (TehoPlayer player : players) {
			if (apply(reply, player, sub, args, rest)) {
				delivered++;
			}
		}
		if (!"verify".equals(sub) && !"integrity".equals(sub) && !"info".equals(sub) && !"clip".equals(sub)) {
			reply.accept(AQUA + sub + GRAY + " sent to " + delivered + "/" + players.size() + " Teho players");
		}
		return true;
	}

	private boolean apply(Consumer<String> reply, TehoPlayer player, String sub, String[] args, String rest) {
		switch (sub) {
			case "title": {
				String[] parts = rest.split("\\|", 2);
				return player.hud().title(Title.of(AdminCommand.colors(parts[0].trim())).subtitle(parts.length > 1 ? AdminCommand.colors(parts[1].trim()) : "")
						.animation(Title.Animation.POP).build());
			}
			case "bar":
				if (args.length < 4) {
					return false;
				}
				if ("off".equalsIgnoreCase(args[3])) {
					return player.hud().removeBars(args[2]);
				}
				return player.hud().bar(Bar.builder(args[2], join(args, 4), Double.parseDouble(args[3]) / 100.0).build());
			case "tint":
				if ("off".equalsIgnoreCase(rest)) {
					return player.hud().clearTint();
				}
				return player.hud().tint(color(rest, 0xFF5555), true, 0.6, 400, 4000);
			case "shake":
				return player.camera().shake(args.length > 2 ? Double.parseDouble(args[2]) : 1.0, args.length > 3 ? Long.parseLong(args[3]) : 600, 18);
			case "fade":
				return player.camera().fade(color(rest, 0x000000), 400, 600, 400);
			case "letterbox":
				return player.camera().letterbox(!"off".equalsIgnoreCase(rest), 0.12, 400);
			case "presence": {
				String[] parts = rest.split("\\|", 2);
				return player.social().presence(Presence.builder(parts[0].trim()).state(parts.length > 1 ? parts[1].trim() : "").startedNow().build());
			}
			case "transfer": {
				if (args.length < 3) {
					return false;
				}
				String[] address = args[2].split(":", 2);
				return player.social().transfer(address[0], address.length > 1 ? Integer.parseInt(address[1]) : 25565, "", "", "");
			}
			case "verify":
				player.verify().identity().thenAccept(proof -> reply.accept(AQUA + player.name() + GRAY + " identity: " + proof));
				return true;
			case "integrity":
				player.verify().integrity().thenAccept(proof -> reply.accept(AQUA + player.name() + GRAY + " integrity: " + proof));
				return true;
			case "info":
				player.info().request("Requested by staff", ClientInfo.Field.values()).whenComplete((info, failure) ->
						reply.accept(AQUA + player.name() + GRAY + " " + (info == null ? String.valueOf(failure) : info)));
				return true;
			case "clip":
				player.clips().mark("clip-" + System.currentTimeMillis(), rest.isEmpty() ? "Highlight" : rest, 15000, 3000)
						.thenAccept(clip -> reply.accept(AQUA + player.name() + GRAY + " " + clip));
				return true;
			case "showcase":
				if (args.length < 3) {
					return false;
				}
				return player.showcase().open("Showcase", "", Arrays.asList(Arrays.copyOfRange(args, 2, args.length)), "", "");
			default:
				return false;
		}
	}

	private static int color(String text, int fallback) {
		String hex = text == null ? "" : text.trim();
		if (hex.startsWith("#")) {
			hex = hex.substring(1);
		}
		try {
			return hex.matches("[0-9a-fA-F]{6}") ? Integer.parseInt(hex, 16) : fallback;
		} catch (NumberFormatException bad) {
			return fallback;
		}
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
