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

public final class Event implements Message {
	public final String kind;
	public final String target;
	public final String value;
	public final int requestId;

	public Event(String kind, String target, String value, int requestId) {
		this.kind = kind == null ? "" : kind;
		this.target = target == null ? "" : target;
		this.value = value == null ? "" : value;
		this.requestId = requestId;
	}

	@Override
	public int type() {
		return TehoProtocol.EVENT;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, kind);
		out.string(2, target);
		out.string(3, value);
		out.uint(4, requestId);
	}

	static Event read(WireReader in) {
		String kind = "";
		String target = "";
		String value = "";
		int requestId = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					kind = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					target = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					value = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					requestId = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new Event(kind, target, value, requestId);
	}

	@Override
	public String toString() {
		return "Event{" + "kind=" + kind + ", " + "target=" + target + ", " + "value=" + value + ", " + "requestId=" + requestId + "}";
	}
}
