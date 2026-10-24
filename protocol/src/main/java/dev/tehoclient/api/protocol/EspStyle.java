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

public final class EspStyle implements Message {
	public final List<EspEntry> entries;

	public EspStyle(List<EspEntry> entries) {
		this.entries = entries == null ? Collections.<EspEntry>emptyList() : Collections.unmodifiableList(new ArrayList<>(entries));
	}

	@Override
	public int type() {
		return TehoProtocol.ESP_STYLE;
	}

	@Override
	public void write(WireWriter out) {
		for (EspEntry item : entries) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(1, nested.toByteArray());
		}
	}

	static EspStyle read(WireReader in) {
		List<EspEntry> entries = new ArrayList<>();
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					byte[] entriesItem = in.readBytes(Chunking.MAX_STREAM);
					if (entries.size() < TehoProtocol.MAX_LIST) {
						entries.add(EspEntry.read(new WireReader(entriesItem)));
					}
					break;
				default:
					in.skip();
			}
		}
		return new EspStyle(entries);
	}

	@Override
	public String toString() {
		return "EspStyle{" + "entries=" + entries + "}";
	}
}
