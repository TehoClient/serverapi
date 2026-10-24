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

public final class ServerLink {
	public final String label;
	public final String url;
	public final String kind;

	public ServerLink(String label, String url, String kind) {
		this.label = label == null ? "" : label;
		this.url = url == null ? "" : url;
		this.kind = kind == null ? "" : kind;
	}

	public void write(WireWriter out) {
		out.string(1, label);
		out.string(2, url);
		out.string(3, kind);
	}

	public static ServerLink read(WireReader in) {
		String label = "";
		String url = "";
		String kind = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					label = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					url = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					kind = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new ServerLink(label, url, kind);
	}

	@Override
	public String toString() {
		return "ServerLink{" + "label=" + label + ", " + "url=" + url + ", " + "kind=" + kind + "}";
	}
}
