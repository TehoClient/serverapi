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

package dev.tehoclient.api.audio;

import java.util.UUID;

import dev.tehoclient.api.Units;
import dev.tehoclient.api.asset.Asset;

public final class Sound {
	private final dev.tehoclient.api.protocol.PlaySound wire;
	private final Asset asset;

	private Sound(dev.tehoclient.api.protocol.PlaySound wire, Asset asset) {
		this.wire = wire;
		this.asset = asset;
	}

	public static Builder asset(String id, Asset asset) {
		if (asset == null || asset.kind() != Asset.Kind.OGG) {
			throw new IllegalArgumentException("sounds must be OGG assets");
		}
		Builder builder = new Builder(id);
		builder.asset = asset;
		return builder;
	}

	public static Builder vanilla(String id, String sound) {
		Builder builder = new Builder(id);
		builder.sound = Units.item(Units.require(sound, "sound"));
		return builder;
	}

	public String id() {
		return wire.id;
	}

	public Asset sourceAsset() {
		return asset;
	}

	public dev.tehoclient.api.protocol.PlaySound wire() {
		return wire;
	}

	public static final class Builder {
		private final String id;
		private Asset asset;
		private String sound = "";
		private int x;
		private int y;
		private int z;
		private String entity = "";
		private boolean positional;
		private int volume = 100;
		private int pitch = 100;
		private boolean loop;
		private int fadeInMs;
		private boolean music;

		private Builder(String id) {
			this.id = Units.require(id, "sound id");
		}

		public Builder at(double x, double y, double z) {
			this.x = Units.centi(x);
			this.y = Units.centi(y);
			this.z = Units.centi(z);
			this.positional = true;
			this.entity = "";
			return this;
		}

		public Builder following(UUID entity) {
			this.entity = Units.id(entity);
			this.positional = true;
			return this;
		}

		public Builder volume(double volume) {
			this.volume = Math.max(0, Math.min(400, Units.percent(volume)));
			return this;
		}

		public Builder pitch(double pitch) {
			this.pitch = Math.max(10, Math.min(400, Units.percent(pitch)));
			return this;
		}

		public Builder loop(boolean loop) {
			this.loop = loop;
			return this;
		}

		public Builder fadeIn(long millis) {
			this.fadeInMs = Units.millis(millis);
			return this;
		}

		public Builder music(boolean music) {
			this.music = music;
			return this;
		}

		public Sound build() {
			return new Sound(new dev.tehoclient.api.protocol.PlaySound(id, asset == null ? new byte[0] : asset.hash(), sound, x, y, z,
					entity, positional, volume, pitch, loop, fadeInMs, music), asset);
		}
	}
}
