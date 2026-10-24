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

public final class Cooldown implements Message {
	public final String id;
	public final String label;
	public final int durationMs;
	public final String item;
	public final byte[] asset;
	public final int color;

	public Cooldown(String id, String label, int durationMs, String item, byte[] asset, int color) {
		this.id = id == null ? "" : id;
		this.label = label == null ? "" : label;
		this.durationMs = durationMs;
		this.item = item == null ? "" : item;
		this.asset = asset == null ? new byte[0] : asset;
		this.color = color;
	}

	@Override
	public int type() {
		return TehoProtocol.COOLDOWN;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, id);
		out.string(2, label);
		out.uint(3, durationMs);
		out.string(4, item);
		out.bytes(5, asset);
		out.fixed32(6, color);
	}

	static Cooldown read(WireReader in) {
		String id = "";
		String label = "";
		int durationMs = 0;
		String item = "";
		byte[] asset = new byte[0];
		int color = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					label = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					durationMs = in.readUint();
					break;
				case 4:
					item = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 5:
					asset = in.readBytes(Chunking.MAX_STREAM);
					break;
				case 6:
					color = in.readFixed32();
					break;
				default:
					in.skip();
			}
		}
		return new Cooldown(id, label, durationMs, item, asset, color);
	}

	@Override
	public String toString() {
		return "Cooldown{" + "id=" + id + ", " + "label=" + label + ", " + "durationMs=" + durationMs + ", " + "item=" + item + ", " + "asset=" + asset.length + "B" + ", " + "color=" + Integer.toHexString(color) + "}";
	}
}
