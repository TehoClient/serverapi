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

package dev.tehoclient.api.statue;

import java.util.UUID;

import dev.tehoclient.api.Units;

public final class Statue {
	public enum Pose {
		STAND, WAVE, SALUTE, SIT, TROPHY
	}

	private final dev.tehoclient.api.protocol.Statue wire;

	private Statue(dev.tehoclient.api.protocol.Statue wire) {
		this.wire = wire;
	}

	public static Builder of(String id, UUID player, String name) {
		return new Builder(id, player, name);
	}

	public String id() {
		return wire.id;
	}

	public dev.tehoclient.api.protocol.Statue wire() {
		return wire;
	}

	public static final class Builder {
		private final String id;
		private final UUID player;
		private final String name;
		private int x;
		private int y;
		private int z;
		private int yaw;
		private Pose pose = Pose.STAND;
		private int scale = 100;
		private String dimension = "";
		private String label = "";
		private String emote = "";

		private Builder(String id, UUID player, String name) {
			this.id = Units.require(id, "statue id");
			if (player == null) {
				throw new IllegalArgumentException("player is required");
			}
			this.player = player;
			this.name = name == null ? "" : name;
		}

		public Builder at(double x, double y, double z, float yaw) {
			this.x = Units.centi(x);
			this.y = Units.centi(y);
			this.z = Units.centi(z);
			this.yaw = Units.centi(yaw);
			return this;
		}

		public Builder pose(Pose pose) {
			this.pose = pose == null ? Pose.STAND : pose;
			return this;
		}

		public Builder scale(double scale) {
			this.scale = Math.max(25, Math.min(400, Units.percent(scale)));
			return this;
		}

		public Builder dimension(String dimension) {
			this.dimension = dimension == null ? "" : dimension;
			return this;
		}

		public Builder label(String label) {
			this.label = label == null ? "" : label;
			return this;
		}

		public Builder emote(String emote) {
			this.emote = emote == null ? "" : emote;
			return this;
		}

		public Statue build() {
			return new Statue(new dev.tehoclient.api.protocol.Statue(id, player.toString(), name, x, y, z, yaw, pose.ordinal(), scale,
					dimension, label, emote));
		}
	}
}
