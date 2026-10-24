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

public final class Transfer implements Message {
	public final String host;
	public final int port;
	public final String token;
	public final String title;
	public final String reason;

	public Transfer(String host, int port, String token, String title, String reason) {
		this.host = host == null ? "" : host;
		this.port = port;
		this.token = token == null ? "" : token;
		this.title = title == null ? "" : title;
		this.reason = reason == null ? "" : reason;
	}

	@Override
	public int type() {
		return TehoProtocol.TRANSFER;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, host);
		out.uint(2, port);
		out.string(3, token);
		out.string(4, title);
		out.string(5, reason);
	}

	static Transfer read(WireReader in) {
		String host = "";
		int port = 0;
		String token = "";
		String title = "";
		String reason = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					host = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					port = in.readUint();
					break;
				case 3:
					token = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					title = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 5:
					reason = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new Transfer(host, port, token, title, reason);
	}

	@Override
	public String toString() {
		return "Transfer{" + "host=" + host + ", " + "port=" + port + ", " + "token=" + token + ", " + "title=" + title + ", " + "reason=" + reason + "}";
	}
}
