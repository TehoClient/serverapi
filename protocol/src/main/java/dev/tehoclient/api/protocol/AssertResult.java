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

public final class AssertResult implements Message {
	public final int requestId;
	public final String token;
	public final String error;

	public AssertResult(int requestId, String token, String error) {
		this.requestId = requestId;
		this.token = token == null ? "" : token;
		this.error = error == null ? "" : error;
	}

	@Override
	public int type() {
		return TehoProtocol.ASSERT_RESULT;
	}

	@Override
	public void write(WireWriter out) {
		out.uint(1, requestId);
		out.string(2, token);
		out.string(3, error);
	}

	static AssertResult read(WireReader in) {
		int requestId = 0;
		String token = "";
		String error = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					requestId = in.readUint();
					break;
				case 2:
					token = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					error = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new AssertResult(requestId, token, error);
	}

	@Override
	public String toString() {
		return "AssertResult{" + "requestId=" + requestId + ", " + "token=" + token + ", " + "error=" + error + "}";
	}
}
