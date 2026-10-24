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

package dev.tehoclient.api.broadcast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dev.tehoclient.api.Units;
import dev.tehoclient.api.protocol.ObserverPlayer;
import dev.tehoclient.api.protocol.ObserverTeam;

public final class Observer {
	private final dev.tehoclient.api.protocol.ObserverHud wire;

	private Observer(dev.tehoclient.api.protocol.ObserverHud wire) {
		this.wire = wire;
	}

	public static Builder builder(String title) {
		return new Builder(title);
	}

	public static Observer off() {
		return new Observer(new dev.tehoclient.api.protocol.ObserverHud(false, "", "", null));
	}

	public dev.tehoclient.api.protocol.ObserverHud wire() {
		return wire;
	}

	public static final class TeamBuilder {
		private final String name;
		private final int argb;
		private int score;
		private final List<ObserverPlayer> players = new ArrayList<>();

		private TeamBuilder(String name, int argb) {
			this.name = name == null ? "" : name;
			this.argb = Units.argb(argb);
		}

		public TeamBuilder score(int score) {
			this.score = score;
			return this;
		}

		public TeamBuilder player(UUID id, String name, double health, boolean alive, int kills, String heldItem) {
			players.add(new ObserverPlayer(Units.id(id), name, Math.max(0, Math.min(100, (int) Math.round(health * 100))), alive,
					Math.max(0, kills), Units.item(heldItem)));
			return this;
		}

		ObserverTeam build() {
			return new ObserverTeam(name, argb, score, players);
		}
	}

	public static final class Builder {
		private final String title;
		private String subtitle = "";
		private final List<TeamBuilder> teams = new ArrayList<>();

		private Builder(String title) {
			this.title = title == null ? "" : title;
		}

		public Builder subtitle(String subtitle) {
			this.subtitle = subtitle == null ? "" : subtitle;
			return this;
		}

		public TeamBuilder team(String name, int argb) {
			TeamBuilder team = new TeamBuilder(name, argb);
			teams.add(team);
			return team;
		}

		public Observer build() {
			List<ObserverTeam> wire = new ArrayList<>();
			for (TeamBuilder team : teams) {
				wire.add(team.build());
			}
			return new Observer(new dev.tehoclient.api.protocol.ObserverHud(true, title, subtitle, wire));
		}
	}
}
