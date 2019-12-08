package com.vhbob.elementrixrobots.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.vhbob.elementrixrobots.Robots;

public class Utils {

	public static String parsePlaceholders(String text, Robot robot, String player, String target, int robots) {
		// Check to make sure we have all data necessary
		if ((text.contains("%r-") && robot == null) || (text.contains("%player%") && player == null)
				|| (text.contains("%target%") && target == null)) {
			return "There was an error in parsing";
		}
		// Replace placeholders
		if (text.contains("%player%"))
			text = text.replace("%player%", player);
		if (text.contains("%target%"))
			text = text.replace("%target%", target);
		if (text.contains("%r-name%"))
			text = text.replace("%r-name%", robot.getName());
		if (text.contains("%r-task%"))
			text = text.replace("%r-task%", robot.getTask().toString());
		if (text.contains("%r-level%")) {
			int level = robot.getLevel();
			if (level == 0) {
				text = text.replace("%r-level%", "Basic");
			} else {
				text = text.replace("%r-level%", Integer.toString(robot.getLevel()));
			}
		}
		if (text.contains("%r-spawned%")) {
			String rpl = ChatColor.RED + "False";
			if (robot.isSpawned())
				rpl = ChatColor.GREEN + "True";
			text = text.replace("%r-spawned%", rpl);
		}
		if (text.contains("%robots%"))
			text = text.replace("%robots%", Integer.toString(robots));
		if (text.contains("\n"))
			text = text.replace("\n", "" + "\n" + "");
		return ChatColor.translateAlternateColorCodes('&', text);
	}

	public static ItemStack itemBuilder(Material type, String reference, Robots plugin, String player, String target,
			int robots, Robot r) {
		ItemStack i = new ItemStack(type);
		ItemMeta im = i.getItemMeta();
		ArrayList<String> lore = parsedList(plugin.getConfig().getStringList("inv." + reference + "-lore"), r, player,
				target, robots);
		im.setDisplayName(parsePlaceholders(plugin.getConfig().getString("inv." + reference + "-name"), r, player,
				target, robots));
		im.setLore(lore);
		i.setItemMeta(im);
		return i;
	}

	public static ArrayList<String> parsedList(List<String> list, Robot r, String player, String target, int robots) {
		ArrayList<String> parsed = new ArrayList<String>();
		for (String s : list) {
			parsed.add(parsePlaceholders(s, r, player, target, robots));
		}
		return parsed;
	}

	public static void openRobotPage(Player p, int page, Robots plugin, int robots) {
		Inventory inv = Bukkit.createInventory(null, 54, Utils
				.parsePlaceholders(plugin.getConfig().getString("inv.gui-title"), null, p.getName(), null, robots));
		// Add clickables
		ItemStack redstone = Utils.itemBuilder(Material.REDSTONE, "total", plugin, p.getName(), p.getName(), robots,
				null);
		ItemStack bed = Utils.itemBuilder(Material.BED, "deactivate", plugin, p.getName(), p.getName(), robots, null);
		inv.setItem(4, redstone);
		inv.setItem(7, bed);
		if (robots > 21 * (page)) {
			ItemStack next = Utils.itemBuilder(Material.ARROW, "next", plugin, p.getName(), p.getName(), robots, null);
			inv.setItem(50, next);
		}
		if (page > 1) {
			ItemStack back = Utils.itemBuilder(Material.ARROW, "back", plugin, p.getName(), p.getName(), robots, null);
			inv.setItem(48, back);
		}
		// Fill in panes
		ItemStack pane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 11);
		for (int i = 0; i < 18; i++) {
			if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
				inv.setItem(i, pane);
			}
		}
		for (int i = 18; i < 45; i++) {
			if (i % 9 == 0 || i % 9 == 8)
				inv.setItem(i, pane);
		}
		for (int i = 45; i < 54; i++) {
			if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
				inv.setItem(i, pane);
			}
		}
		// Add robots
		if (robots > 0) {
			ArrayList<Robot> robotsList = Robots.getRobots().get(p.getUniqueId());
			for (int i = 21 * (page - 1); i < 21 * page; i++) {
				if (robotsList.size() <= i)
					break;
				ItemStack rItem = itemBuilder(Material.SKULL_ITEM, "robot", plugin, p.getName(), p.getName(), robots,
						robotsList.get(i));
				ArrayList<String> lore = (ArrayList<String>) rItem.getItemMeta().getLore();
				rItem.setDurability((short) 2);
				ItemMeta RIM = rItem.getItemMeta();
				String spaces = "";
				for (int j = 0; j < i; j++) {
					spaces += " ";
				}
				lore.add(spaces);
				RIM.setLore(lore);
				rItem.setItemMeta(RIM);
				inv.addItem(rItem);
			}
		}
		p.openInventory(inv);
		if (Robots.getModifying().containsKey(p)) {
			Robots.getModifying().remove(p);
		}
		if (Robots.getModifyingRobot().containsKey(p)) {
			Robots.getModifyingRobot().remove(p);
		}
		Robots.getModifying().put(p, page);
	}

	public static void printMessage(String configSection, Robot r, String sender, String target, Player recepient,
			Robots plugin) {
		for (String s : plugin.getConfig().getStringList(configSection)) {
			recepient.sendMessage(Utils.parsePlaceholders(s, r, sender, target, -1));
		}
	}

	public static ArrayList<Block> getBlocks(Block start, int radius) {
		ArrayList<Block> blocks = new ArrayList<Block>();
		for (double x = start.getLocation().getX() - radius; x <= start.getLocation().getX() + radius; x++) {
			for (double y = start.getLocation().getY() - radius; y <= start.getLocation().getY() + radius; y++) {
				for (double z = start.getLocation().getZ() - radius; z <= start.getLocation().getZ() + radius; z++) {
					Location loc = new Location(start.getWorld(), x, y, z);
					blocks.add(loc.getBlock());
				}
			}
		}
		return blocks;
	}

}
