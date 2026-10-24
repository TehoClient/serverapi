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

public final class TargetStats implements Message {
	public final String player;
	public final List<InspectRow> rows;
	public final int ttlMs;

	public TargetStats(String player, List<InspectRow> rows, int ttlMs) {
		this.player = player == null ? "" : player;
		this.rows = rows == null ? Collections.<InspectRow>emptyList() : Collections.unmodifiableList(new ArrayList<>(rows));
		this.ttlMs = ttlMs;
	}

	@Override
	public int type() {
		return TehoProtocol.TARGET_STATS;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, player);
		for (InspectRow item : rows) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(2, nested.toByteArray());
		}
		out.uint(3, ttlMs);
	}

	static TargetStats read(WireReader in) {
		String player = "";
		List<InspectRow> rows = new ArrayList<>();
		int ttlMs = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					player = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					byte[] rowsItem = in.readBytes(Chunking.MAX_STREAM);
					if (rows.size() < TehoProtocol.MAX_LIST) {
						rows.add(InspectRow.read(new WireReader(rowsItem)));
					}
					break;
				case 3:
					ttlMs = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new TargetStats(player, rows, ttlMs);
	}

	@Override
	public String toString() {
		return "TargetStats{" + "player=" + player + ", " + "rows=" + rows + ", " + "ttlMs=" + ttlMs + "}";
	}
}
