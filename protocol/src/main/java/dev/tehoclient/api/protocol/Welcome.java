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

public final class Welcome implements Message {
	public final int protocol;
	public final long session;
	public final List<String> enabled;
	public final String serverName;
	public final String grant;
	public final String verified;

	public Welcome(int protocol, long session, List<String> enabled, String serverName) {
		this(protocol, session, enabled, serverName, "", "");
	}

	public Welcome(int protocol, long session, List<String> enabled, String serverName, String grant) {
		this(protocol, session, enabled, serverName, grant, "");
	}

	public Welcome(int protocol, long session, List<String> enabled, String serverName, String grant, String verified) {
		this.grant = grant == null ? "" : grant;
		this.verified = verified == null ? "" : verified;
		this.protocol = protocol;
		this.session = session;
		this.enabled = Collections.unmodifiableList(new ArrayList<>(enabled));
		this.serverName = serverName == null ? "" : serverName;
	}

	@Override
	public int type() {
		return TehoProtocol.WELCOME;
	}

	@Override
	public void write(WireWriter out) {
		out.uint(1, protocol);
		out.uint(2, session);
		for (String capability : enabled) {
			out.string(3, capability);
		}
		out.string(4, serverName);
		if (!grant.isEmpty()) {
			out.string(5, grant);
		}
		if (!verified.isEmpty()) {
			out.string(6, verified);
		}
	}

	static Welcome read(WireReader in) {
		int protocol = 0;
		long session = 0;
		List<String> enabled = new ArrayList<>();
		String serverName = "";
		String grant = "";
		String verified = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					protocol = in.readUint();
					break;
				case 2:
					session = in.readVarint();
					break;
				case 3:
					String capability = in.readString(TehoProtocol.MAX_STRING);
					if (enabled.size() < TehoProtocol.MAX_LIST) {
						enabled.add(capability);
					}
					break;
				case 4:
					serverName = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 5:
					grant = in.readString(TehoProtocol.MAX_GRANT);
					break;
				case 6:
					verified = in.readString(TehoProtocol.MAX_GRANT);
					break;
				default:
					in.skip();
			}
		}
		return new Welcome(protocol, session, enabled, serverName, grant, verified);
	}

	@Override
	public String toString() {
		return "Welcome{protocol=" + protocol + ", session=" + session + ", enabled=" + enabled
				+ ", serverName=" + serverName + ", grant=" + (grant.isEmpty() ? "none" : grant.length() + "B")
				+ ", verified=" + (verified.isEmpty() ? "none" : verified.length() + "B") + "}";
	}
}
