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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Shape implements Message {
	public final String id;
	public final int kind;
	public final List<Integer> points;
	public final int radius;
	public final int height;
	public final int color;
	public final int fill;
	public final int width;
	public final boolean throughWalls;
	public final String label;
	public final String dimension;
	public final int ttlMs;

	public Shape(String id, int kind, List<Integer> points, int radius, int height, int color, int fill, int width, boolean throughWalls, String label, String dimension, int ttlMs) {
		this.id = id == null ? "" : id;
		this.kind = kind;
		this.points = points == null ? Collections.<Integer>emptyList() : Collections.unmodifiableList(new ArrayList<>(points));
		this.radius = radius;
		this.height = height;
		this.color = color;
		this.fill = fill;
		this.width = width;
		this.throughWalls = throughWalls;
		this.label = label == null ? "" : label;
		this.dimension = dimension == null ? "" : dimension;
		this.ttlMs = ttlMs;
	}

	@Override
	public int type() {
		return TehoProtocol.SHAPE;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, id);
		out.uint(2, kind);
		for (Integer item : points) {
			out.sint(3, item);
		}
		out.uint(4, radius);
		out.uint(5, height);
		out.fixed32(6, color);
		out.fixed32(7, fill);
		out.uint(8, width);
		out.bool(9, throughWalls);
		out.string(10, label);
		out.string(11, dimension);
		out.uint(12, ttlMs);
	}

	static Shape read(WireReader in) {
		String id = "";
		int kind = 0;
		List<Integer> points = new ArrayList<>();
		int radius = 0;
		int height = 0;
		int color = 0;
		int fill = 0;
		int width = 0;
		boolean throughWalls = false;
		String label = "";
		String dimension = "";
		int ttlMs = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					kind = in.readUint();
					break;
				case 3:
					int pointsItem = in.readSint();
					if (points.size() < TehoProtocol.MAX_POINTS) {
						points.add(pointsItem);
					}
					break;
				case 4:
					radius = in.readUint();
					break;
				case 5:
					height = in.readUint();
					break;
				case 6:
					color = in.readFixed32();
					break;
				case 7:
					fill = in.readFixed32();
					break;
				case 8:
					width = in.readUint();
					break;
				case 9:
					throughWalls = in.readBool();
					break;
				case 10:
					label = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 11:
					dimension = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 12:
					ttlMs = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new Shape(id, kind, points, radius, height, color, fill, width, throughWalls, label, dimension, ttlMs);
	}

	@Override
	public String toString() {
		return "Shape{" + "id=" + id + ", " + "kind=" + kind + ", " + "points=" + points + ", " + "radius=" + radius + ", " + "height=" + height + ", " + "color=" + Integer.toHexString(color) + ", " + "fill=" + Integer.toHexString(fill) + ", " + "width=" + width + ", " + "throughWalls=" + throughWalls + ", " + "label=" + label + ", " + "dimension=" + dimension + ", " + "ttlMs=" + ttlMs + "}";
	}
}
