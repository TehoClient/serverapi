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

public final class UiEvent implements Message {
	public final String id;
	public final String node;
	public final int action;
	public final String value;

	public UiEvent(String id, String node, int action, String value) {
		this.id = id == null ? "" : id;
		this.node = node == null ? "" : node;
		this.action = action;
		this.value = value == null ? "" : value;
	}

	@Override
	public int type() {
		return TehoProtocol.UI_EVENT;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, id);
		out.string(2, node);
		out.uint(3, action);
		out.string(4, value);
	}

	static UiEvent read(WireReader in) {
		String id = "";
		String node = "";
		int action = 0;
		String value = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					node = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					action = in.readUint();
					break;
				case 4:
					value = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new UiEvent(id, node, action, value);
	}

	@Override
	public String toString() {
		return "UiEvent{" + "id=" + id + ", " + "node=" + node + ", " + "action=" + action + ", " + "value=" + value + "}";
	}
}
