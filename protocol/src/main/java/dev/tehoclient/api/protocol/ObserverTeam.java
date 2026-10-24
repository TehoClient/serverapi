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

public final class ObserverTeam {
	public final String name;
	public final int color;
	public final int score;
	public final List<ObserverPlayer> players;

	public ObserverTeam(String name, int color, int score, List<ObserverPlayer> players) {
		this.name = name == null ? "" : name;
		this.color = color;
		this.score = score;
		this.players = players == null ? Collections.<ObserverPlayer>emptyList() : Collections.unmodifiableList(new ArrayList<>(players));
	}

	public void write(WireWriter out) {
		out.string(1, name);
		out.fixed32(2, color);
		out.sint(3, score);
		for (ObserverPlayer item : players) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(4, nested.toByteArray());
		}
	}

	public static ObserverTeam read(WireReader in) {
		String name = "";
		int color = 0;
		int score = 0;
		List<ObserverPlayer> players = new ArrayList<>();
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					name = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					color = in.readFixed32();
					break;
				case 3:
					score = in.readSint();
					break;
				case 4:
					byte[] playersItem = in.readBytes(Chunking.MAX_STREAM);
					if (players.size() < TehoProtocol.MAX_LIST) {
						players.add(ObserverPlayer.read(new WireReader(playersItem)));
					}
					break;
				default:
					in.skip();
			}
		}
		return new ObserverTeam(name, color, score, players);
	}

	@Override
	public String toString() {
		return "ObserverTeam{" + "name=" + name + ", " + "color=" + Integer.toHexString(color) + ", " + "score=" + score + ", " + "players=" + players + "}";
	}
}
