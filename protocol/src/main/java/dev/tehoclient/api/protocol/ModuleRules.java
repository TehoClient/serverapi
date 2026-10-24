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

public final class ModuleRules implements Message {
	public final List<String> disabled;
	public final List<String> disabledCategories;
	public final String reason;
	public final boolean competitive;
	public final List<String> allowed;

	public ModuleRules(List<String> disabled, List<String> disabledCategories, String reason, boolean competitive, List<String> allowed) {
		this.disabled = disabled == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(disabled));
		this.disabledCategories = disabledCategories == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(disabledCategories));
		this.reason = reason == null ? "" : reason;
		this.competitive = competitive;
		this.allowed = allowed == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(allowed));
	}

	@Override
	public int type() {
		return TehoProtocol.MODULE_RULES;
	}

	@Override
	public void write(WireWriter out) {
		for (String item : disabled) {
			out.string(1, item);
		}
		for (String item : disabledCategories) {
			out.string(2, item);
		}
		out.string(3, reason);
		out.bool(4, competitive);
		for (String item : allowed) {
			out.string(5, item);
		}
	}

	static ModuleRules read(WireReader in) {
		List<String> disabled = new ArrayList<>();
		List<String> disabledCategories = new ArrayList<>();
		String reason = "";
		boolean competitive = false;
		List<String> allowed = new ArrayList<>();
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					String disabledItem = in.readString(TehoProtocol.MAX_STRING);
					if (disabled.size() < TehoProtocol.MAX_LIST) {
						disabled.add(disabledItem);
					}
					break;
				case 2:
					String disabledCategoriesItem = in.readString(TehoProtocol.MAX_STRING);
					if (disabledCategories.size() < TehoProtocol.MAX_LIST) {
						disabledCategories.add(disabledCategoriesItem);
					}
					break;
				case 3:
					reason = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					competitive = in.readBool();
					break;
				case 5:
					String allowedItem = in.readString(TehoProtocol.MAX_STRING);
					if (allowed.size() < TehoProtocol.MAX_LIST) {
						allowed.add(allowedItem);
					}
					break;
				default:
					in.skip();
			}
		}
		return new ModuleRules(disabled, disabledCategories, reason, competitive, allowed);
	}

	@Override
	public String toString() {
		return "ModuleRules{" + "disabled=" + disabled + ", " + "disabledCategories=" + disabledCategories + ", " + "reason=" + reason + ", " + "competitive=" + competitive + ", " + "allowed=" + allowed + "}";
	}
}
