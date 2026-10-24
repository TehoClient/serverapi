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

package dev.tehoclient.api.common;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import dev.tehoclient.api.asset.Asset;
import dev.tehoclient.api.asset.AssetStore;

final class AssetHub implements AssetStore {
	static final long MAX_BYTES = 128L * 1024 * 1024;

	private final Map<String, Asset> assets = new LinkedHashMap<>(16, 0.75f, true);
	private long bytes;

	@Override
	public synchronized Asset register(Asset asset) {
		if (asset == null) {
			throw new IllegalArgumentException("asset is required");
		}
		Asset existing = assets.get(asset.hex());
		if (existing != null) {
			return existing;
		}
		assets.put(asset.hex(), asset);
		bytes += asset.size();
		Iterator<Map.Entry<String, Asset>> oldest = assets.entrySet().iterator();
		while (bytes > MAX_BYTES && oldest.hasNext()) {
			Asset evicted = oldest.next().getValue();
			if (evicted == asset) {
				break;
			}
			bytes -= evicted.size();
			oldest.remove();
		}
		return asset;
	}

	@Override
	public synchronized Optional<Asset> find(byte[] hash) {
		return Optional.ofNullable(assets.get(hex(hash)));
	}

	static String hex(byte[] hash) {
		StringBuilder out = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			out.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
		}
		return out.toString();
	}
}
