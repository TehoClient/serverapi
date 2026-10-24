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

public final class PackStatus implements Message {
	public final String sha1;
	public final int state;
	public final String error;

	public PackStatus(String sha1, int state, String error) {
		this.sha1 = sha1 == null ? "" : sha1;
		this.state = state;
		this.error = error == null ? "" : error;
	}

	@Override
	public int type() {
		return TehoProtocol.PACK_STATUS;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, sha1);
		out.uint(2, state);
		out.string(3, error);
	}

	static PackStatus read(WireReader in) {
		String sha1 = "";
		int state = 0;
		String error = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					sha1 = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					state = in.readUint();
					break;
				case 3:
					error = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new PackStatus(sha1, state, error);
	}

	@Override
	public String toString() {
		return "PackStatus{" + "sha1=" + sha1 + ", " + "state=" + state + ", " + "error=" + error + "}";
	}
}
