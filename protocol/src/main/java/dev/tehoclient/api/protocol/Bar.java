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

public final class Bar implements Message {
	public final String id;
	public final String text;
	public final int progress;
	public final int color;
	public final int style;
	public final int ttlMs;
	public final String item;

	public Bar(String id, String text, int progress, int color, int style, int ttlMs, String item) {
		this.id = id == null ? "" : id;
		this.text = text == null ? "" : text;
		this.progress = progress;
		this.color = color;
		this.style = style;
		this.ttlMs = ttlMs;
		this.item = item == null ? "" : item;
	}

	@Override
	public int type() {
		return TehoProtocol.BAR;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, id);
		out.string(2, text);
		out.uint(3, progress);
		out.fixed32(4, color);
		out.uint(5, style);
		out.uint(6, ttlMs);
		out.string(7, item);
	}

	static Bar read(WireReader in) {
		String id = "";
		String text = "";
		int progress = 0;
		int color = 0;
		int style = 0;
		int ttlMs = 0;
		String item = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					text = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					progress = in.readUint();
					break;
				case 4:
					color = in.readFixed32();
					break;
				case 5:
					style = in.readUint();
					break;
				case 6:
					ttlMs = in.readUint();
					break;
				case 7:
					item = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new Bar(id, text, progress, color, style, ttlMs, item);
	}

	@Override
	public String toString() {
		return "Bar{" + "id=" + id + ", " + "text=" + text + ", " + "progress=" + progress + ", " + "color=" + Integer.toHexString(color) + ", " + "style=" + style + ", " + "ttlMs=" + ttlMs + ", " + "item=" + item + "}";
	}
}
