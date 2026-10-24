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

public final class ModList implements Message {
	public final int requestId;
	public final boolean granted;
	public final List<ModEntry> mods;
	public final List<String> tehoModules;

	public ModList(int requestId, boolean granted, List<ModEntry> mods, List<String> tehoModules) {
		this.requestId = requestId;
		this.granted = granted;
		this.mods = mods == null ? Collections.<ModEntry>emptyList() : Collections.unmodifiableList(new ArrayList<>(mods));
		this.tehoModules = tehoModules == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(tehoModules));
	}

	@Override
	public int type() {
		return TehoProtocol.MOD_LIST;
	}

	@Override
	public void write(WireWriter out) {
		out.uint(1, requestId);
		out.bool(2, granted);
		for (ModEntry item : mods) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(3, nested.toByteArray());
		}
		for (String item : tehoModules) {
			out.string(4, item);
		}
	}

	static ModList read(WireReader in) {
		int requestId = 0;
		boolean granted = false;
		List<ModEntry> mods = new ArrayList<>();
		List<String> tehoModules = new ArrayList<>();
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					requestId = in.readUint();
					break;
				case 2:
					granted = in.readBool();
					break;
				case 3:
					byte[] modsItem = in.readBytes(Chunking.MAX_STREAM);
					if (mods.size() < TehoProtocol.MAX_LIST) {
						mods.add(ModEntry.read(new WireReader(modsItem)));
					}
					break;
				case 4:
					String tehoModulesItem = in.readString(TehoProtocol.MAX_STRING);
					if (tehoModules.size() < TehoProtocol.MAX_LIST) {
						tehoModules.add(tehoModulesItem);
					}
					break;
				default:
					in.skip();
			}
		}
		return new ModList(requestId, granted, mods, tehoModules);
	}

	@Override
	public String toString() {
		return "ModList{" + "requestId=" + requestId + ", " + "granted=" + granted + ", " + "mods=" + mods + ", " + "tehoModules=" + tehoModules + "}";
	}
}
