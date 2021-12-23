package me.vaape.xmas2021event;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;


public class DamageListener implements Listener{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSpawn (CreatureSpawnEvent event) {
		
		if (event.getEntityType() == EntityType.SNOWMAN) {
			
			event.setCancelled(false);
		}
	}
}