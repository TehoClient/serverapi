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
import java.util.Arrays;

public final class WireWriter {
	public static final int VARINT = 0;
	public static final int LENGTH = 2;
	public static final int FIXED32 = 5;

	private byte[] buffer = new byte[64];
	private int length;

	public WireWriter rawVarint(long value) {
		while ((value & ~0x7FL) != 0) {
			put((byte) ((value & 0x7F) | 0x80));
			value >>>= 7;
		}
		put((byte) value);
		return this;
	}

	public WireWriter tag(int field, int wireType) {
		return rawVarint(((long) field << 3) | wireType);
	}

	public WireWriter uint(int field, long value) {
		tag(field, VARINT);
		return rawVarint(value);
	}

	public WireWriter sint(int field, int value) {
		tag(field, VARINT);
		return rawVarint(((value << 1) ^ (value >> 31)) & 0xFFFFFFFFL);
	}

	public WireWriter bool(int field, boolean value) {
		return uint(field, value ? 1 : 0);
	}

	public WireWriter fixed32(int field, int value) {
		tag(field, FIXED32);
		put((byte) value);
		put((byte) (value >>> 8));
		put((byte) (value >>> 16));
		put((byte) (value >>> 24));
		return this;
	}

	public WireWriter string(int field, String value) {
		if (value == null) {
			return this;
		}
		return bytes(field, value.getBytes(StandardCharsets.UTF_8));
	}

	public WireWriter bytes(int field, byte[] value) {
		if (value == null) {
			return this;
		}
		tag(field, LENGTH);
		rawVarint(value.length);
		ensure(value.length);
		System.arraycopy(value, 0, buffer, length, value.length);
		length += value.length;
		return this;
	}

	public int size() {
		return length;
	}

	public byte[] toByteArray() {
		return Arrays.copyOf(buffer, length);
	}

	private void put(byte value) {
		ensure(1);
		buffer[length++] = value;
	}

	private void ensure(int extra) {
		if (length + extra > buffer.length) {
			buffer = Arrays.copyOf(buffer, Math.max(buffer.length * 2, length + extra));
		}
	}
}
