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

public final class StaffAlert {
	public enum Severity {
		INFO, WARN, HIGH
	}

	private final String id;
	private final Severity severity;
	private final String title;
	private final String body;
	private final UUID player;
	private final int x;
	private final int y;
	private final int z;
	private final String dimension;
	private final int ttlMs;

	private StaffAlert(Builder builder) {
		this.id = builder.id;
		this.severity = builder.severity;
		this.title = builder.title;
		this.body = builder.body;
		this.player = builder.player;
		this.x = builder.x;
		this.y = builder.y;
		this.z = builder.z;
		this.dimension = builder.dimension;
		this.ttlMs = builder.ttlMs;
	}

	public static Builder builder(String id, String title) {
		return new Builder(id, title);
	}

	public String id() {
		return id;
	}

	public Severity severity() {
		return severity;
	}

	public String title() {
		return title;
	}

	public String body() {
		return body;
	}

	public UUID player() {
		return player;
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

	public int ttlMs() {
		return ttlMs;
	}

	public static final class Builder {
		private final String id;
		private final String title;
		private Severity severity = Severity.WARN;
		private String body = "";
		private UUID player;
		private int x;
		private int y;
		private int z;
		private String dimension = "";
		private int ttlMs = 60000;

		private Builder(String id, String title) {
			if (id == null || id.isEmpty()) {
				throw new IllegalArgumentException("alert id is required");
			}
			this.id = id;
			this.title = title == null ? "" : title;
		}

		public Builder severity(Severity severity) {
			this.severity = severity;
			return this;
		}

		public Builder body(String body) {
			this.body = body == null ? "" : body;
			return this;
		}

		public Builder player(UUID player) {
			this.player = player;
			return this;
		}

		public Builder at(int x, int y, int z, String dimension) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.dimension = dimension == null ? "" : dimension;
			return this;
		}

		public Builder lifetime(int milliseconds) {
			this.ttlMs = Math.max(1000, milliseconds);
			return this;
		}

		public StaffAlert build() {
			return new StaffAlert(this);
		}
	}
}
