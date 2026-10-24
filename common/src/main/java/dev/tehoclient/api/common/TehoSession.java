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
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.hud.Cooldown;
import dev.tehoclient.api.hud.HudApi;
import dev.tehoclient.api.hud.Timer;
import dev.tehoclient.api.modules.ModuleRules;
import dev.tehoclient.api.modules.ModulesApi;
import dev.tehoclient.api.modules.SettingCap;
import dev.tehoclient.api.notify.Notification;
import dev.tehoclient.api.protocol.ConsentResult;
import dev.tehoclient.api.protocol.Dismiss;
import dev.tehoclient.api.protocol.EspEntry;
import dev.tehoclient.api.protocol.EspStyle;
import dev.tehoclient.api.protocol.Hello;
import dev.tehoclient.api.protocol.Message;
import dev.tehoclient.api.protocol.Notify;
import dev.tehoclient.api.protocol.SettingCaps;
import dev.tehoclient.api.protocol.Signal;
import dev.tehoclient.api.protocol.StaffRevoke;
import dev.tehoclient.api.protocol.StaffState;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.protocol.TimerRemove;
import dev.tehoclient.api.protocol.WaypointRemove;
import dev.tehoclient.api.protocol.WaypointSet;
import dev.tehoclient.api.protocol.XrayBlock;
import dev.tehoclient.api.protocol.ActionWheel;
import dev.tehoclient.api.protocol.Alert;
import dev.tehoclient.api.protocol.FreezeScreen;
import dev.tehoclient.api.protocol.HighlightRemove;
import dev.tehoclient.api.protocol.InspectAction;
import dev.tehoclient.api.protocol.InspectCard;
import dev.tehoclient.api.protocol.InspectRow;
import dev.tehoclient.api.protocol.LagMap;
import dev.tehoclient.api.protocol.ModEntry;
import dev.tehoclient.api.protocol.ModList;
import dev.tehoclient.api.protocol.ModListRequest;
import dev.tehoclient.api.protocol.ScreenshotRequest;
import dev.tehoclient.api.protocol.ScreenshotResult;
import dev.tehoclient.api.protocol.Spectate;
import dev.tehoclient.api.protocol.StaffSupport;
import dev.tehoclient.api.protocol.TargetStats;
import dev.tehoclient.api.protocol.Trail;
import dev.tehoclient.api.staff.Highlight;
import dev.tehoclient.api.staff.InspectResponder;
import dev.tehoclient.api.staff.LagCell;
import dev.tehoclient.api.staff.StaffAlert;
import dev.tehoclient.api.staff.StaffApi;
import dev.tehoclient.api.staff.StatRow;
import dev.tehoclient.api.staff.Target;
import dev.tehoclient.api.staff.WheelAction;
import dev.tehoclient.api.suspect.ModListResult;
import dev.tehoclient.api.suspect.Screenshot;
import dev.tehoclient.api.suspect.SuspectApi;
import dev.tehoclient.api.staff.StaffGrant;
import dev.tehoclient.api.staff.StaffTool;
import dev.tehoclient.api.waypoint.Waypoint;
import dev.tehoclient.api.waypoint.WaypointsApi;

public final class TehoSession implements TehoPlayer {
	private final TehoCore core;
	private final UUID id;
	private final String name;
	private volatile Hello hello;
	private volatile Set<String> enabled = Collections.emptySet();
	private volatile long session;
	private volatile int protocol;

	private final Map<String, Waypoint> waypoints = new LinkedHashMap<>();
	private ModuleRules rules = ModuleRules.NONE;
	private StaffGrant grant;
	private final Set<StaffTool> accepted = EnumSet.noneOf(StaffTool.class);
	private final Set<StaffTool> active = EnumSet.noneOf(StaffTool.class);
	private EspStyle esp;
	private final Map<String, Long> cooldownEnds = new LinkedHashMap<>();
	private final Map<String, Cooldown> cooldowns = new LinkedHashMap<>();
	private final Map<String, Timer> timers = new LinkedHashMap<>();
	private final Map<String, Long> timerSent = new LinkedHashMap<>();
	private final Set<StaffTool> supported = EnumSet.noneOf(StaffTool.class);
	private volatile Set<String> privileges = Collections.emptySet();
	private volatile String privilegeError = "";
	private final Map<String, dev.tehoclient.api.protocol.Highlight> highlights = new LinkedHashMap<>();
	private ActionWheel wheel;
	private FreezeScreen freeze;
	private final AtomicInteger requests = new AtomicInteger(1);
	private final Map<Integer, CompletableFuture<Screenshot>> screenshots = new ConcurrentHashMap<>();
	private final Map<Integer, CompletableFuture<ModListResult>> modLists = new ConcurrentHashMap<>();

