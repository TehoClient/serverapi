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

package dev.tehoclient.api.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.tehoclient.api.Units;
import dev.tehoclient.api.asset.Asset;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.protocol.UiNode;

public final class Screen {
	public static final int MAX_NODES = 400;

	public enum Surface {
		MODAL(TehoProtocol.UI_MODAL), PANEL(TehoProtocol.UI_PANEL), HUD(TehoProtocol.UI_HUD);

		private final int wire;

		Surface(int wire) {
			this.wire = wire;
		}
	}

	public enum Anchor {
		CENTER, LEFT, RIGHT, TOP, BOTTOM
	}

	private final dev.tehoclient.api.protocol.UiOpen wire;
	private final List<Asset> assets;
	private final UiHandler handler;

	private Screen(dev.tehoclient.api.protocol.UiOpen wire, List<Asset> assets, UiHandler handler) {
		this.wire = wire;
		this.assets = Collections.unmodifiableList(assets);
		this.handler = handler;
	}

	public static Builder modal(String id, String title) {
		return new Builder(id, Surface.MODAL, title);
	}

	public static Builder panel(String id, String title) {
		return new Builder(id, Surface.PANEL, title).anchor(Anchor.RIGHT);
	}

	public static Builder hud(String id) {
		return new Builder(id, Surface.HUD, "").closeable(false).anchor(Anchor.LEFT);
	}

	public String id() {
		return wire.id;
	}

	public List<Asset> assets() {
		return assets;
	}

	public UiHandler handler() {
		return handler;
	}

	public dev.tehoclient.api.protocol.UiOpen wire() {
		return wire;
	}

	public static List<UiNode> flatten(String parent, List<Node> nodes, List<Asset> assets) {
		List<UiNode> out = new ArrayList<>();
		for (Node node : nodes) {
			node.flatten(parent, out, assets);
		}
		Set<String> seen = new HashSet<>();
		for (UiNode node : out) {
			if (!seen.add(node.id)) {
				throw new IllegalArgumentException("duplicate node id " + node.id);
			}
		}
		if (out.size() > MAX_NODES) {
			throw new IllegalArgumentException("a screen holds at most " + MAX_NODES + " nodes");
		}
		return out;
	}

	public static final class Builder {
		private final String id;
		private final Surface surface;
		private final String title;
		private final List<Node> nodes = new ArrayList<>();
		private int width;
		private boolean closeable = true;
		private Anchor anchor = Anchor.CENTER;
		private UiHandler handler;

		private Builder(String id, Surface surface, String title) {
			this.id = Units.require(id, "screen id");
			this.surface = surface;
			this.title = title == null ? "" : title;
		}

		public Builder add(Node... nodes) {
			for (Node node : nodes) {
				this.nodes.add(node);
			}
			return this;
		}

		public Builder width(int width) {
			this.width = Math.max(0, Math.min(1200, width));
			return this;
		}

		public Builder closeable(boolean closeable) {
			this.closeable = closeable;
			return this;
		}

		public Builder anchor(Anchor anchor) {
			this.anchor = anchor == null ? Anchor.CENTER : anchor;
			return this;
		}

		public Builder onInteract(UiHandler handler) {
			this.handler = handler;
			return this;
		}

		public Screen build() {
			List<Asset> assets = new ArrayList<>();
			List<UiNode> flat = flatten("", nodes, assets);
			return new Screen(new dev.tehoclient.api.protocol.UiOpen(id, surface.wire, title, flat, width, closeable, anchor.ordinal()),
					assets, handler);
		}
	}
}
