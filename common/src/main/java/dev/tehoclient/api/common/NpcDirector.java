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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import dev.tehoclient.api.npc.NpcApi;
import dev.tehoclient.api.npc.NpcEmote;
import dev.tehoclient.api.npc.NpcLook;
import dev.tehoclient.api.protocol.Message;
import dev.tehoclient.api.protocol.NpcClear;
import dev.tehoclient.api.protocol.NpcSlot;
import dev.tehoclient.api.protocol.TehoProtocol;

final class NpcDirector implements NpcApi {
	static final int MAX_NPCS = 64;

	private final Supplier<Iterable<TehoSession>> sessions;
	private final Map<UUID, NpcLook> looks = new LinkedHashMap<>();
	private final Map<UUID, NpcEmote> emotes = new LinkedHashMap<>();

	NpcDirector(Supplier<Iterable<TehoSession>> sessions) {
		this.sessions = sessions;
	}

	@Override
	public synchronized void look(UUID npc, NpcLook look) {
		if (look.mode() == NpcLook.Mode.CLEAR) {
			looks.remove(npc);
		} else {
			if (!looks.containsKey(npc) && looks.size() >= MAX_NPCS) {
				throw new IllegalStateException("At most " + MAX_NPCS + " NPCs can carry Teho cosmetics");
			}
			looks.put(npc, look);
		}
		broadcast(lookMessage(npc, look));
	}

	@Override
	public synchronized void emote(UUID npc, NpcEmote emote) {
		if (!emotes.containsKey(npc) && emotes.size() >= MAX_NPCS) {
			throw new IllegalStateException("At most " + MAX_NPCS + " NPCs can perform emotes");
		}
		if (emote.looping()) {
			emotes.put(npc, emote);
		} else {
			emotes.remove(npc);
		}
		broadcast(new dev.tehoclient.api.protocol.NpcEmote(npc.toString(), TehoProtocol.NPC_EMOTE_START, emote.emote(),
				emote.looping()));
	}

	@Override
	public synchronized void stopEmote(UUID npc) {
		emotes.remove(npc);
		broadcast(new dev.tehoclient.api.protocol.NpcEmote(npc.toString(), TehoProtocol.NPC_EMOTE_STOP, "", false));
	}

	@Override
	public synchronized void resetEmote(UUID npc) {
		broadcast(new dev.tehoclient.api.protocol.NpcEmote(npc.toString(), TehoProtocol.NPC_EMOTE_RESET, "", false));
	}

	@Override
	public synchronized void remove(UUID npc) {
		looks.remove(npc);
		emotes.remove(npc);
		broadcast(new NpcClear(Collections.singletonList(npc.toString())));
	}

	@Override
	public synchronized void clear() {
		looks.clear();
		emotes.clear();
		broadcast(new NpcClear(Collections.<String>emptyList()));
	}

	@Override
	public synchronized Map<UUID, NpcLook> looks() {
		return Collections.unmodifiableMap(new LinkedHashMap<>(looks));
	}

	@Override
	public synchronized Map<UUID, NpcEmote> emotes() {
		return Collections.unmodifiableMap(new LinkedHashMap<>(emotes));
	}

	synchronized void replay(TehoSession session) {
		if (!session.supports(TehoProtocol.NPC)) {
			return;
		}
		for (Map.Entry<UUID, NpcLook> entry : looks.entrySet()) {
			session.deliver(TehoProtocol.NPC, lookMessage(entry.getKey(), entry.getValue()));
		}
		for (Map.Entry<UUID, NpcEmote> entry : emotes.entrySet()) {
			session.deliver(TehoProtocol.NPC, new dev.tehoclient.api.protocol.NpcEmote(entry.getKey().toString(),
					TehoProtocol.NPC_EMOTE_START, entry.getValue().emote(), true));
		}
	}

	private void broadcast(Message message) {
		for (TehoSession session : sessions.get()) {
			session.deliver(TehoProtocol.NPC, message);
		}
	}

	private static Message lookMessage(UUID npc, NpcLook look) {
		int mode;
		switch (look.mode()) {
			case MIRROR_VIEWER:
				mode = TehoProtocol.NPC_MODE_MIRROR;
				break;
			case CLEAR:
				mode = TehoProtocol.NPC_MODE_CLEAR;
				break;
			default:
				mode = TehoProtocol.NPC_MODE_EXPLICIT;
		}
		List<NpcSlot> slots = new ArrayList<>();
		for (Map.Entry<String, String> slot : look.slots().entrySet()) {
			slots.add(new NpcSlot(slot.getKey(), slot.getValue()));
		}
		return new dev.tehoclient.api.protocol.NpcLook(npc.toString(), mode, slots);
	}
}