	private final WaypointsApi waypointsApi = new Waypoints();
	private final ModulesApi modulesApi = new Modules();
	private final StaffApi staffApi = new Staff();
	private final HudApi hudApi = new Hud();
	private final SuspectApi suspectApi = new Suspect();
	private final WorldPart world = new WorldPart(this);
	private final PlayersPart players = new PlayersPart(this);
	private final InputPart input = new InputPart(this);
	private final SocialPart social = new SocialPart(this);
	private final UiPart ui = new UiPart(this);
	private final InfoPart info = new InfoPart(this);
	private final HudPart hudExtras = new HudPart(this);
	private final PolicyPart policy = new PolicyPart(this);
	private final List<Part> parts = java.util.Arrays.<Part>asList(world, players, input, social, ui, info, hudExtras, policy);
	private final Set<String> offered = new java.util.HashSet<>();

	TehoSession(TehoCore core, UUID id, String name) {
		this.core = core;
		this.id = id;
		this.name = name;
	}

	synchronized void handshake(Hello hello, Set<String> enabled, long session, int protocol) {
		this.hello = hello;
		this.enabled = Collections.unmodifiableSet(new LinkedHashSet<>(enabled));
		this.session = session;
		this.protocol = protocol;
		this.active.clear();
		this.supported.clear();
		this.privileges = Collections.emptySet();
		this.privilegeError = "";
		this.offered.clear();
	}

	TehoCore core() {
		return core;
	}

	synchronized void offer(dev.tehoclient.api.asset.Asset asset) {
		if (asset == null || !enabled.contains(TehoProtocol.ASSET)) {
			return;
		}
		core.assets().register(asset);
		if (offered.add(asset.hex())) {
			sendIf(TehoProtocol.ASSET, new dev.tehoclient.api.protocol.AssetOffer(asset.hash(), asset.kind().wire(), asset.size()));
		}
	}

	void onAssetWant(dev.tehoclient.api.protocol.AssetWant want) {
		int sent = 0;
		for (byte[] hash : want.hashes) {
			if (sent >= 16) {
				break;
			}
			synchronized (this) {
				if (!offered.contains(AssetHub.hex(hash))) {
					continue;
				}
			}
			Optional<dev.tehoclient.api.asset.Asset> asset = core.assets().find(hash);
			if (asset.isPresent()) {
				sendIf(TehoProtocol.ASSET, new dev.tehoclient.api.protocol.AssetData(asset.get().hash(), asset.get().kind().wire(), asset.get().data()));
				sent++;
			}
		}
	}

	boolean receive(Message message) {
		for (Part part : parts) {
			if (part.receive(message)) {
				return true;
			}
		}
		return false;
	}

	Hello hello() {
		return hello;
	}

	synchronized void reapply() {
		for (Waypoint waypoint : waypoints.values()) {
			sendIf(TehoProtocol.WAYPOINT, toMessage(waypoint, false));
		}
		if (!rules.isEmpty()) {
			sendRules();
		}
		if (grant != null) {
			sendIf(TehoProtocol.STAFF, toMessage(grant));
		}
		if (esp != null) {
			sendIf(TehoProtocol.STAFF, esp);
		}
		for (dev.tehoclient.api.protocol.Highlight highlight : highlights.values()) {
			sendIf(TehoProtocol.STAFF, highlight);
		}
		if (wheel != null) {
			sendIf(TehoProtocol.STAFF, wheel);
		}
		if (freeze != null) {
			sendIf(TehoProtocol.SUSPECT, freeze);
		}
		long now = System.currentTimeMillis();
		for (Map.Entry<String, Cooldown> entry : new ArrayList<>(cooldowns.entrySet())) {
			long left = cooldownEnds.get(entry.getKey()) - now;
			if (left <= 0) {
				cooldowns.remove(entry.getKey());
				cooldownEnds.remove(entry.getKey());
				continue;
			}
			sendIf(TehoProtocol.HUD, toMessage(entry.getValue(), left));
		}
		for (Timer timer : timers.values()) {
			sendIf(TehoProtocol.HUD, toMessage(timer, adjusted(timer, now)));
		}
		for (Part part : parts) {
			part.reapply();
		}
		core.npcDirector().replay(this);
		core.statueDirector().replay(this);
		core.brandDirector().replay(this);
	}

