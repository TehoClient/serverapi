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

public final class Keyframe {
	public final int x;
	public final int y;
	public final int z;
	public final int yaw;
	public final int pitch;
	public final int roll;
	public final int fov;
	public final int atMs;
	public final int ease;

	public Keyframe(int x, int y, int z, int yaw, int pitch, int roll, int fov, int atMs, int ease) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.roll = roll;
		this.fov = fov;
		this.atMs = atMs;
		this.ease = ease;
	}

	public void write(WireWriter out) {
		out.sint(1, x);
		out.sint(2, y);
		out.sint(3, z);
		out.sint(4, yaw);
		out.sint(5, pitch);
		out.sint(6, roll);
		out.uint(7, fov);
		out.uint(8, atMs);
		out.uint(9, ease);
	}

	public static Keyframe read(WireReader in) {
		int x = 0;
		int y = 0;
		int z = 0;
		int yaw = 0;
		int pitch = 0;
		int roll = 0;
		int fov = 0;
		int atMs = 0;
		int ease = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					x = in.readSint();
					break;
				case 2:
					y = in.readSint();
					break;
				case 3:
					z = in.readSint();
					break;
				case 4:
					yaw = in.readSint();
					break;
				case 5:
					pitch = in.readSint();
					break;
				case 6:
					roll = in.readSint();
					break;
				case 7:
					fov = in.readUint();
					break;
				case 8:
					atMs = in.readUint();
					break;
				case 9:
					ease = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new Keyframe(x, y, z, yaw, pitch, roll, fov, atMs, ease);
	}

	@Override
	public String toString() {
		return "Keyframe{" + "x=" + x + ", " + "y=" + y + ", " + "z=" + z + ", " + "yaw=" + yaw + ", " + "pitch=" + pitch + ", " + "roll=" + roll + ", " + "fov=" + fov + ", " + "atMs=" + atMs + ", " + "ease=" + ease + "}";
	}
}
