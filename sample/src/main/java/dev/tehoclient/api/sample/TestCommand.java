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

package dev.tehoclient.api.sample;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import dev.tehoclient.api.TehoPlayer;
import dev.tehoclient.api.audio.Sound;
import dev.tehoclient.api.broadcast.BroadcastApi;
import dev.tehoclient.api.broadcast.Observer;
import dev.tehoclient.api.bukkit.BukkitTeho;
import dev.tehoclient.api.camera.Cutscene;
import dev.tehoclient.api.hud.Bar;
import dev.tehoclient.api.hud.Cooldown;
import dev.tehoclient.api.hud.Sidebar;
import dev.tehoclient.api.hud.Timer;
import dev.tehoclient.api.hud.Title;
import dev.tehoclient.api.info.ClientInfo;
import dev.tehoclient.api.keybind.Keybind;
import dev.tehoclient.api.modules.CombatRules;
import dev.tehoclient.api.notify.Notification;
import dev.tehoclient.api.players.Nametag;
import dev.tehoclient.api.players.TabList;
import dev.tehoclient.api.players.Team;
import dev.tehoclient.api.social.Presence;
import dev.tehoclient.api.ui.Node;
import dev.tehoclient.api.ui.Screen;
import dev.tehoclient.api.world.Beam;
import dev.tehoclient.api.world.EntityStyle;
import dev.tehoclient.api.world.Hologram;
import dev.tehoclient.api.world.Shape;

final class TestCommand implements CommandExecutor {
	static final List<String> PARTS = Arrays.asList("all", "hud", "world", "players", "social", "keybinds", "rules", "combat", "ui",
			"camera", "cutscene", "audio", "info", "clip", "broadcast", "verify", "clear");

	static final Map<UUID, double[]> HOLOGRAMS = new java.util.HashMap<>();
	static final Map<UUID, String> DIMENSIONS = new java.util.HashMap<>();
	static final Map<UUID, Integer> CLICKS = new java.util.HashMap<>();
	static final Map<UUID, String> SELECTED = new java.util.HashMap<>();

	private final JavaPlugin plugin;

	static Hologram hologram(UUID player, String hint) {
		double[] at = HOLOGRAMS.get(player);
		return Hologram.at("test_holo", at[0], at[1], at[2]).lines("§bTeho hologram", hint, "§7clickable")
				.item("diamond").clickable(true).dimension(DIMENSIONS.get(player)).build();
	}

	static void clicked(TehoPlayer teho, Player player) {
		if (!HOLOGRAMS.containsKey(player.getUniqueId())) {
			return;
		}
		int count = CLICKS.merge(player.getUniqueId(), 1, Integer::sum);
		teho.world().hologram(hologram(player.getUniqueId(), "§aClicked " + count + (count == 1 ? " time" : " times")));
		teho.notify(Notification.builder("holo").title("Hologram clicked").body("Clicked " + count + (count == 1 ? " time" : " times"))
				.accent(0x7CDB8A).duration(2500).build());
	}

