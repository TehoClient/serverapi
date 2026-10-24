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

public final class Timer {
	public enum Mode {
		COUNTDOWN, STOPWATCH
	}

	private final String id;
	private final String label;
	private final Mode mode;
	private final long millis;
	private final boolean paused;
	private final int color;

	private Timer(String id, String label, Mode mode, long millis, boolean paused, int color) {
		if (id == null || id.isEmpty()) {
			throw new IllegalArgumentException("timer id is required");
		}
		this.id = id;
		this.label = label == null ? "" : label;
		this.mode = mode;
		this.millis = millis;
		this.paused = paused;
		this.color = color & 0xFFFFFF;
	}

	public static Timer countdown(String id, String label, long remainingMs) {
		return new Timer(id, label, Mode.COUNTDOWN, Math.max(0, remainingMs), false, 0);
	}

	public static Timer stopwatch(String id, String label, long elapsedMs) {
		return new Timer(id, label, Mode.STOPWATCH, Math.max(0, elapsedMs), false, 0);
	}

	public Timer paused(boolean paused) {
		return new Timer(id, label, mode, millis, paused, color);
	}

	public Timer color(int rgb) {
		return new Timer(id, label, mode, millis, paused, rgb);
	}

	public String id() {
		return id;
	}

	public String label() {
		return label;
	}

	public Mode mode() {
		return mode;
	}

	public long millis() {
		return millis;
	}

	public boolean isPaused() {
		return paused;
	}

	public int color() {
		return color;
	}
}
