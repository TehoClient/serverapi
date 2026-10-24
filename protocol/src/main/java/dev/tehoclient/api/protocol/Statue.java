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

public final class Statue implements Message {
	public final String id;
	public final String player;
	public final String name;
	public final int x;
	public final int y;
	public final int z;
	public final int yaw;
	public final int pose;
	public final int scale;
	public final String dimension;
	public final String label;
	public final String emote;

	public Statue(String id, String player, String name, int x, int y, int z, int yaw, int pose, int scale, String dimension, String label, String emote) {
		this.id = id == null ? "" : id;
		this.player = player == null ? "" : player;
		this.name = name == null ? "" : name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pose = pose;
		this.scale = scale;
		this.dimension = dimension == null ? "" : dimension;
		this.label = label == null ? "" : label;
		this.emote = emote == null ? "" : emote;
	}

	@Override
	public int type() {
		return TehoProtocol.STATUE_SET;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, id);
		out.string(2, player);
		out.string(3, name);
		out.sint(4, x);
		out.sint(5, y);
		out.sint(6, z);
		out.sint(7, yaw);
		out.uint(8, pose);
		out.uint(9, scale);
		out.string(10, dimension);
		out.string(11, label);
		out.string(12, emote);
	}

	static Statue read(WireReader in) {
		String id = "";
		String player = "";
		String name = "";
		int x = 0;
		int y = 0;
		int z = 0;
		int yaw = 0;
		int pose = 0;
		int scale = 0;
		String dimension = "";
		String label = "";
		String emote = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					player = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					name = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					x = in.readSint();
					break;
				case 5:
					y = in.readSint();
					break;
				case 6:
					z = in.readSint();
					break;
				case 7:
					yaw = in.readSint();
					break;
				case 8:
					pose = in.readUint();
					break;
				case 9:
					scale = in.readUint();
					break;
				case 10:
					dimension = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 11:
					label = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 12:
					emote = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new Statue(id, player, name, x, y, z, yaw, pose, scale, dimension, label, emote);
	}

	@Override
	public String toString() {
		return "Statue{" + "id=" + id + ", " + "player=" + player + ", " + "name=" + name + ", " + "x=" + x + ", " + "y=" + y + ", " + "z=" + z + ", " + "yaw=" + yaw + ", " + "pose=" + pose + ", " + "scale=" + scale + ", " + "dimension=" + dimension + ", " + "label=" + label + ", " + "emote=" + emote + "}";
	}
}
