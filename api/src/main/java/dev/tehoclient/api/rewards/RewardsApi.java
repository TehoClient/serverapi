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

package dev.tehoclient.api.rewards;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface RewardsApi {
	final class Result {
		private final boolean granted;
		private final String item;
		private final String error;
		private final int remaining;

		public Result(boolean granted, String item, String error, int remaining) {
			this.granted = granted;
			this.item = item == null ? "" : item;
			this.error = error == null ? "" : error;
			this.remaining = remaining;
		}

		public boolean granted() {
			return granted;
		}

		public String item() {
			return item;
		}

		public String error() {
			return error;
		}

		public int remaining() {
			return remaining;
		}

		@Override
		public String toString() {
			return "Reward{" + item + ", " + (granted ? "granted, " + remaining + " left" : "refused: " + error) + "}";
		}
	}

	boolean enabled();

	CompletableFuture<Result> grant(UUID player, String item, String reason);
}
