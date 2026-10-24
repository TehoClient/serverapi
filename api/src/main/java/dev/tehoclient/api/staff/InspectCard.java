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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InspectCard {
	private final String title;
	private final String subtitle;
	private final List<StatRow> rows;
	private final Map<String, String> actions;
	private final int ttlMs;

	private InspectCard(Builder builder) {
		this.title = builder.title;
		this.subtitle = builder.subtitle;
		this.rows = Collections.unmodifiableList(new ArrayList<>(builder.rows));
		this.actions = Collections.unmodifiableMap(new LinkedHashMap<>(builder.actions));
		this.ttlMs = builder.ttlMs;
	}

	public static Builder builder(String title) {
		return new Builder(title);
	}

	public String title() {
		return title;
	}

	public String subtitle() {
		return subtitle;
	}

	public List<StatRow> rows() {
		return rows;
	}

	public Map<String, String> actions() {
		return actions;
	}

	public int ttlMs() {
		return ttlMs;
	}

	public static final class Builder {
		private final String title;
		private final List<StatRow> rows = new ArrayList<>();
		private final Map<String, String> actions = new LinkedHashMap<>();
		private String subtitle = "";
		private int ttlMs = 15000;

		private Builder(String title) {
			this.title = title == null ? "" : title;
		}

		public Builder subtitle(String subtitle) {
			this.subtitle = subtitle == null ? "" : subtitle;
			return this;
		}

		public Builder row(String label, Object value) {
			rows.add(StatRow.of(label, value));
			return this;
		}

		public Builder row(String label, Object value, int rgb) {
			rows.add(StatRow.of(label, value, rgb));
			return this;
		}

		public Builder action(String id, String label) {
			actions.put(id, label);
			return this;
		}

		public Builder lifetime(int milliseconds) {
			this.ttlMs = Math.max(1000, milliseconds);
			return this;
		}

		public InspectCard build() {
			return new InspectCard(this);
		}
	}
}
