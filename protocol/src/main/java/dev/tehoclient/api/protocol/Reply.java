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

public final class Reply implements Message {
	public final int requestId;
	public final boolean ok;
	public final String error;
	public final byte[] data;

	public Reply(int requestId, boolean ok, String error, byte[] data) {
		this.requestId = requestId;
		this.ok = ok;
		this.error = error == null ? "" : error;
		this.data = data == null ? new byte[0] : data;
	}

	@Override
	public int type() {
		return TehoProtocol.REPLY;
	}

	@Override
	public void write(WireWriter out) {
		out.uint(1, requestId);
		out.bool(2, ok);
		out.string(3, error);
		out.bytes(4, data);
	}

	static Reply read(WireReader in) {
		int requestId = 0;
		boolean ok = false;
		String error = "";
		byte[] data = new byte[0];
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					requestId = in.readUint();
					break;
				case 2:
					ok = in.readBool();
					break;
				case 3:
					error = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					data = in.readBytes(Chunking.MAX_STREAM);
					break;
				default:
					in.skip();
			}
		}
		return new Reply(requestId, ok, error, data);
	}

	@Override
	public String toString() {
		return "Reply{" + "requestId=" + requestId + ", " + "ok=" + ok + ", " + "error=" + error + ", " + "data=" + data.length + "B" + "}";
	}
}
