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

public final class CameraFollow implements Message {
	public final String player;
	public final int mode;
	public final int distance;
	public final boolean smooth;

	public CameraFollow(String player, int mode, int distance, boolean smooth) {
		this.player = player == null ? "" : player;
		this.mode = mode;
		this.distance = distance;
		this.smooth = smooth;
	}

	@Override
	public int type() {
		return TehoProtocol.CAMERA_FOLLOW;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, player);
		out.uint(2, mode);
		out.uint(3, distance);
		out.bool(4, smooth);
	}

	static CameraFollow read(WireReader in) {
		String player = "";
		int mode = 0;
		int distance = 0;
		boolean smooth = false;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					player = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					mode = in.readUint();
					break;
				case 3:
					distance = in.readUint();
					break;
				case 4:
					smooth = in.readBool();
					break;
				default:
					in.skip();
			}
		}
		return new CameraFollow(player, mode, distance, smooth);
	}

	@Override
	public String toString() {
		return "CameraFollow{" + "player=" + player + ", " + "mode=" + mode + ", " + "distance=" + distance + ", " + "smooth=" + smooth + "}";
	}
}