	synchronized void onConsent(ConsentResult consent) {
		if (!"staff".equals(consent.scope)) {
			return;
		}
		for (String item : consent.items) {
			StaffTool tool = StaffTool.byId(item);
			if (tool == null) {
				continue;
			}
			if (consent.granted) {
				accepted.add(tool);
			} else {
				accepted.remove(tool);
				active.remove(tool);
			}
		}
	}

	synchronized Optional<StaffTool> onStaffState(StaffState state) {
		StaffTool tool = StaffTool.byId(state.tool);
		if (tool == null || grant == null || !grant.tools().contains(tool)) {
			return Optional.empty();
		}
		if (state.active) {
			active.add(tool);
		} else {
			active.remove(tool);
		}
		return Optional.of(tool);
	}

	synchronized void onSupport(StaffSupport support) {
		supported.clear();
		for (String id : support.tools) {
			StaffTool tool = StaffTool.byId(id);
			if (tool != null) {
				supported.add(tool);
			}
		}
	}

	synchronized boolean mayAct(String action) {
		if (grant == null) {
			return false;
		}
		return accepted.contains(StaffTool.ACTION_WHEEL) || accepted.contains(StaffTool.ALERTS)
				|| accepted.contains(StaffTool.CLICK_TELEPORT) || accepted.contains(StaffTool.INSPECTOR);
	}

	InspectResponder responder(int requestId, String target) {
		return new InspectResponder() {
			private volatile boolean done;

			@Override
			public boolean answered() {
				return done;
			}

			@Override
			public void reply(dev.tehoclient.api.staff.InspectCard card) {
				if (done) {
					return;
				}
				done = true;
				List<InspectRow> rows = new ArrayList<>();
				for (StatRow row : card.rows()) {
					rows.add(new InspectRow(row.label(), row.value(), row.color()));
				}
				List<InspectAction> actions = new ArrayList<>();
				for (Map.Entry<String, String> action : card.actions().entrySet()) {
					actions.add(new InspectAction(action.getKey(), action.getValue()));
				}
				sendIf(TehoProtocol.STAFF, new InspectCard(requestId, card.title(), card.subtitle(), rows, actions,
						card.ttlMs(), target));
			}
		};
	}

	void onScreenshot(ScreenshotResult result) {
		CompletableFuture<Screenshot> future = screenshots.remove(result.requestId);
		if (future != null) {
			future.complete(new Screenshot(result.granted, result.png, result.width, result.height));
		}
	}

	void onModList(ModList list) {
		CompletableFuture<ModListResult> future = modLists.remove(list.requestId);
		if (future != null) {
			List<ModListResult.Mod> mods = new ArrayList<>();
			for (ModEntry entry : list.mods) {
				mods.add(new ModListResult.Mod(entry.id, entry.name, entry.version));
			}
			future.complete(new ModListResult(list.granted, mods, list.tehoModules));
		}
	}

	void close() {
		for (Part part : parts) {
			part.close();
		}
		IllegalStateException gone = new IllegalStateException(name + " left before answering");
		for (CompletableFuture<Screenshot> future : screenshots.values()) {
			future.completeExceptionally(gone);
		}
		for (CompletableFuture<ModListResult> future : modLists.values()) {
			future.completeExceptionally(gone);
		}
		screenshots.clear();
		modLists.clear();
	}

	void onPrivileges(List<String> capabilities, String error) {
		privileges = Collections.unmodifiableSet(new LinkedHashSet<>(capabilities));
		privilegeError = error == null ? "" : error;
	}

	@Override
	public Set<String> privileges() {
		return privileges;
	}

	@Override
	public String privilegeError() {
		return privilegeError;
	}

