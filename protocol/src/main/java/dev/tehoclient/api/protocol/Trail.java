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

public final class Trail implements Message {
	public final String player;
	public final List<Integer> points;
	public final int color;
	public final String label;
	public final int ttlMs;
	public final String dimension;

	public Trail(String player, List<Integer> points, int color, String label, int ttlMs, String dimension) {
		this.player = player == null ? "" : player;
		this.points = points;
		this.color = color;
		this.label = label == null ? "" : label;
		this.ttlMs = ttlMs;
		this.dimension = dimension == null ? "" : dimension;
	}

	@Override
	public int type() {
		return TehoProtocol.TRAIL;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, player);
		for (int item : points) {
			out.sint(2, item);
		}
		out.fixed32(3, color);
		out.string(4, label);
		out.uint(5, ttlMs);
		out.string(6, dimension);
	}

	static Trail read(WireReader in) {
		String player = "";
		List<Integer> points = new ArrayList<>();
		int color = 0;
		String label = "";
		int ttlMs = 0;
		String dimension = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					player = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					int pointsItem = in.readSint();
					if (points.size() < TehoProtocol.MAX_POINTS) {
						points.add(pointsItem);
					}
					break;
				case 3:
					color = in.readFixed32();
					break;
				case 4:
					label = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 5:
					ttlMs = in.readUint();
					break;
				case 6:
					dimension = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new Trail(player, points, color, label, ttlMs, dimension);
	}

	@Override
	public String toString() {
		return "Trail{" + "player=" + player + ", " + "points=" + points + ", " + "color=" + Integer.toHexString(color) + ", " + "label=" + label + ", " + "ttlMs=" + ttlMs + ", " + "dimension=" + dimension + "}";
	}
}
