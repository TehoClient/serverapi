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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import dev.tehoclient.api.TehoApi;
import dev.tehoclient.api.TehoListener;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.protocol.Chunk;
import dev.tehoclient.api.protocol.Chunking;
import dev.tehoclient.api.protocol.ConsentResult;
import dev.tehoclient.api.protocol.Event;
import dev.tehoclient.api.protocol.InspectRequest;
import dev.tehoclient.api.protocol.ModList;
import dev.tehoclient.api.protocol.PrivateStatus;
import dev.tehoclient.api.protocol.ScreenshotResult;
import dev.tehoclient.api.protocol.StaffAction;
import dev.tehoclient.api.protocol.StaffSupport;
import dev.tehoclient.api.staff.InspectResponder;
import dev.tehoclient.api.staff.StaffTool;
import dev.tehoclient.api.staff.Target;
import dev.tehoclient.api.protocol.Hello;
import dev.tehoclient.api.protocol.Message;
import dev.tehoclient.api.protocol.ProtocolException;
import dev.tehoclient.api.protocol.Signal;
import dev.tehoclient.api.protocol.StaffState;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.protocol.Welcome;

public final class TehoCore implements TehoApi {
	public static final long HELLO_GRACE_MS = 1000;

	private final PlatformBridge bridge;
	private final Map<UUID, TehoSession> sessions = new ConcurrentHashMap<>();
	private final Map<UUID, Chunking> inbound = new ConcurrentHashMap<>();
	private final List<TehoListener> listeners = new CopyOnWriteArrayList<>();
	private final List<Consumer<TehoSession>> handshakeHooks = new CopyOnWriteArrayList<>();
	private final SecureRandom random = new SecureRandom();
	private volatile Set<String> features = new LinkedHashSet<>(TehoProtocol.known());
	private volatile String serverName = "";
	private volatile boolean debug;
	private volatile String grant = "";
	private final NpcDirector npcs = new NpcDirector(() -> new ArrayList<TehoSession>(sessions.values()));
	private final AssetHub assets = new AssetHub();
	private final Backend backend = new Backend();
	private final TokenVerifier verifier = new TokenVerifier(backend);
	private final NetworkDirectors.Statues statues = new NetworkDirectors.Statues(() -> new ArrayList<TehoSession>(sessions.values()));
	private final NetworkDirectors.Brand brand = new NetworkDirectors.Brand(() -> new ArrayList<TehoSession>(sessions.values()));
	private final NetworkDirectors.Rewards rewards = new NetworkDirectors.Rewards(backend);
	private volatile String verified = "";

	public TehoCore(PlatformBridge bridge) {
		this.bridge = bridge;
	}

	public void install() {
		TehoApi.INSTANCE.set(this);
	}

	public void uninstall() {
		TehoApi.INSTANCE.compareAndSet(this, null);
		backend.shutdown();
		sessions.clear();
		inbound.clear();
	}

	public void grant(String token) {
		this.grant = token == null ? "" : token.trim();
	}

	public boolean hasGrant() {
		return !grant.isEmpty();
	}

	@Override
	public dev.tehoclient.api.npc.NpcApi npcs() {
		return npcs;
	}

	NpcDirector npcDirector() {
		return npcs;
	}

	NetworkDirectors.Statues statueDirector() {
		return statues;
	}

	NetworkDirectors.Brand brandDirector() {
		return brand;
	}

	@Override
	public dev.tehoclient.api.asset.AssetStore assets() {
		return assets;
	}

	@Override
	public dev.tehoclient.api.statue.StatueApi statues() {
		return statues;
	}

	@Override
	public dev.tehoclient.api.branding.BrandingApi branding() {
		return brand;
	}


	@Override
	public dev.tehoclient.api.rewards.RewardsApi rewards() {
		return rewards;
	}

	public Backend backend() {
		return backend;
	}

	TokenVerifier verifier() {
		return verifier;
	}

	public void verified(String certificate) {
		this.verified = certificate == null ? "" : certificate.trim();
	}

	public String verifiedCertificate() {
		return verified;
	}

	void fire(Consumer<TehoListener> event) {
		for (TehoListener listener : listeners) {
			safely(() -> event.accept(listener));
		}
	}

