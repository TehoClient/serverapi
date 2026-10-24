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

package dev.tehoclient.api.players;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import dev.tehoclient.api.Units;

public final class Team {
	private final dev.tehoclient.api.protocol.Team wire;

	private Team(dev.tehoclient.api.protocol.Team wire) {
		this.wire = wire;
	}

	public static Builder builder(String id) {
		return new Builder(id);
	}

	public String id() {
		return wire.id;
	}

	public dev.tehoclient.api.protocol.Team wire() {
		return wire;
	}

	public static final class Builder {
		private final String id;
		private String name = "";
		private int argb = 0xFF4DA9FF;
		private final List<String> members = new ArrayList<>();
		private boolean markers = true;
		private boolean throughWalls;
		private boolean health = true;
		private boolean compass = true;

		private Builder(String id) {
			this.id = Units.require(id, "team id");
		}

		public Builder name(String name) {
			this.name = name == null ? "" : name;
			return this;
		}

		public Builder color(int argb) {
			this.argb = Units.argb(argb);
			return this;
		}

		public Builder member(UUID player) {
			members.add(Units.id(player));
			return this;
		}

		public Builder members(Collection<UUID> players) {
			for (UUID player : players) {
				member(player);
			}
			return this;
		}

		public Builder markers(boolean markers) {
			this.markers = markers;
			return this;
		}

		public Builder throughWalls(boolean throughWalls) {
			this.throughWalls = throughWalls;
			return this;
		}

		public Builder health(boolean health) {
			this.health = health;
			return this;
		}

		public Builder compass(boolean compass) {
			this.compass = compass;
			return this;
		}

		public Team build() {
			return new Team(new dev.tehoclient.api.protocol.Team(id, name, argb, members, markers, throughWalls, health, compass));
		}
	}
}
