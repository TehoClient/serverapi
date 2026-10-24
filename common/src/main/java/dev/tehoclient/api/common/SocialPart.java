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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import dev.tehoclient.api.Units;
import dev.tehoclient.api.broadcast.BroadcastApi;
import dev.tehoclient.api.broadcast.Observer;
import dev.tehoclient.api.clips.ClipsApi;
import dev.tehoclient.api.pack.PackApi;
import dev.tehoclient.api.protocol.CameraFollow;
import dev.tehoclient.api.protocol.ClipMark;
import dev.tehoclient.api.protocol.ClipResult;
import dev.tehoclient.api.protocol.InviteSheet;
import dev.tehoclient.api.protocol.Message;
import dev.tehoclient.api.protocol.PackHint;
import dev.tehoclient.api.protocol.PackStatus;
import dev.tehoclient.api.protocol.ServerLink;
import dev.tehoclient.api.protocol.ServerLinks;
import dev.tehoclient.api.protocol.ShowcaseOpen;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.protocol.Transfer;
import dev.tehoclient.api.showcase.ShowcaseApi;
import dev.tehoclient.api.social.Presence;
import dev.tehoclient.api.social.SocialApi;

final class SocialPart extends Part implements SocialApi, PackApi, ClipsApi, BroadcastApi, ShowcaseApi {
	static final int MAX_LINKS = 6;
	static final int MAX_PENDING_CLIPS = 8;

	private Presence presence;
	private ServerLinks links;
	private PackHint pack;
	private volatile State packState;
	private Observer observer;
	private CameraFollow follow;
	private final Map<String, CompletableFuture<Clip>> clips = new ConcurrentHashMap<>();

	SocialPart(TehoSession session) {
		super(session);
	}

	@Override
	public synchronized boolean presence(Presence next) {
		presence = next;
		return send(TehoProtocol.SOCIAL, next.wire());
	}

	@Override
	public synchronized boolean clearPresence() {
		presence = null;
		return send(TehoProtocol.SOCIAL, new dev.tehoclient.api.protocol.Presence("", "", "", 0L, 0, 0, false, true));
	}

	@Override
	public boolean openInvites(String reason, String party, String address) {
		return send(TehoProtocol.SOCIAL, new InviteSheet(reason, party, address));
	}

	@Override
	public boolean transfer(String host, int port, String token, String title, String reason) {
		Units.require(host, "transfer host");
		if (port < 1 || port > 65535) {
			throw new IllegalArgumentException("port out of range");
		}
		return send(TehoProtocol.SOCIAL, new Transfer(host, port, token, title, reason));
	}

	@Override
	public synchronized boolean links(Map<String, String> labelToUrl) {
		List<ServerLink> out = new ArrayList<>();
		for (Map.Entry<String, String> entry : labelToUrl.entrySet()) {
			String url = entry.getValue() == null ? "" : entry.getValue().trim();
			if (!url.startsWith("https://")) {
				throw new IllegalArgumentException("server links must be https: " + url);
			}
			if (out.size() >= MAX_LINKS) {
				throw new IllegalArgumentException("At most " + MAX_LINKS + " server links");
			}
			out.add(new ServerLink(entry.getKey(), url, kind(url)));
		}
		links = new ServerLinks(out);
		return send(TehoProtocol.SOCIAL, links);
	}

	private static String kind(String url) {
		String lower = url.toLowerCase(java.util.Locale.ROOT);
		if (lower.contains("discord.gg") || lower.contains("discord.com")) {
			return "discord";
		}
		if (lower.contains("store") || lower.contains("tebex") || lower.contains("shop")) {
			return "store";
		}
		return "website";
	}

	@Override
	public synchronized boolean hint(String id, String url, String sha1, long size, boolean required, String prompt) {
		if (url == null || !url.startsWith("https://")) {
			throw new IllegalArgumentException("pack URLs must be https");
		}
		if (sha1 == null || !sha1.matches("[0-9a-fA-F]{40}")) {
			throw new IllegalArgumentException("pack sha1 must be 40 hex characters");
		}
		pack = new PackHint(url, sha1.toLowerCase(java.util.Locale.ROOT), (int) Math.max(0, Math.min(Integer.MAX_VALUE, size)), required,
				prompt, id);
		return send(TehoProtocol.PACK, pack);
	}

	@Override
	public State lastState() {
		return packState;
	}

	@Override
	public CompletableFuture<Clip> mark(String id, String title, long beforeMs, long afterMs) {
		Units.require(id, "clip id");
		CompletableFuture<Clip> future = new CompletableFuture<>();
		if (clips.size() >= MAX_PENDING_CLIPS) {
			future.complete(new Clip(id, false, "", "too many clips pending"));
			return future;
		}
		if (!session.supports(TehoProtocol.CLIPS)) {
			future.complete(new Clip(id, false, "", "this client cannot record clips"));
			return future;
		}
		clips.put(id, future);
		send(TehoProtocol.CLIPS, new ClipMark(id, title, Math.min(60000, Units.millis(beforeMs)), Math.min(30000, Units.millis(afterMs))));
		return future;
	}

	@Override
	public synchronized boolean observer(Observer next) {
		observer = next.wire().on ? next : null;
		return send(TehoProtocol.BROADCAST, next.wire());
	}

	@Override
	public synchronized boolean follow(UUID player, Follow mode, double distance, boolean smooth) {
		follow = new CameraFollow(Units.id(player), mode == null ? 0 : mode.ordinal(),
				Math.max(100, Math.min(3200, Units.centi(distance))), smooth);
		return send(TehoProtocol.BROADCAST, follow);
	}

	@Override
	public synchronized boolean stopFollowing() {
		follow = null;
		return send(TehoProtocol.BROADCAST, new CameraFollow("", 0, 0, false));
	}

	@Override
	public boolean open(String title, String subtitle, List<String> items, String emote, String focus) {
		if (items == null || items.isEmpty() || items.size() > 16) {
			throw new IllegalArgumentException("showcase between 1 and 16 items");
		}
		return send(TehoProtocol.SHOWCASE, new ShowcaseOpen(title, items, emote, focus, subtitle));
	}

	@Override
	synchronized void reapply() {
		if (presence != null) {
			send(TehoProtocol.SOCIAL, presence.wire());
		}
		if (links != null) {
			send(TehoProtocol.SOCIAL, links);
		}
		if (pack != null) {
			send(TehoProtocol.PACK, pack);
		}
		if (observer != null) {
			send(TehoProtocol.BROADCAST, observer.wire());
		}
		if (follow != null) {
			send(TehoProtocol.BROADCAST, follow);
		}
	}

	@Override
	boolean receive(Message message) {
		if (message instanceof PackStatus) {
			PackStatus status = (PackStatus) message;
			State[] states = State.values();
			State state = status.state < states.length ? states[status.state] : State.FAILED;
			packState = state;
			core().fire(listener -> listener.onPackStatus(session, status.sha1, state));
			return true;
		}
		if (message instanceof ClipResult) {
			ClipResult result = (ClipResult) message;
			CompletableFuture<Clip> future = clips.remove(result.id);
			if (future != null) {
				future.complete(new Clip(result.id, result.saved, result.url, result.error));
			}
			return true;
		}
		return false;
	}

	@Override
	synchronized void reset() {
		presence = null;
		links = null;
		pack = null;
		observer = null;
		follow = null;
	}

	@Override
	void close() {
		for (CompletableFuture<Clip> future : clips.values()) {
			future.complete(new Clip("", false, "", "player left"));
		}
		clips.clear();
	}
}
