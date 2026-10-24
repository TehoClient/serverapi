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

public final class SettingCap {
	public final String module;
	public final String key;
	public final int min;
	public final int max;
	public final boolean bounded;
	public final String pinned;

	public SettingCap(String module, String key, int min, int max, boolean bounded, String pinned) {
		this.module = module == null ? "" : module;
		this.key = key == null ? "" : key;
		this.min = min;
		this.max = max;
		this.bounded = bounded;
		this.pinned = pinned == null ? "" : pinned;
	}

	public void write(WireWriter out) {
		out.string(1, module);
		out.string(2, key);
		out.sint(3, min);
		out.sint(4, max);
		out.bool(5, bounded);
		out.string(6, pinned);
	}

	public static SettingCap read(WireReader in) {
		String module = "";
		String key = "";
		int min = 0;
		int max = 0;
		boolean bounded = false;
		String pinned = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					module = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					key = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					min = in.readSint();
					break;
				case 4:
					max = in.readSint();
					break;
				case 5:
					bounded = in.readBool();
					break;
				case 6:
					pinned = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new SettingCap(module, key, min, max, bounded, pinned);
	}

	@Override
	public String toString() {
		return "SettingCap{" + "module=" + module + ", " + "key=" + key + ", " + "min=" + min + ", " + "max=" + max + ", " + "bounded=" + bounded + ", " + "pinned=" + pinned + "}";
	}
}
