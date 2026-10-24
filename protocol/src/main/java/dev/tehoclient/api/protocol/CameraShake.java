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

public final class CameraShake implements Message {
	public final int intensity;
	public final int durationMs;
	public final int frequency;

	public CameraShake(int intensity, int durationMs, int frequency) {
		this.intensity = intensity;
		this.durationMs = durationMs;
		this.frequency = frequency;
	}

	@Override
	public int type() {
		return TehoProtocol.CAMERA_SHAKE;
	}

	@Override
	public void write(WireWriter out) {
		out.uint(1, intensity);
		out.uint(2, durationMs);
		out.uint(3, frequency);
	}

	static CameraShake read(WireReader in) {
		int intensity = 0;
		int durationMs = 0;
		int frequency = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					intensity = in.readUint();
					break;
				case 2:
					durationMs = in.readUint();
					break;
				case 3:
					frequency = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new CameraShake(intensity, durationMs, frequency);
	}

	@Override
	public String toString() {
		return "CameraShake{" + "intensity=" + intensity + ", " + "durationMs=" + durationMs + ", " + "frequency=" + frequency + "}";
	}
}
