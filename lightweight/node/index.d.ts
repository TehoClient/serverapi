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

export interface Hello {
  protocol?: number;
  client?: string;
  minecraft?: string;
  platform?: string;
  capabilities?: string[];
}

export interface Welcome {
  protocol?: number;
  session?: bigint;
  enabled?: string[];
  serverName?: string;
  grant?: string;
  verified?: string;
}

export interface HelloRequest {
}

export interface Reset {
}

export interface Notify {
  id?: string;
  title?: string;
  body?: string;
  accent?: number;
  durationMs?: number;
  sound?: boolean;
}

export interface Dismiss {
  id?: string;
}

export interface WaypointSet {
  id?: string;
  name?: string;
  x?: number;
  y?: number;
  z?: number;
  dimension?: string;
  color?: number;
  focus?: boolean;
}

export interface WaypointRemove {
  id?: string;
}

export interface WaypointClear {
}

export interface Reply {
  requestId?: number;
  ok?: boolean;
  error?: string;
  data?: Uint8Array;
}

export interface Event {
  kind?: string;
  target?: string;
  value?: string;
  requestId?: number;
}

export interface Chunk {
  stream?: number;
  index?: number;
  total?: number;
  data?: Uint8Array;
}

export interface AssetOffer {
  hash?: Uint8Array;
  kind?: number;
  size?: number;
}

export interface AssetWant {
  hashes?: Uint8Array[];
}

export interface AssetData {
  hash?: Uint8Array;
  kind?: number;
  data?: Uint8Array;
}

export interface ConsentResult {
  scope?: string;
  granted?: boolean;
  items?: string[];
}

export interface ModuleRules {
  disabled?: string[];
  disabledCategories?: string[];
  reason?: string;
  competitive?: boolean;
  allowed?: string[];
}

export interface SettingCap {
  module?: string;
  key?: string;
  min?: number;
  max?: number;
  bounded?: boolean;
  pinned?: string;
}

export interface SettingCaps {
  caps?: SettingCap[];
}

export interface XrayBlock {
  id?: string;
  color?: number;
}

export interface StaffGrant {
  tools?: string[];
  reason?: string;
  xrayBlocks?: XrayBlock[];
  radius?: number;
  storageBlocks?: XrayBlock[];
  entityTypes?: XrayBlock[];
}

export interface StaffRevoke {
  tools?: string[];
}

export interface StaffState {
  tool?: string;
  active?: boolean;
}

export interface EspEntry {
  player?: string;
  color?: number;
  label?: string;
}

export interface EspStyle {
  entries?: EspEntry[];
}

export interface Cooldown {
  id?: string;
  label?: string;
  durationMs?: number;
  item?: string;
  asset?: Uint8Array;
  color?: number;
}

export interface CooldownClear {
}

export interface Timer {
  id?: string;
  label?: string;
  mode?: number;
  millis?: number;
  paused?: boolean;
  color?: number;
}

export interface TimerRemove {
  id?: string;
}

export interface StaffSupport {
  tools?: string[];
}

export interface Highlight {
  id?: string;
  kind?: number;
  entity?: string;
  x?: number;
  y?: number;
  z?: number;
  x2?: number;
  y2?: number;
  z2?: number;
  color?: number;
  label?: string;
  style?: number;
  tracer?: boolean;
  ttlMs?: number;
  dimension?: string;
}

export interface HighlightRemove {
  ids?: string[];
}

export interface Trail {
  player?: string;
  points?: number[];
  color?: number;
  label?: string;
  ttlMs?: number;
  dimension?: string;
}

export interface InspectRequest {
  requestId?: number;
  kind?: number;
  target?: string;
  x?: number;
  y?: number;
  z?: number;
}

export interface InspectRow {
  label?: string;
  value?: string;
  color?: number;
}

export interface InspectAction {
  id?: string;
  label?: string;
}

export interface InspectCard {
  requestId?: number;
  title?: string;
  subtitle?: string;
  rows?: InspectRow[];
  actions?: InspectAction[];
  ttlMs?: number;
  target?: string;
}

export interface WheelAction {
  id?: string;
  label?: string;
  item?: string;
  color?: number;
  target?: number;
}

export interface ActionWheel {
  actions?: WheelAction[];
  title?: string;
}

