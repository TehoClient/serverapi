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

import dev.tehoclient.api.Units;
import dev.tehoclient.api.protocol.TehoProtocol;

public final class Title {
	public enum Animation {
		FADE(TehoProtocol.TITLE_FADE), TYPEWRITER(TehoProtocol.TITLE_TYPE), SLIDE(TehoProtocol.TITLE_SLIDE), POP(TehoProtocol.TITLE_POP);

		private final int wire;

		Animation(int wire) {
			this.wire = wire;
		}
	}

	private final dev.tehoclient.api.protocol.Title wire;

	private Title(dev.tehoclient.api.protocol.Title wire) {
		this.wire = wire;
	}

	public static Builder of(String title) {
		return new Builder(title);
	}

	public dev.tehoclient.api.protocol.Title wire() {
		return wire;
	}

	public static final class Builder {
		private final String title;
		private String subtitle = "";
		private Animation animation = Animation.FADE;
		private int fadeIn = 250;
		private int stay = 2500;
		private int fadeOut = 400;
		private int argb;
		private int scale = 100;

		private Builder(String title) {
			this.title = title == null ? "" : title;
		}

		public Builder subtitle(String subtitle) {
			this.subtitle = subtitle == null ? "" : subtitle;
			return this;
		}

		public Builder animation(Animation animation) {
			this.animation = animation == null ? Animation.FADE : animation;
			return this;
		}

		public Builder times(long fadeInMs, long stayMs, long fadeOutMs) {
			this.fadeIn = Math.min(10000, Units.millis(fadeInMs));
			this.stay = Math.min(60000, Units.millis(stayMs));
			this.fadeOut = Math.min(10000, Units.millis(fadeOutMs));
			return this;
		}

		public Builder color(int argb) {
			this.argb = Units.argb(argb);
			return this;
		}

		public Builder scale(double scale) {
			this.scale = Math.max(50, Math.min(300, Units.percent(scale)));
			return this;
		}

		public Title build() {
			return new Title(new dev.tehoclient.api.protocol.Title(title, subtitle, animation.wire, fadeIn, stay, fadeOut, argb, scale));
		}
	}
}
