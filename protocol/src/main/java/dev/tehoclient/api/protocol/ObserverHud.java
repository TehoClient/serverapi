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

public final class ObserverHud implements Message {
	public final boolean on;
	public final String title;
	public final String subtitle;
	public final List<ObserverTeam> teams;

	public ObserverHud(boolean on, String title, String subtitle, List<ObserverTeam> teams) {
		this.on = on;
		this.title = title == null ? "" : title;
		this.subtitle = subtitle == null ? "" : subtitle;
		this.teams = teams == null ? Collections.<ObserverTeam>emptyList() : Collections.unmodifiableList(new ArrayList<>(teams));
	}

	@Override
	public int type() {
		return TehoProtocol.OBSERVER_HUD;
	}

	@Override
	public void write(WireWriter out) {
		out.bool(1, on);
		out.string(2, title);
		out.string(3, subtitle);
		for (ObserverTeam item : teams) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(4, nested.toByteArray());
		}
	}

	static ObserverHud read(WireReader in) {
		boolean on = false;
		String title = "";
		String subtitle = "";
		List<ObserverTeam> teams = new ArrayList<>();
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					on = in.readBool();
					break;
				case 2:
					title = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					subtitle = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					byte[] teamsItem = in.readBytes(Chunking.MAX_STREAM);
					if (teams.size() < TehoProtocol.MAX_LIST) {
						teams.add(ObserverTeam.read(new WireReader(teamsItem)));
					}
					break;
				default:
					in.skip();
			}
		}
		return new ObserverHud(on, title, subtitle, teams);
	}

	@Override
	public String toString() {
		return "ObserverHud{" + "on=" + on + ", " + "title=" + title + ", " + "subtitle=" + subtitle + ", " + "teams=" + teams + "}";
	}
}