export interface Alert {
  id?: string;
  severity?: number;
  title?: string;
  body?: string;
  player?: string;
  x?: number;
  y?: number;
  z?: number;
  dimension?: string;
  ttlMs?: number;
}

export interface TargetStats {
  player?: string;
  rows?: InspectRow[];
  ttlMs?: number;
}

export interface Spectate {
  player?: string;
  distance?: number;
}

export interface StaffAction {
  action?: string;
  targetKind?: number;
  target?: string;
  x?: number;
  y?: number;
  z?: number;
}

export interface LagCell {
  chunkX?: number;
  chunkZ?: number;
  score?: number;
  label?: string;
}

export interface LagMap {
  cells?: LagCell[];
  dimension?: string;
  ttlMs?: number;
}

export interface FreezeScreen {
  on?: boolean;
  title?: string;
  body?: string;
}

export interface ScreenshotRequest {
  requestId?: number;
  reason?: string;
  maxWidth?: number;
}

export interface ScreenshotResult {
  requestId?: number;
  granted?: boolean;
  png?: Uint8Array;
  width?: number;
  height?: number;
}

export interface ModListRequest {
  requestId?: number;
  reason?: string;
}

export interface ModEntry {
  id?: string;
  name?: string;
  version?: string;
}

export interface ModList {
  requestId?: number;
  granted?: boolean;
  mods?: ModEntry[];
  tehoModules?: string[];
}

export interface PrivateStatus {
  capabilities?: string[];
  error?: string;
  server?: string;
}

export interface NpcSlot {
  slot?: string;
  id?: string;
}

export interface NpcLook {
  npc?: string;
  mode?: number;
  slots?: NpcSlot[];
}

export interface NpcEmote {
  npc?: string;
  action?: number;
  emote?: string;
  loop?: boolean;
}

export interface NpcClear {
  npcs?: string[];
}

export interface CombatRules {
  legacySwing?: boolean;
  legacyBlockhit?: boolean;
  noHitDelayVisual?: boolean;
  missPenaltyVisual?: boolean;
  legacyHitColor?: boolean;
}

export interface ThirdPartyPolicy {
  deny?: string[];
  reason?: string;
  report?: boolean;
}

export interface PolicyReport {
  granted?: boolean;
  violations?: ModEntry[];
}

export interface SidebarRow {
  id?: string;
  text?: string;
  value?: string;
  item?: string;
  color?: number;
  progress?: number;
  asset?: Uint8Array;
  order?: number;
}

export interface SidebarSet {
  title?: string;
  rows?: SidebarRow[];
  remove?: string[];
  replace?: boolean;
  accent?: number;
  footer?: string;
}

export interface SidebarClear {
}

export interface Bar {
  id?: string;
  text?: string;
  progress?: number;
  color?: number;
  style?: number;
  ttlMs?: number;
  item?: string;
}

export interface BarRemove {
  ids?: string[];
}

export interface KillFeed {
  killer?: string;
  victim?: string;
  weapon?: string;
  text?: string;
  color?: number;
  killerId?: string;
  victimId?: string;
  ttlMs?: number;
}

export interface ScreenTint {
  color?: number;
  vignette?: boolean;
  fadeMs?: number;
  ttlMs?: number;
  strength?: number;
}

export interface Title {
  title?: string;
  subtitle?: string;
  animation?: number;
  fadeInMs?: number;
  stayMs?: number;
  fadeOutMs?: number;
  color?: number;
  scale?: number;
}

export interface TntFuse {
  fuseTicks?: number;
}

export interface Beam {
  id?: string;
  x?: number;
  y?: number;
  z?: number;
  entity?: string;
  color?: number;
  height?: number;
  width?: number;
  pulse?: boolean;
  dimension?: string;
  ttlMs?: number;
}

export interface Hologram {
  id?: string;
  x?: number;
  y?: number;
  z?: number;
  entity?: string;
  lines?: string[];
  item?: string;
  asset?: Uint8Array;
  scale?: number;
  background?: number;
  clickable?: boolean;
  throughWalls?: boolean;
  dimension?: string;
  ttlMs?: number;
  range?: number;
}

export interface Shape {
  id?: string;
  kind?: number;
  points?: number[];
  radius?: number;
  height?: number;
  color?: number;
  fill?: number;
  width?: number;
  throughWalls?: boolean;
  label?: string;
  dimension?: string;
  ttlMs?: number;
}

