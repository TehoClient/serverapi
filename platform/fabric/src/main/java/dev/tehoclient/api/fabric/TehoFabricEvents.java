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

package dev.tehoclient.api.fabric;

import java.util.List;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.staff.StaffTool;

public final class TehoFabricEvents {
	public static final Event<Ready> READY = EventFactory.createArrayBacked(Ready.class, listeners -> (teho, player) -> {
		for (Ready listener : listeners) {
			listener.onReady(teho, player);
		}
	});

	public static final Event<ClientEvent> CLIENT_EVENT = EventFactory.createArrayBacked(ClientEvent.class,
			listeners -> (teho, player, kind, target, value) -> {
				for (ClientEvent listener : listeners) {
					listener.onClientEvent(teho, player, kind, target, value);
				}
			});

	public static final Event<Consent> CONSENT = EventFactory.createArrayBacked(Consent.class,
			listeners -> (teho, player, scope, items, granted) -> {
				for (Consent listener : listeners) {
					listener.onConsent(teho, player, scope, items, granted);
				}
			});

	public static final Event<StaffToolChanged> STAFF_TOOL = EventFactory.createArrayBacked(StaffToolChanged.class,
			listeners -> (teho, player, tool, active) -> {
				for (StaffToolChanged listener : listeners) {
					listener.onStaffTool(teho, player, tool, active);
				}
			});

	private TehoFabricEvents() {}

	@FunctionalInterface
	public interface Ready {
		void onReady(TehoPlayer teho, ServerPlayer player);
	}

	@FunctionalInterface
	public interface ClientEvent {
		void onClientEvent(TehoPlayer teho, ServerPlayer player, String kind, String target, String value);
	}

	@FunctionalInterface
	public interface Consent {
		void onConsent(TehoPlayer teho, ServerPlayer player, String scope, List<String> items, boolean granted);
	}

	@FunctionalInterface
	public interface StaffToolChanged {
		void onStaffTool(TehoPlayer teho, ServerPlayer player, StaffTool tool, boolean active);
	}
}
