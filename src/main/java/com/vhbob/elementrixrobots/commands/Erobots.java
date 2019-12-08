package com.vhbob.elementrixrobots.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.vhbob.elementrixrobots.Robots;
import com.vhbob.elementrixrobots.utils.Robot;
import com.vhbob.elementrixrobots.utils.Utils;

public class Erobots implements CommandExecutor {

	private Robots plugin;

	public Erobots(Robots plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("Erobots")) {
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("give")) {
					if (sender.hasPermission("erobots.admin")) {
						if (Bukkit.getPlayer(args[1]) != null) {
							// Give target the robot
							Player target = Bukkit.getPlayer(args[1]);
							Robot r = new Robot(target.getUniqueId(), 0, "Robot", plugin);
							target.getInventory().addItem(r.getEgg());
							printMessage("messages.commands.give-egg", null, sender.getName(), args[1], sender);
							printMessage("messages.commands.receive-egg", null, sender.getName(), args[1], target);
						} else {
							printMessage("messages.commands.no-target", null, sender.getName(), args[1], sender);
						}
					} else {
						printMessage("messages.commands.missing-perm", null, sender.getName(), args[1], sender);
					}
				} else {
					printMessage("messages.commands.default", null, sender.getName(), null, sender);
				}
			} else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					if (sender.hasPermission("erobots.admin")) {
						// Do reload
						plugin.reloadConfig();
						printMessage("messages.commands.reloaded", null, sender.getName(), null, sender);
					} else {
						printMessage("messages.commands.missing-perm", null, sender.getName(), null, sender);
					}
				} else {
					printMessage("messages.commands.default", null, sender.getName(), null, sender);
				}
			} else {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					// Calc number of robots
					int robots = 0;
					if (Robots.getRobots().containsKey(p.getUniqueId())) {
						robots = Robots.getRobots().get(p.getUniqueId()).size();
					}
					// Open the Robots GUI
					Utils.openRobotPage(p, 1, plugin, robots);
				} else {
					printMessage("messages.commands.default", null, sender.getName(), null, sender);
				}
			}
		}
		return false;
	}

	private void printMessage(String configSection, Robot r, String sender, String target, CommandSender recepient) {
		for (String s : plugin.getConfig().getStringList(configSection)) {
			recepient.sendMessage(Utils.parsePlaceholders(s, r, sender, target, -1));
		}
	}

}
