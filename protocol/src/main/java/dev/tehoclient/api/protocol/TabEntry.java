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

public final class TabEntry {
	public final String player;
	public final String display;
	public final String group;
	public final byte[] icon;
	public final String ping;
	public final int order;
	public final int color;

	public TabEntry(String player, String display, String group, byte[] icon, String ping, int order, int color) {
		this.player = player == null ? "" : player;
		this.display = display == null ? "" : display;
		this.group = group == null ? "" : group;
		this.icon = icon == null ? new byte[0] : icon;
		this.ping = ping == null ? "" : ping;
		this.order = order;
		this.color = color;
	}

	public void write(WireWriter out) {
		out.string(1, player);
		out.string(2, display);
		out.string(3, group);
		out.bytes(4, icon);
		out.string(5, ping);
		out.sint(6, order);
		out.fixed32(7, color);
	}

	public static TabEntry read(WireReader in) {
		String player = "";
		String display = "";
		String group = "";
		byte[] icon = new byte[0];
		String ping = "";
		int order = 0;
		int color = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					player = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					display = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					group = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					icon = in.readBytes(Chunking.MAX_STREAM);
					break;
				case 5:
					ping = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 6:
					order = in.readSint();
					break;
				case 7:
					color = in.readFixed32();
					break;
				default:
					in.skip();
			}
		}
		return new TabEntry(player, display, group, icon, ping, order, color);
	}

	@Override
	public String toString() {
		return "TabEntry{" + "player=" + player + ", " + "display=" + display + ", " + "group=" + group + ", " + "icon=" + icon.length + "B" + ", " + "ping=" + ping + ", " + "order=" + order + ", " + "color=" + Integer.toHexString(color) + "}";
	}
}
