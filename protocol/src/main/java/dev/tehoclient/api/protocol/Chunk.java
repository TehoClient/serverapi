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

public final class Chunk implements Message {
	public final int stream;
	public final int index;
	public final int total;
	public final byte[] data;

	public Chunk(int stream, int index, int total, byte[] data) {
		this.stream = stream;
		this.index = index;
		this.total = total;
		this.data = data == null ? new byte[0] : data;
	}

	@Override
	public int type() {
		return TehoProtocol.CHUNK;
	}

	@Override
	public void write(WireWriter out) {
		out.uint(1, stream);
		out.uint(2, index);
		out.uint(3, total);
		out.bytes(4, data);
	}

	static Chunk read(WireReader in) {
		int stream = 0;
		int index = 0;
		int total = 0;
		byte[] data = new byte[0];
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					stream = in.readUint();
					break;
				case 2:
					index = in.readUint();
					break;
				case 3:
					total = in.readUint();
					break;
				case 4:
					data = in.readBytes(Chunking.MAX_STREAM);
					break;
				default:
					in.skip();
			}
		}
		return new Chunk(stream, index, total, data);
	}

	@Override
	public String toString() {
		return "Chunk{" + "stream=" + stream + ", " + "index=" + index + ", " + "total=" + total + ", " + "data=" + data.length + "B" + "}";
	}
}
