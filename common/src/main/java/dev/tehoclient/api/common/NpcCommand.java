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

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import dev.tehoclient.api.npc.NpcApi;
import dev.tehoclient.api.npc.NpcEmote;
import dev.tehoclient.api.npc.NpcLook;

public final class NpcCommand {
	public static final String USAGE = "npc <uuid|near> look <slot:id...> | mirror | clear | emote <id> [loop|once] | stop | reset | remove";
	private static final String AQUA = "§b";
	private static final String GRAY = "§7";
	private static final String RED = "§c";

	private NpcCommand() {}

	public static boolean run(NpcApi npcs, boolean granted, UUID npc, String[] args, int from, Consumer<String> reply) {
		if (!granted) {
			reply.accept(RED + "NPC cosmetics need a Teho-approved private grant (private.grant in the config)");
			return true;
		}
		if (args.length <= from) {
			return false;
		}
		String action = args[from].toLowerCase(Locale.ROOT);
		try {
			switch (action) {
				case "look":
					if (args.length <= from + 1) {
						return false;
					}
					NpcLook.Builder look = NpcLook.wearing();
					for (int i = from + 1; i < args.length; i++) {
						look.cosmetic(args[i]);
					}
					npcs.look(npc, look.build());
					reply.accept(AQUA + "NPC " + npc + " now wears " + look.build().slots());
					return true;
				case "mirror":
					npcs.mirrorViewer(npc);
					reply.accept(AQUA + "NPC " + npc + " now mirrors each viewer's own cosmetics");
					return true;
				case "clear":
					npcs.clearLook(npc);
					reply.accept(AQUA + "Cleared the cosmetics on NPC " + npc);
					return true;
				case "emote":
					if (args.length <= from + 1) {
						return false;
					}
					boolean loop = args.length > from + 2 && "loop".equalsIgnoreCase(args[from + 2]);
					npcs.emote(npc, loop ? NpcEmote.loop(args[from + 1]) : NpcEmote.once(args[from + 1]));
					reply.accept(AQUA + "NPC " + npc + (loop ? " is looping " : " plays once ") + args[from + 1]);
					return true;
				case "stop":
					npcs.stopEmote(npc);
					reply.accept(AQUA + "Stopped the emote on NPC " + npc);
					return true;
				case "reset":
					npcs.resetEmote(npc);
					reply.accept(AQUA + "Restarted the emote on NPC " + npc);
					return true;
				case "remove":
					npcs.remove(npc);
					reply.accept(AQUA + "Removed every Teho effect from NPC " + npc);
					return true;
				default:
					return false;
			}
		} catch (IllegalArgumentException | IllegalStateException refused) {
			reply.accept(RED + refused.getMessage());
			return true;
		}
	}

	public static void list(NpcApi npcs, Consumer<String> reply) {
		Map<UUID, NpcLook> looks = npcs.looks();
		Map<UUID, NpcEmote> emotes = npcs.emotes();
		reply.accept(AQUA + "Teho NPCs: " + looks.size() + " dressed, " + emotes.size() + " looping emotes");
		for (Map.Entry<UUID, NpcLook> entry : looks.entrySet()) {
			NpcEmote emote = emotes.get(entry.getKey());
			reply.accept(GRAY + " " + entry.getKey() + " " + entry.getValue().mode().name().toLowerCase(Locale.ROOT)
					+ " " + entry.getValue().slots() + (emote == null ? "" : " emote " + emote.emote()));
		}
	}

	public static UUID uuid(String raw) {
		try {
			return UUID.fromString(raw);
		} catch (IllegalArgumentException malformed) {
			return null;
		}
	}
}
