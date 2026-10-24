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

public final class Presence implements Message {
	public final String game;
	public final String map;
	public final String state;
	public final long started;
	public final int partySize;
	public final int partyMax;
	public final boolean joinable;
	public final boolean clear;

	public Presence(String game, String map, String state, long started, int partySize, int partyMax, boolean joinable, boolean clear) {
		this.game = game == null ? "" : game;
		this.map = map == null ? "" : map;
		this.state = state == null ? "" : state;
		this.started = started;
		this.partySize = partySize;
		this.partyMax = partyMax;
		this.joinable = joinable;
		this.clear = clear;
	}

	@Override
	public int type() {
		return TehoProtocol.PRESENCE;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, game);
		out.string(2, map);
		out.string(3, state);
		out.uint(4, started);
		out.uint(5, partySize);
		out.uint(6, partyMax);
		out.bool(7, joinable);
		out.bool(8, clear);
	}

	static Presence read(WireReader in) {
		String game = "";
		String map = "";
		String state = "";
		long started = 0L;
		int partySize = 0;
		int partyMax = 0;
		boolean joinable = false;
		boolean clear = false;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					game = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					map = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					state = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					started = in.readVarint();
					break;
				case 5:
					partySize = in.readUint();
					break;
				case 6:
					partyMax = in.readUint();
					break;
				case 7:
					joinable = in.readBool();
					break;
				case 8:
					clear = in.readBool();
					break;
				default:
					in.skip();
			}
		}
		return new Presence(game, map, state, started, partySize, partyMax, joinable, clear);
	}

	@Override
	public String toString() {
		return "Presence{" + "game=" + game + ", " + "map=" + map + ", " + "state=" + state + ", " + "started=" + started + ", " + "partySize=" + partySize + ", " + "partyMax=" + partyMax + ", " + "joinable=" + joinable + ", " + "clear=" + clear + "}";
	}
}
