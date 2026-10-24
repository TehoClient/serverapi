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

public final class UiNode {
	public final String id;
	public final String parent;
	public final int kind;
	public final String text;
	public final String value;
	public final int min;
	public final int max;
	public final int step;
	public final byte[] asset;
	public final String item;
	public final String player;
	public final int color;
	public final int columns;
	public final boolean disabled;
	public final String placeholder;
	public final List<String> options;
	public final long millis;
	public final int style;
	public final int width;
	public final int height;
	public final String tooltip;

	public UiNode(String id, String parent, int kind, String text, String value, int min, int max, int step, byte[] asset, String item, String player, int color, int columns, boolean disabled, String placeholder, List<String> options, long millis, int style, int width, int height, String tooltip) {
		this.id = id == null ? "" : id;
		this.parent = parent == null ? "" : parent;
		this.kind = kind;
		this.text = text == null ? "" : text;
		this.value = value == null ? "" : value;
		this.min = min;
		this.max = max;
		this.step = step;
		this.asset = asset == null ? new byte[0] : asset;
		this.item = item == null ? "" : item;
		this.player = player == null ? "" : player;
		this.color = color;
		this.columns = columns;
		this.disabled = disabled;
		this.placeholder = placeholder == null ? "" : placeholder;
		this.options = options == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(options));
		this.millis = millis;
		this.style = style;
		this.width = width;
		this.height = height;
		this.tooltip = tooltip == null ? "" : tooltip;
	}

	public void write(WireWriter out) {
		out.string(1, id);
		out.string(2, parent);
		out.uint(3, kind);
		out.string(4, text);
		out.string(5, value);
		out.sint(6, min);
		out.sint(7, max);
		out.uint(8, step);
		out.bytes(9, asset);
		out.string(10, item);
		out.string(11, player);
		out.fixed32(12, color);
		out.uint(13, columns);
		out.bool(14, disabled);
		out.string(15, placeholder);
		for (String item : options) {
			out.string(16, item);
		}
		out.uint(17, millis);
		out.uint(18, style);
		out.uint(19, width);
		out.uint(20, height);
		out.string(21, tooltip);
	}

	public static UiNode read(WireReader in) {
		String id = "";
		String parent = "";
		int kind = 0;
		String text = "";
		String value = "";
		int min = 0;
		int max = 0;
		int step = 0;
		byte[] asset = new byte[0];
		String item = "";
		String player = "";
		int color = 0;
		int columns = 0;
		boolean disabled = false;
		String placeholder = "";
		List<String> options = new ArrayList<>();
		long millis = 0L;
		int style = 0;
		int width = 0;
		int height = 0;
		String tooltip = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					parent = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					kind = in.readUint();
					break;
				case 4:
					text = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 5:
					value = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 6:
					min = in.readSint();
					break;
				case 7:
					max = in.readSint();
					break;
				case 8:
					step = in.readUint();
					break;
				case 9:
					asset = in.readBytes(Chunking.MAX_STREAM);
					break;
				case 10:
					item = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 11:
					player = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 12:
					color = in.readFixed32();
					break;
				case 13:
					columns = in.readUint();
					break;
				case 14:
					disabled = in.readBool();
					break;
				case 15:
					placeholder = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 16:
					String optionsItem = in.readString(TehoProtocol.MAX_STRING);
					if (options.size() < TehoProtocol.MAX_LIST) {
						options.add(optionsItem);
					}
					break;
				case 17:
					millis = in.readVarint();
					break;
				case 18:
					style = in.readUint();
					break;
				case 19:
					width = in.readUint();
					break;
				case 20:
					height = in.readUint();
					break;
				case 21:
					tooltip = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new UiNode(id, parent, kind, text, value, min, max, step, asset, item, player, color, columns, disabled, placeholder, options, millis, style, width, height, tooltip);
	}

	@Override
	public String toString() {
		return "UiNode{" + "id=" + id + ", " + "parent=" + parent + ", " + "kind=" + kind + ", " + "text=" + text + ", " + "value=" + value + ", " + "min=" + min + ", " + "max=" + max + ", " + "step=" + step + ", " + "asset=" + asset.length + "B" + ", " + "item=" + item + ", " + "player=" + player + ", " + "color=" + Integer.toHexString(color) + ", " + "columns=" + columns + ", " + "disabled=" + disabled + ", " + "placeholder=" + placeholder + ", " + "options=" + options + ", " + "millis=" + millis + ", " + "style=" + style + ", " + "width=" + width + ", " + "height=" + height + ", " + "tooltip=" + tooltip + "}";
	}
}
