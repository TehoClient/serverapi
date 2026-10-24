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

public final class InspectCard implements Message {
	public final int requestId;
	public final String title;
	public final String subtitle;
	public final List<InspectRow> rows;
	public final List<InspectAction> actions;
	public final int ttlMs;
	public final String target;

	public InspectCard(int requestId, String title, String subtitle, List<InspectRow> rows, List<InspectAction> actions, int ttlMs, String target) {
		this.requestId = requestId;
		this.title = title == null ? "" : title;
		this.subtitle = subtitle == null ? "" : subtitle;
		this.rows = rows == null ? Collections.<InspectRow>emptyList() : Collections.unmodifiableList(new ArrayList<>(rows));
		this.actions = actions == null ? Collections.<InspectAction>emptyList() : Collections.unmodifiableList(new ArrayList<>(actions));
		this.ttlMs = ttlMs;
		this.target = target == null ? "" : target;
	}

	@Override
	public int type() {
		return TehoProtocol.INSPECT_CARD;
	}

	@Override
	public void write(WireWriter out) {
		out.uint(1, requestId);
		out.string(2, title);
		out.string(3, subtitle);
		for (InspectRow item : rows) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(4, nested.toByteArray());
		}
		for (InspectAction item : actions) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(5, nested.toByteArray());
		}
		out.uint(6, ttlMs);
		out.string(7, target);
	}

	static InspectCard read(WireReader in) {
		int requestId = 0;
		String title = "";
		String subtitle = "";
		List<InspectRow> rows = new ArrayList<>();
		List<InspectAction> actions = new ArrayList<>();
		int ttlMs = 0;
		String target = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					requestId = in.readUint();
					break;
				case 2:
					title = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					subtitle = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					byte[] rowsItem = in.readBytes(Chunking.MAX_STREAM);
					if (rows.size() < TehoProtocol.MAX_LIST) {
						rows.add(InspectRow.read(new WireReader(rowsItem)));
					}
					break;
				case 5:
					byte[] actionsItem = in.readBytes(Chunking.MAX_STREAM);
					if (actions.size() < TehoProtocol.MAX_LIST) {
						actions.add(InspectAction.read(new WireReader(actionsItem)));
					}
					break;
				case 6:
					ttlMs = in.readUint();
					break;
				case 7:
					target = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new InspectCard(requestId, title, subtitle, rows, actions, ttlMs, target);
	}

	@Override
	public String toString() {
		return "InspectCard{" + "requestId=" + requestId + ", " + "title=" + title + ", " + "subtitle=" + subtitle + ", " + "rows=" + rows + ", " + "actions=" + actions + ", " + "ttlMs=" + ttlMs + ", " + "target=" + target + "}";
	}
}
