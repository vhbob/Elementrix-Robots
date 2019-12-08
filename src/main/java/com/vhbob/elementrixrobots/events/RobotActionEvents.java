package com.vhbob.elementrixrobots.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.vhbob.elementrixrobots.Robots;
import com.vhbob.elementrixrobots.utils.Robot;

public class RobotActionEvents implements Listener {

	private static Robots plugin;

	public RobotActionEvents(Robots plugin) {
		RobotActionEvents.plugin = plugin;
	}

	@EventHandler
	public void onKill(EntityDeathEvent e) {
		for (Robot r : Robots.getKilled().keySet()) {
			if (Robots.getKilled().get(r) != null && Robots.getKilled().get(r).contains(e.getEntity())) {
				Robots.getKilled().get(r).remove(e.getEntity());
				List<ItemStack> drops = e.getDrops();
				if (r.getLinkedChest() != null) {
					List<ItemStack> successful = new ArrayList<ItemStack>();
					for (ItemStack i : drops) {
						int multi = 1;
						if (plugin.getConfig().getInt("tiers.looting." + r.getLevel()) > 0) {
							multi = new Random()
									.nextInt(plugin.getConfig().getInt("tiers.looting." + r.getLevel()) + 1);
						}
						i.setAmount(i.getAmount() + multi);
						if (r.getLinkedChest().getInventory().addItem(i).isEmpty())
							successful.add(i);
					}
					for (ItemStack i : successful)
						drops.remove(i);

				}
			}
		}
	}

}
