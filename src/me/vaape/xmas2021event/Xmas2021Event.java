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
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import me.vaape.rewards.Rewards;
import net.md_5.bungee.api.ChatColor;

public class Xmas2021Event extends JavaPlugin implements Listener{
	
	private FileConfiguration config = this.getConfig();
		
	public static Xmas2021Event instance;
	private static DamageListener damageListener;
	
	public List<Location> spawnPoints = new ArrayList<Location>();
	
	public HashMap<ItemStack, Double> items = new HashMap<ItemStack, Double>();
	
	ItemStack goldenApple = new ItemStack(Material.GOLDEN_APPLE); //				1/10
	ItemStack goldenCarrot = new ItemStack(Material.GOLDEN_CARROT); //				1/10
	ItemStack pumpkin = new ItemStack(Material.PUMPKIN); //							1/10
	ItemStack snowBlock = new ItemStack(Material.SNOW_BLOCK); //					1/10
	ItemStack redDye = new ItemStack(Material.RED_DYE); //							1/10
	ItemStack greenDye = new ItemStack(Material.GREEN_DYE); //						1/10
	ItemStack leatherHorseArmor = new ItemStack(Material.LEATHER_HORSE_ARMOR);//	1/50
	ItemStack leatherHelmet = new ItemStack(Material.LEATHER_HELMET); //			1/50
	ItemStack leatherChest = new ItemStack(Material.LEATHER_CHESTPLATE); //			1/50
	ItemStack leatherLegs = new ItemStack(Material.LEATHER_LEGGINGS); //			1/50
	ItemStack leatherBoots = new ItemStack(Material.LEATHER_BOOTS); //				1/50
	ItemStack salmon = new ItemStack(Material.SALMON); //							1/10
	ItemStack cod = new ItemStack(Material.COD); //									1/10
	ItemStack sweetBerries = new ItemStack(Material.SWEET_BERRIES); //				1/10
	ItemStack bell = new ItemStack(Material.BELL); //								1/100
	ItemStack greenWool = new ItemStack(Material.GREEN_WOOL); //					1/10
	ItemStack redWool = new ItemStack(Material.RED_WOOL); //						1/10
	ItemStack enchantingPot = new ItemStack(Material.EXPERIENCE_BOTTLE); //			1/10
	ItemStack eventClock = new ItemStack(Material.CLOCK); //						1/2000
	ItemStack frozenHeart = new ItemStack(Material.SNOWBALL); //					1/500
	
	
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
	    
	    if (config.getConfigurationSection("spawn points") != null) {
	    	Set<String> IDs = config.getConfigurationSection("spawn points").getKeys(true);
		    
		    for (String ID : IDs) {
		    	
		    	int x = config.getInt("spawn points." + ID + ".x");
		    	int y = config.getInt("spawn points." + ID + ".y");
		    	int z = config.getInt("spawn points." + ID + ".z");
		    	
		    	spawnPoints.add(new Location(Bukkit.getServer().getWorld("world"), x, y, z));
		    }
	    }
	    	    
	    eventClock.setLore(Collections.singletonList(ChatColor.DARK_PURPLE + "Christmas 2021"));
	    List<String> clockLore = new ArrayList<String>();
	    clockLore.add(ChatColor.DARK_PURPLE + "Christmas 2021");
	    clockLore.add(ChatColor.GRAY + "Snowmen killed: 0");
	    eventClock.setLore(clockLore);
	    eventClock.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
	    eventClock.addItemFlags(ItemFlag.HIDE_ENCHANTS);
	    ItemMeta clockMeta = eventClock.getItemMeta();
	    clockMeta.setDisplayName(ChatColor.GREEN + "Christmas Watch");
	    eventClock.setItemMeta(clockMeta);
	    
	    List<String> heartLore = new ArrayList<String>();
	    heartLore.add("A frozen heart ripped from");
	    heartLore.add("a snowman's chest");
	    frozenHeart.setLore(heartLore);
	    frozenHeart.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
	    frozenHeart.addItemFlags(ItemFlag.HIDE_ENCHANTS);	    
	    ItemMeta heartMeta = frozenHeart.getItemMeta();
	    heartMeta.setDisplayName(ChatColor.GREEN + "Frozen Heart");
	    frozenHeart.setItemMeta(heartMeta);
	    
