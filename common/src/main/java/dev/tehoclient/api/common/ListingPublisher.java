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

import dev.tehoclient.api.listing.ListingApi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ListingPublisher {
	private static final long MAX_IMAGE = 4 * 1024 * 1024;

	private ListingPublisher() {}

	public static void publish(TehoCore core, Map<?, ?> root, Path folder, Consumer<String> warn) {
		Object raw = root == null ? null : root.get("listing");
		if (!(raw instanceof Map)) {
			return;
		}
		Map<?, ?> section = (Map<?, ?>) raw;
		if (!Boolean.TRUE.equals(section.get("enabled"))) {
			return;
		}
		if (!core.listing().enabled()) {
			warn.accept("listing.enabled is on, but backend.key is empty, so the server cannot be listed in Discover");
			return;
		}
		ListingApi.Listing listing = new ListingApi.Listing()
				.name(text(section, "name"))
				.address(text(section, "address"))
				.description(text(section, "description"))
				.versions(text(section, "versions"));
		for (Object category : list(section.get("categories"))) {
			listing.category(String.valueOf(category));
		}
		try {
			listing.icon(image(folder, text(section, "icon")));
			listing.banner(image(folder, text(section, "banner")));
		} catch (IOException unreadable) {
			warn.accept("Not publishing the Discover listing: " + unreadable.getMessage());
			return;
		}
		core.listing().publish(listing).thenAccept(result -> {
			if (!result.ok()) {
				warn.accept("Discover listing refused: " + result.error());
			}
		});
	}

	private static byte[] image(Path folder, String name) throws IOException {
		if (name.isEmpty() || folder == null) {
			return null;
		}
		Path path = folder.resolve(name).normalize();
		if (!path.startsWith(folder.normalize())) {
			throw new IOException(name + " must be inside the plugin folder");
		}
		if (!Files.isRegularFile(path)) {
			throw new IOException(name + " was not found in the plugin folder");
		}
		if (Files.size(path) > MAX_IMAGE) {
			throw new IOException(name + " is larger than 4 MiB");
		}
		return Files.readAllBytes(path);
	}

	private static String text(Map<?, ?> section, String key) {
		Object value = section.get(key);
		return value == null ? "" : String.valueOf(value).trim();
	}

	private static List<?> list(Object value) {
		return value instanceof List ? (List<?>) value : Collections.emptyList();
	}
}
