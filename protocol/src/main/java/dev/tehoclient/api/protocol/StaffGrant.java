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

public final class StaffGrant implements Message {
	public final List<String> tools;
	public final String reason;
	public final List<XrayBlock> xrayBlocks;
	public final int radius;
	public final List<XrayBlock> storageBlocks;
	public final List<XrayBlock> entityTypes;

	public StaffGrant(List<String> tools, String reason, List<XrayBlock> xrayBlocks, int radius, List<XrayBlock> storageBlocks, List<XrayBlock> entityTypes) {
		this.tools = tools == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(tools));
		this.reason = reason == null ? "" : reason;
		this.xrayBlocks = xrayBlocks == null ? Collections.<XrayBlock>emptyList() : Collections.unmodifiableList(new ArrayList<>(xrayBlocks));
		this.radius = radius;
		this.storageBlocks = storageBlocks == null ? Collections.<XrayBlock>emptyList() : Collections.unmodifiableList(new ArrayList<>(storageBlocks));
		this.entityTypes = entityTypes == null ? Collections.<XrayBlock>emptyList() : Collections.unmodifiableList(new ArrayList<>(entityTypes));
	}

	@Override
	public int type() {
		return TehoProtocol.STAFF_GRANT;
	}

	@Override
	public void write(WireWriter out) {
		for (String item : tools) {
			out.string(1, item);
		}
		out.string(2, reason);
		for (XrayBlock item : xrayBlocks) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(3, nested.toByteArray());
		}
		out.uint(4, radius);
		for (XrayBlock item : storageBlocks) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(5, nested.toByteArray());
		}
		for (XrayBlock item : entityTypes) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(6, nested.toByteArray());
		}
	}

	static StaffGrant read(WireReader in) {
		List<String> tools = new ArrayList<>();
		String reason = "";
		List<XrayBlock> xrayBlocks = new ArrayList<>();
		int radius = 0;
		List<XrayBlock> storageBlocks = new ArrayList<>();
		List<XrayBlock> entityTypes = new ArrayList<>();
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					String toolsItem = in.readString(TehoProtocol.MAX_STRING);
					if (tools.size() < TehoProtocol.MAX_LIST) {
						tools.add(toolsItem);
					}
					break;
				case 2:
					reason = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					byte[] xrayBlocksItem = in.readBytes(Chunking.MAX_STREAM);
					if (xrayBlocks.size() < TehoProtocol.MAX_LIST) {
						xrayBlocks.add(XrayBlock.read(new WireReader(xrayBlocksItem)));
					}
					break;
				case 4:
					radius = in.readUint();
					break;
				case 5:
					byte[] storageBlocksItem = in.readBytes(Chunking.MAX_STREAM);
					if (storageBlocks.size() < TehoProtocol.MAX_LIST) {
						storageBlocks.add(XrayBlock.read(new WireReader(storageBlocksItem)));
					}
					break;
				case 6:
					byte[] entityTypesItem = in.readBytes(Chunking.MAX_STREAM);
					if (entityTypes.size() < TehoProtocol.MAX_LIST) {
						entityTypes.add(XrayBlock.read(new WireReader(entityTypesItem)));
					}
					break;
				default:
					in.skip();
			}
		}
		return new StaffGrant(tools, reason, xrayBlocks, radius, storageBlocks, entityTypes);
	}

	@Override
	public String toString() {
		return "StaffGrant{" + "tools=" + tools + ", " + "reason=" + reason + ", " + "xrayBlocks=" + xrayBlocks + ", " + "radius=" + radius + ", " + "storageBlocks=" + storageBlocks + ", " + "entityTypes=" + entityTypes + "}";
	}
}
