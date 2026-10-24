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

package dev.tehoclient.api.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EntityStyle implements Message {
	public final String entity;
	public final int glow;
	public final String name;
	public final int scale;
	public final boolean rainbow;
	public final List<String> hide;
	public final int ttlMs;
	public final boolean hideName;

	public EntityStyle(String entity, int glow, String name, int scale, boolean rainbow, List<String> hide, int ttlMs, boolean hideName) {
		this.entity = entity == null ? "" : entity;
		this.glow = glow;
		this.name = name == null ? "" : name;
		this.scale = scale;
		this.rainbow = rainbow;
		this.hide = hide == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(hide));
		this.ttlMs = ttlMs;
		this.hideName = hideName;
	}

	@Override
	public int type() {
		return TehoProtocol.ENTITY_STYLE;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, entity);
		out.fixed32(2, glow);
		out.string(3, name);
		out.uint(4, scale);
		out.bool(5, rainbow);
		for (String item : hide) {
			out.string(6, item);
		}
		out.uint(7, ttlMs);
		out.bool(8, hideName);
	}

	static EntityStyle read(WireReader in) {
		String entity = "";
		int glow = 0;
		String name = "";
		int scale = 0;
		boolean rainbow = false;
		List<String> hide = new ArrayList<>();
		int ttlMs = 0;
		boolean hideName = false;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					entity = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					glow = in.readFixed32();
					break;
				case 3:
					name = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					scale = in.readUint();
					break;
				case 5:
					rainbow = in.readBool();
					break;
				case 6:
					String hideItem = in.readString(TehoProtocol.MAX_STRING);
					if (hide.size() < TehoProtocol.MAX_LIST) {
						hide.add(hideItem);
					}
					break;
				case 7:
					ttlMs = in.readUint();
					break;
				case 8:
					hideName = in.readBool();
					break;
				default:
					in.skip();
			}
		}
		return new EntityStyle(entity, glow, name, scale, rainbow, hide, ttlMs, hideName);
	}

	@Override
	public String toString() {
		return "EntityStyle{" + "entity=" + entity + ", " + "glow=" + Integer.toHexString(glow) + ", " + "name=" + name + ", " + "scale=" + scale + ", " + "rainbow=" + rainbow + ", " + "hide=" + hide + ", " + "ttlMs=" + ttlMs + ", " + "hideName=" + hideName + "}";
	}
}
