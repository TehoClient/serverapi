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

public final class NpcLook implements Message {
	public final String npc;
	public final int mode;
	public final List<NpcSlot> slots;

	public NpcLook(String npc, int mode, List<NpcSlot> slots) {
		this.npc = npc == null ? "" : npc;
		this.mode = mode;
		this.slots = slots == null ? Collections.<NpcSlot>emptyList() : Collections.unmodifiableList(new ArrayList<>(slots));
	}

	@Override
	public int type() {
		return TehoProtocol.NPC_LOOK;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, npc);
		out.uint(2, mode);
		for (NpcSlot item : slots) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(3, nested.toByteArray());
		}
	}

	static NpcLook read(WireReader in) {
		String npc = "";
		int mode = 0;
		List<NpcSlot> slots = new ArrayList<>();
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					npc = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					mode = in.readUint();
					break;
				case 3:
					byte[] slotsItem = in.readBytes(Chunking.MAX_STREAM);
					if (slots.size() < TehoProtocol.MAX_LIST) {
						slots.add(NpcSlot.read(new WireReader(slotsItem)));
					}
					break;
				default:
					in.skip();
			}
		}
		return new NpcLook(npc, mode, slots);
	}

	@Override
	public String toString() {
		return "NpcLook{" + "npc=" + npc + ", " + "mode=" + mode + ", " + "slots=" + slots + "}";
	}
}