export interface WorldRemove {
  kind?: number;
  ids?: string[];
}

export interface EntityStyle {
  entity?: string;
  glow?: number;
  name?: string;
  scale?: number;
  rainbow?: boolean;
  hide?: string[];
  ttlMs?: number;
  hideName?: boolean;
}

export interface EntityStyleClear {
  entities?: string[];
}

export interface BorderStyle {
  color?: number;
  hideWall?: boolean;
  reset?: boolean;
}

export interface FireColor {
  entity?: string;
  color?: number;
  reset?: boolean;
}

export interface Nametag {
  player?: string;
  above?: string[];
  below?: string[];
  prefix?: string;
  suffix?: string;
  badge?: Uint8Array;
  hide?: boolean;
  badgeItem?: string;
}

export interface NametagClear {
  players?: string[];
}

export interface TabEntry {
  player?: string;
  display?: string;
  group?: string;
  icon?: Uint8Array;
  ping?: string;
  order?: number;
  color?: number;
}

export interface TabList {
  header?: string[];
  footer?: string[];
  entries?: TabEntry[];
  hidePing?: boolean;
  replace?: boolean;
  remove?: string[];
  reset?: boolean;
}

export interface Team {
  id?: string;
  name?: string;
  color?: number;
  members?: string[];
  markers?: boolean;
  throughWalls?: boolean;
  health?: boolean;
  compass?: boolean;
}

export interface TeamRemove {
  ids?: string[];
}

export interface NickHint {
  nick?: string;
  player?: string;
}

export interface Keybind {
  id?: string;
  label?: string;
  key?: number;
  button?: string;
  hold?: boolean;
  description?: string;
}

export interface Keybinds {
  binds?: Keybind[];
  category?: string;
}

export interface KeyPress {
  id?: string;
  pressed?: boolean;
  heldMs?: number;
}

export interface CameraShake {
  intensity?: number;
  durationMs?: number;
  frequency?: number;
}

export interface FovPunch {
  amount?: number;
  durationMs?: number;
}

export interface LookAt {
  x?: number;
  y?: number;
  z?: number;
  entity?: string;
  durationMs?: number;
  holdMs?: number;
}

export interface Letterbox {
  on?: boolean;
  size?: number;
  fadeMs?: number;
}

export interface Keyframe {
  x?: number;
  y?: number;
  z?: number;
  yaw?: number;
  pitch?: number;
  roll?: number;
  fov?: number;
  atMs?: number;
  ease?: number;
}

export interface Cutscene {
  id?: string;
  frames?: Keyframe[];
  skippable?: boolean;
  hideHud?: boolean;
  letterbox?: boolean;
  loop?: boolean;
  dimension?: string;
}

export interface CutsceneStop {
  id?: string;
}

export interface ScreenFade {
  color?: number;
  inMs?: number;
  holdMs?: number;
  outMs?: number;
}

export interface CutsceneEnded {
  id?: string;
  skipped?: boolean;
}

export interface PlaySound {
  id?: string;
  asset?: Uint8Array;
  sound?: string;
  x?: number;
  y?: number;
  z?: number;
  entity?: string;
  positional?: boolean;
  volume?: number;
  pitch?: number;
  loop?: boolean;
  fadeInMs?: number;
  music?: boolean;
}

export interface StopSound {
  ids?: string[];
  fadeOutMs?: number;
}

export interface Presence {
  game?: string;
  map?: string;
  state?: string;
  started?: bigint;
  partySize?: number;
  partyMax?: number;
  joinable?: boolean;
  clear?: boolean;
}

export interface InviteSheet {
  reason?: string;
  party?: string;
  address?: string;
}

export interface Transfer {
  host?: string;
  port?: number;
  token?: string;
  title?: string;
  reason?: string;
}

export interface ServerLink {
  label?: string;
  url?: string;
  kind?: string;
}

export interface ServerLinks {
  links?: ServerLink[];
}

export interface UiNode {
  id?: string;
  parent?: string;
  kind?: number;
  text?: string;
  value?: string;
  min?: number;
  max?: number;
  step?: number;
  asset?: Uint8Array;
  item?: string;
  player?: string;
  color?: number;
  columns?: number;
  disabled?: boolean;
  placeholder?: string;
  options?: string[];
  millis?: bigint;
  style?: number;
  width?: number;
  height?: number;
  tooltip?: string;
}

