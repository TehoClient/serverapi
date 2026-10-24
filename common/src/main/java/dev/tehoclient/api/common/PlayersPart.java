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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dev.tehoclient.api.asset.Asset;
import dev.tehoclient.api.players.Nametag;
import dev.tehoclient.api.players.PlayersApi;
import dev.tehoclient.api.players.TabList;
import dev.tehoclient.api.players.Team;
import dev.tehoclient.api.protocol.NametagClear;
import dev.tehoclient.api.protocol.NickHint;
import dev.tehoclient.api.protocol.TabEntry;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.protocol.TeamRemove;

final class PlayersPart extends Part implements PlayersApi {
	static final int MAX_NAMETAGS = 512;
	static final int MAX_TEAMS = 64;

	private final Map<String, Nametag> nametags = new LinkedHashMap<>();
	private final Map<String, Team> teams = new LinkedHashMap<>();
	private final Map<String, TabEntry> tabEntries = new LinkedHashMap<>();
	private final Map<String, Asset> tabIcons = new LinkedHashMap<>();
	private List<String> tabHeader = Collections.emptyList();
	private List<String> tabFooter = Collections.emptyList();
	private boolean tabHidePing;
	private boolean tabActive;
	private String nick = "";

	PlayersPart(TehoSession session) {
		super(session);
	}

	@Override
	public synchronized boolean nametag(Nametag nametag) {
		String player = nametag.wire().player;
		if (!nametags.containsKey(player) && nametags.size() >= MAX_NAMETAGS) {
			throw new IllegalStateException("At most " + MAX_NAMETAGS + " custom nametags per player");
		}
		nametags.put(player, nametag);
		return send(TehoProtocol.PLAYERS, nametag.wire(), nametag.badge());
	}

	@Override
	public synchronized boolean clearNametags(UUID... players) {
		List<String> ids = new ArrayList<>();
		for (UUID player : players) {
			ids.add(player.toString());
			nametags.remove(player.toString());
		}
		if (players.length == 0) {
			nametags.clear();
		}
		return send(TehoProtocol.PLAYERS, new NametagClear(ids));
	}

	@Override
	public synchronized boolean tabList(TabList tabList) {
		dev.tehoclient.api.protocol.TabList wire = tabList.wire();
		if (wire.reset) {
			tabEntries.clear();
			tabIcons.clear();
			tabHeader = Collections.emptyList();
			tabFooter = Collections.emptyList();
			tabHidePing = false;
			tabActive = false;
			return send(TehoProtocol.PLAYERS, wire);
		}
		if (wire.replace) {
			tabEntries.clear();
			tabIcons.clear();
			tabHeader = wire.header;
			tabFooter = wire.footer;
			tabHidePing = wire.hidePing;
		} else {
			if (!wire.header.isEmpty()) {
				tabHeader = wire.header;
			}
			if (!wire.footer.isEmpty()) {
				tabFooter = wire.footer;
			}
			tabHidePing = wire.hidePing;
		}
		for (String removed : wire.remove) {
			tabEntries.remove(removed);
			tabIcons.remove(removed);
		}
		for (TabEntry entry : wire.entries) {
			tabEntries.put(entry.player, entry);
		}
		for (Asset icon : tabList.icons()) {
			for (TabEntry entry : wire.entries) {
				if (Arrays.equals(entry.icon, icon.hash())) {
					tabIcons.put(entry.player, icon);
				}
			}
		}
		tabActive = true;
		return send(TehoProtocol.PLAYERS, wire, tabList.icons());
	}

	@Override
	public synchronized boolean team(Team team) {
		if (!teams.containsKey(team.id()) && teams.size() >= MAX_TEAMS) {
			throw new IllegalStateException("At most " + MAX_TEAMS + " teams per player");
		}
		teams.put(team.id(), team);
		return send(TehoProtocol.PLAYERS, team.wire());
	}

	@Override
	public synchronized boolean removeTeams(String... ids) {
		if (ids.length == 0) {
			teams.clear();
		}
		for (String id : ids) {
			teams.remove(id);
		}
		return send(TehoProtocol.PLAYERS, new TeamRemove(Arrays.asList(ids)));
	}

	@Override
	public synchronized boolean nick(String nick) {
		this.nick = nick == null ? "" : nick;
		return send(TehoProtocol.PLAYERS, new NickHint(this.nick, session.uniqueId().toString()));
	}

	@Override
	synchronized void reapply() {
		for (Nametag nametag : nametags.values()) {
			send(TehoProtocol.PLAYERS, nametag.wire(), nametag.badge());
		}
		if (tabActive) {
			send(TehoProtocol.PLAYERS, new dev.tehoclient.api.protocol.TabList(tabHeader, tabFooter,
					new ArrayList<>(tabEntries.values()), tabHidePing, true, null, false), tabIcons.values());
		}
		for (Team team : teams.values()) {
			send(TehoProtocol.PLAYERS, team.wire());
		}
		if (!nick.isEmpty()) {
			send(TehoProtocol.PLAYERS, new NickHint(nick, session.uniqueId().toString()));
		}
	}

	@Override
	synchronized void reset() {
		nametags.clear();
		teams.clear();
		tabEntries.clear();
		tabIcons.clear();
		tabHeader = Collections.emptyList();
		tabFooter = Collections.emptyList();
		tabActive = false;
		nick = "";
	}
}
