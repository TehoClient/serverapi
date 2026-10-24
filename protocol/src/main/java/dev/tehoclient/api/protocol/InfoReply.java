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

public final class InfoReply implements Message {
	public final int requestId;
	public final List<String> granted;
	public final String input;
	public final int fps;
	public final int frameP50;
	public final int frameP99;
	public final int renderDistance;
	public final List<ModEntry> mods;
	public final List<String> tehoModules;
	public final String locale;
	public final int guiScale;
	public final int width;
	public final int height;
	public final String os;
	public final int memoryMb;

	public InfoReply(int requestId, List<String> granted, String input, int fps, int frameP50, int frameP99, int renderDistance, List<ModEntry> mods, List<String> tehoModules, String locale, int guiScale, int width, int height, String os, int memoryMb) {
		this.requestId = requestId;
		this.granted = granted == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(granted));
		this.input = input == null ? "" : input;
		this.fps = fps;
		this.frameP50 = frameP50;
		this.frameP99 = frameP99;
		this.renderDistance = renderDistance;
		this.mods = mods == null ? Collections.<ModEntry>emptyList() : Collections.unmodifiableList(new ArrayList<>(mods));
		this.tehoModules = tehoModules == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(tehoModules));
		this.locale = locale == null ? "" : locale;
		this.guiScale = guiScale;
		this.width = width;
		this.height = height;
		this.os = os == null ? "" : os;
		this.memoryMb = memoryMb;
	}

	@Override
	public int type() {
		return TehoProtocol.INFO_REPLY;
	}

	@Override
	public void write(WireWriter out) {
		out.uint(1, requestId);
		for (String item : granted) {
			out.string(2, item);
		}
		out.string(3, input);
		out.uint(4, fps);
		out.uint(5, frameP50);
		out.uint(6, frameP99);
		out.uint(7, renderDistance);
		for (ModEntry item : mods) {
			WireWriter nested = new WireWriter();
			item.write(nested);
			out.bytes(8, nested.toByteArray());
		}
		for (String item : tehoModules) {
			out.string(9, item);
		}
		out.string(10, locale);
		out.uint(11, guiScale);
		out.uint(12, width);
		out.uint(13, height);
		out.string(14, os);
		out.uint(15, memoryMb);
	}

	static InfoReply read(WireReader in) {
		int requestId = 0;
		List<String> granted = new ArrayList<>();
		String input = "";
		int fps = 0;
		int frameP50 = 0;
		int frameP99 = 0;
		int renderDistance = 0;
		List<ModEntry> mods = new ArrayList<>();
		List<String> tehoModules = new ArrayList<>();
		String locale = "";
		int guiScale = 0;
		int width = 0;
		int height = 0;
		String os = "";
		int memoryMb = 0;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					requestId = in.readUint();
					break;
				case 2:
					String grantedItem = in.readString(TehoProtocol.MAX_STRING);
					if (granted.size() < TehoProtocol.MAX_LIST) {
						granted.add(grantedItem);
					}
					break;
				case 3:
					input = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 4:
					fps = in.readUint();
					break;
				case 5:
					frameP50 = in.readUint();
					break;
				case 6:
					frameP99 = in.readUint();
					break;
				case 7:
					renderDistance = in.readUint();
					break;
				case 8:
					byte[] modsItem = in.readBytes(Chunking.MAX_STREAM);
					if (mods.size() < TehoProtocol.MAX_LIST) {
						mods.add(ModEntry.read(new WireReader(modsItem)));
					}
					break;
				case 9:
					String tehoModulesItem = in.readString(TehoProtocol.MAX_STRING);
					if (tehoModules.size() < TehoProtocol.MAX_LIST) {
						tehoModules.add(tehoModulesItem);
					}
					break;
				case 10:
					locale = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 11:
					guiScale = in.readUint();
					break;
				case 12:
					width = in.readUint();
					break;
				case 13:
					height = in.readUint();
					break;
				case 14:
					os = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 15:
					memoryMb = in.readUint();
					break;
				default:
					in.skip();
			}
		}
		return new InfoReply(requestId, granted, input, fps, frameP50, frameP99, renderDistance, mods, tehoModules, locale, guiScale, width, height, os, memoryMb);
	}

	@Override
	public String toString() {
		return "InfoReply{" + "requestId=" + requestId + ", " + "granted=" + granted + ", " + "input=" + input + ", " + "fps=" + fps + ", " + "frameP50=" + frameP50 + ", " + "frameP99=" + frameP99 + ", " + "renderDistance=" + renderDistance + ", " + "mods=" + mods + ", " + "tehoModules=" + tehoModules + ", " + "locale=" + locale + ", " + "guiScale=" + guiScale + ", " + "width=" + width + ", " + "height=" + height + ", " + "os=" + os + ", " + "memoryMb=" + memoryMb + "}";
	}
}
