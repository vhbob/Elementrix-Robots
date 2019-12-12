package com.vhbob.elementrixrobots;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.vhbob.elementrixrobots.commands.Erobots;
import com.vhbob.elementrixrobots.events.ExploitPreventionEvents;
import com.vhbob.elementrixrobots.events.ModifyRobotEvents;
import com.vhbob.elementrixrobots.events.RobotActionEvents;
import com.vhbob.elementrixrobots.events.RobotEggEvents;
import com.vhbob.elementrixrobots.utils.Robot;
import com.vhbob.elementrixrobots.utils.RobotStorageFile;
import com.vhbob.elementrixrobots.utils.RobotTask;
import net.milkbowl.vault.economy.Economy;

public class Robots extends JavaPlugin {

	private static HashMap<UUID, ArrayList<Robot>> robots;
	// Modifying will contain a player and the page they are viewing / index of the
	// robot they are modifying
	private static HashMap<Player, Integer> modifying;
	private static HashMap<Player, Integer> modifyingRobot;
	private static HashMap<Robot, ArrayList<LivingEntity>> killed;
	private static Economy econ;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		final Robots r = this;
		new BukkitRunnable() {

			public void run() {
				getCommand("Erobots").setExecutor(new Erobots(r));
				if (!setupEconomy()) {
					Bukkit.getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!",
							getDescription().getName()));
					getServer().getPluginManager().disablePlugin(r);
					return;
				}
				// Load all pre-existing robots (loop thru files in directory)
				robots = new HashMap<UUID, ArrayList<Robot>>();
				File dir = new File(getDataFolder() + File.separator + "robots");
				File[] dirs = dir.listFiles();
				if (dirs != null) {
					for (File file : dirs) {
						UUID id = UUID.fromString(file.getName().replace(".yml", ""));
						System.out.println(id);
						RobotStorageFile storageFile = new RobotStorageFile(id, r);
						ArrayList<Robot> robotsList = storageFile.getRobots();
						if (robots != null)
							robots.put(id, robotsList);
					}
				}
				modifying = new HashMap<Player, Integer>();
				modifyingRobot = new HashMap<Player, Integer>();
				killed = new HashMap<Robot, ArrayList<LivingEntity>>();
				Bukkit.getPluginManager().registerEvents(new RobotEggEvents(r), r);
				Bukkit.getPluginManager().registerEvents(new ModifyRobotEvents(r), r);
				Bukkit.getPluginManager().registerEvents(new ExploitPreventionEvents(r), r);
				Bukkit.getPluginManager().registerEvents(new RobotActionEvents(r), r);
				// Schedule Tasks
				scheduleTasks();

			}
		}.runTaskLater(this, 10);
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Elementrix Robots has been enabled!");
	}

	@Override
	public void onDisable() {
		// Save all robot files with UUID's from activeRobots
		if (!robots.isEmpty())
			for (UUID id : robots.keySet()) {
				RobotStorageFile dataFile = new RobotStorageFile(id, this);
				dataFile.saveRobots();
				for (Robot robot : robots.get(id)) {
					if (robot.getStand() != null) {
						robot.despawn();
					}
				}
			}
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Elementrix Robots has been successfully disabled!");
	}

	public static HashMap<UUID, ArrayList<Robot>> getRobots() {
		return robots;
	}

	public static HashMap<Player, Integer> getModifying() {
		return modifying;
	}

	public static HashMap<Player, Integer> getModifyingRobot() {
		return modifyingRobot;
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public static Economy getEconomy() {
		return econ;
	}

	private void scheduleTasks() {
		// Repeatedly loop through robots to see which should be grinding
		new BukkitRunnable() {
			public void run() {
				for (ArrayList<Robot> list : robots.values()) {
					for (Robot r : list) {
						// Stop if not spawned or deactive
						if (r.getStand() == null || r.getTask() == RobotTask.NONE)
							continue;
						// Check if Grinding
						if (r.getTask() == RobotTask.GRIND) {
							for (Entity e : r.getStand().getNearbyEntities(4, 4, 4)) {
								if (e.getType().isSpawnable() && e instanceof LivingEntity) {
									LivingEntity m = (LivingEntity) e;
									// Damage calc / drops if needed
									double dmg = 7 + 1.25 * r.getLevel();
									if (m.getHealth() <= dmg) {
										ArrayList<LivingEntity> killedEntities = new ArrayList<LivingEntity>();
										if (killed.get(r) != null) {
											killedEntities = killed.get(r);
										}
										killedEntities.add(m);
										killed.put(r, killedEntities);
									}
									m.damage(dmg);
								}
							}
						}
					}
				}
			}
		}.runTaskTimer(this, 30, 30);
	}

	public static HashMap<Robot, ArrayList<LivingEntity>> getKilled() {
		return killed;
	}
}