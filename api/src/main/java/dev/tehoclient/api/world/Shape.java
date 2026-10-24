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
import java.util.List;

import dev.tehoclient.api.Units;
import dev.tehoclient.api.protocol.TehoProtocol;

public final class Shape {
	public static final int MAX_POINTS = 1024;

	private final dev.tehoclient.api.protocol.Shape wire;

	private Shape(dev.tehoclient.api.protocol.Shape wire) {
		this.wire = wire;
	}

	public static Builder cuboid(String id, double x1, double y1, double z1, double x2, double y2, double z2) {
		return new Builder(id, TehoProtocol.SHAPE_CUBOID).point(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2))
				.point(Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
	}

	public static Builder line(String id) {
		return new Builder(id, TehoProtocol.SHAPE_LINE);
	}

	public static Builder sphere(String id, double x, double y, double z, double radius) {
		Builder builder = new Builder(id, TehoProtocol.SHAPE_SPHERE).point(x, y, z);
		builder.radius = Math.max(0, Units.centi(radius));
		return builder;
	}

	public static Builder cylinder(String id, double x, double y, double z, double radius, double height) {
		Builder builder = new Builder(id, TehoProtocol.SHAPE_CYLINDER).point(x, y, z);
		builder.radius = Math.max(0, Units.centi(radius));
		builder.height = Math.max(0, Units.centi(height));
		return builder;
	}

	public static Builder polygon(String id, double floorY, double height) {
		Builder builder = new Builder(id, TehoProtocol.SHAPE_POLYGON);
		builder.height = Math.max(0, Units.centi(height));
		builder.floor = Units.centi(floorY);
		return builder;
	}

	public String id() {
		return wire.id;
	}

	public dev.tehoclient.api.protocol.Shape wire() {
		return wire;
	}

	public static final class Builder {
		private final String id;
		private final int kind;
		private final List<Integer> points = new ArrayList<>();
		private int radius;
		private int height;
		private int floor;
		private int argb = 0xFF4DA9FF;
		private int fill;
		private int width = 20;
		private boolean throughWalls;
		private String label = "";
		private String dimension = "";
		private int ttlMs;

		private Builder(String id, int kind) {
			this.id = Units.require(id, "shape id");
			this.kind = kind;
		}

		public Builder point(double x, double y, double z) {
			if (points.size() / 3 >= MAX_POINTS) {
				throw new IllegalArgumentException("a shape holds at most " + MAX_POINTS + " points");
			}
			points.add(Units.centi(x));
			points.add(Units.centi(y));
			points.add(Units.centi(z));
			return this;
		}

		public Builder corner(double x, double z) {
			return point(x, Units.blocks(floor), z);
		}

		public Builder color(int argb) {
			this.argb = Units.argb(argb);
			return this;
		}

		public Builder fill(int argb) {
			this.fill = argb;
			return this;
		}

		public Builder lineWidth(double pixels) {
			this.width = Math.max(5, Math.min(1000, Units.percent(pixels)));
			return this;
		}

		public Builder throughWalls(boolean throughWalls) {
			this.throughWalls = throughWalls;
			return this;
		}

		public Builder label(String label) {
			this.label = label == null ? "" : label;
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

		public Shape build() {
			int needed = kind == TehoProtocol.SHAPE_POLYGON ? 3 : kind == TehoProtocol.SHAPE_CUBOID || kind == TehoProtocol.SHAPE_LINE ? 2 : 1;
			if (points.size() / 3 < needed) {
				throw new IllegalStateException("shape " + id + " needs at least " + needed + " points");
			}
			return new Shape(new dev.tehoclient.api.protocol.Shape(id, kind, points, radius, height, argb, fill, width, throughWalls,
					label, dimension, ttlMs));
		}
	}
}