	boolean deliver(String capability, Message message) {
		return sendIf(capability, message);
	}

	private boolean sendIf(String capability, Message message) {
		if (!enabled.contains(capability) || !core.bridge().online(id)) {
			return false;
		}
		core.send(id, message, protocol);
		return true;
	}

	@Override
	public UUID uniqueId() {
		return id;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int protocol() {
		return protocol;
	}

	@Override
	public String clientVersion() {
		return hello.client;
	}

	@Override
	public String minecraftVersion() {
		return hello.minecraft;
	}

	@Override
	public String platform() {
		return hello.platform;
	}

	@Override
	public List<String> advertised() {
		return hello.capabilities;
	}

	@Override
	public Set<String> capabilities() {
		return enabled;
	}

	@Override
	public boolean supports(String capability) {
		return enabled.contains(capability);
	}

	@Override
	public long session() {
		return session;
	}

	@Override
	public boolean notify(Notification notification) {
		return sendIf(TehoProtocol.NOTIFY, new Notify(notification.id(), notification.title(), notification.body(),
				notification.accent(), notification.durationMs(), notification.sound()));
	}

	@Override
	public boolean dismiss(String notificationId) {
		return sendIf(TehoProtocol.NOTIFY, new Dismiss(notificationId));
	}

	@Override
	public WaypointsApi waypoints() {
		return waypointsApi;
	}

	@Override
	public ModulesApi modules() {
		return modulesApi;
	}

	@Override
	public StaffApi staff() {
		return staffApi;
	}

	@Override
	public HudApi hud() {
		return hudApi;
	}

	@Override
	public SuspectApi suspect() {
		return suspectApi;
	}

	@Override
	public dev.tehoclient.api.world.WorldApi world() {
		return world;
	}

	@Override
	public dev.tehoclient.api.players.PlayersApi players() {
		return players;
	}

	@Override
	public dev.tehoclient.api.keybind.KeybindApi keybinds() {
		return input;
	}

	@Override
	public dev.tehoclient.api.camera.CameraApi camera() {
		return input;
	}

	@Override
	public dev.tehoclient.api.audio.AudioApi audio() {
		return input;
	}

	@Override
	public dev.tehoclient.api.social.SocialApi social() {
		return social;
	}

	@Override
	public dev.tehoclient.api.ui.UiApi ui() {
		return ui;
	}

	@Override
	public dev.tehoclient.api.info.InfoApi info() {
		return info;
	}

	@Override
	public dev.tehoclient.api.verify.VerifyApi verify() {
		return info;
	}

	@Override
	public dev.tehoclient.api.pack.PackApi pack() {
		return social;
	}

	@Override
	public dev.tehoclient.api.clips.ClipsApi clips() {
		return social;
	}

	@Override
	public dev.tehoclient.api.broadcast.BroadcastApi broadcast() {
		return social;
	}

	@Override
	public dev.tehoclient.api.showcase.ShowcaseApi showcase() {
		return social;
	}

	@Override
	public boolean verifiedServer() {
		return core.verifiedCertificate() != null && !core.verifiedCertificate().isEmpty();
	}

	@Override
	public synchronized void reset() {
		for (Part part : parts) {
			part.reset();
		}
		waypoints.clear();
		rules = ModuleRules.NONE;
		grant = null;
		accepted.clear();
		active.clear();
		esp = null;
		cooldowns.clear();
		cooldownEnds.clear();
		timers.clear();
		timerSent.clear();
		highlights.clear();
		wheel = null;
		freeze = null;
		if (core.bridge().online(id)) {
			core.send(id, Signal.RESET, protocol);
		}
	}

	@Override
	public String toString() {
		Hello current = hello;
		return name + " " + current.client + " (" + current.minecraft + ", " + current.platform + ") " + enabled;
	}

	private static WaypointSet toMessage(Waypoint waypoint, boolean focus) {
		return new WaypointSet(waypoint.id(), waypoint.name(), waypoint.x(), waypoint.y(), waypoint.z(),
				waypoint.dimension(), waypoint.color(), focus);
	}

	private void sendRules() {
		sendIf(TehoProtocol.MODULES, new dev.tehoclient.api.protocol.ModuleRules(new ArrayList<>(rules.disabled()),
				new ArrayList<>(rules.disabledCategories()), rules.reason(), rules.competitive(),
				new ArrayList<>(rules.allowed())));
		List<dev.tehoclient.api.protocol.SettingCap> caps = new ArrayList<>();
		for (SettingCap cap : rules.caps()) {
			caps.add(new dev.tehoclient.api.protocol.SettingCap(cap.module(), cap.key(), cap.min(), cap.max(),
					cap.bounded(), cap.pinned()));
		}
		sendIf(TehoProtocol.MODULES, new SettingCaps(caps));
	}

	private static dev.tehoclient.api.protocol.StaffGrant toMessage(StaffGrant grant) {
		List<String> tools = new ArrayList<>();
		for (StaffTool tool : grant.tools()) {
			tools.add(tool.id());
		}
		List<XrayBlock> blocks = new ArrayList<>();
		for (dev.tehoclient.api.staff.XrayBlock block : grant.xrayBlocks()) {
			blocks.add(new XrayBlock(block.block(), block.color()));
		}
		List<XrayBlock> storage = new ArrayList<>();
		for (dev.tehoclient.api.staff.XrayBlock block : grant.storageBlocks()) {
			storage.add(new XrayBlock(block.block(), block.color()));
		}
		List<XrayBlock> entities = new ArrayList<>();
		for (dev.tehoclient.api.staff.XrayBlock type : grant.entityTypes()) {
			entities.add(new XrayBlock(type.block(), type.color()));
		}
		return new dev.tehoclient.api.protocol.StaffGrant(tools, grant.reason(), blocks, grant.radius(), storage, entities);
	}

	private static dev.tehoclient.api.protocol.Cooldown toMessage(Cooldown cooldown, long remainingMs) {
		return new dev.tehoclient.api.protocol.Cooldown(cooldown.id(), cooldown.label(),
				(int) Math.min(Integer.MAX_VALUE, remainingMs), cooldown.item(), new byte[0], cooldown.color());
	}

	private static dev.tehoclient.api.protocol.Timer toMessage(Timer timer, long millis) {
		int mode = timer.mode() == Timer.Mode.STOPWATCH ? TehoProtocol.TIMER_STOPWATCH : TehoProtocol.TIMER_COUNTDOWN;
		return new dev.tehoclient.api.protocol.Timer(timer.id(), timer.label(), mode,
				(int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, millis)), timer.isPaused(), timer.color());
	}

