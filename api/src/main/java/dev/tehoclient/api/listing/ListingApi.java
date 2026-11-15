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

package dev.tehoclient.api.listing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ListingApi {
	final class Listing {
		private String name = "";
		private String address = "";
		private String description = "";
		private String versions = "";
		private final List<String> categories = new ArrayList<>();
		private byte[] icon;
		private byte[] banner;

		public Listing name(String value) {
			this.name = value == null ? "" : value;
			return this;
		}

		public Listing address(String value) {
			this.address = value == null ? "" : value;
			return this;
		}

		public Listing description(String value) {
			this.description = value == null ? "" : value;
			return this;
		}

		public Listing versions(String value) {
			this.versions = value == null ? "" : value;
			return this;
		}

		public Listing category(String value) {
			if (value != null && !value.trim().isEmpty()) {
				categories.add(value.trim());
			}
			return this;
		}

		public Listing icon(byte[] png) {
			this.icon = png;
			return this;
		}

		public Listing banner(byte[] png) {
			this.banner = png;
			return this;
		}

		public String name() {
			return name;
		}

		public String address() {
			return address;
		}

		public String description() {
			return description;
		}

		public String versions() {
			return versions;
		}

		public List<String> categories() {
			return Collections.unmodifiableList(categories);
		}

		public byte[] icon() {
			return icon;
		}

		public byte[] banner() {
			return banner;
		}
	}

	final class Result {
		private final boolean ok;
		private final boolean listed;
		private final String tier;
		private final String error;

		public Result(boolean ok, boolean listed, String tier, String error) {
			this.ok = ok;
			this.listed = listed;
			this.tier = tier == null ? "" : tier;
			this.error = error == null ? "" : error;
		}

		public boolean ok() {
			return ok;
		}

		public boolean listed() {
			return listed;
		}

		public String tier() {
			return tier;
		}

		public String error() {
			return error;
		}

		@Override
		public String toString() {
			return ok ? "Listing{" + (listed ? "listed as " + tier : "not listed") + "}" : "Listing{refused: " + error + "}";
		}
	}

	boolean enabled();

	CompletableFuture<Result> publish(Listing listing);

	CompletableFuture<Result> status();

	CompletableFuture<Result> unlist();
}
