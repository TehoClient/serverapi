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

public final class LagMap implements Message {
	public final List<LagCell> cells;
	public final String dimension;
	public final int ttlMs;

	public LagMap(List<LagCell> cells, String dimension, int ttlMs) {
		this.cells = cells == null ? Collections.<LagCell>emptyList() : Collections.unmodifiableList(new ArrayList<>(cells));
		this.dimension = dimension == null ? "" : dimension;
		this.ttlMs = ttlMs;
	}

	@Override
	public int type() {
		return TehoProtocol.LAG_MAP;
	}

	@Override
	public void write(WireWriter out) {
		for (LagCell item : cells) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(1, nested.toByteArray());
		}
		out.string(2, dimension);
		out.uint(3, ttlMs);
	}

	static LagMap read(WireReader in) {
		List<LagCell> cells = new ArrayList<>();
		String dimension = "";
		int ttlMs = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					byte[] cellsItem = in.readBytes(Chunking.MAX_STREAM);
					if (cells.size() < TehoProtocol.MAX_LIST) {
						cells.add(LagCell.read(new WireReader(cellsItem)));
					}
					break;
				case 2:
					dimension = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					ttlMs = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new LagMap(cells, dimension, ttlMs);
	}

	@Override
	public String toString() {
		return "LagMap{" + "cells=" + cells + ", " + "dimension=" + dimension + ", " + "ttlMs=" + ttlMs + "}";
	}
}
