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

package dev.tehoclient.api.notify;

public final class Notification {
	private final String id;
	private final String title;
	private final String body;
	private final int accent;
	private final int durationMs;
	private final boolean sound;

	private Notification(Builder builder) {
		this.id = builder.id;
		this.title = builder.title;
		this.body = builder.body;
		this.accent = builder.accent;
		this.durationMs = builder.durationMs;
		this.sound = builder.sound;
	}

	public static Builder builder(String id) {
		return new Builder(id);
	}

	public String id() {
		return id;
	}

	public String title() {
		return title;
	}

	public String body() {
		return body;
	}

	public int accent() {
		return accent;
	}

	public int durationMs() {
		return durationMs;
	}

	public boolean sound() {
		return sound;
	}

	public static final class Builder {
		private final String id;
		private String title = "";
		private String body = "";
		private int accent;
		private int durationMs;
		private boolean sound;

		private Builder(String id) {
			if (id == null || id.isEmpty()) {
				throw new IllegalArgumentException("notification id is required");
			}
			this.id = id;
		}

		public Builder title(String title) {
			this.title = title == null ? "" : title;
			return this;
		}

		public Builder body(String body) {
			this.body = body == null ? "" : body;
			return this;
		}

		public Builder accent(int rgb) {
			this.accent = rgb & 0xFFFFFF;
			return this;
		}

		public Builder duration(int milliseconds) {
			this.durationMs = Math.max(0, milliseconds);
			return this;
		}

		public Builder sound(boolean sound) {
			this.sound = sound;
			return this;
		}

		public Notification build() {
			if (title.isEmpty()) {
				throw new IllegalArgumentException("notification title is required");
			}
			return new Notification(this);
		}
	}
}
