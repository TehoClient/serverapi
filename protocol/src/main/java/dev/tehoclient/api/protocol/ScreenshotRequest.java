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

public final class ScreenshotRequest implements Message {
	public final int requestId;
	public final String reason;
	public final int maxWidth;

	public ScreenshotRequest(int requestId, String reason, int maxWidth) {
		this.requestId = requestId;
		this.reason = reason == null ? "" : reason;
		this.maxWidth = maxWidth;
	}

	@Override
	public int type() {
		return TehoProtocol.SCREENSHOT_REQUEST;
	}

	@Override
	public void write(WireWriter out) {
		out.uint(1, requestId);
		out.string(2, reason);
		out.uint(3, maxWidth);
	}

	static ScreenshotRequest read(WireReader in) {
		int requestId = 0;
		String reason = "";
		int maxWidth = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					requestId = in.readUint();
					break;
				case 2:
					reason = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					maxWidth = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new ScreenshotRequest(requestId, reason, maxWidth);
	}

	@Override
	public String toString() {
		return "ScreenshotRequest{" + "requestId=" + requestId + ", " + "reason=" + reason + ", " + "maxWidth=" + maxWidth + "}";
	}
}
