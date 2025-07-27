package de.varilx.sit.listener;

import de.varilx.BaseAPI;
import de.varilx.configuration.VaxConfiguration;
import de.varilx.sit.VSit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.ArmorStand;
import org.bukkit.persistence.PersistentDataType;

public class PlayerSitListener implements Listener {

    private final VSit plugin;

    public PlayerSitListener(VSit plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractAtEntity(PlayerInteractEntityEvent event) {
        VaxConfiguration configuration = BaseAPI.get().getConfiguration();
        Player player = event.getPlayer();
        

        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        
        if(player.getPersistentDataContainer().getOrDefault(plugin.getSitBlockedKey(), PersistentDataType.BOOLEAN, false)) return;
        if (!configuration.getBoolean("players.enabled")) return;
        if (!configuration.getBoolean("enabled")) return;
        if (configuration.getStringList("players.blocked-worlds").contains(player.getWorld().getName())) return;
        if (player.isSneaking()) return;
        
        if (configuration.getBoolean("players.require-empty-hand") && !player.getInventory().getItemInMainHand().isEmpty()) return;

        if (!(event.getRightClicked() instanceof Player other)) return;
        
        event.setCancelled(true);
        plugin.sitOnPlayer(player, other);
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        Player player = event.getPlayer();
        

        if (player.getVehicle() != null) {
            if (player.getVehicle() instanceof ArmorStand) {
                player.getVehicle().removePassenger(player);
                plugin.removeSitStand(player);
            }
            return;
        }
        

        if (!player.getPassengers().isEmpty()) {
            plugin.kickPassenger(player);
        }
    }
    

    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.removeSitStand(event.getPlayer());
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player target)) return;
        

        if (attacker.getVehicle() instanceof ArmorStand stand) {
            String targetUUID = stand.getPersistentDataContainer().get(
                new NamespacedKey(plugin, "player-sit"), PersistentDataType.STRING);
            
            if (targetUUID != null && targetUUID.equals(target.getUniqueId().toString())) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (player.getVehicle() instanceof ArmorStand) {
            if (player.getLocation().getBlock().getType().isCollidable() || 
                player.getLocation().add(0, 1, 0).getBlock().getType().isCollidable()) {
                
        
                ArmorStand stand = (ArmorStand) player.getVehicle();
                String targetUUID = stand.getPersistentDataContainer().get(
                    new NamespacedKey(plugin, "player-sit"), PersistentDataType.STRING);
                
                if (targetUUID != null) {
                    Player targetPlayer = plugin.getServer().getPlayer(java.util.UUID.fromString(targetUUID));
                    if (targetPlayer != null) {
                        plugin.kickPassenger(targetPlayer);
                    }
                }
            }
        }
    }

}
