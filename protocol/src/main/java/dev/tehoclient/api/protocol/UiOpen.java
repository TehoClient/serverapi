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

public final class UiOpen implements Message {
	public final String id;
	public final int surface;
	public final String title;
	public final List<UiNode> nodes;
	public final int width;
	public final boolean closeable;
	public final int anchor;

	public UiOpen(String id, int surface, String title, List<UiNode> nodes, int width, boolean closeable, int anchor) {
		this.id = id == null ? "" : id;
		this.surface = surface;
		this.title = title == null ? "" : title;
		this.nodes = nodes == null ? Collections.<UiNode>emptyList() : Collections.unmodifiableList(new ArrayList<>(nodes));
		this.width = width;
		this.closeable = closeable;
		this.anchor = anchor;
	}

	@Override
	public int type() {
		return TehoProtocol.UI_OPEN;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, id);
		out.uint(2, surface);
		out.string(3, title);
		for (UiNode item : nodes) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(4, nested.toByteArray());
		}
		out.uint(5, width);
		out.bool(6, closeable);
		out.uint(7, anchor);
	}

	static UiOpen read(WireReader in) {
		String id = "";
		int surface = 0;
		String title = "";
		List<UiNode> nodes = new ArrayList<>();
		int width = 0;
		boolean closeable = false;
		int anchor = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					surface = in.readUint();
					break;
				case 3:
					title = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					byte[] nodesItem = in.readBytes(Chunking.MAX_STREAM);
					if (nodes.size() < TehoProtocol.MAX_LIST) {
						nodes.add(UiNode.read(new WireReader(nodesItem)));
					}
					break;
				case 5:
					width = in.readUint();
					break;
				case 6:
					closeable = in.readBool();
					break;
				case 7:
					anchor = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new UiOpen(id, surface, title, nodes, width, closeable, anchor);
	}

	@Override
	public String toString() {
		return "UiOpen{" + "id=" + id + ", " + "surface=" + surface + ", " + "title=" + title + ", " + "nodes=" + nodes + ", " + "width=" + width + ", " + "closeable=" + closeable + ", " + "anchor=" + anchor + "}";
	}
}