export interface UiOpen {
  id?: string;
  surface?: number;
  title?: string;
  nodes?: UiNode[];
  width?: number;
  closeable?: boolean;
  anchor?: number;
}

export interface UiPatch {
  id?: string;
  nodes?: UiNode[];
  remove?: string[];
  title?: string;
}

export interface UiClose {
  id?: string;
}

export interface UiEvent {
  id?: string;
  node?: string;
  action?: number;
  value?: string;
}

export interface InfoRequest {
  requestId?: number;
  fields?: string[];
  reason?: string;
}

export interface InfoReply {
  requestId?: number;
  granted?: string[];
  input?: string;
  fps?: number;
  frameP50?: number;
  frameP99?: number;
  renderDistance?: number;
  mods?: ModEntry[];
  tehoModules?: string[];
  locale?: string;
  guiScale?: number;
  width?: number;
  height?: number;
  os?: string;
  memoryMb?: number;
}

export interface AssertRequest {
  requestId?: number;
  nonce?: string;
}

export interface AssertResult {
  requestId?: number;
  token?: string;
  error?: string;
}

export interface AttestRequest {
  requestId?: number;
  nonce?: string;
}

export interface AttestResult {
  requestId?: number;
  token?: string;
  error?: string;
}

export interface PackHint {
  url?: string;
  sha1?: string;
  size?: number;
  required?: boolean;
  prompt?: string;
  id?: string;
}

export interface PackStatus {
  sha1?: string;
  state?: number;
  error?: string;
}

export interface ClipMark {
  id?: string;
  title?: string;
  beforeMs?: number;
  afterMs?: number;
}

export interface ClipResult {
  id?: string;
  saved?: boolean;
  url?: string;
  error?: string;
}

export interface ObserverPlayer {
  player?: string;
  name?: string;
  health?: number;
  alive?: boolean;
  kills?: number;
  item?: string;
}

export interface ObserverTeam {
  name?: string;
  color?: number;
  score?: number;
  players?: ObserverPlayer[];
}

export interface ObserverHud {
  on?: boolean;
  title?: string;
  subtitle?: string;
  teams?: ObserverTeam[];
}

export interface CameraFollow {
  player?: string;
  mode?: number;
  distance?: number;
  smooth?: boolean;
}

export interface Statue {
  id?: string;
  player?: string;
  name?: string;
  x?: number;
  y?: number;
  z?: number;
  yaw?: number;
  pose?: number;
  scale?: number;
  dimension?: string;
  label?: string;
  emote?: string;
}

export interface StatueRemove {
  ids?: string[];
}

export interface Branding {
  accent?: number;
  logo?: Uint8Array;
  banner?: Uint8Array;
  background?: Uint8Array;
  tagline?: string;
  reset?: boolean;
}

export interface ShowcaseOpen {
  title?: string;
  items?: string[];
  emote?: string;
  focus?: string;
  subtitle?: string;
}

