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

public final class WorldRemove implements Message {
	public final int kind;
	public final List<String> ids;

	public WorldRemove(int kind, List<String> ids) {
		this.kind = kind;
		this.ids = ids == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(ids));
	}

	@Override
	public int type() {
		return TehoProtocol.WORLD_REMOVE;
	}

	@Override
	public void write(WireWriter out) {
		out.uint(1, kind);
		for (String item : ids) {
			out.string(2, item);
		}
	}

	static WorldRemove read(WireReader in) {
		int kind = 0;
		List<String> ids = new ArrayList<>();
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					kind = in.readUint();
					break;
				case 2:
					String idsItem = in.readString(TehoProtocol.MAX_STRING);
					if (ids.size() < TehoProtocol.MAX_LIST) {
						ids.add(idsItem);
					}
					break;
				default:
					in.skip();
			}
		}
		return new WorldRemove(kind, ids);
	}

	@Override
	public String toString() {
		return "WorldRemove{" + "kind=" + kind + ", " + "ids=" + ids + "}";
	}
}
