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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dev.tehoclient.api.protocol.BorderStyle;
import dev.tehoclient.api.protocol.EntityStyleClear;
import dev.tehoclient.api.protocol.Event;
import dev.tehoclient.api.protocol.FireColor;
import dev.tehoclient.api.protocol.Message;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.protocol.WorldRemove;
import dev.tehoclient.api.world.Beam;
import dev.tehoclient.api.world.EntityStyle;
import dev.tehoclient.api.world.Hologram;
import dev.tehoclient.api.world.Shape;
import dev.tehoclient.api.world.WorldApi;

final class WorldPart extends Part implements WorldApi {
	static final int MAX_EACH = 256;

	private final Map<String, Beam> beams = new LinkedHashMap<>();
	private final Map<String, Hologram> holograms = new LinkedHashMap<>();
	private final Map<String, Shape> shapes = new LinkedHashMap<>();
	private final Map<String, EntityStyle> styles = new LinkedHashMap<>();
	private final Map<String, FireColor> fires = new LinkedHashMap<>();
	private BorderStyle border;

	WorldPart(TehoSession session) {
		super(session);
	}

	private static <T> void put(Map<String, T> map, String id, T value, String what) {
		if (!map.containsKey(id) && map.size() >= MAX_EACH) {
			throw new IllegalStateException("At most " + MAX_EACH + " " + what + " per player");
		}
		map.put(id, value);
	}

	@Override
	public synchronized boolean beam(Beam beam) {
		put(beams, beam.id(), beam, "beams");
		return send(TehoProtocol.WORLD, beam.wire());
	}

	@Override
	public synchronized boolean hologram(Hologram hologram) {
		put(holograms, hologram.id(), hologram, "holograms");
		return send(TehoProtocol.WORLD, hologram.wire(), hologram.image());
	}

	@Override
	public synchronized boolean shape(Shape shape) {
		put(shapes, shape.id(), shape, "shapes");
		return send(TehoProtocol.WORLD, shape.wire());
	}

	@Override
	public synchronized boolean removeBeams(String... ids) {
		return remove(beams, TehoProtocol.WORLD_BEAM, ids);
	}

	@Override
	public synchronized boolean removeHolograms(String... ids) {
		return remove(holograms, TehoProtocol.WORLD_HOLOGRAM, ids);
	}

	@Override
	public synchronized boolean removeShapes(String... ids) {
		return remove(shapes, TehoProtocol.WORLD_SHAPE, ids);
	}

	private boolean remove(Map<String, ?> map, int kind, String... ids) {
		if (ids.length == 0) {
			map.clear();
		} else {
			for (String id : ids) {
				map.remove(id);
			}
		}
		return send(TehoProtocol.WORLD, new WorldRemove(kind, Arrays.asList(ids)));
	}

	@Override
	public synchronized boolean clear() {
		beams.clear();
		holograms.clear();
		shapes.clear();
		return send(TehoProtocol.WORLD, new WorldRemove(TehoProtocol.WORLD_ALL, new ArrayList<String>()));
	}

	@Override
	public synchronized boolean style(EntityStyle style) {
		put(styles, style.wire().entity, style, "entity styles");
		return send(TehoProtocol.WORLD, style.wire());
	}

	@Override
	public synchronized boolean clearStyles(UUID... entities) {
		List<String> ids = new ArrayList<>();
		for (UUID entity : entities) {
			ids.add(entity.toString());
			styles.remove(entity.toString());
		}
		if (entities.length == 0) {
			styles.clear();
		}
		return send(TehoProtocol.WORLD, new EntityStyleClear(ids));
	}

	@Override
	public synchronized boolean borderColor(int argb, boolean hideWall) {
		border = new BorderStyle(argb, hideWall, false);
		return send(TehoProtocol.WORLD, border);
	}

	@Override
	public synchronized boolean resetBorder() {
		border = null;
		return send(TehoProtocol.WORLD, new BorderStyle(0, false, true));
	}

	@Override
	public synchronized boolean fireColor(UUID entity, int argb) {
		String id = entity == null ? "" : entity.toString();
		FireColor fire = new FireColor(id, dev.tehoclient.api.Units.argb(argb), false);
		put(fires, id, fire, "fire colours");
		return send(TehoProtocol.WORLD, fire);
	}

	@Override
	public synchronized boolean resetFire(UUID entity) {
		String id = entity == null ? "" : entity.toString();
		if (id.isEmpty()) {
			fires.clear();
		} else {
			fires.remove(id);
		}
		return send(TehoProtocol.WORLD, new FireColor(id, 0, true));
	}

	@Override
	synchronized void reapply() {
		for (Beam beam : beams.values()) {
			send(TehoProtocol.WORLD, beam.wire());
		}
		for (Hologram hologram : holograms.values()) {
			send(TehoProtocol.WORLD, hologram.wire(), hologram.image());
		}
		for (Shape shape : shapes.values()) {
			send(TehoProtocol.WORLD, shape.wire());
		}
		for (EntityStyle style : styles.values()) {
			send(TehoProtocol.WORLD, style.wire());
		}
		for (FireColor fire : fires.values()) {
			send(TehoProtocol.WORLD, fire);
		}
		if (border != null) {
			send(TehoProtocol.WORLD, border);
		}
	}

	@Override
	boolean receive(Message message) {
		if (message instanceof Event && "hologram.click".equals(((Event) message).kind)) {
			String id = ((Event) message).target;
			boolean known;
			synchronized (this) {
				Hologram hologram = holograms.get(id);
				known = hologram != null && hologram.wire().clickable;
			}
			if (known) {
				core().fire(listener -> listener.onHologramClick(session, id));
			}
			return true;
		}
		return false;
	}

	@Override
	synchronized void reset() {
		beams.clear();
		holograms.clear();
		shapes.clear();
		styles.clear();
		fires.clear();
		border = null;
	}
}
