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

public final class LookAt implements Message {
	public final int x;
	public final int y;
	public final int z;
	public final String entity;
	public final int durationMs;
	public final int holdMs;

	public LookAt(int x, int y, int z, String entity, int durationMs, int holdMs) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.entity = entity == null ? "" : entity;
		this.durationMs = durationMs;
		this.holdMs = holdMs;
	}

	@Override
	public int type() {
		return TehoProtocol.LOOK_AT;
	}

	@Override
	public void write(WireWriter out) {
		out.sint(1, x);
		out.sint(2, y);
		out.sint(3, z);
		out.string(4, entity);
		out.uint(5, durationMs);
		out.uint(6, holdMs);
	}

	static LookAt read(WireReader in) {
		int x = 0;
		int y = 0;
		int z = 0;
		String entity = "";
		int durationMs = 0;
		int holdMs = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					x = in.readSint();
					break;
				case 2:
					y = in.readSint();
					break;
				case 3:
					z = in.readSint();
					break;
				case 4:
					entity = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 5:
					durationMs = in.readUint();
					break;
				case 6:
					holdMs = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new LookAt(x, y, z, entity, durationMs, holdMs);
	}

	@Override
	public String toString() {
		return "LookAt{" + "x=" + x + ", " + "y=" + y + ", " + "z=" + z + ", " + "entity=" + entity + ", " + "durationMs=" + durationMs + ", " + "holdMs=" + holdMs + "}";
	}
}
