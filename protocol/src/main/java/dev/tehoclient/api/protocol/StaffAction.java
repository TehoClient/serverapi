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

public final class StaffAction implements Message {
	public final String action;
	public final int targetKind;
	public final String target;
	public final int x;
	public final int y;
	public final int z;

	public StaffAction(String action, int targetKind, String target, int x, int y, int z) {
		this.action = action == null ? "" : action;
		this.targetKind = targetKind;
		this.target = target == null ? "" : target;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public int type() {
		return TehoProtocol.STAFF_ACTION;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, action);
		out.uint(2, targetKind);
		out.string(3, target);
		out.sint(4, x);
		out.sint(5, y);
		out.sint(6, z);
	}

	static StaffAction read(WireReader in) {
		String action = "";
		int targetKind = 0;
		String target = "";
		int x = 0;
		int y = 0;
		int z = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					action = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					targetKind = in.readUint();
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
		return new StaffAction(action, targetKind, target, x, y, z);
	}

	@Override
	public String toString() {
		return "StaffAction{" + "action=" + action + ", " + "targetKind=" + targetKind + ", " + "target=" + target + ", " + "x=" + x + ", " + "y=" + y + ", " + "z=" + z + "}";
	}
}
