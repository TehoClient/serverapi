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

public final class Bar {
	public enum Style {
		SOLID(TehoProtocol.BAR_SOLID), SEGMENTED(TehoProtocol.BAR_SEGMENTED), NOTCHED(TehoProtocol.BAR_NOTCHED);

		private final int wire;

		Style(int wire) {
			this.wire = wire;
		}
	}

	private final dev.tehoclient.api.protocol.Bar wire;

	private Bar(dev.tehoclient.api.protocol.Bar wire) {
		this.wire = wire;
	}

	public static Builder builder(String id, String text, double progress) {
		return new Builder(id, text, progress);
	}

	public String id() {
		return wire.id;
	}

	public dev.tehoclient.api.protocol.Bar wire() {
		return wire;
	}

	public static final class Builder {
		private final String id;
		private final String text;
		private final int progress;
		private int argb = 0xFF4DA9FF;
		private Style style = Style.SOLID;
		private int ttlMs;
		private String item = "";

		private Builder(String id, String text, double progress) {
			this.id = Units.require(id, "bar id");
			this.text = text == null ? "" : text;
			this.progress = (int) Math.max(0, Math.min(10000, Math.round(progress * 10000)));
		}

		public Builder color(int argb) {
			this.argb = Units.argb(argb);
			return this;
		}

		public Builder style(Style style) {
			this.style = style == null ? Style.SOLID : style;
			return this;
		}

		public Builder ttl(long millis) {
			this.ttlMs = Units.millis(millis);
			return this;
		}

		public Builder icon(String item) {
			this.item = Units.item(item);
			return this;
		}

		public Bar build() {
			return new Bar(new dev.tehoclient.api.protocol.Bar(id, text, progress, argb, style.wire, ttlMs, item));
		}
	}
}
