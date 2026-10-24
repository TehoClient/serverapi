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

import java.util.UUID;

import dev.tehoclient.api.Units;

public final class Beam {
	private final dev.tehoclient.api.protocol.Beam wire;

	private Beam(dev.tehoclient.api.protocol.Beam wire) {
		this.wire = wire;
	}

	public static Builder at(String id, double x, double y, double z) {
		Builder builder = new Builder(id);
		builder.x = Units.centi(x);
		builder.y = Units.centi(y);
		builder.z = Units.centi(z);
		return builder;
	}

	public static Builder on(String id, UUID entity) {
		Builder builder = new Builder(id);
		builder.entity = Units.id(entity);
		return builder;
	}

	public String id() {
		return wire.id;
	}

	public dev.tehoclient.api.protocol.Beam wire() {
		return wire;
	}

	public static final class Builder {
		private final String id;
		private int x;
		private int y;
		private int z;
		private String entity = "";
		private int argb = 0xFF4DA9FF;
		private int height = 256;
		private int width = 30;
		private boolean pulse;
		private String dimension = "";
		private int ttlMs;

		private Builder(String id) {
			this.id = Units.require(id, "beam id");
		}

		public Builder color(int argb) {
			this.argb = Units.argb(argb);
			return this;
		}

		public Builder height(int blocks) {
			this.height = Math.max(1, Math.min(1024, blocks));
			return this;
		}

		public Builder width(double blocks) {
			this.width = Math.max(5, Math.min(400, Units.centi(blocks)));
			return this;
		}

		public Builder pulse(boolean pulse) {
			this.pulse = pulse;
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

		public Beam build() {
			return new Beam(new dev.tehoclient.api.protocol.Beam(id, x, y, z, entity, argb, height, width, pulse, dimension, ttlMs));
		}
	}
}
