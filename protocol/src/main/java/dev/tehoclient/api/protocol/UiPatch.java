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

public final class UiPatch implements Message {
	public final String id;
	public final List<UiNode> nodes;
	public final List<String> remove;
	public final String title;

	public UiPatch(String id, List<UiNode> nodes, List<String> remove, String title) {
		this.id = id == null ? "" : id;
		this.nodes = nodes == null ? Collections.<UiNode>emptyList() : Collections.unmodifiableList(new ArrayList<>(nodes));
		this.remove = remove == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(remove));
		this.title = title == null ? "" : title;
	}

	@Override
	public int type() {
		return TehoProtocol.UI_PATCH;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, id);
		for (UiNode item : nodes) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(2, nested.toByteArray());
		}
		for (String item : remove) {
			out.string(3, item);
		}
		out.string(4, title);
	}

	static UiPatch read(WireReader in) {
		String id = "";
		List<UiNode> nodes = new ArrayList<>();
		List<String> remove = new ArrayList<>();
		String title = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					byte[] nodesItem = in.readBytes(Chunking.MAX_STREAM);
					if (nodes.size() < TehoProtocol.MAX_LIST) {
						nodes.add(UiNode.read(new WireReader(nodesItem)));
					}
					break;
				case 3:
					String removeItem = in.readString(TehoProtocol.MAX_STRING);
					if (remove.size() < TehoProtocol.MAX_LIST) {
						remove.add(removeItem);
					}
					break;
				case 4:
					title = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new UiPatch(id, nodes, remove, title);
	}

	@Override
	public String toString() {
		return "UiPatch{" + "id=" + id + ", " + "nodes=" + nodes + ", " + "remove=" + remove + ", " + "title=" + title + "}";
	}
}
