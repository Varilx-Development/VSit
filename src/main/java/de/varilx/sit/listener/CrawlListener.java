package de.varilx.sit.listener;

import de.varilx.BaseAPI;
import de.varilx.configuration.VaxConfiguration;
import de.varilx.sit.VSit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import org.bukkit.persistence.PersistentDataType;

public class CrawlListener implements Listener {

    private final VSit plugin;

    public CrawlListener(VSit plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().getOrDefault(plugin.getCrawlKey(), PersistentDataType.BOOLEAN, false)) {
            plugin.stopCrawling(player);
        }
    }

    

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        VaxConfiguration configuration = BaseAPI.get().getConfiguration();
        
        if (configuration.getBoolean("crawl.stop-on-sneak") && 
            event.isSneaking() &&
            player.getPersistentDataContainer().getOrDefault(plugin.getCrawlKey(), PersistentDataType.BOOLEAN, false)) {
            plugin.stopCrawling(player);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        VaxConfiguration configuration = BaseAPI.get().getConfiguration();
        
        if (configuration.getBoolean("crawl.stop-on-damage") && 
            player.getPersistentDataContainer().getOrDefault(plugin.getCrawlKey(), PersistentDataType.BOOLEAN, false)) {
            plugin.stopCrawling(player);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        VaxConfiguration configuration = BaseAPI.get().getConfiguration();
        
        if (player.getPersistentDataContainer().getOrDefault(plugin.getCrawlKey(), PersistentDataType.BOOLEAN, false)) {

            if (configuration.getBoolean("crawl.stop-on-vertical-movement") &&
                event.getTo() != null && event.getFrom().getY() < event.getTo().getY() - 0.1) {
                plugin.stopCrawling(player);
                return;
            }
            
    
            if (event.getTo() != null && !event.getFrom().equals(event.getTo())) {
                plugin.updateCrawlingBarrier(player);
            }
        }
    }

}