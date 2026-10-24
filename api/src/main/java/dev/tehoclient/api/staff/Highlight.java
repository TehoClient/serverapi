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

package dev.tehoclient.api.staff;

import java.util.UUID;

public final class Highlight {
	public enum Style {
		OUTLINE, FILL, BOTH
	}

	private final String id;
	private final Target.Kind kind;
	private final UUID entity;
	private final int x;
	private final int y;
	private final int z;
	private final int x2;
	private final int y2;
	private final int z2;
	private final String dimension;
	private final int argb;
	private final String label;
	private final Style style;
	private final boolean tracer;
	private final int ttlMs;

	private Highlight(Builder builder) {
		this.id = builder.id;
		this.kind = builder.kind;
		this.entity = builder.entity;
		this.x = builder.x;
		this.y = builder.y;
		this.z = builder.z;
		this.x2 = builder.x2;
		this.y2 = builder.y2;
		this.z2 = builder.z2;
		this.dimension = builder.dimension;
		this.argb = builder.argb;
		this.label = builder.label;
		this.style = builder.style;
		this.tracer = builder.tracer;
		this.ttlMs = builder.ttlMs;
	}

	public static Builder entity(String id, UUID entity) {
		Builder builder = new Builder(id, Target.Kind.ENTITY);
		builder.entity = entity;
		return builder;
	}

	public static Builder block(String id, int x, int y, int z) {
		Builder builder = new Builder(id, Target.Kind.BLOCK);
		builder.x = x;
		builder.y = y;
		builder.z = z;
		return builder;
	}

	public static Builder area(String id, int x1, int y1, int z1, int x2, int y2, int z2) {
		Builder builder = new Builder(id, Target.Kind.NONE);
		builder.x = Math.min(x1, x2);
		builder.y = Math.min(y1, y2);
		builder.z = Math.min(z1, z2);
		builder.x2 = Math.max(x1, x2);
		builder.y2 = Math.max(y1, y2);
		builder.z2 = Math.max(z1, z2);
		return builder;
	}

	public String id() {
		return id;
	}

	public Target.Kind kind() {
		return kind;
	}

	public UUID entity() {
		return entity;
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

	public int x2() {
		return x2;
	}

	public int y2() {
		return y2;
	}

	public int z2() {
		return z2;
	}

	public String dimension() {
		return dimension;
	}

	public int argb() {
		return argb;
	}

	public String label() {
		return label;
	}

	public Style style() {
		return style;
	}

	public boolean tracer() {
		return tracer;
	}

	public int ttlMs() {
		return ttlMs;
	}

	public static final class Builder {
		private final String id;
		private final Target.Kind kind;
		private UUID entity;
		private int x;
		private int y;
		private int z;
		private int x2;
		private int y2;
		private int z2;
		private String dimension = "";
		private int argb = 0xFFB07CFF;
		private String label = "";
		private Style style = Style.OUTLINE;
		private boolean tracer;
		private int ttlMs;

		private Builder(String id, Target.Kind kind) {
			if (id == null || id.isEmpty()) {
				throw new IllegalArgumentException("highlight id is required");
			}
			this.id = id;
			this.kind = kind;
		}

		public Builder dimension(String dimension) {
			this.dimension = dimension == null ? "" : dimension;
			return this;
		}

		public Builder color(int rgb) {
			this.argb = 0xFF000000 | (rgb & 0xFFFFFF);
			return this;
		}

		public Builder argb(int argb) {
			this.argb = argb;
			return this;
		}

		public Builder label(String label) {
			this.label = label == null ? "" : label;
			return this;
		}

		public Builder style(Style style) {
			this.style = style;
			return this;
		}

		public Builder tracer(boolean tracer) {
			this.tracer = tracer;
			return this;
		}

		public Builder lifetime(int milliseconds) {
			this.ttlMs = Math.max(0, milliseconds);
			return this;
		}

		public Highlight build() {
			return new Highlight(this);
		}
	}
}
