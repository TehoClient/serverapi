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

package dev.tehoclient.api.hud;

import java.util.UUID;

public interface HudApi {
	boolean cooldown(Cooldown cooldown);

	boolean clearCooldown(String id);

	boolean clearCooldowns();

	boolean timer(Timer timer);

	boolean removeTimer(String id);

	boolean sidebar(Sidebar sidebar);

	boolean clearSidebar();

	boolean bar(Bar bar);

	boolean removeBars(String... ids);

	boolean kill(String killer, UUID killerId, String victim, UUID victimId, String weaponItem, int argb);

	boolean feed(String text, int argb);

	boolean tint(int argb, boolean vignette, double strength, long fadeMs, long ttlMs);

	boolean clearTint();

	boolean title(Title title);

	boolean tntFuse(int ticks);
}
