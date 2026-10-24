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

public final class AssetOffer implements Message {
	public final byte[] hash;
	public final int kind;
	public final int size;

	public AssetOffer(byte[] hash, int kind, int size) {
		this.hash = hash == null ? new byte[0] : hash;
		this.kind = kind;
		this.size = size;
	}

	@Override
	public int type() {
		return TehoProtocol.ASSET_OFFER;
	}

	@Override
	public void write(WireWriter out) {
		out.bytes(1, hash);
		out.uint(2, kind);
		out.uint(3, size);
	}

	static AssetOffer read(WireReader in) {
		byte[] hash = new byte[0];
		int kind = 0;
		int size = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					hash = in.readBytes(Chunking.MAX_STREAM);
					break;
				case 2:
					kind = in.readUint();
					break;
				case 3:
					size = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new AssetOffer(hash, kind, size);
	}

	@Override
	public String toString() {
		return "AssetOffer{" + "hash=" + hash.length + "B" + ", " + "kind=" + kind + ", " + "size=" + size + "}";
	}
}
