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

package dev.tehoclient.api.keybind;

import dev.tehoclient.api.Units;

public final class Keybind {
	public static final int UNBOUND = -1;

	private final String id;
	private final String label;
	private final int key;
	private final String button;
	private final boolean hold;
	private final String description;

	private Keybind(Builder builder) {
		this.id = builder.id;
		this.label = builder.label;
		this.key = builder.key;
		this.button = builder.button;
		this.hold = builder.hold;
		this.description = builder.description;
	}

	public static Builder builder(String id, String label) {
		return new Builder(id, label);
	}

	public String id() {
		return id;
	}

	public dev.tehoclient.api.protocol.Keybind wire() {
		return new dev.tehoclient.api.protocol.Keybind(id, label, key, button, hold, description);
	}

	public static final class Builder {
		private final String id;
		private final String label;
		private int key = UNBOUND;
		private String button = "";
		private boolean hold;
		private String description = "";

		private Builder(String id, String label) {
			this.id = Units.require(id, "keybind id");
			this.label = Units.require(label, "keybind label");
		}

		public Builder key(int glfwKey) {
			this.key = glfwKey;
			return this;
		}

		public Builder key(char letter) {
			char upper = Character.toUpperCase(letter);
			if ((upper >= 'A' && upper <= 'Z') || (upper >= '0' && upper <= '9')) {
				this.key = upper;
			}
			return this;
		}

		public Builder controller(String button) {
			this.button = button == null ? "" : button;
			return this;
		}

		public Builder hold(boolean hold) {
			this.hold = hold;
			return this;
		}

		public Builder description(String description) {
			this.description = description == null ? "" : description;
			return this;
		}

		public Keybind build() {
			return new Keybind(this);
		}
	}
}
