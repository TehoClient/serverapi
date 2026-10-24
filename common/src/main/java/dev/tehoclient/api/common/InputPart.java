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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dev.tehoclient.api.Units;
import dev.tehoclient.api.audio.AudioApi;
import dev.tehoclient.api.audio.Sound;
import dev.tehoclient.api.camera.CameraApi;
import dev.tehoclient.api.camera.Cutscene;
import dev.tehoclient.api.keybind.Keybind;
import dev.tehoclient.api.keybind.KeybindApi;
import dev.tehoclient.api.protocol.CameraShake;
import dev.tehoclient.api.protocol.CutsceneEnded;
import dev.tehoclient.api.protocol.CutsceneStop;
import dev.tehoclient.api.protocol.FovPunch;
import dev.tehoclient.api.protocol.KeyPress;
import dev.tehoclient.api.protocol.Keybinds;
import dev.tehoclient.api.protocol.Letterbox;
import dev.tehoclient.api.protocol.LookAt;
import dev.tehoclient.api.protocol.Message;
import dev.tehoclient.api.protocol.ScreenFade;
import dev.tehoclient.api.protocol.StopSound;
import dev.tehoclient.api.protocol.TehoProtocol;

final class InputPart extends Part implements KeybindApi, CameraApi, AudioApi {
	static final int MAX_KEYBINDS = 32;
	static final int MAX_LOOPS = 16;
	static final long MIN_PRESS_GAP_MS = 50;

	private final List<Keybind> keybinds = new ArrayList<>();
	private String category = "";
	private final Map<String, Long> lastPress = new LinkedHashMap<>();
	private final Map<String, Sound> loops = new LinkedHashMap<>();
	private Letterbox letterbox;

	InputPart(TehoSession session) {
		super(session);
	}

	@Override
	public synchronized boolean register(String category, List<Keybind> binds) {
		if (binds.size() > MAX_KEYBINDS) {
			throw new IllegalArgumentException("At most " + MAX_KEYBINDS + " keybinds per server");
		}
		keybinds.clear();
		keybinds.addAll(binds);
		this.category = category == null ? "" : category;
		return send(TehoProtocol.KEYBIND, wire());
	}

	@Override
	public synchronized boolean clear() {
		keybinds.clear();
		return send(TehoProtocol.KEYBIND, wire());
	}

	@Override
	public synchronized List<Keybind> registered() {
		return Collections.unmodifiableList(new ArrayList<>(keybinds));
	}

	private Keybinds wire() {
		List<dev.tehoclient.api.protocol.Keybind> out = new ArrayList<>();
		for (Keybind bind : keybinds) {
			out.add(bind.wire());
		}
		return new Keybinds(out, category);
	}

	@Override
	public boolean shake(double intensity, long durationMs, double frequency) {
		return send(TehoProtocol.CAMERA, new CameraShake(Math.max(0, Math.min(1000, Units.percent(intensity))),
				Math.min(30000, Units.millis(durationMs)), Math.max(1, Math.min(6000, Units.percent(frequency)))));
	}

	@Override
	public boolean fovPunch(int degrees, long durationMs) {
		return send(TehoProtocol.CAMERA, new FovPunch(Math.max(-60, Math.min(60, degrees)), Math.min(10000, Units.millis(durationMs))));
	}

	@Override
	public boolean lookAt(double x, double y, double z, long turnMs, long holdMs) {
		return send(TehoProtocol.CAMERA, new LookAt(Units.centi(x), Units.centi(y), Units.centi(z), "",
				Math.min(10000, Units.millis(turnMs)), Math.min(30000, Units.millis(holdMs))));
	}

	@Override
	public boolean lookAt(UUID entity, long turnMs, long holdMs) {
		return send(TehoProtocol.CAMERA, new LookAt(0, 0, 0, Units.id(entity), Math.min(10000, Units.millis(turnMs)),
				Math.min(30000, Units.millis(holdMs))));
	}

	@Override
	public synchronized boolean letterbox(boolean on, double size, long fadeMs) {
		letterbox = new Letterbox(on, Math.max(0, Math.min(40, Units.percent(size))), Math.min(5000, Units.millis(fadeMs)));
		return send(TehoProtocol.CAMERA, letterbox);
	}

	@Override
	public boolean cutscene(Cutscene cutscene) {
		return send(TehoProtocol.CAMERA, cutscene.wire());
	}

	@Override
	public boolean stopCutscene(String id) {
		return send(TehoProtocol.CAMERA, new CutsceneStop(id));
	}

	@Override
	public boolean fade(int argb, long inMs, long holdMs, long outMs) {
		return send(TehoProtocol.CAMERA, new ScreenFade(Units.argb(argb), Math.min(10000, Units.millis(inMs)),
				Math.min(60000, Units.millis(holdMs)), Math.min(10000, Units.millis(outMs))));
	}

	@Override
	public synchronized boolean play(Sound sound) {
		if (sound.wire().loop) {
			if (!loops.containsKey(sound.id()) && loops.size() >= MAX_LOOPS) {
				throw new IllegalStateException("At most " + MAX_LOOPS + " looping sounds per player");
			}
			loops.put(sound.id(), sound);
		}
		return send(TehoProtocol.AUDIO, sound.wire(), sound.sourceAsset());
	}

	@Override
	public synchronized boolean stop(long fadeOutMs, String... ids) {
		List<String> list = new ArrayList<>();
		for (String id : ids) {
			list.add(id);
			loops.remove(id);
		}
		return send(TehoProtocol.AUDIO, new StopSound(list, Math.min(10000, Units.millis(fadeOutMs))));
	}

	@Override
	public synchronized boolean stopAll(long fadeOutMs) {
		loops.clear();
		return send(TehoProtocol.AUDIO, new StopSound(Collections.<String>emptyList(), Math.min(10000, Units.millis(fadeOutMs))));
	}

	@Override
	synchronized void reapply() {
		if (!keybinds.isEmpty()) {
			send(TehoProtocol.KEYBIND, wire());
		}
		if (letterbox != null && letterbox.on) {
			send(TehoProtocol.CAMERA, letterbox);
		}
		for (Sound sound : loops.values()) {
			send(TehoProtocol.AUDIO, sound.wire(), sound.sourceAsset());
		}
	}

	@Override
	boolean receive(Message message) {
		if (message instanceof KeyPress) {
			KeyPress press = (KeyPress) message;
			boolean known = false;
			synchronized (this) {
				for (Keybind bind : keybinds) {
					known |= bind.id().equals(press.id);
				}
				long now = System.currentTimeMillis();
				Long last = lastPress.get(press.id);
				if (press.pressed && last != null && now - last < MIN_PRESS_GAP_MS) {
					known = false;
				} else if (press.pressed) {
					lastPress.put(press.id, now);
				}
			}
			if (known) {
				core().fire(listener -> listener.onKey(session, press.id, press.pressed, press.heldMs));
			}
			return true;
		}
		if (message instanceof CutsceneEnded) {
			CutsceneEnded ended = (CutsceneEnded) message;
			core().fire(listener -> listener.onCutsceneEnded(session, ended.id, ended.skipped));
			return true;
		}
		return false;
	}

	@Override
	synchronized void reset() {
		keybinds.clear();
		loops.clear();
		letterbox = null;
		lastPress.clear();
	}
}
