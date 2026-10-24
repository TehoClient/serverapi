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

public final class Title implements Message {
	public final String title;
	public final String subtitle;
	public final int animation;
	public final int fadeInMs;
	public final int stayMs;
	public final int fadeOutMs;
	public final int color;
	public final int scale;

	public Title(String title, String subtitle, int animation, int fadeInMs, int stayMs, int fadeOutMs, int color, int scale) {
		this.title = title == null ? "" : title;
		this.subtitle = subtitle == null ? "" : subtitle;
		this.animation = animation;
		this.fadeInMs = fadeInMs;
		this.stayMs = stayMs;
		this.fadeOutMs = fadeOutMs;
		this.color = color;
		this.scale = scale;
	}

	@Override
	public int type() {
		return TehoProtocol.TITLE;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, title);
		out.string(2, subtitle);
		out.uint(3, animation);
		out.uint(4, fadeInMs);
		out.uint(5, stayMs);
		out.uint(6, fadeOutMs);
		out.fixed32(7, color);
		out.uint(8, scale);
	}

	static Title read(WireReader in) {
		String title = "";
		String subtitle = "";
		int animation = 0;
		int fadeInMs = 0;
		int stayMs = 0;
		int fadeOutMs = 0;
		int color = 0;
		int scale = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					title = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					subtitle = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					animation = in.readUint();
					break;
				case 4:
					fadeInMs = in.readUint();
					break;
				case 5:
					stayMs = in.readUint();
					break;
				case 6:
					fadeOutMs = in.readUint();
					break;
				case 7:
					color = in.readFixed32();
					break;
				case 8:
					scale = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new Title(title, subtitle, animation, fadeInMs, stayMs, fadeOutMs, color, scale);
	}

	@Override
	public String toString() {
		return "Title{" + "title=" + title + ", " + "subtitle=" + subtitle + ", " + "animation=" + animation + ", " + "fadeInMs=" + fadeInMs + ", " + "stayMs=" + stayMs + ", " + "fadeOutMs=" + fadeOutMs + ", " + "color=" + Integer.toHexString(color) + ", " + "scale=" + scale + "}";
	}
}
