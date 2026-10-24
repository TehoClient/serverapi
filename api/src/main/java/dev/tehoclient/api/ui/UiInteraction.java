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

package dev.tehoclient.api.ui;

public final class UiInteraction {
	public enum Action {
		CLICK, CHANGE, SUBMIT, CLOSED
	}

	private final String screen;
	private final String node;
	private final Action action;
	private final String value;

	public UiInteraction(String screen, String node, Action action, String value) {
		this.screen = screen;
		this.node = node;
		this.action = action;
		this.value = value == null ? "" : value;
	}

	public static Action action(int wire) {
		Action[] all = Action.values();
		return wire >= 0 && wire < all.length ? all[wire] : null;
	}

	public String screen() {
		return screen;
	}

	public String node() {
		return node;
	}

	public Action action() {
		return action;
	}

	public String value() {
		return value;
	}

	public boolean clicked(String nodeId) {
		return action == Action.CLICK && node.equals(nodeId);
	}

	public boolean booleanValue() {
		return "true".equals(value);
	}

	public int intValue() {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException malformed) {
			return 0;
		}
	}

	@Override
	public String toString() {
		return "UiInteraction{" + screen + "/" + node + " " + action + " " + value + "}";
	}
}
