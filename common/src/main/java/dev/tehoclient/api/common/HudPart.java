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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dev.tehoclient.api.Units;
import dev.tehoclient.api.asset.Asset;
import dev.tehoclient.api.hud.Bar;
import dev.tehoclient.api.hud.Sidebar;
import dev.tehoclient.api.hud.Title;
import dev.tehoclient.api.protocol.BarRemove;
import dev.tehoclient.api.protocol.KillFeed;
import dev.tehoclient.api.protocol.ScreenTint;
import dev.tehoclient.api.protocol.SidebarRow;
import dev.tehoclient.api.protocol.SidebarSet;
import dev.tehoclient.api.protocol.Signal;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.protocol.TntFuse;

final class HudPart extends Part {
	static final int MAX_BARS = 8;

	private String sidebarTitle;
	private int sidebarAccent;
	private String sidebarFooter = "";
	private final Map<String, SidebarRow> rows = new LinkedHashMap<>();
	private final Map<String, Asset> rowIcons = new LinkedHashMap<>();
	private final Map<String, Bar> bars = new LinkedHashMap<>();
	private final Map<String, Long> barEnds = new LinkedHashMap<>();
	private ScreenTint tint;
	private long tintEnds;
	private int tntFuse;

	HudPart(TehoSession session) {
		super(session);
	}

	synchronized boolean sidebar(Sidebar sidebar) {
		SidebarSet wire = sidebar.wire();
		if (wire.replace) {
			rows.clear();
			rowIcons.clear();
			sidebarTitle = wire.title;
			sidebarAccent = wire.accent;
			sidebarFooter = wire.footer;
		} else {
			if (sidebarTitle == null) {
				sidebarTitle = "";
			}
			if (!wire.title.isEmpty()) {
				sidebarTitle = wire.title;
			}
			if (wire.accent != 0) {
				sidebarAccent = wire.accent;
			}
			if (!wire.footer.isEmpty()) {
				sidebarFooter = wire.footer;
			}
		}
		for (String id : wire.remove) {
			rows.remove(id);
			rowIcons.remove(id);
		}
		for (SidebarRow row : wire.rows) {
			rows.put(row.id, row);
		}
		for (Asset icon : sidebar.assets()) {
			for (SidebarRow row : wire.rows) {
				if (Arrays.equals(row.asset, icon.hash())) {
					rowIcons.put(row.id, icon);
				}
			}
		}
		if (rows.size() > Sidebar.MAX_ROWS) {
			throw new IllegalStateException("a sidebar holds at most " + Sidebar.MAX_ROWS + " rows");
		}
		return send(TehoProtocol.HUD, wire, sidebar.assets());
	}

	synchronized boolean clearSidebar() {
		sidebarTitle = null;
		rows.clear();
		rowIcons.clear();
		return send(TehoProtocol.HUD, Signal.SIDEBAR_CLEAR);
	}

	synchronized boolean bar(Bar bar) {
		if (!bars.containsKey(bar.id()) && bars.size() >= MAX_BARS) {
			throw new IllegalStateException("At most " + MAX_BARS + " bars per player");
		}
		bars.put(bar.id(), bar);
		if (bar.wire().ttlMs > 0) {
			barEnds.put(bar.id(), System.currentTimeMillis() + bar.wire().ttlMs);
		} else {
			barEnds.remove(bar.id());
		}
		return send(TehoProtocol.HUD, bar.wire());
	}

	synchronized boolean removeBars(String... ids) {
		if (ids.length == 0) {
			bars.clear();
			barEnds.clear();
		}
		for (String id : ids) {
			bars.remove(id);
			barEnds.remove(id);
		}
		return send(TehoProtocol.HUD, new BarRemove(Arrays.asList(ids)));
	}

	boolean kill(String killer, UUID killerId, String victim, UUID victimId, String weapon, int argb) {
		return send(TehoProtocol.HUD, new KillFeed(killer, victim, Units.item(weapon), "", argb == 0 ? 0 : Units.argb(argb),
				Units.id(killerId), Units.id(victimId), 6000));
	}

	boolean feed(String text, int argb) {
		return send(TehoProtocol.HUD, new KillFeed("", "", "", text, argb == 0 ? 0 : Units.argb(argb), "", "", 6000));
	}

	synchronized boolean tint(int argb, boolean vignette, double strength, long fadeMs, long ttlMs) {
		tint = new ScreenTint(Units.argb(argb), vignette, Math.min(10000, Units.millis(fadeMs)), Units.millis(ttlMs),
				Math.max(0, Math.min(100, Units.percent(strength))));
		tintEnds = ttlMs > 0 ? System.currentTimeMillis() + ttlMs : 0;
		return send(TehoProtocol.HUD, tint);
	}

	synchronized boolean clearTint() {
		tint = null;
		return send(TehoProtocol.HUD, new ScreenTint(0, false, 300, 0, 0));
	}

	boolean title(Title title) {
		return send(TehoProtocol.HUD, title.wire());
	}

	synchronized boolean tntFuse(int ticks) {
		tntFuse = Math.max(0, Math.min(1200, ticks));
		return send(TehoProtocol.HUD, new TntFuse(tntFuse));
	}

	@Override
	synchronized void reapply() {
		long now = System.currentTimeMillis();
		if (sidebarTitle != null) {
			send(TehoProtocol.HUD, new SidebarSet(sidebarTitle, new ArrayList<>(rows.values()), new ArrayList<String>(), true, sidebarAccent,
					sidebarFooter), rowIcons.values());
		}
		List<String> expired = new ArrayList<>();
		for (Bar bar : bars.values()) {
			Long ends = barEnds.get(bar.id());
			if (ends != null && ends <= now) {
				expired.add(bar.id());
				continue;
			}
			dev.tehoclient.api.protocol.Bar wire = bar.wire();
			int ttl = ends == null ? 0 : (int) (ends - now);
			send(TehoProtocol.HUD, new dev.tehoclient.api.protocol.Bar(wire.id, wire.text, wire.progress, wire.color, wire.style, ttl, wire.item));
		}
		for (String id : expired) {
			bars.remove(id);
			barEnds.remove(id);
		}
		if (tint != null && (tintEnds == 0 || tintEnds > now)) {
			send(TehoProtocol.HUD, new ScreenTint(tint.color, tint.vignette, 0, tintEnds == 0 ? 0 : (int) (tintEnds - now), tint.strength));
		}
		if (tntFuse > 0) {
			send(TehoProtocol.HUD, new TntFuse(tntFuse));
		}
	}

	@Override
	synchronized void reset() {
		sidebarTitle = null;
		rows.clear();
		rowIcons.clear();
		bars.clear();
		barEnds.clear();
		tint = null;
		tntFuse = 0;
	}
}
