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

package dev.tehoclient.api;

import java.util.List;

import dev.tehoclient.api.staff.InspectResponder;
import dev.tehoclient.api.staff.StaffTool;
import dev.tehoclient.api.staff.Target;

public interface TehoListener {
	default void onReady(TehoPlayer player) {
	}

	default void onLeave(TehoPlayer player) {
	}

	default void onConsent(TehoPlayer player, String scope, List<String> items, boolean granted) {
	}

	default void onStaffTool(TehoPlayer player, StaffTool tool, boolean active) {
	}

	default void onEvent(TehoPlayer player, String kind, String target, String value) {
	}

	default void onPrivileges(TehoPlayer player, java.util.Set<String> privileges, String error) {
	}

	default void onInspect(TehoPlayer staff, Target target, InspectResponder responder) {
	}

	default void onStaffAction(TehoPlayer staff, String action, Target target) {
	}

	default void onKey(TehoPlayer player, String keybind, boolean pressed, long heldMs) {
	}

	default void onUi(TehoPlayer player, dev.tehoclient.api.ui.UiInteraction interaction) {
	}

	default void onHologramClick(TehoPlayer player, String hologram) {
	}

	default void onCutsceneEnded(TehoPlayer player, String cutscene, boolean skipped) {
	}

	default void onPackStatus(TehoPlayer player, String sha1, dev.tehoclient.api.pack.PackApi.State state) {
	}
}
