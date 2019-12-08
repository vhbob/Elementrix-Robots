package com.vhbob.elementrixrobots.events;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.vhbob.elementrixrobots.Robots;
import com.vhbob.elementrixrobots.utils.Robot;
import com.vhbob.elementrixrobots.utils.RobotTask;
import com.vhbob.elementrixrobots.utils.Utils;
import com.wasteofplastic.askyblock.ASkyBlockAPI;

public class ModifyRobotEvents implements Listener {

	private Robots plugin;
	private static HashMap<Player, Robot> chestSetting;
	private static HashMap<Player, Robot> nameSetting;
	private static ArrayList<Player> claiming;

	public ModifyRobotEvents(Robots plugin) {
		this.plugin = plugin;
		chestSetting = new HashMap<Player, Robot>();
		nameSetting = new HashMap<Player, Robot>();
		claiming = new ArrayList<Player>();
	}

	// For main GUI
	@SuppressWarnings("deprecation")
	@EventHandler
	public void openRobotMenu(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();
			if (Robots.getModifying().keySet().contains(p)) {
				if (e.getCurrentItem() != null) {
					int page = Robots.getModifying().get(p);
					// Calc number of robots
					int robots = 0;
					if (Robots.getRobots().containsKey(p.getUniqueId())) {
						robots = Robots.getRobots().get(p.getUniqueId()).size();
					}
					// Check if they clicked the despawn all
					if (e.getCurrentItem().equals(Utils.itemBuilder(Material.BED, "deactivate", plugin, p.getName(),
							p.getName(), robots, null))) {
						if (robots > 0) {
							for (Robot r : Robots.getRobots().get(p.getUniqueId())) {
								r.setTask(RobotTask.NONE);
							}
							Utils.printMessage("messages.events.deactivate-all", null, p.getName(), p.getName(), p,
									plugin);
						} else {
							Utils.printMessage("messages.events.deactivate-none", null, p.getName(), p.getName(), p,
									plugin);
						}
						p.closeInventory();
					}
					// Check if they clicked next page
					else if (e.getCurrentItem().equals(Utils.itemBuilder(Material.ARROW, "next", plugin, p.getName(),
							p.getName(), robots, null))) {
						// Open next page
						Utils.openRobotPage(p, page + 1, plugin, robots);
					}
					// Check for previous page
					else if (e.getCurrentItem().equals(Utils.itemBuilder(Material.ARROW, "back", plugin, p.getName(),
							p.getName(), robots, null))) {
						// Open next page
						Utils.openRobotPage(p, page - 1, plugin, robots);
					}
					// Check if they edited a robot (slot)
					if (e.getSlot() > 18 && e.getSlot() < 44 && e.getSlot() % 9 != 0 && e.getSlot() % 9 != 8) {
						int robotIndex = 0;
						for (int i = 18; i < e.getSlot(); i++) {
							if (e.getInventory().getItem(i) != null
									&& !e.getInventory().getItem(i).getType().equals(Material.STAINED_GLASS_PANE))
								robotIndex++;
						}
						if (robots > robotIndex) {
							// Open Inventory
							Robot r = Robots.getRobots().get(p.getUniqueId()).get(robotIndex);
							Inventory inv = Bukkit.createInventory(null, 54,
									Utils.parsePlaceholders(plugin.getConfig().getString("inv.editor-title"), r,
											p.getName(), p.getName(), robots));
							// Add items
							inv.setItem(4, e.getCurrentItem());
							inv.setItem(1, Utils.itemBuilder(Material.REDSTONE_COMPARATOR, "task-change", plugin,
									p.getName(), p.getName(), robots, r));
							inv.setItem(7, Utils.itemBuilder(Material.ENDER_PEARL, "teleport", plugin, p.getName(),
									p.getName(), robots, r));
							inv.setItem(11, Utils.itemBuilder(Material.CHEST, "chest", plugin, p.getName(), p.getName(),
									robots, r));
							inv.setItem(12, Utils.itemBuilder(Material.NAME_TAG, "name", plugin, p.getName(),
									p.getName(), robots, r));
							inv.setItem(15, Utils.itemBuilder(Material.GOLD_INGOT, "upgrade", plugin, p.getName(),
									p.getName(), robots, r));
							ItemStack leave = Utils.itemBuilder(Material.STAINED_GLASS_PANE, "leave", plugin,
									p.getName(), p.getName(), robots, r);
							leave.setDurability((short) 14);
							inv.setItem(17, leave);
							ItemStack egg = Utils.itemBuilder(Material.MONSTER_EGG, "claim", plugin, p.getName(),
									p.getName(), robots, r);
							egg.setDurability(EntityType.ENDERMITE.getTypeId());
							inv.setItem(18, egg);
							// Fill with panes
							ItemStack pane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 11);
							for (int i = 0; i <= 26; i++)
								if (inv.getItem(i) == null)
									inv.setItem(i, pane);
							for (int i = 27; i < inv.getSize(); i++)
								if (inv.getItem(i) == null && i % 9 == 0 || i % 9 == 8)
									inv.setItem(i, pane);
							p.openInventory(inv);
							Robots.getModifying().remove(p);
							Robots.getModifyingRobot().put(p, robotIndex);
						}
					}
				}
				e.setCancelled(true);
			}
		}
	}

	// For individual Robot GUI
	@SuppressWarnings("deprecation")
	@EventHandler
	public void editRobot(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();
			if (Robots.getModifyingRobot().containsKey(p)) {
				// Get robots number and robot currently modifying
				Robot r = Robots.getRobots().get(p.getUniqueId()).get(Robots.getModifyingRobot().get(p));
				int robots = Robots.getRobots().get(p.getUniqueId()).size();
				// Create pre-items
				ItemStack leave = Utils.itemBuilder(Material.STAINED_GLASS_PANE, "leave", plugin, p.getName(),
						p.getName(), robots, r);
				leave.setDurability((short) 14);
				ItemStack egg = Utils.itemBuilder(Material.MONSTER_EGG, "claim", plugin, p.getName(), p.getName(),
						robots, r);
				egg.setDurability(EntityType.ENDERMITE.getTypeId());
				// Check if task change
				if (e.getCurrentItem().equals(Utils.itemBuilder(Material.REDSTONE_COMPARATOR, "task-change", plugin,
						p.getName(), p.getName(), robots, r))) {
					e.getInventory().setItem(28, Utils.itemBuilder(Material.DIAMOND_SWORD, "task-grind", plugin,
							p.getName(), p.getName(), robots, r));
					e.getInventory().setItem(29, Utils.itemBuilder(Material.BARRIER, "task-none", plugin, p.getName(),
							p.getName(), robots, r));
					for (int i = 1; i < 8; i++) {
						e.getInventory().setItem(36 + i, null);
					}
				}
				// Check for each task change
				else if (e.getCurrentItem().equals(Utils.itemBuilder(Material.DIAMOND_SWORD, "task-grind", plugin,
						p.getName(), p.getName(), robots, r))) {
					r.setTask(RobotTask.GRIND);
					Utils.printMessage("messages.events.task-change", r, p.getName(), r.getName(), p, plugin);
					p.closeInventory();
				} else if (e.getCurrentItem().equals(Utils.itemBuilder(Material.BARRIER, "task-none", plugin,
						p.getName(), p.getName(), robots, r))) {
					r.deactivate();
					Utils.printMessage("messages.events.task-change", r, p.getName(), r.getName(), p, plugin);
					p.closeInventory();
				}
				// Check if teleport
				else if (e.getCurrentItem().equals(Utils.itemBuilder(Material.ENDER_PEARL, "teleport", plugin,
						p.getName(), p.getName(), robots, r))) {
					if (ASkyBlockAPI.getInstance().getIslandAt(p.getLocation()) == null) {
						Utils.printMessage("messages.events.invalid-place", r, p.getName(), r.getName(), p, plugin);
						e.setCancelled(true);
						p.closeInventory();
						return;
					} else if (!ASkyBlockAPI.getInstance().getIslandAt(p.getLocation()).getMembers()
							.contains(p.getUniqueId())) {
						Utils.printMessage("messages.events.invalid-place", r, p.getName(), r.getName(), p, plugin);
						e.setCancelled(true);
						p.closeInventory();
						return;
					}
					if (r.getStand() == null)
						r.activate(p.getLocation());
					else
						r.teleport(p.getLocation());
					p.closeInventory();
					Utils.printMessage("messages.events.teleport", r, p.getName(), r.getName(), p, plugin);
				}
				// Check if chest set
				else if (e.getCurrentItem().equals(
						Utils.itemBuilder(Material.CHEST, "chest", plugin, p.getName(), p.getName(), robots, r))) {
					chestSetting.put(p, r);
					p.closeInventory();
					Utils.printMessage("messages.events.add-chest", r, p.getName(), r.getName(), p, plugin);
					final Player taskPlayer = p;
					final Robot taskRobot = r;
					new BukkitRunnable() {
						public void run() {
							if (chestSetting.containsKey(taskPlayer)) {
								chestSetting.remove(taskPlayer);
								Utils.printMessage("messages.events.chest-timeout", taskRobot, taskPlayer.getName(),
										taskRobot.getName(), taskPlayer, plugin);
							}
						}
					}.runTaskLater(plugin, 20 * 30);
				}
				// Check if name change
				else if (e.getCurrentItem().equals(
						Utils.itemBuilder(Material.NAME_TAG, "name", plugin, p.getName(), p.getName(), robots, r))) {
					nameSetting.put(p, r);
					p.closeInventory();
					Utils.printMessage("messages.events.set-name", r, p.getName(), r.getName(), p, plugin);
					final Player taskPlayer = p;
					final Robot taskRobot = r;
					new BukkitRunnable() {
						public void run() {
							if (chestSetting.containsKey(taskPlayer)) {
								chestSetting.remove(taskPlayer);
								Utils.printMessage("messages.events.name-timeout", taskRobot, taskPlayer.getName(),
										taskRobot.getName(), taskPlayer, plugin);
							}
						}
					}.runTaskLater(plugin, 20 * 30);
				}
				// Check if upgrade
				else if (e.getCurrentItem().equals(Utils.itemBuilder(Material.GOLD_INGOT, "upgrade", plugin,
						p.getName(), p.getName(), robots, r))) {
					for (int i = 28; i < 31; i++)
						e.getInventory().setItem(i, null);
					Material[] mats = { Material.SEA_LANTERN, Material.COAL_BLOCK, Material.IRON_BLOCK,
							Material.REDSTONE_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK,
							Material.EMERALD_BLOCK };
					for (int i = 1; i < 8; i++) {
						ItemStack item = Utils.itemBuilder(mats[i - 1], "upgrade-" + i, plugin, p.getName(),
								p.getName(), robots, r);
						if (r.getLevel() == i) {
							ItemMeta im = item.getItemMeta();
							im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
							im.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
							item.setItemMeta(im);
						}
						e.getInventory().setItem(36 + i, item);
					}
				}
				// Check if leave
				else if (e.getCurrentItem().equals(leave)) {
					Utils.openRobotPage(p, 1, plugin, robots);
				}
				// Check if get egg
				else if (e.getCurrentItem().equals(egg)) {
					if (claiming.contains(p)) {
						p.getInventory().addItem(r.getEgg());
						Robots.getRobots().get(p.getUniqueId()).remove(r);
						r.despawn();
						r.deactivate();
						Utils.printMessage("messages.events.take-egg", r, p.getName(), p.getName(), p, plugin);
						r = null;
						p.closeInventory();
						claiming.remove(p);
					} else {
						claiming.add(p);
						final Player runP = p;
						final Robot runR = r;
						Utils.printMessage("messages.events.egg-confirm", r, p.getName(), p.getName(), p, plugin);
						new BukkitRunnable() {
							public void run() {
								if (claiming.contains(runP)) {
									Utils.printMessage("messages.events.name-timeout", runR, runP.getName(),
											runR.getName(), runP, plugin);
									claiming.remove(runP);
								}
							}
						}.runTaskLater(plugin, 20 * 30);
					}
				}
				// Check if using upgrade
				else {
					Material[] mats = { Material.SEA_LANTERN, Material.COAL_BLOCK, Material.IRON_BLOCK,
							Material.REDSTONE_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK,
							Material.EMERALD_BLOCK };
					int found = -1;
					for (int i = 1; i < 8; i++) {
						if (e.getCurrentItem().equals(Utils.itemBuilder(mats[i - 1], "upgrade-" + i, plugin,
								p.getName(), p.getName(), robots, r))) {
							found = i;
							break;
						}
					}
					if (found > 0) {
						if (found != r.getLevel() + 1) {
							Utils.printMessage("messages.events.level-off", r, p.getName(), p.getName(), p, plugin);
						} else if (Robots.getEconomy().getBalance(p) < plugin.getConfig()
								.getDouble("tiers.costs." + found)) {
							Utils.printMessage("messages.events.low-bal", r, p.getName(), p.getName(), p, plugin);
						} else {
							Robots.getEconomy().withdrawPlayer(p, plugin.getConfig().getDouble("tiers.costs." + found));
							r.setLevel(found);
							Utils.printMessage("messages.events.upgraded", r, p.getName(), p.getName(), p, plugin);
						}
					}
				}
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void changeName(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		if (nameSetting.containsKey(p)) {
			Robot r = nameSetting.get(p);
			String newName = e.getMessage();
			if (newName.length() > 16)
				Utils.printMessage("messages.events.name-length", r, p.getName(), r.getName(), p, plugin);
			else {
				nameSetting.remove(p);
				r.setName(newName);
				Utils.printMessage("messages.events.name-set", r, p.getName(), r.getName(), p, plugin);
			}
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void selectChest(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (chestSetting.containsKey(p)) {
			Robot r = chestSetting.get(p);
			if (e.getAction().toString().contains("BLOCK")) {
				Block b = e.getClickedBlock();
				if (b.getType() == Material.CHEST) {
					Utils.printMessage("messages.events.added-chest", r, p.getName(), r.getName(), p, plugin);
					Chest c = (Chest) b.getState();
					r.setLinkedChest(c);
					chestSetting.remove(p);
				}
			}
		}
	}

	@EventHandler
	public void closeInv(InventoryCloseEvent e) {
		if (e.getPlayer() instanceof Player) {
			Player p = (Player) e.getPlayer();
			if (Robots.getModifying().containsKey(p))
				Robots.getModifying().remove(p);
			if (Robots.getModifyingRobot().containsKey(p))
				Robots.getModifyingRobot().remove(p);
		}
	}

}
