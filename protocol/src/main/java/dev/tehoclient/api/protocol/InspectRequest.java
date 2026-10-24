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

public final class InspectRequest implements Message {
	public final int requestId;
	public final int kind;
	public final String target;
	public final int x;
	public final int y;
	public final int z;

	public InspectRequest(int requestId, int kind, String target, int x, int y, int z) {
		this.requestId = requestId;
		this.kind = kind;
		this.target = target == null ? "" : target;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public int type() {
		return TehoProtocol.INSPECT_REQUEST;
	}

	@Override
	public void write(WireWriter out) {
		out.uint(1, requestId);
		out.uint(2, kind);
		out.string(3, target);
		out.sint(4, x);
		out.sint(5, y);
		out.sint(6, z);
	}

	static InspectRequest read(WireReader in) {
		int requestId = 0;
		int kind = 0;
		String target = "";
		int x = 0;
		int y = 0;
		int z = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					requestId = in.readUint();
					break;
				case 2:
					kind = in.readUint();
					break;
				case 3:
					target = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					x = in.readSint();
					break;
				case 5:
					y = in.readSint();
					break;
				case 6:
					z = in.readSint();
					break;
				default:
					in.skip();
			}
		}
		return new InspectRequest(requestId, kind, target, x, y, z);
	}

	@Override
	public String toString() {
		return "InspectRequest{" + "requestId=" + requestId + ", " + "kind=" + kind + ", " + "target=" + target + ", " + "x=" + x + ", " + "y=" + y + ", " + "z=" + z + "}";
	}
}
