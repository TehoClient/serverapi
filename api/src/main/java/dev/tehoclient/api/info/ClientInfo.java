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

package dev.tehoclient.api.info;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import dev.tehoclient.api.suspect.ModListResult;

public final class ClientInfo {
	public enum Field {
		INPUT("input"), PERFORMANCE("performance"), MODS("mods"), DISPLAY("display"), SYSTEM("system");

		private final String wire;

		Field(String wire) {
			this.wire = wire;
		}

		public String wire() {
			return wire;
		}

		public static Field byWire(String wire) {
			for (Field field : values()) {
				if (field.wire.equals(wire)) {
					return field;
				}
			}
			return null;
		}
	}

	public enum Input {
		KEYBOARD_MOUSE, CONTROLLER, HANDHELD, TOUCH, UNKNOWN;

		public static Input byWire(String wire) {
			switch (wire == null ? "" : wire) {
				case "keyboard":
					return KEYBOARD_MOUSE;
				case "controller":
					return CONTROLLER;
				case "handheld":
					return HANDHELD;
				case "touch":
					return TOUCH;
				default:
					return UNKNOWN;
			}
		}
	}

	private final Set<Field> granted;
	private final Input input;
	private final int fps;
	private final int frameP50Micros;
	private final int frameP99Micros;
	private final int renderDistance;
	private final List<ModListResult.Mod> mods;
	private final List<String> tehoModules;
	private final String locale;
	private final int guiScale;
	private final int width;
	private final int height;
	private final String os;
	private final int memoryMb;

	public ClientInfo(Set<Field> granted, Input input, int fps, int frameP50Micros, int frameP99Micros, int renderDistance,
			List<ModListResult.Mod> mods, List<String> tehoModules, String locale, int guiScale, int width, int height, String os,
			int memoryMb) {
		this.granted = granted.isEmpty() ? Collections.<Field>emptySet() : Collections.unmodifiableSet(EnumSet.copyOf(granted));
		this.input = input;
		this.fps = fps;
		this.frameP50Micros = frameP50Micros;
		this.frameP99Micros = frameP99Micros;
		this.renderDistance = renderDistance;
		this.mods = Collections.unmodifiableList(mods);
		this.tehoModules = Collections.unmodifiableList(tehoModules);
		this.locale = locale;
		this.guiScale = guiScale;
		this.width = width;
		this.height = height;
		this.os = os;
		this.memoryMb = memoryMb;
	}

	public Set<Field> granted() {
		return granted;
	}

	public boolean has(Field field) {
		return granted.contains(field);
	}

	public Input input() {
		return input;
	}

	public int fps() {
		return fps;
	}

	public double frameP50Ms() {
		return frameP50Micros / 1000.0;
	}

	public double frameP99Ms() {
		return frameP99Micros / 1000.0;
	}

	public int renderDistance() {
		return renderDistance;
	}

	public List<ModListResult.Mod> mods() {
		return mods;
	}

	public List<String> tehoModules() {
		return tehoModules;
	}

	public String locale() {
		return locale;
	}

	public int guiScale() {
		return guiScale;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

	public String os() {
		return os;
	}

	public int memoryMb() {
		return memoryMb;
	}

	@Override
	public String toString() {
		return "ClientInfo{granted=" + granted + ", input=" + input + ", fps=" + fps + ", renderDistance=" + renderDistance
				+ ", mods=" + mods.size() + ", locale=" + locale + "}";
	}
}
