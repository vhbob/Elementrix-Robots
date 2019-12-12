package com.vhbob.elementrixrobots.utils;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.vhbob.elementrixrobots.Robots;

public class Robot {

	private String name;
	private int level;
	private RobotTask task;
	private Chest linkedChest;
	private UUID owner, id;
	private Robots plugin;
	private ArmorStand stand;

	// Constructors
	public Robot(UUID owner, int level, String name, Robots plugin) {
		this.setOwner(owner);
		this.setLevel(level);
		this.setName(name);
		this.plugin = plugin;
		this.setTask(RobotTask.NONE);
		this.id = UUID.randomUUID();
	}

	// Field Manipulators
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		if (stand != null)
			stand.setCustomName(ChatColor.DARK_AQUA + name);
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public RobotTask getTask() {
		return task;
	}

	public void setTask(RobotTask task) {
		this.task = task;
	}

	public Location getLoc() {
		if (stand == null)
			return null;
		return stand.getLocation();
	}

	public void teleport(Location loc) {
		stand.teleport(loc);
	}

	public Chest getLinkedChest() {
		return linkedChest;
	}

	public void setLinkedChest(Chest linkedChest) {
		this.linkedChest = linkedChest;
	}

	public UUID getOwner() {
		return owner;
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
	}

	public UUID getId() {
		return id;
	}

	public boolean isSpawned() {
		if (this.stand == null) {
			return false;
		}
		return true;
	}

	public void setStand(ArmorStand stand) {
		this.stand = stand;
	}

	public void activate(Location loc) {
		if (stand != null)
			despawn();
		ArmorStand newStand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		newStand.setSmall(true);
		newStand.setCustomName(ChatColor.DARK_AQUA + this.name);
		newStand.setCustomNameVisible(true);
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
		SkullMeta headMeta = (SkullMeta) head.getItemMeta();
		headMeta.setOwner(Bukkit.getOfflinePlayer(owner).getName());
		head.setItemMeta(headMeta);
		newStand.setHelmet(head);
		ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta lcm = (LeatherArmorMeta) chest.getItemMeta();
		lcm.setColor(Color.RED);
		chest.setItemMeta(lcm);
		ItemStack leg = new ItemStack(Material.LEATHER_LEGGINGS);
		LeatherArmorMeta llm = (LeatherArmorMeta) leg.getItemMeta();
		llm.setColor(Color.RED);
		leg.setItemMeta(llm);
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
		LeatherArmorMeta lbm = (LeatherArmorMeta) boots.getItemMeta();
		lbm.setColor(Color.GREEN);
		boots.setItemMeta(lbm);
		newStand.setChestplate(chest);
		newStand.setLeggings(leg);
		newStand.setBoots(boots);
		newStand.setGravity(false);
		newStand.setArms(true);
		stand = newStand;
	}

	public void deactivate() {
		this.task = RobotTask.NONE;
	}

	public void despawn() {
		if (stand != null)
			this.getStand().setHealth(0);
	}

	public ArmorStand getStand() {
		return this.stand;
	}

	// This method will construct a spawn egg for the robot
	public ItemStack getEgg() {
		@SuppressWarnings("deprecation")
		ItemStack egg = new ItemStack(Material.MONSTER_EGG, 1, EntityType.ENDERMITE.getTypeId());
		ItemMeta eggm = egg.getItemMeta();
		if (this.level > 0)
			eggm.setDisplayName(
					ChatColor.translateAlternateColorCodes('&', "&3&LRobot Egg &f&l(Tier " + this.level + ")"));
		else
			eggm.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&3&LRobot Egg &f&l(Tier Basic)"));
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("");
		if (this.level > 0) {
			lore.add(ChatColor.translateAlternateColorCodes('&', "&b&lTier: &f" + this.level));
		} else
			lore.add(ChatColor.translateAlternateColorCodes('&', "&b&lTier: &fBasic"));
		lore.add("");
		lore.add(ChatColor.translateAlternateColorCodes('&',
				"&b&lSharpness: &f" + plugin.getConfig().getInt("tiers.sharpness." + this.level)));
		lore.add(ChatColor.translateAlternateColorCodes('&',
				"&b&lLooting: &f" + plugin.getConfig().getInt("tiers.looting." + this.level)));
		lore.add("");
		lore.add(ChatColor.translateAlternateColorCodes('&', "&b&l* &7(( Claim this robot egg to add the robot"));
		lore.add(ChatColor.translateAlternateColorCodes('&',
				"&7to the robot manager. Access the robot manager with /robots ))"));
		lore.add("");
		eggm.setLore(lore);
		egg.setItemMeta(eggm);
		return egg;
	}

}
