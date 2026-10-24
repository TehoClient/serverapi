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
import java.util.List;
import java.util.UUID;

import dev.tehoclient.api.Units;
import dev.tehoclient.api.asset.Asset;

public final class Nametag {
	public static final int MAX_LINES = 4;

	private final dev.tehoclient.api.protocol.Nametag wire;
	private final Asset badge;

	private Nametag(dev.tehoclient.api.protocol.Nametag wire, Asset badge) {
		this.wire = wire;
		this.badge = badge;
	}

	public static Builder of(UUID player) {
		if (player == null) {
			throw new IllegalArgumentException("player is required");
		}
		return new Builder(player);
	}

	public Asset badge() {
		return badge;
	}

	public dev.tehoclient.api.protocol.Nametag wire() {
		return wire;
	}

	public static final class Builder {
		private final UUID player;
		private final List<String> above = new ArrayList<>();
		private final List<String> below = new ArrayList<>();
		private String prefix = "";
		private String suffix = "";
		private Asset badge;
		private String badgeItem = "";
		private boolean hide;

		private Builder(UUID player) {
			this.player = player;
		}

		public Builder above(String... lines) {
			add(above, Arrays.asList(lines));
			return this;
		}

		public Builder below(String... lines) {
			add(below, Arrays.asList(lines));
			return this;
		}

		private static void add(List<String> into, List<String> lines) {
			for (String line : lines) {
				if (into.size() < MAX_LINES) {
					into.add(line == null ? "" : line);
				}
			}
		}

		public Builder prefix(String prefix) {
			this.prefix = prefix == null ? "" : prefix;
			return this;
		}

		public Builder suffix(String suffix) {
			this.suffix = suffix == null ? "" : suffix;
			return this;
		}

		public Builder badge(Asset badge) {
			if (badge != null && badge.kind() != Asset.Kind.PNG) {
				throw new IllegalArgumentException("nametag badges must be PNG assets");
			}
			this.badge = badge;
			return this;
		}

		public Builder badgeItem(String item) {
			this.badgeItem = Units.item(item);
			return this;
		}

		public Builder hide(boolean hide) {
			this.hide = hide;
			return this;
		}

		public Nametag build() {
			return new Nametag(new dev.tehoclient.api.protocol.Nametag(player.toString(), above, below, prefix, suffix,
					badge == null ? new byte[0] : badge.hash(), hide, badgeItem), badge);
		}
	}
}
