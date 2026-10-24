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

public final class WheelAction {
	public final String id;
	public final String label;
	public final String item;
	public final int color;
	public final int target;

	public WheelAction(String id, String label, String item, int color, int target) {
		this.id = id == null ? "" : id;
		this.label = label == null ? "" : label;
		this.item = item == null ? "" : item;
		this.color = color;
		this.target = target;
	}

	public void write(WireWriter out) {
		out.string(1, id);
		out.string(2, label);
		out.string(3, item);
		out.fixed32(4, color);
		out.uint(5, target);
	}

	public static WheelAction read(WireReader in) {
		String id = "";
		String label = "";
		String item = "";
		int color = 0;
		int target = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					label = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					item = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					color = in.readFixed32();
					break;
				case 5:
					target = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new WheelAction(id, label, item, color, target);
	}

	@Override
	public String toString() {
		return "WheelAction{" + "id=" + id + ", " + "label=" + label + ", " + "item=" + item + ", " + "color=" + Integer.toHexString(color) + ", " + "target=" + target + "}";
	}
}
