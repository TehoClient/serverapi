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

public final class ActionWheel implements Message {
	public final List<WheelAction> actions;
	public final String title;

	public ActionWheel(List<WheelAction> actions, String title) {
		this.actions = actions == null ? Collections.<WheelAction>emptyList() : Collections.unmodifiableList(new ArrayList<>(actions));
		this.title = title == null ? "" : title;
	}

	@Override
	public int type() {
		return TehoProtocol.ACTION_WHEEL;
	}

	@Override
	public void write(WireWriter out) {
		for (WheelAction item : actions) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(1, nested.toByteArray());
		}
		out.string(2, title);
	}

	static ActionWheel read(WireReader in) {
		List<WheelAction> actions = new ArrayList<>();
		String title = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					byte[] actionsItem = in.readBytes(Chunking.MAX_STREAM);
					if (actions.size() < TehoProtocol.MAX_LIST) {
						actions.add(WheelAction.read(new WireReader(actionsItem)));
					}
					break;
				case 2:
					title = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new ActionWheel(actions, title);
	}

	@Override
	public String toString() {
		return "ActionWheel{" + "actions=" + actions + ", " + "title=" + title + "}";
	}
}
