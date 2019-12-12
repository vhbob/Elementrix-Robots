package com.vhbob.elementrixrobots.events;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.vhbob.elementrixrobots.Robots;
import com.vhbob.elementrixrobots.utils.Robot;
import com.vhbob.elementrixrobots.utils.Utils;

public class RobotEggEvents implements Listener {

	private Robots plugin;

	public RobotEggEvents(Robots plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void useEgg(PlayerInteractEvent e) {
		// Check if they are using Robot Egg
		if (e.getAction().toString().contains("RIGHT_CLICK") && e.getItem() != null
				&& e.getItem().getType() == Material.MONSTER_EGG) {
			if (e.getItem().hasItemMeta()
					&& ChatColor.stripColor(e.getItem().getItemMeta().getDisplayName()).contains("Robot Egg (Tier ")) {
				Player p = e.getPlayer();
				// Create Robot Object
				String name = ChatColor.stripColor(e.getItem().getItemMeta().getDisplayName());
				String take = name.replace("Robot Egg (Tier ", "");
				Robot r;
				if (take.equalsIgnoreCase("basic)")) {
					r = new Robot(p.getUniqueId(), 0, "Robot", plugin);
				} else {
					int i = Integer.parseInt(take.substring(0, 1));
					r = new Robot(p.getUniqueId(), i, "Robot", plugin);
				}
				// Assign Robot to user
				if (Robots.getRobots().containsKey(p.getUniqueId())) {
					Robots.getRobots().get(p.getUniqueId()).add(r);
				} else {
					ArrayList<Robot> robots = new ArrayList<Robot>();
					robots.add(r);
					Robots.getRobots().put(p.getUniqueId(), robots);
				}
				// Consume Egg
				ItemStack hand = p.getItemInHand();
				hand.setAmount(hand.getAmount() - 1);
				p.setItemInHand(hand);
				printMessage("messages.events.use-egg", r, p.getName(), r.getName(), p);
				e.setCancelled(true);
			}
		}
	}

	private void printMessage(String configSection, Robot r, String sender, String target, CommandSender recepient) {
		for (String s : plugin.getConfig().getStringList(configSection)) {
			recepient.sendMessage(Utils.parsePlaceholders(s, r, sender, target, -1));
		}
	}

}
