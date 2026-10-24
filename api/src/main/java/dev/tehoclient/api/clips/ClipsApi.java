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

package dev.tehoclient.api.clips;

import java.util.concurrent.CompletableFuture;

public interface ClipsApi {
	final class Clip {
		private final String id;
		private final boolean saved;
		private final String url;
		private final String error;

		public Clip(String id, boolean saved, String url, String error) {
			this.id = id;
			this.saved = saved;
			this.url = url == null ? "" : url;
			this.error = error == null ? "" : error;
		}

		public String id() {
			return id;
		}

		public boolean saved() {
			return saved;
		}

		public String url() {
			return url;
		}

		public String error() {
			return error;
		}

		@Override
		public String toString() {
			return "Clip{" + id + ", " + (saved ? "saved" : "not saved: " + error) + "}";
		}
	}

	CompletableFuture<Clip> mark(String id, String title, long beforeMs, long afterMs);
}
