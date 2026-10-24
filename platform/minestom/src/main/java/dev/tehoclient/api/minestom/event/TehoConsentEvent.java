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

package dev.tehoclient.api.minestom.event;

import java.util.List;

import net.minestom.server.entity.Player;
import dev.tehoclient.api.TehoPlayer;

public final class TehoConsentEvent extends TehoPlayerEvent {
	private final String scope;
	private final List<String> items;
	private final boolean granted;

	public TehoConsentEvent(TehoPlayer tehoPlayer, Player player, String scope, List<String> items, boolean granted) {
		super(tehoPlayer, player);
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