	public void configure(String serverName, Collection<String> features, boolean debug) {
		this.serverName = serverName == null ? "" : serverName;
		this.features = new LinkedHashSet<>(features);
		this.debug = debug;
	}

	public Set<String> features() {
		return Collections.unmodifiableSet(features);
	}

	PlatformBridge bridge() {
		return bridge;
	}

	@Override
	public Optional<TehoPlayer> player(UUID id) {
		return Optional.<TehoPlayer>ofNullable(sessions.get(id));
	}

	public Optional<TehoSession> session(UUID id) {
		return Optional.ofNullable(sessions.get(id));
	}

	@Override
	public Collection<TehoPlayer> players() {
		return Collections.unmodifiableList(new ArrayList<TehoPlayer>(sessions.values()));
	}

	@Override
	public void addListener(TehoListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(TehoListener listener) {
		listeners.remove(listener);
	}

	public void addHandshakeHook(Consumer<TehoSession> hook) {
		handshakeHooks.add(hook);
	}

	public boolean debug() {
		return debug;
	}

	public void debug(boolean on) {
		this.debug = on;
	}

	public void onChannelRegistered(UUID player) {
		bridge.runLater(player, () -> {
			if (bridge.online(player) && !sessions.containsKey(player)) {
				sendFrame(player, Signal.HELLO_REQUEST, 1);
			}
		}, HELLO_GRACE_MS);
	}

	public void onMessage(UUID player, byte[] frame) {
		receive(player, frame, true);
	}

	public void onRelayed(UUID player, byte[] frame) {
		receive(player, frame, false);
	}

	private void receive(UUID player, byte[] frame, boolean allowHello) {
		Message message;
		try {
			message = TehoProtocol.decode(frame);
			if (message instanceof Chunk) {
				Chunking chunks = inbound.computeIfAbsent(player, ignored -> new Chunking());
				byte[] whole = chunks.accept((Chunk) message, System.currentTimeMillis());
				if (whole == null) {
					return;
				}
				message = Chunking.decodeWhole(whole);
			}
		} catch (ProtocolException malformed) {
			if (debug) {
				bridge.info("Malformed frame from " + bridge.name(player) + ": " + malformed.getMessage());
			}
			return;
		}
		if (message == null) {
			return;
		}
		if (debug) {
			bridge.info("<- " + bridge.name(player) + " " + message);
		}
		if (message instanceof Hello) {
			if (allowHello) {
				handshake(player, (Hello) message);
			}
			return;
		}
		TehoSession session = sessions.get(player);
		if (session == null) {
			return;
		}
		if (message instanceof dev.tehoclient.api.protocol.AssetWant) {
			session.onAssetWant((dev.tehoclient.api.protocol.AssetWant) message);
			return;
		}
		if (session.receive(message)) {
			return;
		}
		if (message instanceof ConsentResult) {
			ConsentResult consent = (ConsentResult) message;
			session.onConsent(consent);
			for (TehoListener listener : listeners) {
				safely(() -> listener.onConsent(session, consent.scope, consent.items, consent.granted));
			}
		} else if (message instanceof StaffState) {
			StaffState state = (StaffState) message;
			session.onStaffState(state).ifPresent(tool -> {
				for (TehoListener listener : listeners) {
					safely(() -> listener.onStaffTool(session, tool, state.active));
				}
			});
		} else if (message instanceof Event) {
			Event event = (Event) message;
			for (TehoListener listener : listeners) {
				safely(() -> listener.onEvent(session, event.kind, event.target, event.value));
			}
		} else if (message instanceof StaffSupport) {
			session.onSupport((StaffSupport) message);
		} else if (message instanceof InspectRequest) {
			inspect(session, (InspectRequest) message);
		} else if (message instanceof StaffAction) {
			StaffAction action = (StaffAction) message;
			if (session.mayAct(action.action)) {
				Target target = Target.of(action.targetKind, action.target, action.x, action.y, action.z);
				for (TehoListener listener : listeners) {
					safely(() -> listener.onStaffAction(session, action.action, target));
				}
			}
		} else if (message instanceof ScreenshotResult) {
			session.onScreenshot((ScreenshotResult) message);
		} else if (message instanceof PrivateStatus) {
			PrivateStatus status = (PrivateStatus) message;
			session.onPrivileges(status.capabilities, status.error);
			if (status.error.isEmpty()) {
				bridge.info(session.name() + " accepted this server's private grant: " + status.capabilities);
			} else {
				bridge.info(session.name() + " rejected this server's private grant: " + status.error);
			}
			for (TehoListener listener : listeners) {
				safely(() -> listener.onPrivileges(session, session.privileges(), status.error));
			}
			npcs.replay(session);
		} else if (message instanceof ModList) {
			session.onModList((ModList) message);
		}
	}

	private void inspect(TehoSession session, InspectRequest request) {
		if (!session.staff().accepted().contains(StaffTool.INSPECTOR)) {
			return;
		}
		Target target = Target.of(request.kind, request.target, request.x, request.y, request.z);
		InspectResponder responder = session.responder(request.requestId, request.target);
		for (TehoListener listener : listeners) {
			if (responder.answered()) {
				return;
			}
			safely(() -> listener.onInspect(session, target, responder));
		}
	}

	public void onQuit(UUID player) {
		inbound.remove(player);
		TehoSession session = sessions.remove(player);
		if (session != null) {
			session.close();
			for (TehoListener listener : listeners) {
				safely(() -> listener.onLeave(session));
			}
		}
	}

	private void handshake(UUID player, Hello hello) {
		if (hello.protocol < 1) {
			return;
		}
		Set<String> enabled = new LinkedHashSet<>(features);
		enabled.retainAll(hello.capabilities);
		long id = random.nextLong() & 0x7FFFFFFFFFFFFFFFL;
		int protocol = Math.min(hello.protocol, TehoProtocol.VERSION);
		establish(player, hello, enabled, id, protocol, true);
	}

	public void attach(UUID player, Hello hello, Collection<String> negotiated, long id, int protocol) {
		if (protocol < 1) {
			return;
		}
		Set<String> enabled = new LinkedHashSet<>(negotiated);
		enabled.retainAll(features);
		establish(player, hello, enabled, id, Math.min(protocol, TehoProtocol.VERSION), false);
	}

	public void refresh(UUID player) {
		TehoSession session = sessions.get(player);
		if (session != null && bridge.online(player)) {
			sendFrame(player, Signal.RESET, session.protocol());
			session.reapply();
		}
	}

	public void reapply(UUID player) {
		TehoSession session = sessions.get(player);
		if (session != null) {
			session.reapply();
		}
	}

	private void establish(UUID player, Hello hello, Set<String> enabled, long id, int protocol, boolean welcome) {
		TehoSession previous = sessions.get(player);
		TehoSession session = previous == null ? new TehoSession(this, player, bridge.name(player)) : previous;
		session.handshake(hello, enabled, id, protocol);
		sessions.put(player, session);
		if (welcome) {
			String name = serverName.isEmpty() ? bridge.serverName() : serverName;
			sendFrame(player, new Welcome(protocol, id, new ArrayList<>(enabled), name, grant, verified), protocol);
		}
		bridge.info(session.name() + " is on " + hello.client + " (" + hello.minecraft + ", " + hello.platform
				+ ", protocol " + protocol + ")" + (welcome ? "" : " via proxy") + " using " + enabled);
		session.reapply();
		if (previous == null) {
			for (TehoListener listener : listeners) {
				safely(() -> listener.onReady(session));
			}
		}
		for (Consumer<TehoSession> hook : handshakeHooks) {
			safely(() -> hook.accept(session));
		}
	}

	void send(UUID player, Message message, int protocol) {
		sendFrame(player, message, protocol);
	}

	private void sendFrame(UUID player, Message message, int protocol) {
		if (debug) {
			bridge.info("-> " + bridge.name(player) + " " + message);
		}
		if (protocol >= 2) {
			for (byte[] frame : Chunking.frames(message, random.nextInt(Integer.MAX_VALUE))) {
				bridge.send(player, frame);
			}
		} else {
			bridge.send(player, TehoProtocol.encode(message));
		}
	}

	private void safely(Runnable action) {
		try {
			action.run();
		} catch (RuntimeException failed) {
			bridge.warn("A Teho API listener failed", failed);
		}
	}
}
