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

package dev.tehoclient.api.waypoint;

public final class Waypoint {
	private final String id;
	private final String name;
	private final int x;
	private final int y;
	private final int z;
	private final String dimension;
	private final int color;
	private final boolean focus;

	private Waypoint(Builder builder) {
		this.id = builder.id;
		this.name = builder.name;
		this.x = builder.x;
		this.y = builder.y;
		this.z = builder.z;
		this.dimension = builder.dimension;
		this.color = builder.color;
		this.focus = builder.focus;
	}

	public static Builder builder(String id) {
		return new Builder(id);
	}

	public String id() {
		return id;
	}

	public String name() {
		return name;
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

	public int z() {
		return z;
	}

	public String dimension() {
		return dimension;
	}

	public int color() {
		return color;
	}

	public boolean focus() {
		return focus;
	}

	public static final class Builder {
		private final String id;
		private String name = "";
		private int x;
		private int y;
		private int z;
		private String dimension = "";
		private int color;
		private boolean focus;

		private Builder(String id) {
			if (id == null || id.isEmpty()) {
				throw new IllegalArgumentException("waypoint id is required");
			}
			this.id = id;
		}

		public Builder name(String name) {
			this.name = name == null ? "" : name;
			return this;
		}

		public Builder at(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
			return this;
		}

		public Builder dimension(String dimension) {
			this.dimension = dimension == null ? "" : dimension;
			return this;
		}

		public Builder color(int rgb) {
			this.color = rgb & 0xFFFFFF;
			return this;
		}

		public Builder focus(boolean focus) {
			this.focus = focus;
			return this;
		}

		public Waypoint build() {
			return new Waypoint(this);
		}
	}
}
