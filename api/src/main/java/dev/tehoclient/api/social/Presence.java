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

package dev.tehoclient.api.social;

public final class Presence {
	private final dev.tehoclient.api.protocol.Presence wire;

	private Presence(dev.tehoclient.api.protocol.Presence wire) {
		this.wire = wire;
	}

	public static Builder builder(String game) {
		return new Builder(game);
	}

	public dev.tehoclient.api.protocol.Presence wire() {
		return wire;
	}

	public static final class Builder {
		private final String game;
		private String map = "";
		private String state = "";
		private long startedMs;
		private int partySize;
		private int partyMax;
		private boolean joinable;

		private Builder(String game) {
			this.game = game == null ? "" : game;
		}

		public Builder map(String map) {
			this.map = map == null ? "" : map;
			return this;
		}

		public Builder state(String state) {
			this.state = state == null ? "" : state;
			return this;
		}

		public Builder startedAt(long epochMillis) {
			this.startedMs = Math.max(0, epochMillis);
			return this;
		}

		public Builder startedNow() {
			return startedAt(System.currentTimeMillis());
		}

		public Builder party(int size, int max) {
			this.partySize = Math.max(0, size);
			this.partyMax = Math.max(this.partySize, max);
			return this;
		}

		public Builder joinable(boolean joinable) {
			this.joinable = joinable;
			return this;
		}

		public Presence build() {
			return new Presence(new dev.tehoclient.api.protocol.Presence(game, map, state, startedMs, partySize, partyMax, joinable, false));
		}
	}
}
