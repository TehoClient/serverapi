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

public final class WaypointSet implements Message {
	public final String id;
	public final String name;
	public final int x;
	public final int y;
	public final int z;
	public final String dimension;
	public final int color;
	public final boolean focus;

	public WaypointSet(String id, String name, int x, int y, int z, String dimension, int color, boolean focus) {
		this.id = id == null ? "" : id;
		this.name = name == null ? "" : name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.dimension = dimension == null ? "" : dimension;
		this.color = color;
		this.focus = focus;
	}

	@Override
	public int type() {
		return TehoProtocol.WAYPOINT_SET;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, id);
		out.string(2, name);
		out.sint(3, x);
		out.sint(4, y);
		out.sint(5, z);
		out.string(6, dimension);
		out.fixed32(7, color);
		out.bool(8, focus);
	}

	static WaypointSet read(WireReader in) {
		String id = "";
		String name = "";
		int x = 0;
		int y = 0;
		int z = 0;
		String dimension = "";
		int color = 0;
		boolean focus = false;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					name = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					x = in.readSint();
					break;
				case 4:
					y = in.readSint();
					break;
				case 5:
					z = in.readSint();
					break;
				case 6:
					dimension = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 7:
					color = in.readFixed32();
					break;
				case 8:
					focus = in.readBool();
					break;
				default:
					in.skip();
			}
		}
		return new WaypointSet(id, name, x, y, z, dimension, color, focus);
	}

	@Override
	public String toString() {
		return "WaypointSet{id=" + id + ", name=" + name + ", x=" + x + ", y=" + y + ", z=" + z
				+ ", dimension=" + dimension + ", color=" + Integer.toHexString(color) + ", focus=" + focus + "}";
	}
}
