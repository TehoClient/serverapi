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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class TehoProtocol {
	public static final String CHANNEL = "teho:api";
	public static final int VERSION = 2;
	public static final int MAX_FRAME = 32000;
	public static final int MAX_STRING = 1024;
	public static final int MAX_LIST = 512;
	public static final int MAX_POINTS = 3072;
	public static final int MAX_GRANT = 8192;

	public static final String NOTIFY = "notify:1";
	public static final String WAYPOINT = "waypoint:1";
	public static final String ASSET = "asset:1";
	public static final String MODULES = "modules:1";
	public static final String STAFF = "staff:1";
	public static final String HUD = "hud:1";
	public static final String SUSPECT = "suspect:1";
	public static final String NPC = "npc:1";
	public static final String WORLD = "world:1";
	public static final String PLAYERS = "players:1";
	public static final String KEYBIND = "keybind:1";
	public static final String CAMERA = "camera:1";
	public static final String AUDIO = "audio:1";
	public static final String SOCIAL = "social:1";
	public static final String UI = "ui:1";
	public static final String INFO = "info:1";
	public static final String ASSERT = "assert:1";
	public static final String ATTEST = "attest:1";
	public static final String PACK = "pack:1";
	public static final String CLIPS = "clips:1";
	public static final String BROADCAST = "broadcast:1";
	public static final String STATUE = "statue:1";
	public static final String BRANDING = "branding:1";
	public static final String SHOWCASE = "showcase:1";

	public static final int HELLO = 0x01;
	public static final int WELCOME = 0x02;
	public static final int HELLO_REQUEST = 0x03;
	public static final int RESET = 0x04;
	public static final int REPLY = 0x05;
	public static final int EVENT = 0x06;
	public static final int CHUNK = 0x07;
	public static final int ASSET_OFFER = 0x08;
	public static final int ASSET_WANT = 0x09;
	public static final int ASSET_DATA = 0x0C;
	public static final int CONSENT_RESULT = 0x0D;
	public static final int PRIVATE_STATUS = 0x0E;
	public static final int NOTIFY_SHOW = 0x10;
	public static final int NOTIFY_DISMISS = 0x11;
	public static final int WAYPOINT_SET = 0x20;
	public static final int WAYPOINT_REMOVE = 0x21;
	public static final int WAYPOINT_CLEAR = 0x22;
	public static final int MODULE_RULES = 0x30;
	public static final int SETTING_CAPS = 0x31;
	public static final int STAFF_GRANT = 0x40;
	public static final int STAFF_REVOKE = 0x41;
	public static final int STAFF_STATE = 0x42;
	public static final int ESP_STYLE = 0x43;
	public static final int STAFF_SUPPORT = 0x44;
	public static final int HIGHLIGHT = 0x45;
	public static final int HIGHLIGHT_REMOVE = 0x46;
	public static final int TRAIL = 0x47;
	public static final int INSPECT_REQUEST = 0x48;
	public static final int INSPECT_CARD = 0x49;
	public static final int ACTION_WHEEL = 0x4A;
	public static final int ALERT = 0x4B;
	public static final int TARGET_STATS = 0x4C;
	public static final int SPECTATE = 0x4D;
	public static final int STAFF_ACTION = 0x4E;
	public static final int LAG_MAP = 0x4F;
	public static final int COOLDOWN = 0x50;
	public static final int COOLDOWN_CLEAR = 0x51;
	public static final int TIMER = 0x52;
	public static final int TIMER_REMOVE = 0x53;
	public static final int FREEZE_SCREEN = 0x60;
	public static final int SCREENSHOT_REQUEST = 0x61;
	public static final int SCREENSHOT_RESULT = 0x62;
	public static final int MOD_LIST_REQUEST = 0x63;
	public static final int MOD_LIST = 0x64;
	public static final int NPC_LOOK = 0x70;
	public static final int NPC_EMOTE = 0x71;
	public static final int NPC_CLEAR = 0x72;
	public static final int COMBAT_RULES = 0x32;
	public static final int THIRD_PARTY_POLICY = 0x33;
	public static final int POLICY_REPORT = 0x34;
	public static final int SIDEBAR_SET = 0x54;
	public static final int BAR = 0x56;
	public static final int BAR_REMOVE = 0x57;
	public static final int KILL_FEED = 0x58;
	public static final int SCREEN_TINT = 0x59;
	public static final int TITLE = 0x5A;
	public static final int TNT_FUSE = 0x5B;
	public static final int BEAM = 0x80;
	public static final int HOLOGRAM = 0x81;
	public static final int SHAPE = 0x82;
	public static final int WORLD_REMOVE = 0x83;
	public static final int ENTITY_STYLE = 0x84;
	public static final int ENTITY_STYLE_CLEAR = 0x85;
	public static final int BORDER_STYLE = 0x86;
	public static final int FIRE_COLOR = 0x87;
	public static final int NAMETAG = 0x90;
	public static final int NAMETAG_CLEAR = 0x91;
	public static final int TAB_LIST = 0x92;
	public static final int TEAM = 0x93;
	public static final int TEAM_REMOVE = 0x94;
	public static final int NICK_HINT = 0x95;
	public static final int KEYBINDS = 0xA0;
	public static final int KEY_PRESS = 0xA1;
	public static final int CAMERA_SHAKE = 0xB0;
	public static final int FOV_PUNCH = 0xB1;
	public static final int LOOK_AT = 0xB2;
	public static final int LETTERBOX = 0xB3;
	public static final int CUTSCENE = 0xB4;
	public static final int CUTSCENE_STOP = 0xB5;
	public static final int SCREEN_FADE = 0xB6;
	public static final int CUTSCENE_ENDED = 0xB7;
	public static final int PLAY_SOUND = 0xC0;
	public static final int STOP_SOUND = 0xC1;
	public static final int PRESENCE = 0xD0;
	public static final int INVITE_SHEET = 0xD1;
	public static final int TRANSFER = 0xD2;
	public static final int SERVER_LINKS = 0xD3;
	public static final int UI_OPEN = 0xE0;
	public static final int UI_PATCH = 0xE1;
	public static final int UI_CLOSE = 0xE2;
	public static final int UI_EVENT = 0xE3;
	public static final int INFO_REQUEST = 0xF0;
	public static final int INFO_REPLY = 0xF1;
	public static final int ASSERT_REQUEST = 0x110;
	public static final int ASSERT_RESULT = 0x111;
	public static final int ATTEST_REQUEST = 0x118;
	public static final int ATTEST_RESULT = 0x119;
	public static final int PACK_HINT = 0x120;
	public static final int PACK_STATUS = 0x121;
	public static final int CLIP_MARK = 0x130;
	public static final int CLIP_RESULT = 0x131;
	public static final int OBSERVER_HUD = 0x140;
	public static final int CAMERA_FOLLOW = 0x141;
	public static final int STATUE_SET = 0x170;
	public static final int STATUE_REMOVE = 0x171;
	public static final int BRANDING_SET = 0x180;
	public static final int SHOWCASE_OPEN = 0x190;
	public static final int SIDEBAR_CLEAR = 0x55;

	public static final int WORLD_ALL = 0;
	public static final int WORLD_BEAM = 1;
	public static final int WORLD_HOLOGRAM = 2;
	public static final int WORLD_SHAPE = 3;

	public static final int SHAPE_CUBOID = 0;
	public static final int SHAPE_LINE = 1;
	public static final int SHAPE_SPHERE = 2;
	public static final int SHAPE_CYLINDER = 3;
	public static final int SHAPE_POLYGON = 4;

	public static final int BAR_SOLID = 0;
	public static final int BAR_SEGMENTED = 1;
	public static final int BAR_NOTCHED = 2;

	public static final int TITLE_FADE = 0;
	public static final int TITLE_TYPE = 1;
	public static final int TITLE_SLIDE = 2;
	public static final int TITLE_POP = 3;

	public static final int EASE_LINEAR = 0;
	public static final int EASE_IN_OUT = 1;
	public static final int EASE_IN = 2;
	public static final int EASE_OUT = 3;
	public static final int EASE_CATMULL = 4;

	public static final int UI_MODAL = 0;
	public static final int UI_PANEL = 1;
	public static final int UI_HUD = 2;

	public static final int NODE_STACK = 0;
	public static final int NODE_ROW = 1;
	public static final int NODE_GRID = 2;
	public static final int NODE_TEXT = 3;
	public static final int NODE_IMAGE = 4;
	public static final int NODE_ITEM = 5;
	public static final int NODE_BUTTON = 6;
	public static final int NODE_TOGGLE = 7;
	public static final int NODE_SLIDER = 8;
	public static final int NODE_INPUT = 9;
	public static final int NODE_LIST = 10;
	public static final int NODE_TABS = 11;
	public static final int NODE_TAB = 12;
	public static final int NODE_PROGRESS = 13;
	public static final int NODE_HEAD = 14;
	public static final int NODE_COUNTDOWN = 15;
	public static final int NODE_DIVIDER = 16;
	public static final int NODE_SELECT = 17;

	public static final int UI_CLICK = 0;
	public static final int UI_CHANGE = 1;
	public static final int UI_SUBMIT = 2;
	public static final int UI_CLOSED = 3;

	public static final int ANCHOR_CENTER = 0;
	public static final int ANCHOR_LEFT = 1;
	public static final int ANCHOR_RIGHT = 2;
	public static final int ANCHOR_TOP = 3;
	public static final int ANCHOR_BOTTOM = 4;

	public static final String INFO_INPUT = "input";
	public static final String INFO_PERFORMANCE = "performance";
	public static final String INFO_MODS = "mods";
	public static final String INFO_DISPLAY = "display";
	public static final String INFO_SYSTEM = "system";

	public static final int PACK_CACHED = 0;
	public static final int PACK_DOWNLOADED = 1;
	public static final int PACK_FAILED = 2;
	public static final int PACK_DECLINED = 3;

	public static final int FOLLOW_THIRD = 0;
	public static final int FOLLOW_FIRST = 1;
	public static final int FOLLOW_ORBIT = 2;

	public static final int POSE_STAND = 0;
	public static final int POSE_WAVE = 1;
	public static final int POSE_SALUTE = 2;
	public static final int POSE_SIT = 3;
	public static final int POSE_TROPHY = 4;

	public static final int NPC_MODE_EXPLICIT = 0;
	public static final int NPC_MODE_MIRROR = 1;
	public static final int NPC_MODE_CLEAR = 2;

	public static final int NPC_EMOTE_START = 0;
	public static final int NPC_EMOTE_STOP = 1;
	public static final int NPC_EMOTE_RESET = 2;

	public static final int TARGET_NONE = 0;
	public static final int TARGET_PLAYER = 1;
	public static final int TARGET_BLOCK = 2;
	public static final int TARGET_ENTITY = 3;

	public static final int HIGHLIGHT_ENTITY = 0;
	public static final int HIGHLIGHT_BLOCK = 1;
	public static final int HIGHLIGHT_AREA = 2;

	public static final int STYLE_OUTLINE = 0;
	public static final int STYLE_FILL = 1;
	public static final int STYLE_BOTH = 2;

	public static final int SEVERITY_INFO = 0;
	public static final int SEVERITY_WARN = 1;
	public static final int SEVERITY_HIGH = 2;

	public static final int ASSET_PNG = 1;
	public static final int ASSET_OGG = 2;
	public static final int ASSET_OBJ = 3;

	public static final int TIMER_COUNTDOWN = 0;
	public static final int TIMER_STOPWATCH = 1;

	public static final String TOOL_XRAY = "xray";
	public static final String TOOL_PLAYER_ESP = "player_esp";
	public static final String TOOL_NAMETAGS = "nametags";
	public static final String TOOL_FREECAM = "freecam";
	public static final String TOOL_STORAGE_ESP = "storage_esp";
	public static final String TOOL_ENTITY_ESP = "entity_esp";
	public static final String TOOL_TRACERS = "tracers";
	public static final String TOOL_FULLBRIGHT = "fullbright";
	public static final String TOOL_HIGHLIGHTS = "highlights";
	public static final String TOOL_LAG_MAP = "lag_map";
	public static final String TOOL_TRAILS = "trails";
	public static final String TOOL_INSPECTOR = "inspector";
	public static final String TOOL_COMBAT_OVERLAY = "combat_overlay";
	public static final String TOOL_SPECTATE = "spectate";
	public static final String TOOL_ACTION_WHEEL = "action_wheel";
	public static final String TOOL_ALERTS = "alerts";
	public static final String TOOL_CLICK_TELEPORT = "click_teleport";

	private static final List<String> KNOWN = Collections.unmodifiableList(Arrays.asList(NOTIFY, WAYPOINT, ASSET, MODULES, STAFF, HUD, SUSPECT, NPC, WORLD, PLAYERS, KEYBIND, CAMERA, AUDIO, SOCIAL, UI, INFO, ASSERT, ATTEST, PACK, CLIPS, BROADCAST, STATUE, BRANDING, SHOWCASE));

	private TehoProtocol() {}

	public static List<String> known() {
		return KNOWN;
	}

	public static String capabilityFor(int type) {
		if (type >= 0x10 && type <= 0x1F) {
			return NOTIFY;
		}
		if (type >= 0x20 && type <= 0x2F) {
			return WAYPOINT;
		}
		if (type == ASSET_OFFER || type == ASSET_WANT || type == ASSET_DATA) {
			return ASSET;
		}
		if (type >= 0x30 && type <= 0x3F) {
			return MODULES;
		}
		if (type >= 0x40 && type <= 0x4F) {
			return STAFF;
		}
		if (type >= 0x50 && type <= 0x5F) {
			return HUD;
		}
		if (type >= 0x60 && type <= 0x6F) {
			return SUSPECT;
		}
		if (type >= 0x70 && type <= 0x7F) {
			return NPC;
		}
		if (type >= 0x80 && type <= 0x8F) {
			return WORLD;
		}
		if (type >= 0x90 && type <= 0x9F) {
			return PLAYERS;
		}
		if (type >= 0xA0 && type <= 0xAF) {
			return KEYBIND;
		}
		if (type >= 0xB0 && type <= 0xBF) {
			return CAMERA;
		}
		if (type >= 0xC0 && type <= 0xCF) {
			return AUDIO;
		}
		if (type >= 0xD0 && type <= 0xDF) {
			return SOCIAL;
		}
		if (type >= 0xE0 && type <= 0xEF) {
			return UI;
		}
		if (type >= 0xF0 && type <= 0xFF) {
			return INFO;
		}
		if (type >= 0x110 && type <= 0x117) {
			return ASSERT;
		}
		if (type >= 0x118 && type <= 0x11F) {
			return ATTEST;
		}
		if (type >= 0x120 && type <= 0x12F) {
			return PACK;
		}
		if (type >= 0x130 && type <= 0x13F) {
			return CLIPS;
		}
		if (type >= 0x140 && type <= 0x14F) {
			return BROADCAST;
		}
		if (type >= 0x170 && type <= 0x17F) {
			return STATUE;
		}
		if (type >= 0x180 && type <= 0x18F) {
			return BRANDING;
		}
		if (type >= 0x190 && type <= 0x19F) {
			return SHOWCASE;
		}
		return null;
	}

	public static byte[] encode(Message message) {
		WireWriter out = new WireWriter();
		out.rawVarint(message.type());
		message.write(out);
		if (out.size() > MAX_FRAME) {
			throw new ProtocolException("frame " + out.size() + " over " + MAX_FRAME);
		}
		return out.toByteArray();
	}

	public static Message decode(byte[] frame) {
		if (frame == null || frame.length == 0) {
			throw new ProtocolException("empty frame");
		}
		if (frame.length > MAX_FRAME) {
			throw new ProtocolException("frame " + frame.length + " over " + MAX_FRAME);
		}
		return decodeUnbounded(frame);
	}

	static Message decodeUnbounded(byte[] frame) {
		if (frame == null || frame.length == 0) {
			throw new ProtocolException("empty frame");
		}
		WireReader in = new WireReader(frame);
		long type = in.rawVarint();
		if (type < 0 || type > Integer.MAX_VALUE) {
			return null;
		}
		switch ((int) type) {
			case HELLO:
				return Hello.read(in);
			case WELCOME:
				return Welcome.read(in);
			case HELLO_REQUEST:
				return Signal.read(HELLO_REQUEST, in);
			case RESET:
				return Signal.read(RESET, in);
			case NOTIFY_SHOW:
				return Notify.read(in);
			case NOTIFY_DISMISS:
				return Dismiss.read(in);
			case WAYPOINT_SET:
				return WaypointSet.read(in);
			case WAYPOINT_REMOVE:
				return WaypointRemove.read(in);
			case WAYPOINT_CLEAR:
				return Signal.read(WAYPOINT_CLEAR, in);
			case REPLY:
				return Reply.read(in);
			case EVENT:
				return Event.read(in);
			case CHUNK:
				return Chunk.read(in);
			case ASSET_OFFER:
				return AssetOffer.read(in);
			case ASSET_WANT:
				return AssetWant.read(in);
			case ASSET_DATA:
				return AssetData.read(in);
			case CONSENT_RESULT:
				return ConsentResult.read(in);
			case MODULE_RULES:
				return ModuleRules.read(in);
			case SETTING_CAPS:
				return SettingCaps.read(in);
			case STAFF_GRANT:
				return StaffGrant.read(in);
			case STAFF_REVOKE:
				return StaffRevoke.read(in);
			case STAFF_STATE:
				return StaffState.read(in);
			case ESP_STYLE:
				return EspStyle.read(in);
			case COOLDOWN:
				return Cooldown.read(in);
			case COOLDOWN_CLEAR:
				return Signal.read(COOLDOWN_CLEAR, in);
			case TIMER:
				return Timer.read(in);
			case TIMER_REMOVE:
				return TimerRemove.read(in);
			case STAFF_SUPPORT:
				return StaffSupport.read(in);
			case HIGHLIGHT:
				return Highlight.read(in);
			case HIGHLIGHT_REMOVE:
				return HighlightRemove.read(in);
			case TRAIL:
				return Trail.read(in);
			case INSPECT_REQUEST:
				return InspectRequest.read(in);
			case INSPECT_CARD:
				return InspectCard.read(in);
			case ACTION_WHEEL:
				return ActionWheel.read(in);
			case ALERT:
				return Alert.read(in);
			case TARGET_STATS:
				return TargetStats.read(in);
			case SPECTATE:
				return Spectate.read(in);
			case STAFF_ACTION:
				return StaffAction.read(in);
			case LAG_MAP:
				return LagMap.read(in);
			case FREEZE_SCREEN:
				return FreezeScreen.read(in);
			case SCREENSHOT_REQUEST:
				return ScreenshotRequest.read(in);
			case SCREENSHOT_RESULT:
				return ScreenshotResult.read(in);
			case MOD_LIST_REQUEST:
				return ModListRequest.read(in);
			case MOD_LIST:
				return ModList.read(in);
			case PRIVATE_STATUS:
				return PrivateStatus.read(in);
			case NPC_LOOK:
				return NpcLook.read(in);
			case NPC_EMOTE:
				return NpcEmote.read(in);
			case NPC_CLEAR:
				return NpcClear.read(in);
			case COMBAT_RULES:
				return CombatRules.read(in);
			case THIRD_PARTY_POLICY:
				return ThirdPartyPolicy.read(in);
			case POLICY_REPORT:
				return PolicyReport.read(in);
			case SIDEBAR_SET:
				return SidebarSet.read(in);
			case BAR:
				return Bar.read(in);
			case BAR_REMOVE:
				return BarRemove.read(in);
			case KILL_FEED:
				return KillFeed.read(in);
			case SCREEN_TINT:
				return ScreenTint.read(in);
			case TITLE:
				return Title.read(in);
			case TNT_FUSE:
				return TntFuse.read(in);
			case BEAM:
				return Beam.read(in);
			case HOLOGRAM:
				return Hologram.read(in);
			case SHAPE:
				return Shape.read(in);
			case WORLD_REMOVE:
				return WorldRemove.read(in);
			case ENTITY_STYLE:
				return EntityStyle.read(in);
			case ENTITY_STYLE_CLEAR:
				return EntityStyleClear.read(in);
			case BORDER_STYLE:
				return BorderStyle.read(in);
			case FIRE_COLOR:
				return FireColor.read(in);
			case NAMETAG:
				return Nametag.read(in);
			case NAMETAG_CLEAR:
				return NametagClear.read(in);
			case TAB_LIST:
				return TabList.read(in);
			case TEAM:
				return Team.read(in);
			case TEAM_REMOVE:
				return TeamRemove.read(in);
			case NICK_HINT:
				return NickHint.read(in);
			case KEYBINDS:
				return Keybinds.read(in);
			case KEY_PRESS:
				return KeyPress.read(in);
			case CAMERA_SHAKE:
				return CameraShake.read(in);
			case FOV_PUNCH:
				return FovPunch.read(in);
			case LOOK_AT:
				return LookAt.read(in);
			case LETTERBOX:
				return Letterbox.read(in);
			case CUTSCENE:
				return Cutscene.read(in);
			case CUTSCENE_STOP:
				return CutsceneStop.read(in);
			case SCREEN_FADE:
				return ScreenFade.read(in);
			case CUTSCENE_ENDED:
				return CutsceneEnded.read(in);
			case PLAY_SOUND:
				return PlaySound.read(in);
			case STOP_SOUND:
				return StopSound.read(in);
			case PRESENCE:
				return Presence.read(in);
			case INVITE_SHEET:
				return InviteSheet.read(in);
			case TRANSFER:
				return Transfer.read(in);
			case SERVER_LINKS:
				return ServerLinks.read(in);
			case UI_OPEN:
				return UiOpen.read(in);
			case UI_PATCH:
				return UiPatch.read(in);
			case UI_CLOSE:
				return UiClose.read(in);
			case UI_EVENT:
				return UiEvent.read(in);
			case INFO_REQUEST:
				return InfoRequest.read(in);
			case INFO_REPLY:
				return InfoReply.read(in);
			case ASSERT_REQUEST:
				return AssertRequest.read(in);
			case ASSERT_RESULT:
				return AssertResult.read(in);
			case ATTEST_REQUEST:
				return AttestRequest.read(in);
			case ATTEST_RESULT:
				return AttestResult.read(in);
			case PACK_HINT:
				return PackHint.read(in);
			case PACK_STATUS:
				return PackStatus.read(in);
			case CLIP_MARK:
				return ClipMark.read(in);
			case CLIP_RESULT:
				return ClipResult.read(in);
			case OBSERVER_HUD:
				return ObserverHud.read(in);
			case CAMERA_FOLLOW:
				return CameraFollow.read(in);
			case STATUE_SET:
				return Statue.read(in);
			case STATUE_REMOVE:
				return StatueRemove.read(in);
			case BRANDING_SET:
				return Branding.read(in);
			case SHOWCASE_OPEN:
				return ShowcaseOpen.read(in);
			case SIDEBAR_CLEAR:
				return Signal.read(SIDEBAR_CLEAR, in);
			default:
				return null;
		}
	}
}
