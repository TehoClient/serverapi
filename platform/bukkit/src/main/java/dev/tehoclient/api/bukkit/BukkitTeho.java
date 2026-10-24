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

package dev.tehoclient.api.bukkit;

import java.lang.reflect.Method;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import dev.tehoclient.api.TehoApi;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.waypoint.Waypoint;

public final class BukkitTeho {
	private BukkitTeho() {}

	public static Optional<TehoPlayer> player(Player player) {
		return player == null ? Optional.<TehoPlayer>empty() : TehoApi.get().player(player.getUniqueId());
	}

	public static boolean isTeho(Player player) {
		return player(player).isPresent();
	}

	public static Waypoint.Builder waypoint(String id, Location location) {
		return Waypoint.builder(id)
				.at(location.getBlockX(), location.getBlockY(), location.getBlockZ())
				.dimension(dimensionOf(location.getWorld()));
	}

	public static String dimensionOf(World world) {
		if (world == null) {
			return "";
		}
		try {
			Method getKey = world.getClass().getMethod("getKey");
			Object key = getKey.invoke(world);
			Method path = key.getClass().getMethod("getKey");
			return String.valueOf(path.invoke(key));
		} catch (ReflectiveOperationException | RuntimeException absent) {
			switch (world.getEnvironment()) {
				case NETHER:
					return "the_nether";
				case THE_END:
					return "the_end";
				default:
					return "overworld";
			}
		}
	}
}