	private long adjusted(Timer timer, long now) {
		if (timer.isPaused()) {
			return timer.millis();
		}
		long elapsed = now - timerSent.getOrDefault(timer.id(), now);
		return timer.mode() == Timer.Mode.STOPWATCH ? timer.millis() + elapsed : Math.max(0, timer.millis() - elapsed);
	}

	private final class Waypoints implements WaypointsApi {
		@Override
		public boolean set(Waypoint waypoint) {
			synchronized (TehoSession.this) {
				waypoints.put(waypoint.id(), waypoint);
				return sendIf(TehoProtocol.WAYPOINT, toMessage(waypoint, waypoint.focus()));
			}
		}

		@Override
		public boolean remove(String waypointId) {
			synchronized (TehoSession.this) {
				waypoints.remove(waypointId);
				return sendIf(TehoProtocol.WAYPOINT, new WaypointRemove(waypointId));
			}
		}

		@Override
		public boolean clear() {
			synchronized (TehoSession.this) {
				waypoints.clear();
				return sendIf(TehoProtocol.WAYPOINT, Signal.WAYPOINT_CLEAR);
			}
		}

		@Override
		public Collection<Waypoint> active() {
			synchronized (TehoSession.this) {
				return Collections.unmodifiableList(new ArrayList<>(waypoints.values()));
			}
		}
	}

	private final class Modules implements ModulesApi {
		@Override
		public ModuleRules rules() {
			synchronized (TehoSession.this) {
				return rules;
			}
		}

		@Override
		public boolean combat(dev.tehoclient.api.modules.CombatRules combat) {
			return policy.combat(combat);
		}

		@Override
		public CompletableFuture<dev.tehoclient.api.modules.PolicyReport> denyMods(String reason, String... modIds) {
			return policy.deny(reason, modIds);
		}

