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

public final class LagCell {
	public final int chunkX;
	public final int chunkZ;
	public final int score;
	public final String label;

	public LagCell(int chunkX, int chunkZ, int score, String label) {
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.score = score;
		this.label = label == null ? "" : label;
	}

	public void write(WireWriter out) {
		out.sint(1, chunkX);
		out.sint(2, chunkZ);
		out.uint(3, score);
		out.string(4, label);
	}

	public static LagCell read(WireReader in) {
		int chunkX = 0;
		int chunkZ = 0;
		int score = 0;
		String label = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					chunkX = in.readSint();
					break;
				case 2:
					chunkZ = in.readSint();
					break;
				case 3:
					score = in.readUint();
					break;
				case 4:
					label = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new LagCell(chunkX, chunkZ, score, label);
	}

	@Override
	public String toString() {
		return "LagCell{" + "chunkX=" + chunkX + ", " + "chunkZ=" + chunkZ + ", " + "score=" + score + ", " + "label=" + label + "}";
	}
}
