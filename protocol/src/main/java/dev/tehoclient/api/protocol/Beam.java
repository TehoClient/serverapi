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

public final class Beam implements Message {
	public final String id;
	public final int x;
	public final int y;
	public final int z;
	public final String entity;
	public final int color;
	public final int height;
	public final int width;
	public final boolean pulse;
	public final String dimension;
	public final int ttlMs;

	public Beam(String id, int x, int y, int z, String entity, int color, int height, int width, boolean pulse, String dimension, int ttlMs) {
		this.id = id == null ? "" : id;
		this.x = x;
		this.y = y;
		this.z = z;
		this.entity = entity == null ? "" : entity;
		this.color = color;
		this.height = height;
		this.width = width;
		this.pulse = pulse;
		this.dimension = dimension == null ? "" : dimension;
		this.ttlMs = ttlMs;
	}

	@Override
	public int type() {
		return TehoProtocol.BEAM;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, id);
		out.sint(2, x);
		out.sint(3, y);
		out.sint(4, z);
		out.string(5, entity);
		out.fixed32(6, color);
		out.uint(7, height);
		out.uint(8, width);
		out.bool(9, pulse);
		out.string(10, dimension);
		out.uint(11, ttlMs);
	}

	static Beam read(WireReader in) {
		String id = "";
		int x = 0;
		int y = 0;
		int z = 0;
		String entity = "";
		int color = 0;
		int height = 0;
		int width = 0;
		boolean pulse = false;
		String dimension = "";
		int ttlMs = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					x = in.readSint();
					break;
				case 3:
					y = in.readSint();
					break;
				case 4:
					z = in.readSint();
					break;
				case 5:
					entity = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 6:
					color = in.readFixed32();
					break;
				case 7:
					height = in.readUint();
					break;
				case 8:
					width = in.readUint();
					break;
				case 9:
					pulse = in.readBool();
					break;
				case 10:
					dimension = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 11:
					ttlMs = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new Beam(id, x, y, z, entity, color, height, width, pulse, dimension, ttlMs);
	}

	@Override
	public String toString() {
		return "Beam{" + "id=" + id + ", " + "x=" + x + ", " + "y=" + y + ", " + "z=" + z + ", " + "entity=" + entity + ", " + "color=" + Integer.toHexString(color) + ", " + "height=" + height + ", " + "width=" + width + ", " + "pulse=" + pulse + ", " + "dimension=" + dimension + ", " + "ttlMs=" + ttlMs + "}";
	}
}
