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

package dev.tehoclient.api.suspect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModListResult {
	public static final class Mod {
		private final String id;
		private final String name;
		private final String version;

		public Mod(String id, String name, String version) {
			this.id = id == null ? "" : id;
			this.name = name == null ? "" : name;
			this.version = version == null ? "" : version;
		}

		public String id() {
			return id;
		}

		public String name() {
			return name;
		}

		public String version() {
			return version;
		}

		@Override
		public String toString() {
			return id + " " + version;
		}
	}

	private final boolean granted;
	private final List<Mod> mods;
	private final List<String> tehoModules;

	public ModListResult(boolean granted, List<Mod> mods, List<String> tehoModules) {
		this.granted = granted;
		this.mods = Collections.unmodifiableList(new ArrayList<>(mods));
		this.tehoModules = Collections.unmodifiableList(new ArrayList<>(tehoModules));
	}

	public boolean granted() {
		return granted;
	}

	public List<Mod> mods() {
		return mods;
	}

	public List<String> tehoModules() {
		return tehoModules;
	}
}
