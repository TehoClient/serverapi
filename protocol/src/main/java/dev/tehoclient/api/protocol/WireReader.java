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

import java.nio.charset.StandardCharsets;

public final class WireReader {
	private final byte[] data;
	private final int end;
	private int position;
	private int wireType = -1;

	public WireReader(byte[] data) {
		this(data, 0, data.length);
	}

	public WireReader(byte[] data, int offset, int length) {
		if (offset < 0 || length < 0 || offset + length > data.length) {
			throw new ProtocolException("bounds");
		}
		this.data = data;
		this.position = offset;
		this.end = offset + length;
	}

	public boolean hasNext() {
		return position < end;
	}

	public int nextField() {
		long tag = rawVarint();
		wireType = (int) (tag & 7);
		long field = tag >>> 3;
		if (field < 1 || field > 0x1FFFFFFF) {
			throw new ProtocolException("field number");
		}
		return (int) field;
	}

	public int wireType() {
		return wireType;
	}

	public long rawVarint() {
		long result = 0;
		for (int shift = 0; shift < 64; shift += 7) {
			if (position >= end) {
				throw new ProtocolException("truncated varint");
			}
			byte next = data[position++];
			result |= (long) (next & 0x7F) << shift;
			if ((next & 0x80) == 0) {
				return result;
			}
		}
		throw new ProtocolException("varint too long");
	}

	public long readVarint() {
		expect(WireWriter.VARINT);
		return rawVarint();
	}

	public int readUint() {
		long value = readVarint();
		if (value < 0 || value > Integer.MAX_VALUE) {
			throw new ProtocolException("uint range");
		}
		return (int) value;
	}

	public int readSint() {
		int raw = (int) readVarint();
		return (raw >>> 1) ^ -(raw & 1);
	}

	public boolean readBool() {
		return readVarint() != 0;
	}

	public int readFixed32() {
		expect(WireWriter.FIXED32);
		need(4);
		int value = (data[position] & 0xFF)
				| (data[position + 1] & 0xFF) << 8
				| (data[position + 2] & 0xFF) << 16
				| (data[position + 3] & 0xFF) << 24;
		position += 4;
		return value;
	}

	public String readString(int maxBytes) {
		return new String(readBytes(maxBytes), StandardCharsets.UTF_8);
	}

	public byte[] readBytes(int maxBytes) {
		expect(WireWriter.LENGTH);
		long length = rawVarint();
		if (length < 0 || length > maxBytes) {
			throw new ProtocolException("length " + length + " over " + maxBytes);
		}
		need((int) length);
		byte[] out = new byte[(int) length];
		System.arraycopy(data, position, out, 0, out.length);
		position += out.length;
		return out;
	}

	public void skip() {
		switch (wireType) {
			case WireWriter.VARINT:
				rawVarint();
				return;
			case 1:
				need(8);
				position += 8;
				return;
			case WireWriter.LENGTH:
				long length = rawVarint();
				if (length < 0 || length > end - position) {
					throw new ProtocolException("skip length");
				}
				position += (int) length;
				return;
			case WireWriter.FIXED32:
				need(4);
				position += 4;
				return;
			default:
				throw new ProtocolException("wire type " + wireType);
		}
	}

	private void expect(int type) {
		if (wireType != type) {
			throw new ProtocolException("wire type " + wireType + " where " + type + " expected");
		}
	}

	private void need(int bytes) {
		if (bytes > end - position) {
			throw new ProtocolException("truncated");
		}
	}
}
