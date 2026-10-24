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

public final class Keybind {
	public final String id;
	public final String label;
	public final int key;
	public final String button;
	public final boolean hold;
	public final String description;

	public Keybind(String id, String label, int key, String button, boolean hold, String description) {
		this.id = id == null ? "" : id;
		this.label = label == null ? "" : label;
		this.key = key;
		this.button = button == null ? "" : button;
		this.hold = hold;
		this.description = description == null ? "" : description;
	}

	public void write(WireWriter out) {
		out.string(1, id);
		out.string(2, label);
		out.sint(3, key);
		out.string(4, button);
		out.bool(5, hold);
		out.string(6, description);
	}

	public static Keybind read(WireReader in) {
		String id = "";
		String label = "";
		int key = 0;
		String button = "";
		boolean hold = false;
		String description = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					label = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					key = in.readSint();
					break;
				case 4:
					button = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 5:
					hold = in.readBool();
					break;
				case 6:
					description = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new Keybind(id, label, key, button, hold, description);
	}

	@Override
	public String toString() {
		return "Keybind{" + "id=" + id + ", " + "label=" + label + ", " + "key=" + key + ", " + "button=" + button + ", " + "hold=" + hold + ", " + "description=" + description + "}";
	}
}
