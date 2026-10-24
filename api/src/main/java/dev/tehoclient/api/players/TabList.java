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

package dev.tehoclient.api.players;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import dev.tehoclient.api.Units;
import dev.tehoclient.api.asset.Asset;
import dev.tehoclient.api.protocol.TabEntry;

public final class TabList {
	private final dev.tehoclient.api.protocol.TabList wire;
	private final List<Asset> icons;

	private TabList(dev.tehoclient.api.protocol.TabList wire, List<Asset> icons) {
		this.wire = wire;
		this.icons = Collections.unmodifiableList(icons);
	}

	public static Builder replace() {
		return new Builder(true);
	}

	public static Builder update() {
		return new Builder(false);
	}

	public static TabList reset() {
		return new TabList(new dev.tehoclient.api.protocol.TabList(null, null, null, false, true, null, true),
				Collections.<Asset>emptyList());
	}

	public List<Asset> icons() {
		return icons;
	}

	public dev.tehoclient.api.protocol.TabList wire() {
		return wire;
	}

	public static final class Entry {
		private final UUID player;
		private String display = "";
		private String group = "";
		private Asset icon;
		private String ping = "";
		private int order;
		private int color;

		private Entry(UUID player) {
			this.player = player;
		}

		public static Entry of(UUID player) {
			if (player == null) {
				throw new IllegalArgumentException("player is required");
			}
			return new Entry(player);
		}

		public Entry display(String display) {
			this.display = display == null ? "" : display;
			return this;
		}

		public Entry group(String group) {
			this.group = group == null ? "" : group;
			return this;
		}

		public Entry icon(Asset icon) {
			if (icon != null && icon.kind() != Asset.Kind.PNG) {
				throw new IllegalArgumentException("tab icons must be PNG assets");
			}
			this.icon = icon;
			return this;
		}

		public Entry ping(String text) {
			this.ping = text == null ? "" : text;
			return this;
		}

		public Entry order(int order) {
			this.order = order;
			return this;
		}

		public Entry color(int argb) {
			this.color = Units.argb(argb);
			return this;
		}
	}

	public static final class Builder {
		private final boolean replace;
		private final List<String> header = new ArrayList<>();
		private final List<String> footer = new ArrayList<>();
		private final List<Entry> entries = new ArrayList<>();
		private final List<String> remove = new ArrayList<>();
		private boolean hidePing;

		private Builder(boolean replace) {
			this.replace = replace;
		}

		public Builder header(String... lines) {
			header.addAll(Arrays.asList(lines));
			return this;
		}

		public Builder footer(String... lines) {
			footer.addAll(Arrays.asList(lines));
			return this;
		}

		public Builder entry(Entry entry) {
			entries.add(entry);
			return this;
		}

		public Builder remove(UUID player) {
			remove.add(Units.id(player));
			return this;
		}

		public Builder hidePing(boolean hide) {
			this.hidePing = hide;
			return this;
		}

		public TabList build() {
			List<TabEntry> wire = new ArrayList<>();
			List<Asset> icons = new ArrayList<>();
			for (Entry entry : entries) {
				if (entry.icon != null) {
					icons.add(entry.icon);
				}
				wire.add(new TabEntry(entry.player.toString(), entry.display, entry.group,
						entry.icon == null ? new byte[0] : entry.icon.hash(), entry.ping, entry.order, entry.color));
			}
			return new TabList(new dev.tehoclient.api.protocol.TabList(header, footer, wire, hidePing, replace, remove, false), icons);
		}
	}
}
