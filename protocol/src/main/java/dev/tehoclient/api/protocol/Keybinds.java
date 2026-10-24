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

public final class Keybinds implements Message {
	public final List<Keybind> binds;
	public final String category;

	public Keybinds(List<Keybind> binds, String category) {
		this.binds = binds == null ? Collections.<Keybind>emptyList() : Collections.unmodifiableList(new ArrayList<>(binds));
		this.category = category == null ? "" : category;
	}

	@Override
	public int type() {
		return TehoProtocol.KEYBINDS;
	}

	@Override
	public void write(WireWriter out) {
		for (Keybind item : binds) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(1, nested.toByteArray());
		}
		out.string(2, category);
	}

	static Keybinds read(WireReader in) {
		List<Keybind> binds = new ArrayList<>();
		String category = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					byte[] bindsItem = in.readBytes(Chunking.MAX_STREAM);
					if (binds.size() < TehoProtocol.MAX_LIST) {
						binds.add(Keybind.read(new WireReader(bindsItem)));
					}
					break;
				case 2:
					category = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new Keybinds(binds, category);
	}

	@Override
	public String toString() {
		return "Keybinds{" + "binds=" + binds + ", " + "category=" + category + "}";
	}
}
