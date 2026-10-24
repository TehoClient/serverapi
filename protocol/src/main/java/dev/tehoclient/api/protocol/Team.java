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

public final class Team implements Message {
	public final String id;
	public final String name;
	public final int color;
	public final List<String> members;
	public final boolean markers;
	public final boolean throughWalls;
	public final boolean health;
	public final boolean compass;

	public Team(String id, String name, int color, List<String> members, boolean markers, boolean throughWalls, boolean health, boolean compass) {
		this.id = id == null ? "" : id;
		this.name = name == null ? "" : name;
		this.color = color;
		this.members = members == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(members));
		this.markers = markers;
		this.throughWalls = throughWalls;
		this.health = health;
		this.compass = compass;
	}

	@Override
	public int type() {
		return TehoProtocol.TEAM;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, id);
		out.string(2, name);
		out.fixed32(3, color);
		for (String item : members) {
			out.string(4, item);
		}
		out.bool(5, markers);
		out.bool(6, throughWalls);
		out.bool(7, health);
		out.bool(8, compass);
	}

	static Team read(WireReader in) {
		String id = "";
		String name = "";
		int color = 0;
		List<String> members = new ArrayList<>();
		boolean markers = false;
		boolean throughWalls = false;
		boolean health = false;
		boolean compass = false;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					name = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					color = in.readFixed32();
					break;
				case 4:
					String membersItem = in.readString(TehoProtocol.MAX_STRING);
					if (members.size() < TehoProtocol.MAX_LIST) {
						members.add(membersItem);
					}
					break;
				case 5:
					markers = in.readBool();
					break;
				case 6:
					throughWalls = in.readBool();
					break;
				case 7:
					health = in.readBool();
					break;
				case 8:
					compass = in.readBool();
					break;
				default:
					in.skip();
			}
		}
		return new Team(id, name, color, members, markers, throughWalls, health, compass);
	}

	@Override
	public String toString() {
		return "Team{" + "id=" + id + ", " + "name=" + name + ", " + "color=" + Integer.toHexString(color) + ", " + "members=" + members + ", " + "markers=" + markers + ", " + "throughWalls=" + throughWalls + ", " + "health=" + health + ", " + "compass=" + compass + "}";
	}
}