	    items.put(goldenApple, 0.15);
	    items.put(goldenCarrot, 0.1);
	    items.put(pumpkin, 0.1);
	    items.put(snowBlock, 0.1);
	    items.put(redDye, 0.1);
	    items.put(greenDye, 0.1);
	    items.put(leatherHorseArmor, 0.02);
	    items.put(leatherHelmet, 0.02);
	    items.put(leatherChest, 0.02);
	    items.put(leatherLegs, 0.02);
	    items.put(leatherBoots, 0.02);
	    items.put(salmon, 0.1);
	    items.put(cod, 0.1);
	    items.put(sweetBerries, 0.1);
	    items.put(bell, 0.01);
	    items.put(greenWool, 0.1);
	    items.put(redWool, 0.1);
	    items.put(enchantingPot, 0.3);
	    items.put(eventClock, 0.0006);
	    items.put(frozenHeart, 0.011);
	    	    
	    BukkitRunnable spawnTimer = new BukkitRunnable() {
	    	
			@Override
			public void run() {
				
				//Remove dropped items in spawn older than 20 seconds
				for (Entity entity : Bukkit.getServer().getWorld("world").getEntities()) {
					if (entity instanceof Item) {
						Item item = (Item) entity;
						
						if (inSpawn(item.getLocation())) {
							
							if (item.getTicksLived() > (20 * 20)) {
								item.remove();
							}
						}
					}
				}
				
				if (!(Bukkit.getWorld("world").getTime() > 12500 && Bukkit.getWorld("world").getTime() < 24000)) {
					return;
				}
								
				//Remove all snowmen if more than 50 in spawn
				int numberOfSnowmenInSpawn = 0;
				
				for (LivingEntity livingEntity : Bukkit.getServer().getWorld("world").getLivingEntities()) {
					
					if (livingEntity.getType() != EntityType.SNOWMAN) {
						continue;
					}
					
					//If in spawn
					if (inSpawn(livingEntity.getLocation())) {
						numberOfSnowmenInSpawn ++;
					}
				}
				
				if (numberOfSnowmenInSpawn > 25) {
					for (LivingEntity livingEntity : Bukkit.getServer().getWorld("world").getLivingEntities()) {
						if (livingEntity.getType() == EntityType.SNOWMAN) {
							livingEntity.remove();
						}
					}
				}
				
				for (Player player : Bukkit.getServer().getOnlinePlayers()) {
					if (inSpawn(player.getLocation())) {
						
						if (player.getNearbyEntities(10, 10, 10).size() < 10) {
							spawnSnowmanRandomly(player);
							spawnSnowmanRandomly(player);
							spawnSnowmanRandomly(player);
						}
					}
				}
			}
		};
		
