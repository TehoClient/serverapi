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

public final class Branding implements Message {
	public final int accent;
	public final byte[] logo;
	public final byte[] banner;
	public final byte[] background;
	public final String tagline;
	public final boolean reset;

	public Branding(int accent, byte[] logo, byte[] banner, byte[] background, String tagline, boolean reset) {
		this.accent = accent;
		this.logo = logo == null ? new byte[0] : logo;
		this.banner = banner == null ? new byte[0] : banner;
		this.background = background == null ? new byte[0] : background;
		this.tagline = tagline == null ? "" : tagline;
		this.reset = reset;
	}

	@Override
	public int type() {
		return TehoProtocol.BRANDING_SET;
	}

	@Override
	public void write(WireWriter out) {
		out.fixed32(1, accent);
		out.bytes(2, logo);
		out.bytes(3, banner);
		out.bytes(4, background);
		out.string(5, tagline);
		out.bool(6, reset);
	}

	static Branding read(WireReader in) {
		int accent = 0;
		byte[] logo = new byte[0];
		byte[] banner = new byte[0];
		byte[] background = new byte[0];
		String tagline = "";
		boolean reset = false;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					accent = in.readFixed32();
					break;
				case 2:
					logo = in.readBytes(Chunking.MAX_STREAM);
					break;
				case 3:
					banner = in.readBytes(Chunking.MAX_STREAM);
					break;
				case 4:
					background = in.readBytes(Chunking.MAX_STREAM);
					break;
				case 5:
					tagline = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 6:
					reset = in.readBool();
					break;
				default:
					in.skip();
			}
		}
		return new Branding(accent, logo, banner, background, tagline, reset);
	}

	@Override
	public String toString() {
		return "Branding{" + "accent=" + Integer.toHexString(accent) + ", " + "logo=" + logo.length + "B" + ", " + "banner=" + banner.length + "B" + ", " + "background=" + background.length + "B" + ", " + "tagline=" + tagline + ", " + "reset=" + reset + "}";
	}
}
