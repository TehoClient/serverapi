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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class Chunking {
	public static final int MAX_STREAM = 4 * 1024 * 1024;
	public static final int SLICE = 30000;
	private static final int MAX_OPEN = 8;
	private static final long STALE_MS = 30_000;

	private final Map<Integer, Partial> open = new HashMap<>();

	public static List<byte[]> frames(Message message, int stream) {
		WireWriter out = new WireWriter();
		out.rawVarint(message.type());
		message.write(out);
		byte[] whole = out.toByteArray();
		List<byte[]> frames = new ArrayList<>();
		if (whole.length <= TehoProtocol.MAX_FRAME) {
			frames.add(whole);
			return frames;
		}
		if (whole.length > MAX_STREAM) {
			throw new ProtocolException("message " + whole.length + " over " + MAX_STREAM);
		}
		int total = (whole.length + SLICE - 1) / SLICE;
		for (int index = 0; index < total; index++) {
			int from = index * SLICE;
			byte[] slice = Arrays.copyOfRange(whole, from, Math.min(whole.length, from + SLICE));
			frames.add(TehoProtocol.encode(new Chunk(stream, index, total, slice)));
		}
		return frames;
	}

	public synchronized byte[] accept(Chunk chunk, long now) {
		expire(now);
		if (chunk.total < 1 || chunk.index >= chunk.total || (long) chunk.total * SLICE > MAX_STREAM + SLICE) {
			throw new ProtocolException("chunk bounds");
		}
		Partial partial = open.get(chunk.stream);
		if (partial == null || partial.parts.length != chunk.total) {
			if (open.size() >= MAX_OPEN && partial == null) {
				throw new ProtocolException("too many open streams");
			}
			partial = new Partial(chunk.total, now);
			open.put(chunk.stream, partial);
		}
		if (partial.parts[chunk.index] == null) {
			partial.bytes += chunk.data.length;
			if (partial.bytes > MAX_STREAM) {
				open.remove(chunk.stream);
				throw new ProtocolException("stream over " + MAX_STREAM);
			}
			partial.parts[chunk.index] = chunk.data;
			partial.received++;
		}
		partial.touched = now;
		if (partial.received < partial.parts.length) {
			return null;
		}
		open.remove(chunk.stream);
		ByteArrayOutputStream joined = new ByteArrayOutputStream(partial.bytes);
		for (byte[] part : partial.parts) {
			joined.write(part, 0, part.length);
		}
		return joined.toByteArray();
	}

	public synchronized void clear() {
		open.clear();
	}

	private void expire(long now) {
		Iterator<Partial> it = open.values().iterator();
		while (it.hasNext()) {
			if (now - it.next().touched > STALE_MS) {
				it.remove();
			}
		}
	}

	public static Message decodeWhole(byte[] whole) {
		if (whole == null || whole.length == 0 || whole.length > MAX_STREAM) {
			throw new ProtocolException("stream size");
		}
		WireReader in = new WireReader(whole);
		long type = in.rawVarint();
		if (type == TehoProtocol.CHUNK) {
			throw new ProtocolException("nested chunk");
		}
		byte[] framed = whole;
		if (whole.length > TehoProtocol.MAX_FRAME) {
			return TehoProtocol.decodeUnbounded(framed);
		}
		return TehoProtocol.decode(framed);
	}

	private static final class Partial {
		final byte[][] parts;
		int received;
		int bytes;
		long touched;

		Partial(int total, long now) {
			this.parts = new byte[total][];
			this.touched = now;
		}
	}
}
