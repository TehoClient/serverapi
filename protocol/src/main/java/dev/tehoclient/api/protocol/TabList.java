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

public final class TabList implements Message {
	public final List<String> header;
	public final List<String> footer;
	public final List<TabEntry> entries;
	public final boolean hidePing;
	public final boolean replace;
	public final List<String> remove;
	public final boolean reset;

	public TabList(List<String> header, List<String> footer, List<TabEntry> entries, boolean hidePing, boolean replace, List<String> remove, boolean reset) {
		this.header = header == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(header));
		this.footer = footer == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(footer));
		this.entries = entries == null ? Collections.<TabEntry>emptyList() : Collections.unmodifiableList(new ArrayList<>(entries));
		this.hidePing = hidePing;
		this.replace = replace;
		this.remove = remove == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(remove));
		this.reset = reset;
	}

	@Override
	public int type() {
		return TehoProtocol.TAB_LIST;
	}

	@Override
	public void write(WireWriter out) {
		for (String item : header) {
			out.string(1, item);
		}
		for (String item : footer) {
			out.string(2, item);
		}
		for (TabEntry item : entries) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(3, nested.toByteArray());
		}
		out.bool(4, hidePing);
		out.bool(5, replace);
		for (String item : remove) {
			out.string(6, item);
		}
		out.bool(7, reset);
	}

	static TabList read(WireReader in) {
		List<String> header = new ArrayList<>();
		List<String> footer = new ArrayList<>();
		List<TabEntry> entries = new ArrayList<>();
		boolean hidePing = false;
		boolean replace = false;
		List<String> remove = new ArrayList<>();
		boolean reset = false;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					String headerItem = in.readString(TehoProtocol.MAX_STRING);
					if (header.size() < TehoProtocol.MAX_LIST) {
						header.add(headerItem);
					}
					break;
				case 2:
					String footerItem = in.readString(TehoProtocol.MAX_STRING);
					if (footer.size() < TehoProtocol.MAX_LIST) {
						footer.add(footerItem);
					}
					break;
				case 3:
					byte[] entriesItem = in.readBytes(Chunking.MAX_STREAM);
					if (entries.size() < TehoProtocol.MAX_LIST) {
						entries.add(TabEntry.read(new WireReader(entriesItem)));
					}
					break;
				case 4:
					hidePing = in.readBool();
					break;
				case 5:
					replace = in.readBool();
					break;
				case 6:
					String removeItem = in.readString(TehoProtocol.MAX_STRING);
					if (remove.size() < TehoProtocol.MAX_LIST) {
						remove.add(removeItem);
					}
					break;
				case 7:
					reset = in.readBool();
					break;
				default:
					in.skip();
			}
		}
		return new TabList(header, footer, entries, hidePing, replace, remove, reset);
	}

	@Override
	public String toString() {
		return "TabList{" + "header=" + header + ", " + "footer=" + footer + ", " + "entries=" + entries + ", " + "hidePing=" + hidePing + ", " + "replace=" + replace + ", " + "remove=" + remove + ", " + "reset=" + reset + "}";
	}
}
