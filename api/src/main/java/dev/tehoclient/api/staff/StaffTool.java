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

package dev.tehoclient.api.staff;

import dev.tehoclient.api.protocol.TehoProtocol;

public enum StaffTool {
	XRAY(TehoProtocol.TOOL_XRAY),
	STORAGE_ESP(TehoProtocol.TOOL_STORAGE_ESP),
	PLAYER_ESP(TehoProtocol.TOOL_PLAYER_ESP),
	NAMETAGS(TehoProtocol.TOOL_NAMETAGS),
	ENTITY_ESP(TehoProtocol.TOOL_ENTITY_ESP),
	TRACERS(TehoProtocol.TOOL_TRACERS),
	FULLBRIGHT(TehoProtocol.TOOL_FULLBRIGHT),
	HIGHLIGHTS(TehoProtocol.TOOL_HIGHLIGHTS),
	LAG_MAP(TehoProtocol.TOOL_LAG_MAP),
	TRAILS(TehoProtocol.TOOL_TRAILS),
	INSPECTOR(TehoProtocol.TOOL_INSPECTOR),
	COMBAT_OVERLAY(TehoProtocol.TOOL_COMBAT_OVERLAY),
	SPECTATE(TehoProtocol.TOOL_SPECTATE),
	FREECAM(TehoProtocol.TOOL_FREECAM),
	ACTION_WHEEL(TehoProtocol.TOOL_ACTION_WHEEL),
	ALERTS(TehoProtocol.TOOL_ALERTS),
	CLICK_TELEPORT(TehoProtocol.TOOL_CLICK_TELEPORT);

	private final String id;

	StaffTool(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	public static StaffTool byId(String id) {
		if (id == null) {
			return null;
		}
		for (StaffTool tool : values()) {
			if (tool.id.equalsIgnoreCase(id) || tool.name().equalsIgnoreCase(id)) {
				return tool;
			}
		}
		return null;
	}
}
