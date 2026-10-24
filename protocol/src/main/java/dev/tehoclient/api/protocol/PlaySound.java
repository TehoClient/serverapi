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

public final class PlaySound implements Message {
	public final String id;
	public final byte[] asset;
	public final String sound;
	public final int x;
	public final int y;
	public final int z;
	public final String entity;
	public final boolean positional;
	public final int volume;
	public final int pitch;
	public final boolean loop;
	public final int fadeInMs;
	public final boolean music;

	public PlaySound(String id, byte[] asset, String sound, int x, int y, int z, String entity, boolean positional, int volume, int pitch, boolean loop, int fadeInMs, boolean music) {
		this.id = id == null ? "" : id;
		this.asset = asset == null ? new byte[0] : asset;
		this.sound = sound == null ? "" : sound;
		this.x = x;
		this.y = y;
		this.z = z;
		this.entity = entity == null ? "" : entity;
		this.positional = positional;
		this.volume = volume;
		this.pitch = pitch;
		this.loop = loop;
		this.fadeInMs = fadeInMs;
		this.music = music;
	}

	@Override
	public int type() {
		return TehoProtocol.PLAY_SOUND;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, id);
		out.bytes(2, asset);
		out.string(3, sound);
		out.sint(4, x);
		out.sint(5, y);
		out.sint(6, z);
		out.string(7, entity);
		out.bool(8, positional);
		out.uint(9, volume);
		out.uint(10, pitch);
		out.bool(11, loop);
		out.uint(12, fadeInMs);
		out.bool(13, music);
	}

	static PlaySound read(WireReader in) {
		String id = "";
		byte[] asset = new byte[0];
		String sound = "";
		int x = 0;
		int y = 0;
		int z = 0;
		String entity = "";
		boolean positional = false;
		int volume = 0;
		int pitch = 0;
		boolean loop = false;
		int fadeInMs = 0;
		boolean music = false;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					asset = in.readBytes(Chunking.MAX_STREAM);
					break;
				case 3:
					sound = in.readString(TehoProtocol.MAX_STRING);
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
					entity = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 8:
					positional = in.readBool();
					break;
				case 9:
					volume = in.readUint();
					break;
				case 10:
					pitch = in.readUint();
					break;
				case 11:
					loop = in.readBool();
					break;
				case 12:
					fadeInMs = in.readUint();
					break;
				case 13:
					music = in.readBool();
					break;
				default:
					in.skip();
			}
		}
		return new PlaySound(id, asset, sound, x, y, z, entity, positional, volume, pitch, loop, fadeInMs, music);
	}

	@Override
	public String toString() {
		return "PlaySound{" + "id=" + id + ", " + "asset=" + asset.length + "B" + ", " + "sound=" + sound + ", " + "x=" + x + ", " + "y=" + y + ", " + "z=" + z + ", " + "entity=" + entity + ", " + "positional=" + positional + ", " + "volume=" + volume + ", " + "pitch=" + pitch + ", " + "loop=" + loop + ", " + "fadeInMs=" + fadeInMs + ", " + "music=" + music + "}";
	}
}
