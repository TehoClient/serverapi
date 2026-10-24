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

package dev.tehoclient.api.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.tehoclient.api.Units;
import dev.tehoclient.api.asset.Asset;
import dev.tehoclient.api.protocol.SidebarRow;

public final class Sidebar {
	public static final int MAX_ROWS = 32;

	private final dev.tehoclient.api.protocol.SidebarSet wire;
	private final List<Asset> assets;

	private Sidebar(dev.tehoclient.api.protocol.SidebarSet wire, List<Asset> assets) {
		this.wire = wire;
		this.assets = Collections.unmodifiableList(assets);
	}

	public static Builder replace(String title) {
		return new Builder(title, true);
	}

	public static Builder update() {
		return new Builder("", false);
	}

	public List<Asset> assets() {
		return assets;
	}

	public dev.tehoclient.api.protocol.SidebarSet wire() {
		return wire;
	}

	public static final class Row {
		private final String id;
		private String text = "";
		private String value = "";
		private String item = "";
		private Asset icon;
		private int color;
		private int progress = -1;
		private int order;

		private Row(String id) {
			this.id = Units.require(id, "row id");
		}

		public static Row of(String id, String text) {
			return new Row(id).text(text);
		}

		public Row text(String text) {
			this.text = text == null ? "" : text;
			return this;
		}

		public Row value(String value) {
			this.value = value == null ? "" : value;
			return this;
		}

		public Row item(String item) {
			this.item = Units.item(item);
			return this;
		}

		public Row icon(Asset icon) {
			if (icon != null && icon.kind() != Asset.Kind.PNG) {
				throw new IllegalArgumentException("row icons must be PNG assets");
			}
			this.icon = icon;
			return this;
		}

		public Row color(int argb) {
			this.color = Units.argb(argb);
			return this;
		}

		public Row progress(double fraction) {
			this.progress = (int) Math.max(0, Math.min(10000, Math.round(fraction * 10000)));
			return this;
		}

		public Row order(int order) {
			this.order = order;
			return this;
		}
	}

	public static final class Builder {
		private final String title;
		private final boolean replace;
		private final List<Row> rows = new ArrayList<>();
		private final List<String> remove = new ArrayList<>();
		private int accent;
		private String footer = "";

		private Builder(String title, boolean replace) {
			this.title = title == null ? "" : title;
			this.replace = replace;
		}

		public Builder row(Row row) {
			if (rows.size() >= MAX_ROWS) {
				throw new IllegalArgumentException("a sidebar holds at most " + MAX_ROWS + " rows");
			}
			rows.add(row);
			return this;
		}

		public Builder remove(String id) {
			remove.add(id);
			return this;
		}

		public Builder accent(int argb) {
			this.accent = Units.argb(argb);
			return this;
		}

		public Builder footer(String footer) {
			this.footer = footer == null ? "" : footer;
			return this;
		}

		public Sidebar build() {
			List<SidebarRow> wire = new ArrayList<>();
			List<Asset> assets = new ArrayList<>();
			int order = 0;
			for (Row row : rows) {
				if (row.icon != null) {
					assets.add(row.icon);
				}
				wire.add(new SidebarRow(row.id, row.text, row.value, row.item, row.color, row.progress < 0 ? 0 : row.progress + 1,
						row.icon == null ? new byte[0] : row.icon.hash(), row.order == 0 ? order : row.order));
				order++;
			}
			return new Sidebar(new dev.tehoclient.api.protocol.SidebarSet(title, wire, remove, replace, accent, footer), assets);
		}
	}
}
