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

public final class ConsentResult implements Message {
	public final String scope;
	public final boolean granted;
	public final List<String> items;

	public ConsentResult(String scope, boolean granted, List<String> items) {
		this.scope = scope == null ? "" : scope;
		this.granted = granted;
		this.items = items == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(items));
	}

	@Override
	public int type() {
		return TehoProtocol.CONSENT_RESULT;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, scope);
		out.bool(2, granted);
		for (String item : items) {
			out.string(3, item);
		}
	}

	static ConsentResult read(WireReader in) {
		String scope = "";
		boolean granted = false;
		List<String> items = new ArrayList<>();
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					scope = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					granted = in.readBool();
					break;
				case 3:
					String itemsItem = in.readString(TehoProtocol.MAX_STRING);
					if (items.size() < TehoProtocol.MAX_LIST) {
						items.add(itemsItem);
					}
					break;
				default:
					in.skip();
			}
		}
		return new ConsentResult(scope, granted, items);
	}

	@Override
	public String toString() {
		return "ConsentResult{" + "scope=" + scope + ", " + "granted=" + granted + ", " + "items=" + items + "}";
	}
}
