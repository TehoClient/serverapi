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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import dev.tehoclient.api.info.ClientInfo;
import dev.tehoclient.api.info.InfoApi;
import dev.tehoclient.api.protocol.AssertRequest;
import dev.tehoclient.api.protocol.AssertResult;
import dev.tehoclient.api.protocol.AttestRequest;
import dev.tehoclient.api.protocol.AttestResult;
import dev.tehoclient.api.protocol.InfoReply;
import dev.tehoclient.api.protocol.InfoRequest;
import dev.tehoclient.api.protocol.Message;
import dev.tehoclient.api.protocol.ModEntry;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.suspect.ModListResult;
import dev.tehoclient.api.verify.Proof;
import dev.tehoclient.api.verify.VerifyApi;

final class InfoPart extends Part implements InfoApi, VerifyApi {
	static final String ASSERT_PURPOSE = "teho-assert";
	static final String ATTEST_PURPOSE = "teho-attest";
	static final int MAX_PENDING = 8;

	private static final SecureRandom RANDOM = new SecureRandom();

	private final AtomicInteger requests = new AtomicInteger(1);
	private final Map<Integer, CompletableFuture<ClientInfo>> infos = new ConcurrentHashMap<>();
	private final Map<Integer, Pending> proofs = new ConcurrentHashMap<>();
	private volatile boolean attested;

	private static final class Pending {
		final Proof.Kind kind;
		final String nonce;
		final CompletableFuture<Proof> future = new CompletableFuture<>();

		Pending(Proof.Kind kind, String nonce) {
			this.kind = kind;
			this.nonce = nonce;
		}
	}

	InfoPart(TehoSession session) {
		super(session);
	}

	@Override
	public CompletableFuture<ClientInfo> request(String reason, ClientInfo.Field... fields) {
		CompletableFuture<ClientInfo> future = new CompletableFuture<>();
		if (!session.supports(TehoProtocol.INFO)) {
			future.completeExceptionally(new IllegalStateException(session.name() + "'s client cannot share client info"));
			return future;
		}
		if (infos.size() >= MAX_PENDING) {
			future.completeExceptionally(new IllegalStateException("too many info requests pending"));
			return future;
		}
		List<String> wanted = new ArrayList<>();
		for (ClientInfo.Field field : fields) {
			wanted.add(field.wire());
		}
		int id = requests.getAndIncrement();
		infos.put(id, future);
		send(TehoProtocol.INFO, new InfoRequest(id, wanted, reason));
		return future;
	}

	@Override
	public CompletableFuture<Proof> identity() {
		return prove(Proof.Kind.IDENTITY, TehoProtocol.ASSERT);
	}

	@Override
	public CompletableFuture<Proof> integrity() {
		return prove(Proof.Kind.INTEGRITY, TehoProtocol.ATTEST);
	}

	@Override
	public boolean verifiedClient() {
		return attested;
	}

	private CompletableFuture<Proof> prove(Proof.Kind kind, String capability) {
		if (!session.supports(capability)) {
			return CompletableFuture.completedFuture(Proof.failed(kind, session.uniqueId(), "this client cannot provide " + capability));
		}
		if (proofs.size() >= MAX_PENDING) {
			return CompletableFuture.completedFuture(Proof.failed(kind, session.uniqueId(), "too many proofs pending"));
		}
		byte[] raw = new byte[18];
		RANDOM.nextBytes(raw);
		String nonce = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
		int id = requests.getAndIncrement();
		Pending pending = new Pending(kind, nonce);
		proofs.put(id, pending);
		send(capability, kind == Proof.Kind.IDENTITY ? new AssertRequest(id, nonce) : new AttestRequest(id, nonce));
		return pending.future;
	}

	@Override
	boolean receive(Message message) {
		if (message instanceof InfoReply) {
			InfoReply reply = (InfoReply) message;
			CompletableFuture<ClientInfo> future = infos.remove(reply.requestId);
			if (future != null) {
				Set<ClientInfo.Field> granted = EnumSet.noneOf(ClientInfo.Field.class);
				for (String field : reply.granted) {
					ClientInfo.Field known = ClientInfo.Field.byWire(field);
					if (known != null) {
						granted.add(known);
					}
				}
				List<ModListResult.Mod> mods = new ArrayList<>();
				for (ModEntry entry : reply.mods) {
					mods.add(new ModListResult.Mod(entry.id, entry.name, entry.version));
				}
				future.complete(new ClientInfo(granted, ClientInfo.Input.byWire(reply.input), reply.fps, reply.frameP50, reply.frameP99,
						reply.renderDistance, mods, reply.tehoModules, reply.locale, reply.guiScale, reply.width, reply.height, reply.os,
						reply.memoryMb));
			}
			return true;
		}
		if (message instanceof AssertResult) {
			AssertResult result = (AssertResult) message;
			settle(result.requestId, result.token, result.error, ASSERT_PURPOSE);
			return true;
		}
		if (message instanceof AttestResult) {
			AttestResult result = (AttestResult) message;
			settle(result.requestId, result.token, result.error, ATTEST_PURPOSE);
			return true;
		}
		return false;
	}

	private void settle(int requestId, String token, String error, String purpose) {
		Pending pending = proofs.remove(requestId);
		if (pending == null) {
			return;
		}
		UUID player = session.uniqueId();
		if (!error.isEmpty() || token.isEmpty()) {
			pending.future.complete(Proof.failed(pending.kind, player, error.isEmpty() ? "no token" : error));
			return;
		}
		core().verifier().verify(token, purpose).whenComplete((claims, failure) -> {
			if (claims == null || !claims.valid) {
				pending.future.complete(Proof.failed(pending.kind, player, claims == null ? String.valueOf(failure) : claims.error));
				return;
			}
			Map<String, Object> c = claims.claims;
			if (!player.toString().equalsIgnoreCase(Json.string(c, "uuid"))) {
				pending.future.complete(Proof.failed(pending.kind, player, "token is for another player"));
				return;
			}
			if (!pending.nonce.equals(Json.string(c, "nonce"))) {
				pending.future.complete(Proof.failed(pending.kind, player, "nonce mismatch"));
				return;
			}
			if (pending.kind == Proof.Kind.INTEGRITY && !"attested".equals(Json.string(c, "integrity"))) {
				pending.future.complete(Proof.failed(pending.kind, player, "client is not attested"));
				return;
			}
			if (pending.kind == Proof.Kind.INTEGRITY) {
				attested = true;
			}
			pending.future.complete(new Proof(pending.kind, true, player, Json.string(c, "name"), Json.string(c, "build"),
					Json.number(c, "iat"), Json.number(c, "exp"), "", token));
		});
	}

	@Override
	void reset() {
		attested = false;
	}

	@Override
	void close() {
		IllegalStateException gone = new IllegalStateException(session.name() + " left before answering");
		for (CompletableFuture<ClientInfo> future : infos.values()) {
			future.completeExceptionally(gone);
		}
		for (Pending pending : proofs.values()) {
			pending.future.complete(Proof.failed(pending.kind, session.uniqueId(), "player left"));
		}
		infos.clear();
		proofs.clear();
	}
}