export interface MessageMap {
  Hello: Hello;
  Welcome: Welcome;
  HelloRequest: HelloRequest;
  Reset: Reset;
  Notify: Notify;
  Dismiss: Dismiss;
  WaypointSet: WaypointSet;
  WaypointRemove: WaypointRemove;
  WaypointClear: WaypointClear;
  Reply: Reply;
  Event: Event;
  Chunk: Chunk;
  AssetOffer: AssetOffer;
  AssetWant: AssetWant;
  AssetData: AssetData;
  ConsentResult: ConsentResult;
  ModuleRules: ModuleRules;
  SettingCaps: SettingCaps;
  StaffGrant: StaffGrant;
  StaffRevoke: StaffRevoke;
  StaffState: StaffState;
  EspStyle: EspStyle;
  Cooldown: Cooldown;
  CooldownClear: CooldownClear;
  Timer: Timer;
  TimerRemove: TimerRemove;
  StaffSupport: StaffSupport;
  Highlight: Highlight;
  HighlightRemove: HighlightRemove;
  Trail: Trail;
  InspectRequest: InspectRequest;
  InspectCard: InspectCard;
  ActionWheel: ActionWheel;
  Alert: Alert;
  TargetStats: TargetStats;
  Spectate: Spectate;
  StaffAction: StaffAction;
  LagMap: LagMap;
  FreezeScreen: FreezeScreen;
  ScreenshotRequest: ScreenshotRequest;
  ScreenshotResult: ScreenshotResult;
  ModListRequest: ModListRequest;
  ModList: ModList;
  PrivateStatus: PrivateStatus;
  NpcLook: NpcLook;
  NpcEmote: NpcEmote;
  NpcClear: NpcClear;
  CombatRules: CombatRules;
  ThirdPartyPolicy: ThirdPartyPolicy;
  PolicyReport: PolicyReport;
  SidebarSet: SidebarSet;
  SidebarClear: SidebarClear;
  Bar: Bar;
  BarRemove: BarRemove;
  KillFeed: KillFeed;
  ScreenTint: ScreenTint;
  Title: Title;
  TntFuse: TntFuse;
  Beam: Beam;
  Hologram: Hologram;
  Shape: Shape;
  WorldRemove: WorldRemove;
  EntityStyle: EntityStyle;
  EntityStyleClear: EntityStyleClear;
  BorderStyle: BorderStyle;
  FireColor: FireColor;
  Nametag: Nametag;
  NametagClear: NametagClear;
  TabList: TabList;
  Team: Team;
  TeamRemove: TeamRemove;
  NickHint: NickHint;
  Keybinds: Keybinds;
  KeyPress: KeyPress;
  CameraShake: CameraShake;
  FovPunch: FovPunch;
  LookAt: LookAt;
  Letterbox: Letterbox;
  Cutscene: Cutscene;
  CutsceneStop: CutsceneStop;
  ScreenFade: ScreenFade;
  CutsceneEnded: CutsceneEnded;
  PlaySound: PlaySound;
  StopSound: StopSound;
  Presence: Presence;
  InviteSheet: InviteSheet;
  Transfer: Transfer;
  ServerLinks: ServerLinks;
  UiOpen: UiOpen;
  UiPatch: UiPatch;
  UiClose: UiClose;
  UiEvent: UiEvent;
  InfoRequest: InfoRequest;
  InfoReply: InfoReply;
  AssertRequest: AssertRequest;
  AssertResult: AssertResult;
  AttestRequest: AttestRequest;
  AttestResult: AttestResult;
  PackHint: PackHint;
  PackStatus: PackStatus;
  ClipMark: ClipMark;
  ClipResult: ClipResult;
  ObserverHud: ObserverHud;
  CameraFollow: CameraFollow;
  Statue: Statue;
  StatueRemove: StatueRemove;
  Branding: Branding;
  ShowcaseOpen: ShowcaseOpen;
}

export type MessageName = keyof MessageMap;

export type Decoded = { [K in MessageName]: { type: K } & Required<MessageMap[K]> }[MessageName];

export declare const CHANNEL: string;
export declare const VERSION: number;
export declare const MAX_FRAME: number;
export declare const CAPABILITIES: string[];
export declare const TYPES: { [K in MessageName]: number };
export declare class ProtocolError extends Error {}
export declare function encode<K extends MessageName>(name: K, message?: MessageMap[K]): Uint8Array;
export declare function decode(frame: Uint8Array): Decoded | null;
export declare function frames<K extends MessageName>(name: K, message: MessageMap[K], stream: number): Uint8Array[];
export declare function capabilityFor(type: number): string | null;
export declare function welcome(hello: Required<Hello>, options: { serverName: string; supported?: string[]; session?: bigint; grant?: string; verified?: string }): Welcome;
export declare class Reassembler {
  accept(chunk: Required<Chunk>, now?: number): Decoded | null;
  clear(): void;
}
export declare class Session {
  constructor(options: { serverName: string; supported?: string[]; grant?: string; verified?: string; send: (frame: Uint8Array) => void });
  readonly hello: Required<Hello> | null;
  readonly enabled: string[];
  has(capability: string): boolean;
  receive(frame: Uint8Array): Decoded | null;
  send<K extends MessageName>(name: K, message?: MessageMap[K]): boolean;
  request<K extends MessageName>(name: K, message?: MessageMap[K]): Promise<Decoded>;
  on(listener: (message: Decoded) => void): () => void;
}
