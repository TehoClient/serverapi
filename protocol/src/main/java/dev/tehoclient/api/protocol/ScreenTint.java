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

package dev.tehoclient.api.protocol;

public final class ScreenTint implements Message {
	public final int color;
	public final boolean vignette;
	public final int fadeMs;
	public final int ttlMs;
	public final int strength;

	public ScreenTint(int color, boolean vignette, int fadeMs, int ttlMs, int strength) {
		this.color = color;
		this.vignette = vignette;
		this.fadeMs = fadeMs;
		this.ttlMs = ttlMs;
		this.strength = strength;
	}

	@Override
	public int type() {
		return TehoProtocol.SCREEN_TINT;
	}

	@Override
	public void write(WireWriter out) {
		out.fixed32(1, color);
		out.bool(2, vignette);
		out.uint(3, fadeMs);
		out.uint(4, ttlMs);
		out.uint(5, strength);
	}

	static ScreenTint read(WireReader in) {
		int color = 0;
		boolean vignette = false;
		int fadeMs = 0;
		int ttlMs = 0;
		int strength = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					color = in.readFixed32();
					break;
				case 2:
					vignette = in.readBool();
					break;
				case 3:
					fadeMs = in.readUint();
					break;
				case 4:
					ttlMs = in.readUint();
					break;
				case 5:
					strength = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new ScreenTint(color, vignette, fadeMs, ttlMs, strength);
	}

	@Override
	public String toString() {
		return "ScreenTint{" + "color=" + Integer.toHexString(color) + ", " + "vignette=" + vignette + ", " + "fadeMs=" + fadeMs + ", " + "ttlMs=" + ttlMs + ", " + "strength=" + strength + "}";
	}
}
