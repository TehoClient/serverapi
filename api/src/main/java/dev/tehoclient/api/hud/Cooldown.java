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

public final class Cooldown {
	private final String id;
	private final String label;
	private final long durationMs;
	private final String item;
	private final int color;

	private Cooldown(Builder builder) {
		this.id = builder.id;
		this.label = builder.label;
		this.durationMs = builder.durationMs;
		this.item = builder.item;
		this.color = builder.color;
	}

	public static Builder builder(String id, long durationMs) {
		return new Builder(id, durationMs);
	}

	public String id() {
		return id;
	}

	public String label() {
		return label;
	}

	public long durationMs() {
		return durationMs;
	}

	public String item() {
		return item;
	}

	public int color() {
		return color;
	}

	public static final class Builder {
		private final String id;
		private final long durationMs;
		private String label = "";
		private String item = "";
		private int color;

		private Builder(String id, long durationMs) {
			if (id == null || id.isEmpty()) {
				throw new IllegalArgumentException("cooldown id is required");
			}
			this.id = id;
			this.durationMs = Math.max(0, durationMs);
		}

		public Builder label(String label) {
			this.label = label == null ? "" : label;
			return this;
		}

		public Builder item(String item) {
			if (item == null || item.isEmpty()) {
				this.item = "";
			} else {
				this.item = item.indexOf(':') < 0 ? "minecraft:" + item : item;
			}
			return this;
		}

		public Builder color(int rgb) {
			this.color = rgb & 0xFFFFFF;
			return this;
		}

		public Cooldown build() {
			return new Cooldown(this);
		}
	}
}
