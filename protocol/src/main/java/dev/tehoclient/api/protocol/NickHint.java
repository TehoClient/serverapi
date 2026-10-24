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

public final class NickHint implements Message {
	public final String nick;
	public final String player;

	public NickHint(String nick, String player) {
		this.nick = nick == null ? "" : nick;
		this.player = player == null ? "" : player;
	}

	@Override
	public int type() {
		return TehoProtocol.NICK_HINT;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, nick);
		out.string(2, player);
	}

	static NickHint read(WireReader in) {
		String nick = "";
		String player = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					nick = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					player = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new NickHint(nick, player);
	}

	@Override
	public String toString() {
		return "NickHint{" + "nick=" + nick + ", " + "player=" + player + "}";
	}
}