	TestCommand(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Run this in game");
			return true;
		}
		Player player = (Player) sender;
		Optional<TehoPlayer> found = BukkitTeho.player(player);
		if (!found.isPresent()) {
			player.sendMessage(ChatColor.RED + "You are not on Teho, or the Server API is off for this server in Teho settings");
			return true;
		}
		TehoPlayer teho = found.get();
		String part = args.length == 0 ? "" : args[0].toLowerCase(java.util.Locale.ROOT);
		if (!PARTS.contains(part)) {
			player.sendMessage(ChatColor.AQUA + "/tehotest <" + String.join("|", PARTS) + ">");
			player.sendMessage(ChatColor.GRAY + "Your client enabled: " + teho.capabilities());
			return true;
		}
		switch (part) {
			case "all":
				hud(teho, player);
				world(teho, player);
				players(teho, player);
				social(teho, player);
				keybinds(teho, player);
				rules(teho, player);
				ui(teho, player);
				break;
			case "hud":
				hud(teho, player);
				break;
			case "world":
				world(teho, player);
				break;
			case "players":
				players(teho, player);
				break;
			case "social":
				social(teho, player);
				break;
			case "keybinds":
				keybinds(teho, player);
				break;
			case "rules":
				rules(teho, player);
				break;
			case "combat":
				boolean on = teho.modules().combat(CombatRules.LEGACY_PVP);
				say(player, "combat", on, "1.8 swing and hit colour are on. Run /tehotest clear to reset");
				break;
			case "ui":
				ui(teho, player);
				break;
			case "camera":
				camera(teho, player);
				break;
			case "cutscene":
				cutscene(teho, player);
				break;
			case "audio":
				audio(teho, player);
				break;
			case "info":
				info(teho, player);
				break;
			case "clip":
				clip(teho, player);
				break;
			case "broadcast":
				broadcast(teho, player);
				break;
			case "verify":
				verify(teho, player);
				break;
			case "clear":
				clear(teho, player);
				break;
			default:
				break;
		}
		return true;
	}

	private void hud(TehoPlayer teho, Player player) {
		teho.notify(Notification.builder("tehotest").title("Teho test").body("Notifications work").accent(0x4DA9FF).duration(5000).build());
		teho.hud().cooldown(Cooldown.builder("test_pearl", 15000).label("Ender Pearl").item("ender_pearl").color(0x5FE0DC).build());
		teho.hud().cooldown(Cooldown.builder("test_ability", 8000).label("Dash").item("feather").color(0xFFC24D).build());
		teho.hud().timer(Timer.countdown("test_round", "Round ends", 120000).color(0xFF5555));
		teho.hud().sidebar(Sidebar.replace("Teho Test")
				.row(Sidebar.Row.of("map", "Map").value("Lighthouse").item("compass"))
				.row(Sidebar.Row.of("kills", "Kills").value("3").color(0x7CDB8A))
				.row(Sidebar.Row.of("bed", "Bed").value("Alive").color(0x5FE0DC))
				.row(Sidebar.Row.of("progress", "Capture").progress(0.65).color(0xFFC24D))
				.accent(0x4DA9FF).footer("play.example.net").build());
		teho.hud().bar(Bar.builder("test_bar", "Dragon health", 0.72).color(0xB07CFF).style(Bar.Style.SEGMENTED).ttl(30000).build());
		teho.hud().bar(Bar.builder("test_bar2", "Wave 3 of 5", 0.6).color(0x5FE0DC).style(Bar.Style.NOTCHED).ttl(30000).build());
		teho.hud().kill(player.getName(), player.getUniqueId(), "Steve", UUID.nameUUIDFromBytes("steve".getBytes()), "diamond_sword", 0xFF5555);
		teho.hud().feed("Blue team captured the flag", 0x4DA9FF);
		teho.hud().title(Title.of("Round 1").subtitle("Fight!").animation(Title.Animation.POP).times(300, 2500, 500).color(0xFFC24D).build());
		teho.hud().tint(0x80FF0000, true, 0.6, 400, 2500);
		teho.hud().tntFuse(80);
		say(player, "hud", true, "toast, 2 cooldowns, round timer, sidebar, 2 bars, kill feed, title, red vignette");
	}

	private void world(TehoPlayer teho, Player player) {
		Location at = player.getLocation();
		String dimension = BukkitTeho.dimensionOf(player.getWorld());
		double x = at.getX();
		double y = at.getY();
		double z = at.getZ();
		teho.world().beam(Beam.at("test_beam", x + 6, y, z).color(0xFF4DA9FF).height(1024).dimension(dimension).build());
		HOLOGRAMS.put(player.getUniqueId(), new double[] {x + 3, y + 2.5, z + 3});
		DIMENSIONS.put(player.getUniqueId(), dimension);
		CLICKS.put(player.getUniqueId(), 0);
		teho.world().hologram(hologram(player.getUniqueId(), "Right-click me"));
		teho.world().shape(Shape.cuboid("test_cuboid", x - 4, y, z - 4, x + 4, y + 3, z + 4).color(0xFF7CDB8A).fill(0x307CDB8A).label("Arena")
				.dimension(dimension).build());
		teho.world().shape(Shape.sphere("test_sphere", x, y + 1, z + 10, 3).color(0xFFFFC24D).dimension(dimension).build());
		teho.world().shape(Shape.cylinder("test_storm", x, y - 2, z, 20, 30).color(0xFFB07CFF).lineWidth(2).dimension(dimension).build());
		teho.world().style(EntityStyle.of(player.getUniqueId()).glow(0xFF5FE0DC).ttl(30000).build());
		teho.world().borderColor(0xFFFF4DA9, false);
		teho.world().fireColor(player.getUniqueId(), 0xFF5FE0DC);
		say(player, "world", true, "beam to your east, a hologram, an arena box, a sphere, a storm ring, pink border, cyan fire. Glow shows on you to others");
	}

	private void players(TehoPlayer teho, Player player) {
		UUID me = player.getUniqueId();
		teho.players().nametag(Nametag.of(me).above("§6Champion").prefix("§b[Teho] ").suffix(" §7★").build());
		teho.players().tabList(TabList.update().header("§bTeho Test Server", "§7tab header from the Server API")
				.footer("§7play.example.net").entry(TabList.Entry.of(me).group("Blue").ping("12ms").order(0).color(0x4DA9FF)).hidePing(true)
				.build());
		teho.players().team(Team.builder("blue").name("Blue").color(0x4DA9FF).member(me).markers(true).health(true).compass(true).build());
		say(player, "players", true, "nametag line, prefix and suffix (F5 to see), tab header/footer/group, blue team");
	}

	private void social(TehoPlayer teho, Player player) {
		teho.social().presence(Presence.builder("Teho Test").map("Lighthouse").state("Testing the Server API").party(1, 4).startedNow().build());
		Map<String, String> links = new LinkedHashMap<>();
		links.put("Discord", "https://discord.gg/teho");
		links.put("Store", "https://tho.pics");
		teho.social().links(links);
		say(player, "social", true, "rich presence in Discord and friends, 2 links in your pause menu");
	}

	private void keybinds(TehoPlayer teho, Player player) {
		teho.keybinds().register("Teho Test", Arrays.asList(
				Keybind.builder("dash", "Dash").key('R').description("Dash forward").build(),
				Keybind.builder("party", "Open party menu").key('P').build()));
		say(player, "keybinds", true, "press R or P. They also appear in Teho Settings > Servers");
	}

	private void rules(TehoPlayer teho, Player player) {
		boolean on = teho.modules().disable("zoom", "minimap");
		say(player, "rules", on, "zoom and minimap are disabled. Open the Teho mods panel to see the banner and locks");
	}

	private void ui(TehoPlayer teho, Player player) {
		Screen screen = Screen.modal("tehotest", "Teho Test Shop").width(420)
				.add(Node.tabs("tabs",
						Node.tab("items", "Items",
								Node.grid("grid", 3,
										Node.item("i1", "diamond_sword").tooltip("Sword"),
										Node.item("i2", "bow").tooltip("Bow"),
										Node.item("i3", "golden_apple").tooltip("Golden apple")),
								Node.text("price", "Pick an item. Each costs 10 coins"),
								Node.button("buy", "Buy selected")),
						Node.tab("settings", "Settings",
								Node.toggle("music", "Lobby music", true),
								Node.slider("difficulty", "Difficulty", 1, 5, 1, 3),
								Node.select("team", "Team", Arrays.asList("Blue", "Red", "Green"), "Blue"),
								Node.input("name", "Party name", "Type a name and press Enter", 24),
								Node.text("partyset", "")),
						Node.tab("you", "You",
								Node.head("head", player.getUniqueId(), 48),
								Node.progress("xp", "Season progress", 0.4),
								Node.countdown("ends", "Season ends", System.currentTimeMillis() + 3600000L),
								Node.divider("div"),
								Node.button("close", "Close"))))
				.onInteract((who, interaction) -> {
					player.sendMessage(ChatColor.GRAY + "UI event: " + interaction);
					String node = interaction.node();
					if (interaction.clicked("close")) {
						who.ui().close("tehotest");
					} else if (node.startsWith("i") && node.length() == 2) {
						String item = interaction.value();
						SELECTED.put(player.getUniqueId(), item);
						who.ui().patch("tehotest", Node.text("price", item.isEmpty() ? "Pick an item. Each costs 10 coins"
								: "Selected " + item.replace('_', ' ') + " for 10 coins"));
					} else if (interaction.clicked("buy")) {
						String item = SELECTED.getOrDefault(player.getUniqueId(), "");
						who.ui().patch("tehotest", Node.text("price", item.isEmpty() ? "Pick an item first" : "Bought " + item.replace('_', ' ') + "!"));
					} else if ("name".equals(node)) {
						who.ui().patch("tehotest", Node.text("partyset", "Party name set to " + interaction.value()));
					}
				})
				.build();
		boolean on = teho.ui().open(screen);
		say(player, "ui", on, "a 3-tab shop screen. Every click is echoed in chat");
	}

	private void camera(TehoPlayer teho, Player player) {
		Location at = player.getLocation();
		teho.camera().shake(1.5, 1200, 12);
		teho.camera().fovPunch(10, 600);
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> teho.camera().lookAt(at.getX() + 10, at.getY() + 5, at.getZ(), 600, 1500), 30L);
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> teho.camera().letterbox(true, 0.12, 400), 70L);
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> teho.camera().letterbox(false, 0, 400), 130L);
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> teho.camera().fade(0xFF000000, 400, 600, 400), 150L);
		say(player, "camera", true, "shake + FOV punch, look-at, letterbox, then a fade to black over ~8 seconds");
	}

	private void cutscene(TehoPlayer teho, Player player) {
		Location at = player.getLocation();
		double x = at.getX();
		double y = at.getY();
		double z = at.getZ();
		String dimension = BukkitTeho.dimensionOf(player.getWorld());
		boolean on = teho.camera().cutscene(Cutscene.builder("tehotest")
				.frame(0, x + 12, y + 8, z, 90, 25)
				.frame(3000, x, y + 12, z + 12, 180, 40, 0, 70, Cutscene.Ease.SMOOTH_PATH)
				.frame(6000, x - 12, y + 6, z, 270, 15, 0, 60, Cutscene.Ease.SMOOTH_PATH)
				.frame(9000, x, y + 3, z - 6, 0, 10, 0, 70, Cutscene.Ease.IN_OUT)
				.skippable(true).hideHud(true).letterbox(true).dimension(dimension).build());
		say(player, "cutscene", on, "a 9 second orbit. Press your skip key or Escape to skip");
	}

	private void audio(TehoPlayer teho, Player player) {
		Location at = player.getLocation();
		teho.audio().play(Sound.vanilla("test_level", "minecraft:entity.player.levelup").volume(1).build());
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> teho.audio().play(Sound.vanilla("test_east", "minecraft:block.note_block.bell")
				.at(at.getX() + 6, at.getY() + 1, at.getZ()).volume(2).pitch(1.2).build()), 30L);
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> teho.audio().play(Sound.vanilla("test_west", "minecraft:block.note_block.bell")
				.at(at.getX() - 6, at.getY() + 1, at.getZ()).volume(2).pitch(0.8).build()), 60L);
		say(player, "audio", true, "a level-up now, then a high bell from the east (1.5 s) and a low bell from the west (3 s)");
	}

	private void info(TehoPlayer teho, Player player) {
		teho.info().request("Teho test", ClientInfo.Field.values()).thenAccept(result -> plugin.getServer().getScheduler().runTask(plugin, () -> {
			player.sendMessage(ChatColor.AQUA + "Info granted: " + result.granted());
			player.sendMessage(ChatColor.GRAY + "input=" + result.input() + " fps=" + result.fps() + " p99=" + result.frameP99Ms() + "ms render="
					+ result.renderDistance() + " locale=" + result.locale() + " gui=" + result.guiScale() + " window=" + result.width() + "x"
					+ result.height() + " mods=" + result.mods().size());
		}));
		say(player, "info", true, "accept the consent prompt, then the answer prints here");
	}

	private void clip(TehoPlayer teho, Player player) {
		teho.clips().mark("tehotest", "Teho test clip", 8000, 2000).thenAccept(clip -> plugin.getServer().getScheduler().runTask(plugin,
				() -> player.sendMessage(ChatColor.AQUA + "Clip: " + clip)));
		say(player, "clip", true, "turn on the replay buffer in Teho Settings > Servers first. Saves the last 8 s into .minecraft/teho-clips");
	}

	private void broadcast(TehoPlayer teho, Player player) {
		Observer.Builder observer = Observer.builder("Teho Cup Finals").subtitle("Game 2");
		observer.team("Blue", 0xFF4DA9FF).score(2).player(player.getUniqueId(), player.getName(), 0.8, true, 4, "diamond_sword");
		observer.team("Red", 0xFFFF5555).score(1).player(UUID.nameUUIDFromBytes("steve".getBytes()), "Steve", 0.3, true, 1, "bow");
		boolean on = teho.broadcast().observer(observer.build());
		teho.broadcast().follow(player.getUniqueId(), BroadcastApi.Follow.ORBIT, 6, true);
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> teho.broadcast().stopFollowing(), 160L);
		say(player, "broadcast", on, "observer HUD with two teams, and an orbit camera on you for 8 seconds");
	}

	private void verify(TehoPlayer teho, Player player) {
		teho.verify().identity().whenComplete((proof, error) -> plugin.getServer().getScheduler().runTask(plugin,
				() -> player.sendMessage(ChatColor.AQUA + "Identity: " + (error != null ? error.getMessage() : proof))));
		teho.verify().integrity().whenComplete((proof, error) -> plugin.getServer().getScheduler().runTask(plugin,
				() -> player.sendMessage(ChatColor.AQUA + "Integrity: " + (error != null ? error.getMessage() : proof))));
		say(player, "verify", true, "needs the new backend deployed; on the dev launcher integrity may be refused");
	}

	private void clear(TehoPlayer teho, Player player) {
		teho.hud().clearCooldowns();
		teho.hud().removeTimer("test_round");
		teho.hud().clearSidebar();
		teho.hud().removeBars("test_bar", "test_bar2");
		teho.hud().clearTint();
		teho.world().clear();
		teho.world().clearStyles(player.getUniqueId());
		teho.world().resetBorder();
		teho.world().resetFire(player.getUniqueId());
		teho.players().clearNametags(player.getUniqueId());
		teho.players().tabList(TabList.reset());
		teho.players().removeTeams("blue");
		teho.social().clearPresence();
		teho.social().links(new LinkedHashMap<String, String>());
		teho.keybinds().clear();
		teho.modules().clear();
		teho.modules().combat(CombatRules.VANILLA);
		teho.ui().close("tehotest");
		teho.audio().stopAll(300);
		teho.broadcast().observer(Observer.off());
		teho.broadcast().stopFollowing();
		teho.camera().stopCutscene("tehotest");
		player.sendMessage(ChatColor.GREEN + "Cleared everything /tehotest set");
	}

	private static void say(Player player, String part, boolean sent, String what) {
		player.sendMessage((sent ? ChatColor.GREEN + "✔ " : ChatColor.RED + "✖ ") + ChatColor.WHITE + part + ChatColor.GRAY + " - "
				+ (sent ? what : "not sent: your client did not enable this capability"));
	}
}
