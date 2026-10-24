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

public final class StopSound implements Message {
	public final List<String> ids;
	public final int fadeOutMs;

	public StopSound(List<String> ids, int fadeOutMs) {
		this.ids = ids == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(ids));
		this.fadeOutMs = fadeOutMs;
	}

	@Override
	public int type() {
		return TehoProtocol.STOP_SOUND;
	}

	@Override
	public void write(WireWriter out) {
		for (String item : ids) {
			out.string(1, item);
		}
		out.uint(2, fadeOutMs);
	}

	static StopSound read(WireReader in) {
		List<String> ids = new ArrayList<>();
		int fadeOutMs = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					String idsItem = in.readString(TehoProtocol.MAX_STRING);
					if (ids.size() < TehoProtocol.MAX_LIST) {
						ids.add(idsItem);
					}
					break;
				case 2:
					fadeOutMs = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new StopSound(ids, fadeOutMs);
	}

	@Override
	public String toString() {
		return "StopSound{" + "ids=" + ids + ", " + "fadeOutMs=" + fadeOutMs + "}";
	}
}
