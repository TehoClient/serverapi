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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.tehoclient.api.asset.Asset;
import dev.tehoclient.api.protocol.Message;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.protocol.UiClose;
import dev.tehoclient.api.protocol.UiEvent;
import dev.tehoclient.api.protocol.UiNode;
import dev.tehoclient.api.protocol.UiOpen;
import dev.tehoclient.api.protocol.UiPatch;
import dev.tehoclient.api.ui.Node;
import dev.tehoclient.api.ui.Screen;
import dev.tehoclient.api.ui.UiApi;
import dev.tehoclient.api.ui.UiHandler;
import dev.tehoclient.api.ui.UiInteraction;

final class UiPart extends Part implements UiApi {
	static final int MAX_SCREENS = 8;
	static final int MAX_EVENTS_PER_SECOND = 30;

	private static final class Live {
		final Screen screen;
		String title;
		final List<UiNode> nodes;
		final List<Asset> assets;

		Live(Screen screen) {
			this.screen = screen;
			this.title = screen.wire().title;
			this.nodes = new ArrayList<>(screen.wire().nodes);
			this.assets = new ArrayList<>(screen.assets());
		}

		UiOpen wire() {
			UiOpen base = screen.wire();
			return new UiOpen(base.id, base.surface, title, nodes, base.width, base.closeable, base.anchor);
		}

		UiNode find(String id) {
			for (UiNode node : nodes) {
				if (node.id.equals(id)) {
					return node;
				}
			}
			return null;
		}
	}

	private final Map<String, Live> screens = new LinkedHashMap<>();
	private long windowStart;
	private int windowEvents;

	UiPart(TehoSession session) {
		super(session);
	}

	@Override
	public synchronized boolean open(Screen screen) {
		if (!screens.containsKey(screen.id()) && screens.size() >= MAX_SCREENS) {
			throw new IllegalStateException("At most " + MAX_SCREENS + " open screens per player");
		}
		Live live = new Live(screen);
		screens.put(screen.id(), live);
		return send(TehoProtocol.UI, live.wire(), live.assets);
	}

	@Override
	public synchronized boolean patch(String screenId, Node... nodes) {
		Live live = screens.get(screenId);
		if (live == null) {
			return false;
		}
		List<UiNode> changed = new ArrayList<>();
		List<Asset> assets = new ArrayList<>();
		for (Node node : nodes) {
			UiNode existing = live.find(node.id());
			String parent = existing == null ? "" : existing.parent;
			List<UiNode> flat = Screen.flatten(parent, Collections.singletonList(node), assets);
			int index = existing == null ? live.nodes.size() : live.nodes.indexOf(existing);
			removeSubtree(live, node.id());
			index = Math.min(index, live.nodes.size());
			live.nodes.addAll(index, flat);
			changed.addAll(flat);
		}
		live.assets.addAll(assets);
		if (live.nodes.size() > Screen.MAX_NODES) {
			throw new IllegalStateException("a screen holds at most " + Screen.MAX_NODES + " nodes");
		}
		return send(TehoProtocol.UI, new UiPatch(screenId, changed, Collections.<String>emptyList(), ""), assets);
	}

	@Override
	public synchronized boolean remove(String screenId, String... nodeIds) {
		Live live = screens.get(screenId);
		if (live == null) {
			return false;
		}
		for (String id : nodeIds) {
			removeSubtree(live, id);
		}
		return send(TehoProtocol.UI, new UiPatch(screenId, Collections.<UiNode>emptyList(), Arrays.asList(nodeIds), ""));
	}

	private static void removeSubtree(Live live, String id) {
		Set<String> doomed = new HashSet<>();
		doomed.add(id);
		boolean grew = true;
		while (grew) {
			grew = false;
			for (UiNode node : live.nodes) {
				if (!doomed.contains(node.id) && doomed.contains(node.parent)) {
					doomed.add(node.id);
					grew = true;
				}
			}
		}
		live.nodes.removeIf(node -> doomed.contains(node.id));
	}

	@Override
	public synchronized boolean retitle(String screenId, String title) {
		Live live = screens.get(screenId);
		if (live == null) {
			return false;
		}
		live.title = title == null ? "" : title;
		return send(TehoProtocol.UI, new UiPatch(screenId, Collections.<UiNode>emptyList(), Collections.<String>emptyList(), live.title));
	}

	@Override
	public synchronized boolean close(String screenId) {
		screens.remove(screenId);
		return send(TehoProtocol.UI, new UiClose(screenId));
	}

	@Override
	public synchronized Set<String> openScreens() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(screens.keySet()));
	}

	@Override
	synchronized void reapply() {
		for (Live live : screens.values()) {
			send(TehoProtocol.UI, live.wire(), live.assets);
		}
	}

	@Override
	boolean receive(Message message) {
		if (!(message instanceof UiEvent)) {
			return false;
		}
		UiEvent event = (UiEvent) message;
		UiInteraction.Action action = UiInteraction.action(event.action);
		UiHandler handler;
		synchronized (this) {
			long now = System.currentTimeMillis();
			if (now - windowStart > 1000) {
				windowStart = now;
				windowEvents = 0;
			}
			if (++windowEvents > MAX_EVENTS_PER_SECOND || action == null) {
				return true;
			}
			Live live = screens.get(event.id);
			if (live == null) {
				return true;
			}
			if (action != UiInteraction.Action.CLOSED) {
				UiNode node = live.find(event.node);
				if (node == null || node.disabled) {
					return true;
				}
			} else {
				screens.remove(event.id);
			}
			handler = live.screen.handler();
		}
		UiInteraction interaction = new UiInteraction(event.id, event.node, action, event.value);
		if (handler != null) {
			try {
				handler.handle(session, interaction);
			} catch (RuntimeException failed) {
				core().bridge().warn("A Teho UI handler failed for " + event.id, failed);
			}
		}
		core().fire(listener -> listener.onUi(session, interaction));
		return true;
	}

	@Override
	synchronized void reset() {
		screens.clear();
	}
}
