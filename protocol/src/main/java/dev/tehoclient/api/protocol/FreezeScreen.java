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

public final class FreezeScreen implements Message {
	public final boolean on;
	public final String title;
	public final String body;

	public FreezeScreen(boolean on, String title, String body) {
		this.on = on;
		this.title = title == null ? "" : title;
		this.body = body == null ? "" : body;
	}

	@Override
	public int type() {
		return TehoProtocol.FREEZE_SCREEN;
	}

	@Override
	public void write(WireWriter out) {
		out.bool(1, on);
		out.string(2, title);
		out.string(3, body);
	}

	static FreezeScreen read(WireReader in) {
		boolean on = false;
		String title = "";
		String body = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					on = in.readBool();
					break;
				case 2:
					title = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					body = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new FreezeScreen(on, title, body);
	}

	@Override
	public String toString() {
		return "FreezeScreen{" + "on=" + on + ", " + "title=" + title + ", " + "body=" + body + "}";
	}
}
