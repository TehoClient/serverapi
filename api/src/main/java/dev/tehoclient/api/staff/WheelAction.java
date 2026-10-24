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

package dev.tehoclient.api.staff;

public final class WheelAction {
	private final String id;
	private final String label;
	private final String item;
	private final int color;
	private final Target.Kind target;

	private WheelAction(String id, String label, String item, int color, Target.Kind target) {
		if (id == null || id.isEmpty()) {
			throw new IllegalArgumentException("action id is required");
		}
		this.id = id;
		this.label = label == null ? id : label;
		this.item = item == null || item.isEmpty() ? "" : (item.indexOf(':') < 0 ? "minecraft:" + item : item);
		this.color = color & 0xFFFFFF;
		this.target = target == null ? Target.Kind.NONE : target;
	}

	public static WheelAction of(String id, String label, String item, Target.Kind target) {
		return new WheelAction(id, label, item, 0, target);
	}

	public WheelAction color(int rgb) {
		return new WheelAction(id, label, item, rgb, target);
	}

	public String id() {
		return id;
	}

	public String label() {
		return label;
	}

	public String item() {
		return item;
	}

	public int color() {
		return color;
	}

	public Target.Kind target() {
		return target;
	}
}
