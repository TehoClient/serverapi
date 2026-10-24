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

public final class Alert implements Message {
	public final String id;
	public final int severity;
	public final String title;
	public final String body;
	public final String player;
	public final int x;
	public final int y;
	public final int z;
	public final String dimension;
	public final int ttlMs;

	public Alert(String id, int severity, String title, String body, String player, int x, int y, int z, String dimension, int ttlMs) {
		this.id = id == null ? "" : id;
		this.severity = severity;
		this.title = title == null ? "" : title;
		this.body = body == null ? "" : body;
		this.player = player == null ? "" : player;
		this.x = x;
		this.y = y;
		this.z = z;
		this.dimension = dimension == null ? "" : dimension;
		this.ttlMs = ttlMs;
	}

	@Override
	public int type() {
		return TehoProtocol.ALERT;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, id);
		out.uint(2, severity);
		out.string(3, title);
		out.string(4, body);
		out.string(5, player);
		out.sint(6, x);
		out.sint(7, y);
		out.sint(8, z);
		out.string(9, dimension);
		out.uint(10, ttlMs);
	}

	static Alert read(WireReader in) {
		String id = "";
		int severity = 0;
		String title = "";
		String body = "";
		String player = "";
		int x = 0;
		int y = 0;
		int z = 0;
		String dimension = "";
		int ttlMs = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					severity = in.readUint();
					break;
				case 3:
					title = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					body = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 5:
					player = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 6:
					x = in.readSint();
					break;
				case 7:
					y = in.readSint();
					break;
				case 8:
					z = in.readSint();
					break;
				case 9:
					dimension = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 10:
					ttlMs = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new Alert(id, severity, title, body, player, x, y, z, dimension, ttlMs);
	}

	@Override
	public String toString() {
		return "Alert{" + "id=" + id + ", " + "severity=" + severity + ", " + "title=" + title + ", " + "body=" + body + ", " + "player=" + player + ", " + "x=" + x + ", " + "y=" + y + ", " + "z=" + z + ", " + "dimension=" + dimension + ", " + "ttlMs=" + ttlMs + "}";
	}
}
