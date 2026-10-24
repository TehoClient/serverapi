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

package dev.tehoclient.api.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import dev.tehoclient.api.protocol.Hello;
import dev.tehoclient.api.protocol.ProtocolException;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.protocol.WireReader;
import dev.tehoclient.api.protocol.WireWriter;

public final class ProxyMessage {
	public static final String CHANNEL = "teho:proxy";
	public static final int VERSION = 1;
	public static final int HANDOFF = 1;
	public static final int RELAY = 2;
	public static final int MAX_BYTES = 32700;

	public final int kind;
	public final UUID player;
	public final long issuedAt;
	public final long nonce;
	public final int protocol;
	public final long session;
	public final List<String> enabled;
	public final String network;
	public final String client;
	public final String minecraft;
	public final String platform;
	public final List<String> advertised;
	public final byte[] frame;

	private ProxyMessage(int kind, UUID player, long issuedAt, long nonce, int protocol, long session,
			Collection<String> enabled, String network, String client, String minecraft, String platform,
			Collection<String> advertised, byte[] frame) {
		this.kind = kind;
		this.player = player;
		this.issuedAt = issuedAt;
		this.nonce = nonce;
		this.protocol = protocol;
		this.session = session;
		this.enabled = Collections.unmodifiableList(new ArrayList<>(enabled));
		this.network = network == null ? "" : network;
		this.client = client == null ? "" : client;
		this.minecraft = minecraft == null ? "" : minecraft;
		this.platform = platform == null ? "" : platform;
		this.advertised = Collections.unmodifiableList(new ArrayList<>(advertised));
		this.frame = frame == null ? new byte[0] : frame;
	}

	public static ProxyMessage handoff(UUID player, long issuedAt, long nonce, int protocol, long session,
			Collection<String> enabled, String network, Hello hello) {
		return new ProxyMessage(HANDOFF, player, issuedAt, nonce, protocol, session, enabled, network,
				hello.client, hello.minecraft, hello.platform, hello.capabilities, null);
	}

	public static ProxyMessage relay(UUID player, long issuedAt, long nonce, byte[] frame) {
		return new ProxyMessage(RELAY, player, issuedAt, nonce, 0, 0, Collections.<String>emptyList(), "", "", "", "",
				Collections.<String>emptyList(), frame);
	}

	public Hello hello() {
		return new Hello(protocol, client, minecraft, platform, advertised);
	}

	byte[] encode() {
		WireWriter out = new WireWriter();
		out.rawVarint(VERSION);
		out.rawVarint(kind);
		out.string(1, player.toString());
		out.uint(2, issuedAt);
		out.uint(3, nonce & 0x7FFFFFFFFFFFFFFFL);
		if (kind == HANDOFF) {
			out.uint(4, protocol);
			out.uint(5, session);
			for (String capability : enabled) {
				out.string(6, capability);
			}
			out.string(7, network);
			out.string(8, client);
			out.string(9, minecraft);
			out.string(10, platform);
			for (String capability : advertised) {
				out.string(11, capability);
			}
		} else {
			out.bytes(12, frame);
		}
		return out.toByteArray();
	}

	static ProxyMessage decode(byte[] body) {
		WireReader in = new WireReader(body);
		long version = in.rawVarint();
		if (version != VERSION) {
			throw new ProtocolException("proxy message version " + version);
		}
		long kind = in.rawVarint();
		if (kind != HANDOFF && kind != RELAY) {
			throw new ProtocolException("proxy message kind " + kind);
		}
		UUID player = null;
		long issuedAt = 0;
		long nonce = 0;
		int protocol = 0;
		long session = 0;
		List<String> enabled = new ArrayList<>();
		String network = "";
		String client = "";
		String minecraft = "";
		String platform = "";
		List<String> advertised = new ArrayList<>();
		byte[] frame = null;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					try {
						player = UUID.fromString(in.readString(64));
					} catch (IllegalArgumentException bad) {
						throw new ProtocolException("player id");
					}
					break;
				case 2:
					issuedAt = in.readVarint();
					break;
				case 3:
					nonce = in.readVarint();
					break;
				case 4:
					protocol = in.readUint();
					break;
				case 5:
					session = in.readVarint();
					break;
				case 6:
					add(enabled, in.readString(TehoProtocol.MAX_STRING));
					break;
				case 7:
					network = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 8:
					client = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 9:
					minecraft = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 10:
					platform = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 11:
					add(advertised, in.readString(TehoProtocol.MAX_STRING));
					break;
				case 12:
					frame = in.readBytes(TehoProtocol.MAX_FRAME);
					break;
				default:
					in.skip();
			}
		}
		if (player == null) {
			throw new ProtocolException("proxy message without player");
		}
		if (kind == RELAY && (frame == null || frame.length == 0)) {
			throw new ProtocolException("empty relay");
		}
		return new ProxyMessage((int) kind, player, issuedAt, nonce, protocol, session, enabled, network, client,
				minecraft, platform, advertised, frame);
	}

	private static void add(List<String> list, String value) {
		if (list.size() < TehoProtocol.MAX_LIST) {
			list.add(value);
		}
	}

	@Override
	public String toString() {
		return kind == HANDOFF
				? "ProxyHandoff{player=" + player + ", protocol=" + protocol + ", enabled=" + enabled + ", client=" + client + "}"
				: "ProxyRelay{player=" + player + ", bytes=" + frame.length + "}";
	}
}
