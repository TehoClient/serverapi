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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import dev.tehoclient.api.asset.Asset;
import dev.tehoclient.api.branding.Branding;
import dev.tehoclient.api.branding.BrandingApi;
import dev.tehoclient.api.protocol.StatueRemove;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.rewards.RewardsApi;
import dev.tehoclient.api.statue.Statue;
import dev.tehoclient.api.statue.StatueApi;

final class NetworkDirectors {
	private NetworkDirectors() {}

	static final class Statues implements StatueApi {
		static final int MAX_STATUES = 32;

		private final Supplier<Iterable<TehoSession>> sessions;
		private final Map<String, Statue> placed = new LinkedHashMap<>();

		Statues(Supplier<Iterable<TehoSession>> sessions) {
			this.sessions = sessions;
		}

		@Override
		public synchronized void place(Statue statue) {
			if (!placed.containsKey(statue.id()) && placed.size() >= MAX_STATUES) {
				throw new IllegalStateException("At most " + MAX_STATUES + " statues");
			}
			placed.put(statue.id(), statue);
			for (TehoSession session : sessions.get()) {
				session.deliver(TehoProtocol.STATUE, statue.wire());
			}
		}

		@Override
		public synchronized void remove(String... ids) {
			for (String id : ids) {
				placed.remove(id);
			}
			StatueRemove message = new StatueRemove(Arrays.asList(ids));
			for (TehoSession session : sessions.get()) {
				session.deliver(TehoProtocol.STATUE, message);
			}
		}

		@Override
		public synchronized void clear() {
			placed.clear();
			StatueRemove message = new StatueRemove(Collections.<String>emptyList());
			for (TehoSession session : sessions.get()) {
				session.deliver(TehoProtocol.STATUE, message);
			}
		}

		@Override
		public synchronized Collection<Statue> placed() {
			return Collections.unmodifiableList(new ArrayList<>(placed.values()));
		}

		synchronized void replay(TehoSession session) {
			for (Statue statue : placed.values()) {
				session.deliver(TehoProtocol.STATUE, statue.wire());
			}
		}
	}

	static final class Brand implements BrandingApi {
		private final Supplier<Iterable<TehoSession>> sessions;
		private Branding current;

		Brand(Supplier<Iterable<TehoSession>> sessions) {
			this.sessions = sessions;
		}

		@Override
		public synchronized void apply(Branding branding) {
			current = branding.wire().reset ? null : branding;
			for (TehoSession session : sessions.get()) {
				send(session, branding);
			}
		}

		@Override
		public synchronized Branding current() {
			return current;
		}

		synchronized void replay(TehoSession session) {
			if (current != null) {
				send(session, current);
			}
		}

		private static void send(TehoSession session, Branding branding) {
			if (!session.supports(TehoProtocol.BRANDING)) {
				return;
			}
			for (Asset asset : branding.assets()) {
				session.offer(asset);
			}
			session.deliver(TehoProtocol.BRANDING, branding.wire());
		}
	}

	static final class Rewards implements RewardsApi {
		private final Backend backend;

		Rewards(Backend backend) {
			this.backend = backend;
		}

		@Override
		public boolean enabled() {
			return backend.hasKey();
		}

		@Override
		public CompletableFuture<Result> grant(UUID player, String item, String reason) {
			if (!enabled()) {
				return CompletableFuture.completedFuture(new Result(false, item, "no server API key configured", 0));
			}
			if (player == null || item == null || !item.matches("[a-z_]+:[a-z0-9_]+")) {
				return CompletableFuture.completedFuture(new Result(false, item, "items are slot:id, for example cape:summer_cup", 0));
			}
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("player", player.toString());
			body.put("item", item);
			body.put("reason", reason == null ? "" : reason);
			return backend.post("/v1/servers/rewards", body).handle((response, failure) -> {
				if (response == null) {
					return new Result(false, item, "rewards service unreachable", 0);
				}
				if (!response.ok()) {
					return new Result(false, item, response.error(), (int) Json.number(response.body, "remaining"));
				}
				return new Result(true, item, "", (int) Json.number(response.body, "remaining"));
			});
		}
	}
}
