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

package dev.tehoclient.api.modules;

public final class SettingCap {
	private final String module;
	private final String key;
	private final int min;
	private final int max;
	private final boolean bounded;
	private final String pinned;

	private SettingCap(String module, String key, int min, int max, boolean bounded, String pinned) {
		if (module == null || module.isEmpty() || key == null || key.isEmpty()) {
			throw new IllegalArgumentException("module and setting key are required");
		}
		this.module = module;
		this.key = key;
		this.min = min;
		this.max = max;
		this.bounded = bounded;
		this.pinned = pinned == null ? "" : pinned;
	}

	public static SettingCap range(String module, String key, int min, int max) {
		return new SettingCap(module, key, Math.min(min, max), Math.max(min, max), true, "");
	}

	public static SettingCap atMost(String module, String key, int max) {
		return new SettingCap(module, key, Integer.MIN_VALUE, max, true, "");
	}

	public static SettingCap pin(String module, String key, String value) {
		return new SettingCap(module, key, 0, 0, false, value);
	}

	public static SettingCap pin(String module, String key, boolean value) {
		return pin(module, key, Boolean.toString(value));
	}

	public static SettingCap pin(String module, String key, int value) {
		return pin(module, key, Integer.toString(value));
	}

	public String module() {
		return module;
	}

	public String key() {
		return key;
	}

	public int min() {
		return min;
	}

	public int max() {
		return max;
	}

	public boolean bounded() {
		return bounded;
	}

	public String pinned() {
		return pinned;
	}

	public String id() {
		return module + "." + key;
	}
}
