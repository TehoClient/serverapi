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

package dev.tehoclient.api.asset;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public final class Asset {
	public enum Kind {
		PNG(1, 2 * 1024 * 1024), OGG(2, 4 * 1024 * 1024), OBJ(3, 2 * 1024 * 1024);

		private final int wire;
		private final int maxBytes;

		Kind(int wire, int maxBytes) {
			this.wire = wire;
			this.maxBytes = maxBytes;
		}

		public int wire() {
			return wire;
		}

		public int maxBytes() {
			return maxBytes;
		}

		public static Kind byWire(int wire) {
			for (Kind kind : values()) {
				if (kind.wire == wire) {
					return kind;
				}
			}
			return null;
		}
	}

	private final Kind kind;
	private final byte[] hash;
	private final byte[] data;

	private Asset(Kind kind, byte[] hash, byte[] data) {
		this.kind = kind;
		this.hash = hash;
		this.data = data;
	}

	public static Asset of(Kind kind, byte[] data) {
		if (kind == null || data == null || data.length == 0) {
			throw new IllegalArgumentException("an asset needs a kind and some bytes");
		}
		if (data.length > kind.maxBytes) {
			throw new IllegalArgumentException(kind + " assets are limited to " + kind.maxBytes + " bytes");
		}
		check(kind, data);
		return new Asset(kind, sha256(data), data.clone());
	}

	private static void check(Kind kind, byte[] data) {
		if (kind == Kind.PNG && !(data.length > 8 && (data[0] & 0xFF) == 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G')) {
			throw new IllegalArgumentException("not a PNG image");
		}
		if (kind == Kind.OGG && !(data.length > 4 && data[0] == 'O' && data[1] == 'g' && data[2] == 'g' && data[3] == 'S')) {
			throw new IllegalArgumentException("not an Ogg Vorbis file");
		}
	}

	static byte[] sha256(byte[] data) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(data);
		} catch (NoSuchAlgorithmException impossible) {
			throw new IllegalStateException(impossible);
		}
	}

	public Kind kind() {
		return kind;
	}

	public byte[] hash() {
		return hash.clone();
	}

	public byte[] data() {
		return data.clone();
	}

	public int size() {
		return data.length;
	}

	public String hex() {
		StringBuilder out = new StringBuilder(64);
		for (byte b : hash) {
			out.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
		}
		return out.toString();
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Asset && Arrays.equals(hash, ((Asset) other).hash);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(hash);
	}

	@Override
	public String toString() {
		return "Asset{" + kind + ", " + hex().substring(0, 12) + ", " + data.length + "B}";
	}
}
