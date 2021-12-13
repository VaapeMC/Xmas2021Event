package me.vaape.xmas2021event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class Xmas2021Event extends JavaPlugin implements Listener{
	
	private FileConfiguration config = this.getConfig();
	
	public static Xmas2021Event instance;
	private static DamageListener damageListener;
	
	public List<Location> spawnPoints;
	
	public static Xmas2021Event getInstance() {
		return instance;
	}
	
	public void onEnable() {
		instance = this;
		getServer().getPluginManager().registerEvents(this, this);
		loadConfiguration();
		getLogger().info(ChatColor.GREEN + "Xmas2021Event has been enabled!");
		
		damageListener = new DamageListener();
	    this.getServer().getPluginManager().registerEvents(damageListener, instance);
	    
	    spawnPoints = (List<Location>) config.getList("spawn points");
	    
	    BukkitRunnable spawnTimer = new BukkitRunnable() {
	    	
			@Override
			public void run() {
				
				//Choose random spawn points
				List<Location> pickedLocations = pickNRandom(spawnPoints, 10);
				
				//Only spawn if there are less than 2 snowmen in that location's chunk
				for (Location spawnPoint : pickedLocations) {
					
					Chunk chunk = spawnPoint.getChunk();
					int numberOfSnowmen = 0;
					boolean shouldSpawn = true;
					
					//Count all snowmen in chunk, if more than 2 then do not spawn
					for (Entity entity : chunk.getEntities()) {
						if (entity.getType() == EntityType.SNOWMAN) {
							numberOfSnowmen ++;
						}
						if (numberOfSnowmen > 1) {
							shouldSpawn = false;
							break;
						}
					}
					
					if (shouldSpawn) {
						if (chunk.isLoaded()) {
							Bukkit.getServer().broadcastMessage(ChatColor.DARK_GREEN + "Spawning snowman");
							Snowman snowman = (Snowman) spawnPoint.getWorld().spawnEntity(spawnPoint, EntityType.SNOWMAN);
							snowman.setHealth(4);
						}
					}
				}
				
				//Remove all snowmen if more than 30 in spawn
				int numberOfSnowmenInSpawn = 0;
				
				for (LivingEntity livingEntity : Bukkit.getServer().getWorld("world").getLivingEntities()) {
					
					if (livingEntity.getType() != EntityType.SNOWMAN) {
						continue;
					}
					
					//If in spawn
					if (livingEntity.getLocation().getX() > -100 && livingEntity.getLocation().getX() < 250 &&
						livingEntity.getLocation().getZ() > -100 && livingEntity.getLocation().getZ() < 350) {
						numberOfSnowmenInSpawn ++;
					}
				}
				
				if (numberOfSnowmenInSpawn > 30) {
					for (LivingEntity livingEntity : Bukkit.getServer().getWorld("world").getLivingEntities()) {
						if (livingEntity.getType() == EntityType.SNOWMAN) {
							livingEntity.remove();
							Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + "Removing snowman");
						}
					}
				}
			}
		};
		
		spawnTimer.runTaskTimer(instance, 20, 15 * 20);
	}
	
	public void loadConfiguration() {
		config.set(("time of server start"), new Date());
		config.options().copyDefaults(true);
		saveConfig();
	}
	
	public void onDisable(){
		saveConfig();
		instance = null;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (cmd.getName().equalsIgnoreCase("spawnsnowman")) {
			if (sender.hasPermission("xmas2021event.spawnsnowman")) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					Location location = player.getLocation();
					Snowman snowman = (Snowman) location.getWorld().spawnEntity(location, EntityType.SNOWMAN);
					snowman.setHealth(4);
					return true;
				}
				else {
					sender.sendMessage(ChatColor.RED + "You must be a player to do that.");
				}
			}
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
		}
		
		else if (cmd.getName().equalsIgnoreCase("AddSnowmanPoint")) {
			if (sender.hasPermission("xmas2021event.addsnowmanpoint")) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					Location location = player.getLocation();

					spawnPoints.add(location);
					config.set("spawn points", spawnPoints);
					saveConfig();
					
					player.sendMessage(ChatColor.GREEN + "Successfully added spawnpoint.");

				}
				else {
					sender.sendMessage(ChatColor.RED + "You must be a player to do that.");
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
			}
		}
		
		else if (cmd.getName().equalsIgnoreCase("AddFirstSnowmanPoint")) {
			if (sender.hasPermission("xmas2021event.addsnowmanpoint")) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					Location location = player.getLocation();
					List<Location> locations = new ArrayList<>();
					locations.add(location);
					
					config.set("spawn points", locations);
					saveConfig();
					
					spawnPoints = (List<Location>) config.getList("spawn points");
					
					player.sendMessage(ChatColor.GREEN + "Successfully added spawnpoint.");
				}
				else {
					sender.sendMessage(ChatColor.RED + "You must be a player to do that.");
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
			}		
		}	
		return false;
	}
	
	public List<Location> pickNRandom(List<Location> list, int n) {
		List<Location> copy = new ArrayList<Location>(list);
	    Collections.shuffle(copy);
	    return n > copy.size() ? copy.subList(0, copy.size()) : copy.subList(0, n);
	}
}