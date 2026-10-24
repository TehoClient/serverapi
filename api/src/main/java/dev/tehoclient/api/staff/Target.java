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

package dev.tehoclient.api.staff;

import java.util.UUID;

public final class Target {
	public enum Kind {
		NONE, PLAYER, BLOCK, ENTITY
	}

	private final Kind kind;
	private final UUID id;
	private final int x;
	private final int y;
	private final int z;

	private Target(Kind kind, UUID id, int x, int y, int z) {
		this.kind = kind;
		this.id = id;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static Target none() {
		return new Target(Kind.NONE, null, 0, 0, 0);
	}

	public static Target player(UUID id) {
		return new Target(Kind.PLAYER, id, 0, 0, 0);
	}

	public static Target entity(UUID id) {
		return new Target(Kind.ENTITY, id, 0, 0, 0);
	}

	public static Target block(int x, int y, int z) {
		return new Target(Kind.BLOCK, null, x, y, z);
	}

	public static Target of(int kind, String id, int x, int y, int z) {
		UUID uuid = null;
		try {
			uuid = id == null || id.isEmpty() ? null : UUID.fromString(id);
		} catch (IllegalArgumentException malformed) {
			uuid = null;
		}
		switch (kind) {
			case 1:
				return uuid == null ? none() : player(uuid);
			case 2:
				return block(x, y, z);
			case 3:
				return uuid == null ? none() : entity(uuid);
			default:
				return none();
		}
	}

	public Kind kind() {
		return kind;
	}

	public UUID id() {
		return id;
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

	public int z() {
		return z;
	}

	@Override
	public String toString() {
		switch (kind) {
			case PLAYER:
			case ENTITY:
				return kind.name().toLowerCase(java.util.Locale.ROOT) + " " + id;
			case BLOCK:
				return "block " + x + " " + y + " " + z;
			default:
				return "none";
		}
	}
}
