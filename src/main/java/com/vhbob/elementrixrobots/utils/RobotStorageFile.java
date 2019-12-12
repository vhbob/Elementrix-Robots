package com.vhbob.elementrixrobots.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import com.vhbob.elementrixrobots.Robots;

public class RobotStorageFile {

	private File file;
	private YamlConfiguration config;
	private Robots plugin;
	private UUID user;

	public RobotStorageFile(UUID user, Robots plugin) {
		this.plugin = plugin;
		this.user = user;
		this.file = new File(this.plugin.getDataFolder() + "/robots", user.toString() + ".yml");
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Robot> getRobots() {
		ArrayList<Robot> robots = new ArrayList<Robot>();
		if (!config.contains("robots"))
			return null;
		for (String roboID : config.getConfigurationSection("robots").getKeys(false)) {
			String name = config.getString("robots." + roboID + ".name");
			int level = config.getInt("robots." + roboID + ".level");
			RobotTask task = RobotTask.fromString(config.getString("robots." + roboID + ".task"));
			Robot r = new Robot(user, level, name, plugin);
			if (robots.contains(r))
				continue;
			r.setTask(task);
			if (config.contains("robots." + roboID + ".chest")) {
				Location chestLoc = (Location) config.get("robots." + roboID + ".chest");
				if (chestLoc.getBlock() != null && chestLoc.getBlock().getType() == Material.CHEST) {
					Chest c = (Chest) chestLoc.getBlock().getState();
					r.setLinkedChest(c);
				}
			}
			if (config.contains("robots." + roboID + ".location")) {
				Location location = (Location) config.get("robots." + roboID + ".location");
				r.activate(location);
			}
			System.out.println("\n\n\nENABLING " + r.getName());
			robots.add(r);
		}
		return robots;
	}

	public void saveRobots() {
		// Save robots for a certain user
		config.set("robots", null);
		for (Robot r : Robots.getRobots().get(user)) {
			config.set("robots." + r.getId() + ".name", r.getName());
			config.set("robots." + r.getId() + ".level", r.getLevel());
			config.set("robots." + r.getId() + ".task", r.getTask().toString());
			if (r.getLinkedChest() != null)
				config.set("robots." + r.getId() + ".chest", r.getLinkedChest().getLocation());
			if (r.getLoc() != null)
				config.set("robots." + r.getId() + ".location", r.getLoc());
		}
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (Robots.getRobots().get(user).isEmpty()) {
			file.deleteOnExit();
		}
	}

}
