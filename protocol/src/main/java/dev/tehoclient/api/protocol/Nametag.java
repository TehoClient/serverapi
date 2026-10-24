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

public final class Nametag implements Message {
	public final String player;
	public final List<String> above;
	public final List<String> below;
	public final String prefix;
	public final String suffix;
	public final byte[] badge;
	public final boolean hide;
	public final String badgeItem;

	public Nametag(String player, List<String> above, List<String> below, String prefix, String suffix, byte[] badge, boolean hide, String badgeItem) {
		this.player = player == null ? "" : player;
		this.above = above == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(above));
		this.below = below == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(below));
		this.prefix = prefix == null ? "" : prefix;
		this.suffix = suffix == null ? "" : suffix;
		this.badge = badge == null ? new byte[0] : badge;
		this.hide = hide;
		this.badgeItem = badgeItem == null ? "" : badgeItem;
	}

	@Override
	public int type() {
		return TehoProtocol.NAMETAG;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, player);
		for (String item : above) {
			out.string(2, item);
		}
		for (String item : below) {
			out.string(3, item);
		}
		out.string(4, prefix);
		out.string(5, suffix);
		out.bytes(6, badge);
		out.bool(7, hide);
		out.string(8, badgeItem);
	}

	static Nametag read(WireReader in) {
		String player = "";
		List<String> above = new ArrayList<>();
		List<String> below = new ArrayList<>();
		String prefix = "";
		String suffix = "";
		byte[] badge = new byte[0];
		boolean hide = false;
		String badgeItem = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					player = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					String aboveItem = in.readString(TehoProtocol.MAX_STRING);
					if (above.size() < TehoProtocol.MAX_LIST) {
						above.add(aboveItem);
					}
					break;
				case 3:
					String belowItem = in.readString(TehoProtocol.MAX_STRING);
					if (below.size() < TehoProtocol.MAX_LIST) {
						below.add(belowItem);
					}
					break;
				case 4:
					prefix = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 5:
					suffix = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 6:
					badge = in.readBytes(Chunking.MAX_STREAM);
					break;
				case 7:
					hide = in.readBool();
					break;
				case 8:
					badgeItem = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new Nametag(player, above, below, prefix, suffix, badge, hide, badgeItem);
	}

	@Override
	public String toString() {
		return "Nametag{" + "player=" + player + ", " + "above=" + above + ", " + "below=" + below + ", " + "prefix=" + prefix + ", " + "suffix=" + suffix + ", " + "badge=" + badge.length + "B" + ", " + "hide=" + hide + ", " + "badgeItem=" + badgeItem + "}";
	}
}
