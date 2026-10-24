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

public final class Hello implements Message {
	public final int protocol;
	public final String client;
	public final String minecraft;
	public final String platform;
	public final List<String> capabilities;

	public Hello(int protocol, String client, String minecraft, String platform, List<String> capabilities) {
		this.protocol = protocol;
		this.client = client == null ? "" : client;
		this.minecraft = minecraft == null ? "" : minecraft;
		this.platform = platform == null ? "" : platform;
		this.capabilities = Collections.unmodifiableList(new ArrayList<>(capabilities));
	}

	@Override
	public int type() {
		return TehoProtocol.HELLO;
	}

	@Override
	public void write(WireWriter out) {
		out.uint(1, protocol);
		out.string(2, client);
		out.string(3, minecraft);
		out.string(4, platform);
		for (String capability : capabilities) {
			out.string(5, capability);
		}
	}

	static Hello read(WireReader in) {
		int protocol = 0;
		String client = "";
		String minecraft = "";
		String platform = "";
		List<String> capabilities = new ArrayList<>();
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					protocol = in.readUint();
					break;
				case 2:
					client = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					minecraft = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					platform = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 5:
					String capability = in.readString(TehoProtocol.MAX_STRING);
					if (capabilities.size() < TehoProtocol.MAX_LIST) {
						capabilities.add(capability);
					}
					break;
				default:
					in.skip();
			}
		}
		return new Hello(protocol, client, minecraft, platform, capabilities);
	}

	@Override
	public String toString() {
		return "Hello{protocol=" + protocol + ", client=" + client + ", minecraft=" + minecraft
				+ ", platform=" + platform + ", capabilities=" + capabilities + "}";
	}
}
