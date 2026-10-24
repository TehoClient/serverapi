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

public final class KillFeed implements Message {
	public final String killer;
	public final String victim;
	public final String weapon;
	public final String text;
	public final int color;
	public final String killerId;
	public final String victimId;
	public final int ttlMs;

	public KillFeed(String killer, String victim, String weapon, String text, int color, String killerId, String victimId, int ttlMs) {
		this.killer = killer == null ? "" : killer;
		this.victim = victim == null ? "" : victim;
		this.weapon = weapon == null ? "" : weapon;
		this.text = text == null ? "" : text;
		this.color = color;
		this.killerId = killerId == null ? "" : killerId;
		this.victimId = victimId == null ? "" : victimId;
		this.ttlMs = ttlMs;
	}

	@Override
	public int type() {
		return TehoProtocol.KILL_FEED;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, killer);
		out.string(2, victim);
		out.string(3, weapon);
		out.string(4, text);
		out.fixed32(5, color);
		out.string(6, killerId);
		out.string(7, victimId);
		out.uint(8, ttlMs);
	}

	static KillFeed read(WireReader in) {
		String killer = "";
		String victim = "";
		String weapon = "";
		String text = "";
		int color = 0;
		String killerId = "";
		String victimId = "";
		int ttlMs = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					killer = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					victim = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					weapon = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					text = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 5:
					color = in.readFixed32();
					break;
				case 6:
					killerId = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 7:
					victimId = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 8:
					ttlMs = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new KillFeed(killer, victim, weapon, text, color, killerId, victimId, ttlMs);
	}

	@Override
	public String toString() {
		return "KillFeed{" + "killer=" + killer + ", " + "victim=" + victim + ", " + "weapon=" + weapon + ", " + "text=" + text + ", " + "color=" + Integer.toHexString(color) + ", " + "killerId=" + killerId + ", " + "victimId=" + victimId + ", " + "ttlMs=" + ttlMs + "}";
	}
}