		@Override
		public boolean apply(ModuleRules next) {
			synchronized (TehoSession.this) {
				rules = next == null ? ModuleRules.NONE : next;
				if (!enabled.contains(TehoProtocol.MODULES)) {
					return false;
				}
				sendRules();
				return true;
			}
		}
	}

	private final class Staff implements StaffApi {
		@Override
		public boolean grant(StaffGrant next) {
			synchronized (TehoSession.this) {
				grant = next;
				accepted.retainAll(next.tools());
				active.retainAll(next.tools());
				return sendIf(TehoProtocol.STAFF, toMessage(next));
			}
		}

		@Override
		public boolean revoke(StaffTool... tools) {
			synchronized (TehoSession.this) {
				if (grant == null) {
					return false;
				}
				EnumSet<StaffTool> remaining = EnumSet.copyOf(grant.tools());
				List<String> ids = new ArrayList<>();
				for (StaffTool tool : tools) {
					remaining.remove(tool);
					accepted.remove(tool);
					active.remove(tool);
					ids.add(tool.id());
				}
				grant = remaining.isEmpty() ? null : StaffGrant.of(remaining).reason(grant.reason())
						.xray(grant.xrayBlocks()).radius(grant.radius()).build();
				return sendIf(TehoProtocol.STAFF, new StaffRevoke(ids));
			}
		}

		@Override
		public boolean revokeAll() {
			synchronized (TehoSession.this) {
				grant = null;
				accepted.clear();
				active.clear();
				esp = null;
				return sendIf(TehoProtocol.STAFF, new StaffRevoke(Collections.<String>emptyList()));
			}
		}

		@Override
		public Set<StaffTool> granted() {
			synchronized (TehoSession.this) {
				return grant == null ? Collections.<StaffTool>emptySet() : grant.tools();
			}
		}

		@Override
		public Set<StaffTool> accepted() {
			synchronized (TehoSession.this) {
				return accepted.isEmpty() ? Collections.<StaffTool>emptySet() : Collections.unmodifiableSet(EnumSet.copyOf(accepted));
			}
		}

		@Override
		public Set<StaffTool> active() {
			synchronized (TehoSession.this) {
				return active.isEmpty() ? Collections.<StaffTool>emptySet() : Collections.unmodifiableSet(EnumSet.copyOf(active));
			}
		}

		@Override
		public boolean espColors(Map<UUID, Integer> colors, Map<UUID, String> labels) {
			synchronized (TehoSession.this) {
				Set<UUID> players = new LinkedHashSet<>(colors.keySet());
				players.addAll(labels.keySet());
				List<EspEntry> entries = new ArrayList<>();
				for (UUID player : players) {
					Integer color = colors.get(player);
					String label = labels.get(player);
					entries.add(new EspEntry(player.toString(), color == null ? 0 : color, label == null ? "" : label));
				}
				esp = new EspStyle(entries);
				return sendIf(TehoProtocol.STAFF, esp);
			}
		}
	
		@Override
		public Set<StaffTool> supported() {
			synchronized (TehoSession.this) {
				return supported.isEmpty() ? Collections.<StaffTool>emptySet() : Collections.unmodifiableSet(EnumSet.copyOf(supported));
			}
		}

		@Override
		public boolean highlight(Highlight highlight) {
			synchronized (TehoSession.this) {
				int kind = highlight.kind() == Target.Kind.ENTITY ? TehoProtocol.HIGHLIGHT_ENTITY
						: highlight.kind() == Target.Kind.BLOCK ? TehoProtocol.HIGHLIGHT_BLOCK : TehoProtocol.HIGHLIGHT_AREA;
				int style = highlight.style() == Highlight.Style.FILL ? TehoProtocol.STYLE_FILL
						: highlight.style() == Highlight.Style.BOTH ? TehoProtocol.STYLE_BOTH : TehoProtocol.STYLE_OUTLINE;
				dev.tehoclient.api.protocol.Highlight message = new dev.tehoclient.api.protocol.Highlight(highlight.id(), kind,
						highlight.entity() == null ? "" : highlight.entity().toString(), highlight.x(), highlight.y(), highlight.z(),
						highlight.x2(), highlight.y2(), highlight.z2(), highlight.argb(), highlight.label(), style,
						highlight.tracer(), highlight.ttlMs(), highlight.dimension());
				if (highlight.ttlMs() == 0) {
					highlights.put(highlight.id(), message);
				} else {
					highlights.remove(highlight.id());
				}
				return sendIf(TehoProtocol.STAFF, message);
			}
		}

