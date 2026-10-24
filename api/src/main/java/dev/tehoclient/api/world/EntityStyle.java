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

import java.util.EnumSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import dev.tehoclient.api.Units;

public final class EntityStyle {
	public enum Part {
		HEAD, BODY, ARMS, LEGS, ARMOR, HELD_ITEMS, CAPE, COSMETICS
	}

	private final dev.tehoclient.api.protocol.EntityStyle wire;

	private EntityStyle(dev.tehoclient.api.protocol.EntityStyle wire) {
		this.wire = wire;
	}

	public static Builder of(UUID entity) {
		if (entity == null) {
			throw new IllegalArgumentException("entity is required");
		}
		return new Builder(entity);
	}

	public dev.tehoclient.api.protocol.EntityStyle wire() {
		return wire;
	}

	public static final class Builder {
		private final UUID entity;
		private int glow;
		private String name = "";
		private int scale = 100;
		private boolean rainbow;
		private boolean hideName;
		private final Set<Part> hidden = EnumSet.noneOf(Part.class);
		private int ttlMs;

		private Builder(UUID entity) {
			this.entity = entity;
		}

		public Builder glow(int argb) {
			this.glow = Units.argb(argb);
			return this;
		}

		public Builder name(String name) {
			this.name = name == null ? "" : name;
			return this;
		}

		public Builder hideName(boolean hide) {
			this.hideName = hide;
			return this;
		}

		public Builder scale(double scale) {
			this.scale = Math.max(10, Math.min(400, Units.percent(scale)));
			return this;
		}

		public Builder rainbow(boolean rainbow) {
			this.rainbow = rainbow;
			return this;
		}

		public Builder hide(Part... parts) {
			for (Part part : parts) {
				hidden.add(part);
			}
			return this;
		}

		public Builder ttl(long millis) {
			this.ttlMs = Units.millis(millis);
			return this;
		}

		public EntityStyle build() {
			List<String> hide = new ArrayList<>();
			for (Part part : hidden) {
				hide.add(part.name().toLowerCase(Locale.ROOT));
			}
			return new EntityStyle(new dev.tehoclient.api.protocol.EntityStyle(entity.toString(), glow, name, scale, rainbow, hide, ttlMs,
					hideName));
		}
	}
}
