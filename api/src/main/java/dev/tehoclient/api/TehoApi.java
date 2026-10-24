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

package dev.tehoclient.api;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public interface TehoApi {
	AtomicReference<TehoApi> INSTANCE = new AtomicReference<>();

	static TehoApi get() {
		TehoApi api = INSTANCE.get();
		if (api == null) {
			throw new IllegalStateException("The Teho API is not running on this server");
		}
		return api;
	}

	static Optional<TehoApi> find() {
		return Optional.ofNullable(INSTANCE.get());
	}

	Optional<TehoPlayer> player(UUID id);

	Collection<TehoPlayer> players();

	default boolean isTeho(UUID id) {
		return player(id).isPresent();
	}

	dev.tehoclient.api.npc.NpcApi npcs();

	dev.tehoclient.api.asset.AssetStore assets();

	dev.tehoclient.api.statue.StatueApi statues();

	dev.tehoclient.api.branding.BrandingApi branding();

	dev.tehoclient.api.rewards.RewardsApi rewards();

	void addListener(TehoListener listener);

	void removeListener(TehoListener listener);
}
