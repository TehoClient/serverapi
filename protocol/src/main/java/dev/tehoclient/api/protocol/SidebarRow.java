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

public final class SidebarRow {
	public final String id;
	public final String text;
	public final String value;
	public final String item;
	public final int color;
	public final int progress;
	public final byte[] asset;
	public final int order;

	public SidebarRow(String id, String text, String value, String item, int color, int progress, byte[] asset, int order) {
		this.id = id == null ? "" : id;
		this.text = text == null ? "" : text;
		this.value = value == null ? "" : value;
		this.item = item == null ? "" : item;
		this.color = color;
		this.progress = progress;
		this.asset = asset == null ? new byte[0] : asset;
		this.order = order;
	}

	public void write(WireWriter out) {
		out.string(1, id);
		out.string(2, text);
		out.string(3, value);
		out.string(4, item);
		out.fixed32(5, color);
		out.uint(6, progress);
		out.bytes(7, asset);
		out.sint(8, order);
	}

	public static SidebarRow read(WireReader in) {
		String id = "";
		String text = "";
		String value = "";
		String item = "";
		int color = 0;
		int progress = 0;
		byte[] asset = new byte[0];
		int order = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					text = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					value = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					item = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 5:
					color = in.readFixed32();
					break;
				case 6:
					progress = in.readUint();
					break;
				case 7:
					asset = in.readBytes(Chunking.MAX_STREAM);
					break;
				case 8:
					order = in.readSint();
					break;
				default:
					in.skip();
			}
		}
		return new SidebarRow(id, text, value, item, color, progress, asset, order);
	}

	@Override
	public String toString() {
		return "SidebarRow{" + "id=" + id + ", " + "text=" + text + ", " + "value=" + value + ", " + "item=" + item + ", " + "color=" + Integer.toHexString(color) + ", " + "progress=" + progress + ", " + "asset=" + asset.length + "B" + ", " + "order=" + order + "}";
	}
}
