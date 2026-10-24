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

public final class EspEntry {
	public final String player;
	public final int color;
	public final String label;

	public EspEntry(String player, int color, String label) {
		this.player = player == null ? "" : player;
		this.color = color;
		this.label = label == null ? "" : label;
	}

	public void write(WireWriter out) {
		out.string(1, player);
		out.fixed32(2, color);
		out.string(3, label);
	}

	public static EspEntry read(WireReader in) {
		String player = "";
		int color = 0;
		String label = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					player = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					color = in.readFixed32();
					break;
				case 3:
					label = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new EspEntry(player, color, label);
	}

	@Override
	public String toString() {
		return "EspEntry{" + "player=" + player + ", " + "color=" + Integer.toHexString(color) + ", " + "label=" + label + "}";
	}
}
