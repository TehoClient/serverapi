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

package dev.tehoclient.api.camera;

import java.util.ArrayList;
import java.util.List;

import dev.tehoclient.api.Units;
import dev.tehoclient.api.protocol.Keyframe;
import dev.tehoclient.api.protocol.TehoProtocol;

public final class Cutscene {
	public static final int MAX_FRAMES = 256;

	public enum Ease {
		LINEAR(TehoProtocol.EASE_LINEAR), IN_OUT(TehoProtocol.EASE_IN_OUT), IN(TehoProtocol.EASE_IN), OUT(TehoProtocol.EASE_OUT),
		SMOOTH_PATH(TehoProtocol.EASE_CATMULL);

		private final int wire;

		Ease(int wire) {
			this.wire = wire;
		}
	}

	private final dev.tehoclient.api.protocol.Cutscene wire;
	private final long durationMs;

	private Cutscene(dev.tehoclient.api.protocol.Cutscene wire, long durationMs) {
		this.wire = wire;
		this.durationMs = durationMs;
	}

	public static Builder builder(String id) {
		return new Builder(id);
	}

	public String id() {
		return wire.id;
	}

	public long durationMs() {
		return durationMs;
	}

	public dev.tehoclient.api.protocol.Cutscene wire() {
		return wire;
	}

	public static final class Builder {
		private final String id;
		private final List<Keyframe> frames = new ArrayList<>();
		private boolean skippable = true;
		private boolean hideHud = true;
		private boolean letterbox = true;
		private boolean loop;
		private String dimension = "";
		private long last = -1;

		private Builder(String id) {
			this.id = Units.require(id, "cutscene id");
		}

		public Builder frame(long atMs, double x, double y, double z, float yaw, float pitch) {
			return frame(atMs, x, y, z, yaw, pitch, 0, 0, Ease.SMOOTH_PATH);
		}

		public Builder frame(long atMs, double x, double y, double z, float yaw, float pitch, float roll, int fov, Ease ease) {
			if (frames.size() >= MAX_FRAMES) {
				throw new IllegalArgumentException("a cutscene holds at most " + MAX_FRAMES + " keyframes");
			}
			if (atMs <= last) {
				throw new IllegalArgumentException("keyframes must be in increasing time order");
			}
			last = atMs;
			frames.add(new Keyframe(Units.centi(x), Units.centi(y), Units.centi(z), Units.centi(yaw), Units.centi(pitch),
					Units.centi(roll), Math.max(0, Math.min(170, fov)), Units.millis(atMs), (ease == null ? Ease.LINEAR : ease).wire));
			return this;
		}

		public Builder skippable(boolean skippable) {
			this.skippable = skippable;
			return this;
		}

		public Builder hideHud(boolean hideHud) {
			this.hideHud = hideHud;
			return this;
		}

		public Builder letterbox(boolean letterbox) {
			this.letterbox = letterbox;
			return this;
		}

		public Builder loop(boolean loop) {
			this.loop = loop;
			return this;
		}

		public Builder dimension(String dimension) {
			this.dimension = dimension == null ? "" : dimension;
			return this;
		}

		public Cutscene build() {
			if (frames.size() < 2) {
				throw new IllegalStateException("a cutscene needs at least two keyframes");
			}
			return new Cutscene(new dev.tehoclient.api.protocol.Cutscene(id, frames, skippable, hideHud, letterbox, loop, dimension), last);
		}
	}
}
