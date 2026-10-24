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

public final class NpcSlot {
	public final String slot;
	public final String id;

	public NpcSlot(String slot, String id) {
		this.slot = slot == null ? "" : slot;
		this.id = id == null ? "" : id;
	}

	public void write(WireWriter out) {
		out.string(1, slot);
		out.string(2, id);
	}

	public static NpcSlot read(WireReader in) {
		String slot = "";
		String id = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					slot = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new NpcSlot(slot, id);
	}

	@Override
	public String toString() {
		return "NpcSlot{" + "slot=" + slot + ", " + "id=" + id + "}";
	}
}
