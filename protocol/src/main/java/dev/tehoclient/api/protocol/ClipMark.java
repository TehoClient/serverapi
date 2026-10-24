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

public final class ClipMark implements Message {
	public final String id;
	public final String title;
	public final int beforeMs;
	public final int afterMs;

	public ClipMark(String id, String title, int beforeMs, int afterMs) {
		this.id = id == null ? "" : id;
		this.title = title == null ? "" : title;
		this.beforeMs = beforeMs;
		this.afterMs = afterMs;
	}

	@Override
	public int type() {
		return TehoProtocol.CLIP_MARK;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, id);
		out.string(2, title);
		out.uint(3, beforeMs);
		out.uint(4, afterMs);
	}

	static ClipMark read(WireReader in) {
		String id = "";
		String title = "";
		int beforeMs = 0;
		int afterMs = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					title = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					beforeMs = in.readUint();
					break;
				case 4:
					afterMs = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new ClipMark(id, title, beforeMs, afterMs);
	}

	@Override
	public String toString() {
		return "ClipMark{" + "id=" + id + ", " + "title=" + title + ", " + "beforeMs=" + beforeMs + ", " + "afterMs=" + afterMs + "}";
	}
}
