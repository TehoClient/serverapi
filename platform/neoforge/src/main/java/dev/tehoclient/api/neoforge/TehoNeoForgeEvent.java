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

package dev.tehoclient.api.neoforge;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.staff.StaffTool;

public abstract class TehoNeoForgeEvent extends Event {
	private final TehoPlayer teho;
	private final ServerPlayer player;

	TehoNeoForgeEvent(TehoPlayer teho, ServerPlayer player) {
		this.teho = teho;
		this.player = player;
	}

	public TehoPlayer getTehoPlayer() {
		return teho;
	}

	public ServerPlayer getPlayer() {
		return player;
	}

	public static final class Ready extends TehoNeoForgeEvent {
		Ready(TehoPlayer teho, ServerPlayer player) {
			super(teho, player);
		}
	}

	public static final class ClientEvent extends TehoNeoForgeEvent {
		private final String kind;
		private final String target;
		private final String value;

		ClientEvent(TehoPlayer teho, ServerPlayer player, String kind, String target, String value) {
			super(teho, player);
			this.kind = kind;
			this.target = target;
			this.value = value;
		}

		public String getKind() {
			return kind;
		}

		public String getTarget() {
			return target;
		}

		public String getValue() {
			return value;
		}
	}

	public static final class Consent extends TehoNeoForgeEvent {
		private final String scope;
		private final List<String> items;
		private final boolean granted;

		Consent(TehoPlayer teho, ServerPlayer player, String scope, List<String> items, boolean granted) {
			super(teho, player);
			this.scope = scope;
			this.items = items;
			this.granted = granted;
		}

		public String getScope() {
			return scope;
		}

		public List<String> getItems() {
			return items;
		}

		public boolean isGranted() {
			return granted;
		}
	}

	public static final class StaffToolChanged extends TehoNeoForgeEvent {
		private final StaffTool tool;
		private final boolean active;

		StaffToolChanged(TehoPlayer teho, ServerPlayer player, StaffTool tool, boolean active) {
			super(teho, player);
			this.tool = tool;
			this.active = active;
		}

		public StaffTool getTool() {
			return tool;
		}

		public boolean isActive() {
			return active;
		}
	}
}
