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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import dev.tehoclient.api.Units;
import dev.tehoclient.api.asset.Asset;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.protocol.UiNode;

public final class Node {
	public enum Style {
		DEFAULT, PRIMARY, DANGER, SUBTLE, TITLE, CAPTION
	}

	private final String id;
	private final int kind;
	private String text = "";
	private String value = "";
	private int min;
	private int max;
	private int step;
	private Asset image;
	private String item = "";
	private String player = "";
	private int color;
	private int columns;
	private boolean disabled;
	private String placeholder = "";
	private final List<String> options = new ArrayList<>();
	private long millis;
	private Style style = Style.DEFAULT;
	private int width;
	private int height;
	private String tooltip = "";
	private final List<Node> children = new ArrayList<>();

	private Node(String id, int kind) {
		this.id = Units.require(id, "node id");
		this.kind = kind;
	}

	public static Node stack(String id, Node... children) {
		return new Node(id, TehoProtocol.NODE_STACK).add(children);
	}

	public static Node row(String id, Node... children) {
		return new Node(id, TehoProtocol.NODE_ROW).add(children);
	}

	public static Node grid(String id, int columns, Node... children) {
		Node node = new Node(id, TehoProtocol.NODE_GRID).add(children);
		node.columns = Math.max(1, Math.min(12, columns));
		return node;
	}

	public static Node text(String id, String text) {
		Node node = new Node(id, TehoProtocol.NODE_TEXT);
		node.text = text == null ? "" : text;
		return node;
	}

	public static Node image(String id, Asset image, int width, int height) {
		if (image == null || image.kind() != Asset.Kind.PNG) {
			throw new IllegalArgumentException("images must be PNG assets");
		}
		Node node = new Node(id, TehoProtocol.NODE_IMAGE);
		node.image = image;
		node.width = Math.max(1, Math.min(1024, width));
		node.height = Math.max(1, Math.min(1024, height));
		return node;
	}

	public static Node item(String id, String item) {
		Node node = new Node(id, TehoProtocol.NODE_ITEM);
		node.item = Units.item(item);
		return node;
	}

	public static Node button(String id, String label) {
		Node node = new Node(id, TehoProtocol.NODE_BUTTON);
		node.text = label == null ? "" : label;
		return node;
	}

	public static Node toggle(String id, String label, boolean on) {
		Node node = new Node(id, TehoProtocol.NODE_TOGGLE);
		node.text = label == null ? "" : label;
		node.value = on ? "true" : "false";
		return node;
	}

	public static Node slider(String id, String label, int min, int max, int step, int value) {
		Node node = new Node(id, TehoProtocol.NODE_SLIDER);
		node.text = label == null ? "" : label;
		node.min = Math.min(min, max);
		node.max = Math.max(min, max);
		node.step = Math.max(1, step);
		node.value = String.valueOf(Math.max(node.min, Math.min(node.max, value)));
		return node;
	}

	public static Node input(String id, String label, String placeholder, int maxLength) {
		Node node = new Node(id, TehoProtocol.NODE_INPUT);
		node.text = label == null ? "" : label;
		node.placeholder = placeholder == null ? "" : placeholder;
		node.max = Math.max(1, Math.min(256, maxLength));
		return node;
	}

	public static Node select(String id, String label, List<String> options, String selected) {
		Node node = new Node(id, TehoProtocol.NODE_SELECT);
		node.text = label == null ? "" : label;
		node.options.addAll(options);
		node.value = selected == null ? "" : selected;
		return node;
	}

	public static Node list(String id, Node... children) {
		return new Node(id, TehoProtocol.NODE_LIST).add(children);
	}

	public static Node tabs(String id, Node... tabs) {
		for (Node tab : tabs) {
			if (tab.kind != TehoProtocol.NODE_TAB) {
				throw new IllegalArgumentException("tabs only hold Node.tab children");
			}
		}
		return new Node(id, TehoProtocol.NODE_TABS).add(tabs);
	}

	public static Node tab(String id, String label, Node... children) {
		Node node = new Node(id, TehoProtocol.NODE_TAB).add(children);
		node.text = label == null ? "" : label;
		return node;
	}

	public static Node progress(String id, String label, double fraction) {
		Node node = new Node(id, TehoProtocol.NODE_PROGRESS);
		node.text = label == null ? "" : label;
		node.value = String.valueOf(Math.max(0, Math.min(10000, Math.round(fraction * 10000))));
		return node;
	}

	public static Node head(String id, UUID player, int size) {
		Node node = new Node(id, TehoProtocol.NODE_HEAD);
		node.player = Units.id(player);
		node.width = Math.max(8, Math.min(128, size));
		return node;
	}

	public static Node countdown(String id, String label, long endsAtEpochMs) {
		Node node = new Node(id, TehoProtocol.NODE_COUNTDOWN);
		node.text = label == null ? "" : label;
		node.millis = Math.max(0, endsAtEpochMs);
		return node;
	}

	public static Node divider(String id) {
		return new Node(id, TehoProtocol.NODE_DIVIDER);
	}

	public Node add(Node... nodes) {
		children.addAll(Arrays.asList(nodes));
		return this;
	}

	public Node style(Style style) {
		this.style = style == null ? Style.DEFAULT : style;
		return this;
	}

	public Node color(int argb) {
		this.color = Units.argb(argb);
		return this;
	}

	public Node disabled(boolean disabled) {
		this.disabled = disabled;
		return this;
	}

	public Node tooltip(String tooltip) {
		this.tooltip = tooltip == null ? "" : tooltip;
		return this;
	}

	public Node icon(String item) {
		this.item = Units.item(item);
		return this;
	}

	public Node size(int width, int height) {
		this.width = Math.max(0, Math.min(2048, width));
		this.height = Math.max(0, Math.min(2048, height));
		return this;
	}

	public String id() {
		return id;
	}

	public List<Node> children() {
		return Collections.unmodifiableList(children);
	}

	public void flatten(String parent, List<UiNode> out, List<Asset> assets) {
		if (image != null) {
			assets.add(image);
		}
		out.add(new UiNode(id, parent, kind, text, value, min, max, step, image == null ? new byte[0] : image.hash(), item, player, color,
				columns, disabled, placeholder, options, millis, style.ordinal(), width, height, tooltip));
		for (Node child : children) {
			child.flatten(id, out, assets);
		}
	}
}
