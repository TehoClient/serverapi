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

public final class BorderStyle implements Message {
	public final int color;
	public final boolean hideWall;
	public final boolean reset;

	public BorderStyle(int color, boolean hideWall, boolean reset) {
		this.color = color;
		this.hideWall = hideWall;
		this.reset = reset;
	}

	@Override
	public int type() {
		return TehoProtocol.BORDER_STYLE;
	}

	@Override
	public void write(WireWriter out) {
		out.fixed32(1, color);
		out.bool(2, hideWall);
		out.bool(3, reset);
	}

	static BorderStyle read(WireReader in) {
		int color = 0;
		boolean hideWall = false;
		boolean reset = false;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					color = in.readFixed32();
					break;
				case 2:
					hideWall = in.readBool();
					break;
				case 3:
					reset = in.readBool();
					break;
				default:
					in.skip();
			}
		}
		return new BorderStyle(color, hideWall, reset);
	}

	@Override
	public String toString() {
		return "BorderStyle{" + "color=" + Integer.toHexString(color) + ", " + "hideWall=" + hideWall + ", " + "reset=" + reset + "}";
	}
}