		@Override
		public boolean removeHighlights(String... ids) {
			synchronized (TehoSession.this) {
				if (ids.length == 0) {
					return true;
				}
				for (String highlightId : ids) {
					highlights.remove(highlightId);
				}
				return sendIf(TehoProtocol.STAFF, new HighlightRemove(java.util.Arrays.asList(ids)));
			}
		}

		@Override
		public boolean clearHighlights() {
			synchronized (TehoSession.this) {
				highlights.clear();
				return sendIf(TehoProtocol.STAFF, new HighlightRemove(Collections.<String>emptyList()));
			}
		}

		@Override
		public boolean trail(UUID player, List<int[]> points, int rgb, String label, String dimension, int ttlMs) {
			List<Integer> flat = new ArrayList<>();
			int start = Math.max(0, points.size() - TehoProtocol.MAX_POINTS / 3);
			for (int i = start; i < points.size(); i++) {
				int[] point = points.get(i);
				flat.add(point[0]);
				flat.add(point[1]);
				flat.add(point[2]);
			}
			return sendIf(TehoProtocol.STAFF, new Trail(player.toString(), flat, rgb & 0xFFFFFF, label, Math.max(0, ttlMs),
					dimension == null ? "" : dimension));
		}

		@Override
		public boolean actionWheel(String title, List<WheelAction> actions) {
			synchronized (TehoSession.this) {
				List<dev.tehoclient.api.protocol.WheelAction> out = new ArrayList<>();
				for (WheelAction action : actions) {
					out.add(new dev.tehoclient.api.protocol.WheelAction(action.id(), action.label(), action.item(), action.color(),
							action.target().ordinal()));
				}
				wheel = new ActionWheel(out, title);
				return sendIf(TehoProtocol.STAFF, wheel);
			}
		}

		@Override
		public boolean alert(StaffAlert alert) {
			return sendIf(TehoProtocol.STAFF, new Alert(alert.id(), alert.severity().ordinal(), alert.title(), alert.body(),
					alert.player() == null ? "" : alert.player().toString(), alert.x(), alert.y(), alert.z(),
					alert.dimension(), alert.ttlMs()));
		}

		@Override
		public boolean targetStats(UUID player, List<StatRow> rows, int ttlMs) {
			List<InspectRow> out = new ArrayList<>();
			for (StatRow row : rows) {
				out.add(new InspectRow(row.label(), row.value(), row.color()));
			}
			return sendIf(TehoProtocol.STAFF, new TargetStats(player.toString(), out, Math.max(0, ttlMs)));
		}

		@Override
		public boolean spectate(UUID player, int distance) {
			return sendIf(TehoProtocol.STAFF, new Spectate(player.toString(), Math.max(2, Math.min(32, distance))));
		}

		@Override
		public boolean stopSpectating() {
			return sendIf(TehoProtocol.STAFF, new Spectate("", 0));
		}

		@Override
		public boolean lagMap(String dimension, Collection<LagCell> cells, int ttlMs) {
			List<dev.tehoclient.api.protocol.LagCell> out = new ArrayList<>();
			for (LagCell cell : cells) {
				out.add(new dev.tehoclient.api.protocol.LagCell(cell.chunkX(), cell.chunkZ(), cell.score(), cell.label()));
			}
			return sendIf(TehoProtocol.STAFF, new LagMap(out, dimension == null ? "" : dimension, Math.max(0, ttlMs)));
		}
	}

	private final class Hud implements HudApi {
		@Override
		public boolean cooldown(Cooldown cooldown) {
			synchronized (TehoSession.this) {
				if (cooldown.durationMs() <= 0) {
					return clearCooldown(cooldown.id());
				}
				cooldowns.put(cooldown.id(), cooldown);
				cooldownEnds.put(cooldown.id(), System.currentTimeMillis() + cooldown.durationMs());
				return sendIf(TehoProtocol.HUD, toMessage(cooldown, cooldown.durationMs()));
			}
		}

