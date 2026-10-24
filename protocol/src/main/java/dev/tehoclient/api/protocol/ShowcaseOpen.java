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

public final class ShowcaseOpen implements Message {
	public final String title;
	public final List<String> items;
	public final String emote;
	public final String focus;
	public final String subtitle;

	public ShowcaseOpen(String title, List<String> items, String emote, String focus, String subtitle) {
		this.title = title == null ? "" : title;
		this.items = items == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(items));
		this.emote = emote == null ? "" : emote;
		this.focus = focus == null ? "" : focus;
		this.subtitle = subtitle == null ? "" : subtitle;
	}

	@Override
	public int type() {
		return TehoProtocol.SHOWCASE_OPEN;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, title);
		for (String item : items) {
			out.string(2, item);
		}
		out.string(3, emote);
		out.string(4, focus);
		out.string(5, subtitle);
	}

	static ShowcaseOpen read(WireReader in) {
		String title = "";
		List<String> items = new ArrayList<>();
		String emote = "";
		String focus = "";
		String subtitle = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					title = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					String itemsItem = in.readString(TehoProtocol.MAX_STRING);
					if (items.size() < TehoProtocol.MAX_LIST) {
						items.add(itemsItem);
					}
					break;
				case 3:
					emote = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					focus = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 5:
					subtitle = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new ShowcaseOpen(title, items, emote, focus, subtitle);
	}

	@Override
	public String toString() {
		return "ShowcaseOpen{" + "title=" + title + ", " + "items=" + items + ", " + "emote=" + emote + ", " + "focus=" + focus + ", " + "subtitle=" + subtitle + "}";
	}
}
