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

public final class Notify implements Message {
	public final String id;
	public final String title;
	public final String body;
	public final int accent;
	public final int durationMs;
	public final boolean sound;

	public Notify(String id, String title, String body, int accent, int durationMs, boolean sound) {
		this.id = id == null ? "" : id;
		this.title = title == null ? "" : title;
		this.body = body == null ? "" : body;
		this.accent = accent;
		this.durationMs = durationMs;
		this.sound = sound;
	}

	@Override
	public int type() {
		return TehoProtocol.NOTIFY_SHOW;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, id);
		out.string(2, title);
		out.string(3, body);
		out.fixed32(4, accent);
		out.uint(5, Math.max(0, durationMs));
		out.bool(6, sound);
	}

	static Notify read(WireReader in) {
		String id = "";
		String title = "";
		String body = "";
		int accent = 0;
		int durationMs = 0;
		boolean sound = false;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					title = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					body = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					accent = in.readFixed32();
					break;
				case 5:
					durationMs = in.readUint();
					break;
				case 6:
					sound = in.readBool();
					break;
				default:
					in.skip();
			}
		}
		return new Notify(id, title, body, accent, durationMs, sound);
	}

	@Override
	public String toString() {
		return "Notify{id=" + id + ", title=" + title + ", body=" + body + ", accent=" + Integer.toHexString(accent)
				+ ", durationMs=" + durationMs + ", sound=" + sound + "}";
	}
}
