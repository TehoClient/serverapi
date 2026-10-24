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

public final class Highlight implements Message {
	public final String id;
	public final int kind;
	public final String entity;
	public final int x;
	public final int y;
	public final int z;
	public final int x2;
	public final int y2;
	public final int z2;
	public final int color;
	public final String label;
	public final int style;
	public final boolean tracer;
	public final int ttlMs;
	public final String dimension;

	public Highlight(String id, int kind, String entity, int x, int y, int z, int x2, int y2, int z2, int color, String label, int style, boolean tracer, int ttlMs, String dimension) {
		this.id = id == null ? "" : id;
		this.kind = kind;
		this.entity = entity == null ? "" : entity;
		this.x = x;
		this.y = y;
		this.z = z;
		this.x2 = x2;
		this.y2 = y2;
		this.z2 = z2;
		this.color = color;
		this.label = label == null ? "" : label;
		this.style = style;
		this.tracer = tracer;
		this.ttlMs = ttlMs;
		this.dimension = dimension == null ? "" : dimension;
	}

	@Override
	public int type() {
		return TehoProtocol.HIGHLIGHT;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, id);
		out.uint(2, kind);
		out.string(3, entity);
		out.sint(4, x);
		out.sint(5, y);
		out.sint(6, z);
		out.sint(7, x2);
		out.sint(8, y2);
		out.sint(9, z2);
		out.fixed32(10, color);
		out.string(11, label);
		out.uint(12, style);
		out.bool(13, tracer);
		out.uint(14, ttlMs);
		out.string(15, dimension);
	}

	static Highlight read(WireReader in) {
		String id = "";
		int kind = 0;
		String entity = "";
		int x = 0;
		int y = 0;
		int z = 0;
		int x2 = 0;
		int y2 = 0;
		int z2 = 0;
		int color = 0;
		String label = "";
		int style = 0;
		boolean tracer = false;
		int ttlMs = 0;
		String dimension = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					kind = in.readUint();
					break;
				case 3:
					entity = in.readString(TehoProtocol.MAX_STRING);
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
				case 7:
					x2 = in.readSint();
					break;
				case 8:
					y2 = in.readSint();
					break;
				case 9:
					z2 = in.readSint();
					break;
				case 10:
					color = in.readFixed32();
					break;
				case 11:
					label = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 12:
					style = in.readUint();
					break;
				case 13:
					tracer = in.readBool();
					break;
				case 14:
					ttlMs = in.readUint();
					break;
				case 15:
					dimension = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new Highlight(id, kind, entity, x, y, z, x2, y2, z2, color, label, style, tracer, ttlMs, dimension);
	}

	@Override
	public String toString() {
		return "Highlight{" + "id=" + id + ", " + "kind=" + kind + ", " + "entity=" + entity + ", " + "x=" + x + ", " + "y=" + y + ", " + "z=" + z + ", " + "x2=" + x2 + ", " + "y2=" + y2 + ", " + "z2=" + z2 + ", " + "color=" + Integer.toHexString(color) + ", " + "label=" + label + ", " + "style=" + style + ", " + "tracer=" + tracer + ", " + "ttlMs=" + ttlMs + ", " + "dimension=" + dimension + "}";
	}
}