		spawnTimer.runTaskTimer(instance, 10 * 20, 15 * 20);
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
					snowman.setMetadata("event", new FixedMetadataValue(instance, "xmas"));
					snowman.setHealth(4);
					return true;
				}
				else {
					sender.sendMessage(ChatColor.RED + "You must be a player to do that.");
				}
			}
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
		}
		
		return false;
	}
	
	@EventHandler
	public void onSnowmanDeath (EntityDeathEvent event) {
		if (event.getEntity() instanceof Snowman) {
			if (event.getEntity().getKiller() != null) {
				Snowman snowman = (Snowman) event.getEntity();
				OfflinePlayer player = snowman.getKiller();
				if (snowman.hasMetadata("event")) {
					
					double random = Math.random();
					int numberOfDrops = 1;
					
					if (random < 0.7) {
						numberOfDrops = 1;
					}
					else if (random >= 0.7 && random < 0.9) {
						numberOfDrops = 2;
					}
					else if (random >= 0.9) {
						numberOfDrops = 3;
					}
					
					event.getDrops().clear();
					
					for (int i = 1; i <= numberOfDrops; i++) {
						ItemStack item = pickRandomItem(items);
						if (item.getType() == Material.SNOWBALL) {
							Rewards.getInstance().giveReward("xmas2021_frozen_heart", player, false);
							Bukkit.getServer().broadcastMessage(ChatColor.of("#3b9dff") + "" + ChatColor.ITALIC + player.getName() + " found a Frozen heart...");
							break;
						}
						event.getDrops().add(item);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onChunkLoad (ChunkLoadEvent event) {
		if (event.getChunk().getX() > -100 && event.getChunk().getX() < 250 &&
				event.getChunk().getZ() > -100 && event.getChunk().getZ() < 350) {
			
			int snowmanCount = 0;
			for (Entity entity : event.getChunk().getEntities()) {
				if (entity instanceof Snowman) {
					snowmanCount ++;
					if (snowmanCount > 2) {
						entity.remove();
						Bukkit.getServer().broadcastMessage("3 snowman in chunk, removing excess snowmen");
					}
				}
			}
		}
			
			
	}
	
	
	
	public List<Location> pickNRandom(List<Location> list, int n) {
		List<Location> copy = new ArrayList<Location>(list);
	    Collections.shuffle(copy);
	    return n > copy.size() ? copy.subList(0, copy.size()) : copy.subList(0, n);
	}
	
	public ItemStack pickRandomItem(HashMap<ItemStack, Double> weightedItems) {
		double total = 0;
		for (ItemStack item : weightedItems.keySet()) {
			double weight = weightedItems.get(item);
			total = total + weight;
		}
		double randomSelection = Math.random() * total;

		//Count up from 0 with increment = each individual weight, when random < counter choose that item
		ItemStack pickedItem = null;
		double counter = 0;
		for (ItemStack item : weightedItems.keySet()) {
			counter = counter + weightedItems.get(item);
			if (counter >= randomSelection) {
				pickedItem = item;
				break;
			}
		}
		return pickedItem;		
	}
	
	public void spawnSnowmanRandomly(Player player) {
		
		Location location = player.getLocation();
		double randomX = (Math.random() - 0.5) * 20; //+-8
		double randomZ = (Math.random() - 0.5) * 20;
		
		location.add(randomX, 5, randomZ);
		
		Snowman snowman = (Snowman) Bukkit.getServer().getWorld("world").spawnEntity(location, EntityType.SNOWMAN);
		snowman.setHealth(3);
		snowman.setMetadata("event", new FixedMetadataValue(instance, "xmas"));
	}
	
	public boolean inSpawn (Location location) {
		if (location.getWorld() == Bukkit.getWorld("world")) {
			if (location.getX() > -100 && location.getX() < 250 &&
					location.getZ() > -100 && location.getZ() < 350) {
				return true;
			}
			return false;
		}
		else return false;
	}
	
	@EventHandler
	public void onSpawnmanTeleport(EntityTeleportEvent event) {
		if (event.getEntity() instanceof Snowman) {
			if (event.getTo().getWorld() == Bukkit.getWorld("world_the_end")) {
				event.setCancelled(true);
				event.getEntity().remove();
			}
		}
	}
	
	@EventHandler
	public void onBedInteract (PlayerBedEnterEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onKill (EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Snowman) {
			if(((Snowman) entity).getKiller() instanceof Player) {
				Player killer = (Player) ((Snowman) entity).getKiller();
				ItemStack hand = killer.getInventory().getItemInMainHand();
				ItemMeta meta = hand.getItemMeta();
				if (meta != null) {
					if (meta.hasLore()) {
						for (String loreLine : meta.getLore()) {
							if (loreLine.contains("Snowmen killed:")) {
								List<String> lore = meta.getLore();
								String[] killLine = lore.get(lore.size() - 1).split("\\s");
								int snowmenkills = Integer.parseInt(killLine[2]);
								String newKillLine = ChatColor.GRAY + killLine[0] + " " + killLine[1] + " " + (snowmenkills + 1);
								lore.set(lore.size() - 1, newKillLine);
								meta.setLore(lore);
								hand.setItemMeta(meta);
								break;
							}
						}
					}
				}
			}
		}
	}
}