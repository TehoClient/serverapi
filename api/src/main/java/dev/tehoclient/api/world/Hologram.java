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

package dev.tehoclient.api.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import dev.tehoclient.api.Units;
import dev.tehoclient.api.asset.Asset;

public final class Hologram {
	public static final int MAX_LINES = 16;

	private final dev.tehoclient.api.protocol.Hologram wire;
	private final Asset image;

	private Hologram(dev.tehoclient.api.protocol.Hologram wire, Asset image) {
		this.wire = wire;
		this.image = image;
	}

	public static Builder at(String id, double x, double y, double z) {
		Builder builder = new Builder(id);
		builder.x = Units.centi(x);
		builder.y = Units.centi(y);
		builder.z = Units.centi(z);
		return builder;
	}

	public static Builder above(String id, UUID entity) {
		Builder builder = new Builder(id);
		builder.entity = Units.id(entity);
		return builder;
	}

	public String id() {
		return wire.id;
	}

	public Asset image() {
		return image;
	}

	public dev.tehoclient.api.protocol.Hologram wire() {
		return wire;
	}

	public static final class Builder {
		private final String id;
		private int x;
		private int y;
		private int z;
		private String entity = "";
		private final List<String> lines = new ArrayList<>();
		private String item = "";
		private Asset image;
		private int scale = 100;
		private int background = 0x40000000;
		private boolean clickable;
		private boolean throughWalls;
		private String dimension = "";
		private int ttlMs;
		private int range = 48;

		private Builder(String id) {
			this.id = Units.require(id, "hologram id");
		}

		public Builder lines(String... lines) {
			return lines(Arrays.asList(lines));
		}

		public Builder lines(List<String> lines) {
			this.lines.clear();
			for (String line : lines) {
				if (this.lines.size() < MAX_LINES) {
					this.lines.add(line == null ? "" : line);
				}
			}
			return this;
		}

		public Builder line(String line) {
			if (lines.size() < MAX_LINES) {
				lines.add(line == null ? "" : line);
			}
			return this;
		}

		public Builder item(String item) {
			this.item = Units.item(item);
			return this;
		}

		public Builder image(Asset image) {
			if (image != null && image.kind() != Asset.Kind.PNG) {
				throw new IllegalArgumentException("hologram images must be PNG assets");
			}
			this.image = image;
			return this;
		}

		public Builder scale(double scale) {
			this.scale = Math.max(10, Math.min(1000, Units.percent(scale)));
			return this;
		}

		public Builder background(int argb) {
			this.background = argb;
			return this;
		}

		public Builder clickable(boolean clickable) {
			this.clickable = clickable;
			return this;
		}

		public Builder throughWalls(boolean throughWalls) {
			this.throughWalls = throughWalls;
			return this;
		}

		public Builder dimension(String dimension) {
			this.dimension = dimension == null ? "" : dimension;
			return this;
		}

		public Builder ttl(long millis) {
			this.ttlMs = Units.millis(millis);
			return this;
		}

		public Builder range(int blocks) {
			this.range = Math.max(4, Math.min(256, blocks));
			return this;
		}

		public Hologram build() {
			byte[] hash = image == null ? new byte[0] : image.hash();
			return new Hologram(new dev.tehoclient.api.protocol.Hologram(id, x, y, z, entity, lines, item, hash, scale, background,
					clickable, throughWalls, dimension, ttlMs, range), image);
		}
	}
}
