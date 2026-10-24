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

public final class ScreenshotResult implements Message {
	public final int requestId;
	public final boolean granted;
	public final byte[] png;
	public final int width;
	public final int height;

	public ScreenshotResult(int requestId, boolean granted, byte[] png, int width, int height) {
		this.requestId = requestId;
		this.granted = granted;
		this.png = png == null ? new byte[0] : png;
		this.width = width;
		this.height = height;
	}

	@Override
	public int type() {
		return TehoProtocol.SCREENSHOT_RESULT;
	}

	@Override
	public void write(WireWriter out) {
		out.uint(1, requestId);
		out.bool(2, granted);
		out.bytes(3, png);
		out.uint(4, width);
		out.uint(5, height);
	}

	static ScreenshotResult read(WireReader in) {
		int requestId = 0;
		boolean granted = false;
		byte[] png = new byte[0];
		int width = 0;
		int height = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					requestId = in.readUint();
					break;
				case 2:
					granted = in.readBool();
					break;
				case 3:
					png = in.readBytes(Chunking.MAX_STREAM);
					break;
				case 4:
					width = in.readUint();
					break;
				case 5:
					height = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new ScreenshotResult(requestId, granted, png, width, height);
	}

	@Override
	public String toString() {
		return "ScreenshotResult{" + "requestId=" + requestId + ", " + "granted=" + granted + ", " + "png=" + png.length + "B" + ", " + "width=" + width + ", " + "height=" + height + "}";
	}
}
