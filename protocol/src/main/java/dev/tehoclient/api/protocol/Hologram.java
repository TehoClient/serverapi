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

public final class Hologram implements Message {
	public final String id;
	public final int x;
	public final int y;
	public final int z;
	public final String entity;
	public final List<String> lines;
	public final String item;
	public final byte[] asset;
	public final int scale;
	public final int background;
	public final boolean clickable;
	public final boolean throughWalls;
	public final String dimension;
	public final int ttlMs;
	public final int range;

	public Hologram(String id, int x, int y, int z, String entity, List<String> lines, String item, byte[] asset, int scale, int background, boolean clickable, boolean throughWalls, String dimension, int ttlMs, int range) {
		this.id = id == null ? "" : id;
		this.x = x;
		this.y = y;
		this.z = z;
		this.entity = entity == null ? "" : entity;
		this.lines = lines == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(lines));
		this.item = item == null ? "" : item;
		this.asset = asset == null ? new byte[0] : asset;
		this.scale = scale;
		this.background = background;
		this.clickable = clickable;
		this.throughWalls = throughWalls;
		this.dimension = dimension == null ? "" : dimension;
		this.ttlMs = ttlMs;
		this.range = range;
	}

	@Override
	public int type() {
		return TehoProtocol.HOLOGRAM;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, id);
		out.sint(2, x);
		out.sint(3, y);
		out.sint(4, z);
		out.string(5, entity);
		for (String item : lines) {
			out.string(6, item);
		}
		out.string(7, item);
		out.bytes(8, asset);
		out.uint(9, scale);
		out.fixed32(10, background);
		out.bool(11, clickable);
		out.bool(12, throughWalls);
		out.string(13, dimension);
		out.uint(14, ttlMs);
		out.uint(15, range);
	}

	static Hologram read(WireReader in) {
		String id = "";
		int x = 0;
		int y = 0;
		int z = 0;
		String entity = "";
		List<String> lines = new ArrayList<>();
		String item = "";
		byte[] asset = new byte[0];
		int scale = 0;
		int background = 0;
		boolean clickable = false;
		boolean throughWalls = false;
		String dimension = "";
		int ttlMs = 0;
		int range = 0;
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
					String linesItem = in.readString(TehoProtocol.MAX_STRING);
					if (lines.size() < TehoProtocol.MAX_LIST) {
						lines.add(linesItem);
					}
					break;
				case 7:
					item = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 8:
					asset = in.readBytes(Chunking.MAX_STREAM);
					break;
				case 9:
					scale = in.readUint();
					break;
				case 10:
					background = in.readFixed32();
					break;
				case 11:
					clickable = in.readBool();
					break;
				case 12:
					throughWalls = in.readBool();
					break;
				case 13:
					dimension = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 14:
					ttlMs = in.readUint();
					break;
				case 15:
					range = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new Hologram(id, x, y, z, entity, lines, item, asset, scale, background, clickable, throughWalls, dimension, ttlMs, range);
	}

	@Override
	public String toString() {
		return "Hologram{" + "id=" + id + ", " + "x=" + x + ", " + "y=" + y + ", " + "z=" + z + ", " + "entity=" + entity + ", " + "lines=" + lines + ", " + "item=" + item + ", " + "asset=" + asset.length + "B" + ", " + "scale=" + scale + ", " + "background=" + Integer.toHexString(background) + ", " + "clickable=" + clickable + ", " + "throughWalls=" + throughWalls + ", " + "dimension=" + dimension + ", " + "ttlMs=" + ttlMs + ", " + "range=" + range + "}";
	}
}
