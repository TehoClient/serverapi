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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Cutscene implements Message {
	public final String id;
	public final List<Keyframe> frames;
	public final boolean skippable;
	public final boolean hideHud;
	public final boolean letterbox;
	public final boolean loop;
	public final String dimension;

	public Cutscene(String id, List<Keyframe> frames, boolean skippable, boolean hideHud, boolean letterbox, boolean loop, String dimension) {
		this.id = id == null ? "" : id;
		this.frames = frames == null ? Collections.<Keyframe>emptyList() : Collections.unmodifiableList(new ArrayList<>(frames));
		this.skippable = skippable;
		this.hideHud = hideHud;
		this.letterbox = letterbox;
		this.loop = loop;
		this.dimension = dimension == null ? "" : dimension;
	}

	@Override
	public int type() {
		return TehoProtocol.CUTSCENE;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, id);
		for (Keyframe item : frames) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(2, nested.toByteArray());
		}
		out.bool(3, skippable);
		out.bool(4, hideHud);
		out.bool(5, letterbox);
		out.bool(6, loop);
		out.string(7, dimension);
	}

	static Cutscene read(WireReader in) {
		String id = "";
		List<Keyframe> frames = new ArrayList<>();
		boolean skippable = false;
		boolean hideHud = false;
		boolean letterbox = false;
		boolean loop = false;
		String dimension = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					byte[] framesItem = in.readBytes(Chunking.MAX_STREAM);
					if (frames.size() < TehoProtocol.MAX_LIST) {
						frames.add(Keyframe.read(new WireReader(framesItem)));
					}
					break;
				case 3:
					skippable = in.readBool();
					break;
				case 4:
					hideHud = in.readBool();
					break;
				case 5:
					letterbox = in.readBool();
					break;
				case 6:
					loop = in.readBool();
					break;
				case 7:
					dimension = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new Cutscene(id, frames, skippable, hideHud, letterbox, loop, dimension);
	}

	@Override
	public String toString() {
		return "Cutscene{" + "id=" + id + ", " + "frames=" + frames + ", " + "skippable=" + skippable + ", " + "hideHud=" + hideHud + ", " + "letterbox=" + letterbox + ", " + "loop=" + loop + ", " + "dimension=" + dimension + "}";
	}
}
