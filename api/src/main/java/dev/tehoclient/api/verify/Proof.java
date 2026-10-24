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

package dev.tehoclient.api.verify;

import java.util.UUID;

public final class Proof {
	public enum Kind {
		IDENTITY, INTEGRITY
	}

	private final Kind kind;
	private final boolean valid;
	private final UUID player;
	private final String name;
	private final String build;
	private final long issuedAt;
	private final long expiresAt;
	private final String error;
	private final String token;

	public Proof(Kind kind, boolean valid, UUID player, String name, String build, long issuedAt, long expiresAt, String error, String token) {
		this.kind = kind;
		this.valid = valid;
		this.player = player;
		this.name = name == null ? "" : name;
		this.build = build == null ? "" : build;
		this.issuedAt = issuedAt;
		this.expiresAt = expiresAt;
		this.error = error == null ? "" : error;
		this.token = token == null ? "" : token;
	}

	public static Proof failed(Kind kind, UUID player, String error) {
		return new Proof(kind, false, player, "", "", 0, 0, error, "");
	}

	public Kind kind() {
		return kind;
	}

	public boolean valid() {
		return valid;
	}

	public UUID player() {
		return player;
	}

	public String name() {
		return name;
	}

	public String build() {
		return build;
	}

	public long issuedAt() {
		return issuedAt;
	}

	public long expiresAt() {
		return expiresAt;
	}

	public String error() {
		return error;
	}

	public String token() {
		return token;
	}

	@Override
	public String toString() {
		return "Proof{" + kind + ", " + (valid ? "valid" : "invalid: " + error) + ", " + player + (build.isEmpty() ? "" : ", " + build) + "}";
	}
}
