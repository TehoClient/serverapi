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

public final class SidebarSet implements Message {
	public final String title;
	public final List<SidebarRow> rows;
	public final List<String> remove;
	public final boolean replace;
	public final int accent;
	public final String footer;

	public SidebarSet(String title, List<SidebarRow> rows, List<String> remove, boolean replace, int accent, String footer) {
		this.title = title == null ? "" : title;
		this.rows = rows == null ? Collections.<SidebarRow>emptyList() : Collections.unmodifiableList(new ArrayList<>(rows));
		this.remove = remove == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(remove));
		this.replace = replace;
		this.accent = accent;
		this.footer = footer == null ? "" : footer;
	}

	@Override
	public int type() {
		return TehoProtocol.SIDEBAR_SET;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, title);
		for (SidebarRow item : rows) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(2, nested.toByteArray());
		}
		for (String item : remove) {
			out.string(3, item);
		}
		out.bool(4, replace);
		out.fixed32(5, accent);
		out.string(6, footer);
	}

	static SidebarSet read(WireReader in) {
		String title = "";
		List<SidebarRow> rows = new ArrayList<>();
		List<String> remove = new ArrayList<>();
		boolean replace = false;
		int accent = 0;
		String footer = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					title = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					byte[] rowsItem = in.readBytes(Chunking.MAX_STREAM);
					if (rows.size() < TehoProtocol.MAX_LIST) {
						rows.add(SidebarRow.read(new WireReader(rowsItem)));
					}
					break;
				case 3:
					String removeItem = in.readString(TehoProtocol.MAX_STRING);
					if (remove.size() < TehoProtocol.MAX_LIST) {
						remove.add(removeItem);
					}
					break;
				case 4:
					replace = in.readBool();
					break;
				case 5:
					accent = in.readFixed32();
					break;
				case 6:
					footer = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new SidebarSet(title, rows, remove, replace, accent, footer);
	}

	@Override
	public String toString() {
		return "SidebarSet{" + "title=" + title + ", " + "rows=" + rows + ", " + "remove=" + remove + ", " + "replace=" + replace + ", " + "accent=" + Integer.toHexString(accent) + ", " + "footer=" + footer + "}";
	}
}