		@Override
		public boolean clearCooldown(String cooldownId) {
			synchronized (TehoSession.this) {
				cooldowns.remove(cooldownId);
				cooldownEnds.remove(cooldownId);
				return sendIf(TehoProtocol.HUD, new dev.tehoclient.api.protocol.Cooldown(cooldownId, "", 0, "", new byte[0], 0));
			}
		}

		@Override
		public boolean clearCooldowns() {
			synchronized (TehoSession.this) {
				cooldowns.clear();
				cooldownEnds.clear();
				return sendIf(TehoProtocol.HUD, Signal.COOLDOWN_CLEAR);
			}
		}

		@Override
		public boolean timer(Timer timer) {
			synchronized (TehoSession.this) {
				timers.put(timer.id(), timer);
				timerSent.put(timer.id(), System.currentTimeMillis());
				return sendIf(TehoProtocol.HUD, toMessage(timer, timer.millis()));
			}
		}

		@Override
		public boolean removeTimer(String timerId) {
			synchronized (TehoSession.this) {
				timers.remove(timerId);
				timerSent.remove(timerId);
				return sendIf(TehoProtocol.HUD, new TimerRemove(timerId));
			}
		}

		@Override
		public boolean sidebar(dev.tehoclient.api.hud.Sidebar sidebar) {
			return hudExtras.sidebar(sidebar);
		}

		@Override
		public boolean clearSidebar() {
			return hudExtras.clearSidebar();
		}

		@Override
		public boolean bar(dev.tehoclient.api.hud.Bar bar) {
			return hudExtras.bar(bar);
		}

		@Override
		public boolean removeBars(String... ids) {
			return hudExtras.removeBars(ids);
		}

		@Override
		public boolean kill(String killer, UUID killerId, String victim, UUID victimId, String weaponItem, int argb) {
			return hudExtras.kill(killer, killerId, victim, victimId, weaponItem, argb);
		}

		@Override
		public boolean feed(String text, int argb) {
			return hudExtras.feed(text, argb);
		}

		@Override
		public boolean tint(int argb, boolean vignette, double strength, long fadeMs, long ttlMs) {
			return hudExtras.tint(argb, vignette, strength, fadeMs, ttlMs);
		}

		@Override
		public boolean clearTint() {
			return hudExtras.clearTint();
		}

		@Override
		public boolean title(dev.tehoclient.api.hud.Title title) {
			return hudExtras.title(title);
		}

		@Override
		public boolean tntFuse(int ticks) {
			return hudExtras.tntFuse(ticks);
		}
	}

	private final class Suspect implements SuspectApi {
		@Override
		public boolean freeze(String title, String body) {
			synchronized (TehoSession.this) {
				freeze = new FreezeScreen(true, title, body);
				return sendIf(TehoProtocol.SUSPECT, freeze);
			}
		}

		@Override
		public boolean unfreeze() {
			synchronized (TehoSession.this) {
				freeze = null;
				return sendIf(TehoProtocol.SUSPECT, new FreezeScreen(false, "", ""));
			}
		}

		@Override
		public boolean frozen() {
			synchronized (TehoSession.this) {
				return freeze != null;
			}
		}

		@Override
		public CompletableFuture<Screenshot> screenshot(String reason, int maxWidth) {
			CompletableFuture<Screenshot> future = new CompletableFuture<>();
			int request = requests.getAndIncrement();
			screenshots.put(request, future);
			if (!sendIf(TehoProtocol.SUSPECT, new ScreenshotRequest(request, reason, Math.max(320, Math.min(1920, maxWidth))))) {
				screenshots.remove(request);
				future.completeExceptionally(new IllegalStateException(name + " does not support screenshot requests"));
			}
			return future;
		}

		@Override
		public CompletableFuture<ModListResult> modList(String reason) {
			CompletableFuture<ModListResult> future = new CompletableFuture<>();
			int request = requests.getAndIncrement();
			modLists.put(request, future);
			if (!sendIf(TehoProtocol.SUSPECT, new ModListRequest(request, reason))) {
				modLists.remove(request);
				future.completeExceptionally(new IllegalStateException(name + " does not support mod list requests"));
			}
			return future;
		}
	}
}
