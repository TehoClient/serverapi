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

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

final class Listings implements ListingApi {
	private final Backend backend;
	private final Supplier<Collection<String>> features;

	Listings(Backend backend, Supplier<Collection<String>> features) {
		this.backend = backend;
		this.features = features;
	}

	@Override
	public boolean enabled() {
		return backend.hasKey();
	}

	@Override
	public CompletableFuture<Result> publish(Listing listing) {
		if (!enabled()) {
			return done(new Result(false, false, "", "no server API key configured"));
		}
		if (listing == null || listing.address().trim().isEmpty()) {
			return done(new Result(false, false, "", "a listing needs an address"));
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", listing.name());
		body.put("address", listing.address().trim());
		body.put("description", listing.description());
		body.put("versions", listing.versions());
		body.put("categories", new ArrayList<Object>(listing.categories()));
		body.put("features", new ArrayList<Object>(features.get()));
		body.put("icon", listing.icon() == null ? "" : Base64.getEncoder().encodeToString(listing.icon()));
		body.put("banner", listing.banner() == null ? "" : Base64.getEncoder().encodeToString(listing.banner()));
		return backend.post("/v1/servers/listing", body).handle((response, failure) -> read(response));
	}

	@Override
	public CompletableFuture<Result> status() {
		if (!enabled()) {
			return done(new Result(false, false, "", "no server API key configured"));
		}
		return backend.get("/v1/servers/listing").handle((response, failure) -> read(response));
	}

	@Override
	public CompletableFuture<Result> unlist() {
		if (!enabled()) {
			return done(new Result(false, false, "", "no server API key configured"));
		}
		return backend.delete("/v1/servers/listing").handle((response, failure) -> response == null
				? new Result(false, false, "", "listing service unreachable")
				: response.ok() ? new Result(true, false, "", "") : new Result(false, false, "", response.error()));
	}

	private static Result read(Backend.Response response) {
		if (response == null) {
			return new Result(false, false, "", "listing service unreachable");
		}
		if (!response.ok()) {
			return new Result(false, false, "", response.error());
		}
		return new Result(true, Json.bool(response.body, "listed"), Json.string(response.body, "tier"), "");
	}

	private static CompletableFuture<Result> done(Result result) {
		CompletableFuture<Result> future = new CompletableFuture<>();
		future.complete(result);
		return future;
	}
}
