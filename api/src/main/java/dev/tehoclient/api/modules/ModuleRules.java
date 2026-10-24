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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ModuleRules {
	public static final ModuleRules NONE = builder().build();

	private final Set<String> disabled;
	private final Set<String> disabledCategories;
	private final boolean competitive;
	private final Set<String> allowed;
	private final String reason;
	private final List<SettingCap> caps;

	private ModuleRules(Builder builder) {
		this.disabled = Collections.unmodifiableSet(new LinkedHashSet<>(builder.disabled));
		this.disabledCategories = Collections.unmodifiableSet(new LinkedHashSet<>(builder.disabledCategories));
		this.competitive = builder.competitive;
		this.allowed = Collections.unmodifiableSet(new LinkedHashSet<>(builder.allowed));
		this.reason = builder.reason;
		this.caps = Collections.unmodifiableList(new ArrayList<>(builder.caps.values()));
	}

	public static Builder builder() {
		return new Builder();
	}

	public Builder toBuilder() {
		Builder builder = new Builder();
		builder.disabled.addAll(disabled);
		builder.disabledCategories.addAll(disabledCategories);
		builder.competitive = competitive;
		builder.allowed.addAll(allowed);
		builder.reason = reason;
		for (SettingCap cap : caps) {
			builder.caps.put(cap.id(), cap);
		}
		return builder;
	}

	public Set<String> disabled() {
		return disabled;
	}

	public Set<String> disabledCategories() {
		return disabledCategories;
	}

	public boolean competitive() {
		return competitive;
	}

	public Set<String> allowed() {
		return allowed;
	}

	public String reason() {
		return reason;
	}

	public List<SettingCap> caps() {
		return caps;
	}

	public boolean isEmpty() {
		return disabled.isEmpty() && disabledCategories.isEmpty() && !competitive && caps.isEmpty();
	}

	public static final class Builder {
		private final Set<String> disabled = new LinkedHashSet<>();
		private final Set<String> disabledCategories = new LinkedHashSet<>();
		private final Set<String> allowed = new LinkedHashSet<>();
		private final Map<String, SettingCap> caps = new LinkedHashMap<>();
		private boolean competitive;
		private String reason = "";

		public Builder disable(String... modules) {
			return disable(Arrays.asList(modules));
		}

		public Builder disable(Collection<String> modules) {
			disabled.addAll(modules);
			return this;
		}

		public Builder enable(String... modules) {
			disabled.removeAll(Arrays.asList(modules));
			return this;
		}

		public Builder disableCategory(String... categories) {
			for (String category : categories) {
				disabledCategories.add(category.toUpperCase(Locale.ROOT));
			}
			return this;
		}

		public Builder competitive(String... allowedModules) {
			return competitive(Arrays.asList(allowedModules));
		}

		public Builder competitive(Collection<String> allowedModules) {
			competitive = true;
			allowed.clear();
			allowed.addAll(allowedModules);
			return this;
		}

		public Builder casual() {
			competitive = false;
			allowed.clear();
			return this;
		}

		public Builder cap(SettingCap cap) {
			caps.put(cap.id(), cap);
			return this;
		}

		public Builder uncap(String module, String key) {
			caps.remove(module + "." + key);
			return this;
		}

		public Builder reason(String reason) {
			this.reason = reason == null ? "" : reason;
			return this;
		}

		public ModuleRules build() {
			return new ModuleRules(this);
		}
	}
}
