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

package dev.tehoclient.api.npc;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class NpcLook {
	public enum Mode {
		EXPLICIT, MIRROR_VIEWER, CLEAR
	}

	private static final NpcLook MIRROR = new NpcLook(Mode.MIRROR_VIEWER, Collections.<String, String>emptyMap());
	private static final NpcLook NONE = new NpcLook(Mode.CLEAR, Collections.<String, String>emptyMap());

	private final Mode mode;
	private final Map<String, String> slots;

	private NpcLook(Mode mode, Map<String, String> slots) {
		this.mode = mode;
		this.slots = Collections.unmodifiableMap(new LinkedHashMap<>(slots));
	}

	public static NpcLook mirrorViewer() {
		return MIRROR;
	}

	public static NpcLook none() {
		return NONE;
	}

	public static Builder wearing() {
		return new Builder();
	}

	public Mode mode() {
		return mode;
	}

	public Map<String, String> slots() {
		return slots;
	}

	public static final class Builder {
		private final Map<String, String> slots = new LinkedHashMap<>();

		public Builder cosmetic(String slot, String id) {
			String key = slot.trim().toLowerCase(Locale.ROOT);
			if ("badge".equals(key)) {
				throw new IllegalArgumentException("badges are identity and can never be put on an NPC");
			}
			slots.put(key, id.trim().toLowerCase(Locale.ROOT));
			return this;
		}

		public Builder cosmetic(String key) {
			int colon = key.indexOf(':');
			if (colon <= 0) {
				throw new IllegalArgumentException("expected slot:id, got " + key);
			}
			return cosmetic(key.substring(0, colon), key.substring(colon + 1));
		}

		public Builder cape(String id) {
			return cosmetic("cape", id);
		}

		public Builder headwear(String id) {
			return cosmetic("headwear", id);
		}

		public Builder wings(String id) {
			return cosmetic("wings", id);
		}

		public Builder bodywear(String id) {
			return cosmetic("bodywear", id);
		}

		public Builder pet(String id) {
			return cosmetic("pet", id);
		}

		public Builder companion(String id) {
			return cosmetic("companion", id);
		}

		public Builder trail(String id) {
			return cosmetic("trail", id);
		}

		public NpcLook build() {
			return new NpcLook(Mode.EXPLICIT, slots);
		}
	}
}
