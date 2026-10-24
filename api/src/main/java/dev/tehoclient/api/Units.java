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

package dev.tehoclient.api;

import java.util.UUID;

public final class Units {
	private Units() {}

	public static int centi(double blocks) {
		double scaled = Math.round(blocks * 100.0);
		if (scaled > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		if (scaled < Integer.MIN_VALUE) {
			return Integer.MIN_VALUE;
		}
		return (int) scaled;
	}

	public static double blocks(int centi) {
		return centi / 100.0;
	}

	public static int percent(double fraction) {
		return (int) Math.max(0, Math.min(100000, Math.round(fraction * 100.0)));
	}

	public static int millis(long millis) {
		return (int) Math.max(0, Math.min(Integer.MAX_VALUE, millis));
	}

	public static int argb(int rgb) {
		return (rgb >>> 24) == 0 ? 0xFF000000 | rgb : rgb;
	}

	public static String id(UUID id) {
		return id == null ? "" : id.toString();
	}

	public static String item(String item) {
		if (item == null || item.isEmpty()) {
			return "";
		}
		return item.indexOf(':') < 0 ? "minecraft:" + item : item;
	}

	public static String require(String value, String what) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException(what + " is required");
		}
		return value;
	}
}
