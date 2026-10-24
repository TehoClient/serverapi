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

package dev.tehoclient.api.bukkit.event;

import org.bukkit.event.HandlerList;
import dev.tehoclient.api.TehoPlayer;

public final class TehoKeyEvent extends TehoPlayerEvent {
	private static final HandlerList HANDLERS = new HandlerList();

	private final String keybind;
	private final boolean pressed;
	private final long heldMs;

	public TehoKeyEvent(TehoPlayer tehoPlayer, String keybind, boolean pressed, long heldMs) {
		super(tehoPlayer);
		this.keybind = keybind;
		this.pressed = pressed;
		this.heldMs = heldMs;
	}

	public String getKeybind() {
		return keybind;
	}

	public boolean isPressed() {
		return pressed;
	}

	public long getHeldMs() {
		return heldMs;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
