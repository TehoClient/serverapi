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

package dev.tehoclient.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import dev.tehoclient.api.hud.HudApi;
import dev.tehoclient.api.modules.ModulesApi;
import dev.tehoclient.api.notify.Notification;
import dev.tehoclient.api.staff.StaffApi;
import dev.tehoclient.api.suspect.SuspectApi;
import dev.tehoclient.api.waypoint.WaypointsApi;

public interface TehoPlayer {
	UUID uniqueId();

	String name();

	int protocol();

	String clientVersion();

	String minecraftVersion();

	String platform();

	List<String> advertised();

	Set<String> capabilities();

	boolean supports(String capability);

	long session();

	Set<String> privileges();

	String privilegeError();

	boolean notify(Notification notification);

	boolean dismiss(String id);

	WaypointsApi waypoints();

	ModulesApi modules();

	StaffApi staff();

	HudApi hud();

	SuspectApi suspect();

	dev.tehoclient.api.world.WorldApi world();

	dev.tehoclient.api.players.PlayersApi players();

	dev.tehoclient.api.keybind.KeybindApi keybinds();

	dev.tehoclient.api.camera.CameraApi camera();

	dev.tehoclient.api.audio.AudioApi audio();

	dev.tehoclient.api.social.SocialApi social();

	dev.tehoclient.api.ui.UiApi ui();

	dev.tehoclient.api.info.InfoApi info();

	dev.tehoclient.api.verify.VerifyApi verify();

	dev.tehoclient.api.pack.PackApi pack();

	dev.tehoclient.api.clips.ClipsApi clips();

	dev.tehoclient.api.broadcast.BroadcastApi broadcast();

	dev.tehoclient.api.showcase.ShowcaseApi showcase();

	boolean verifiedServer();

	void reset();
}
