package de.varilx.sit.listener;

import de.varilx.BaseAPI;
import de.varilx.configuration.VaxConfiguration;
import de.varilx.sit.VSit;
import de.varilx.utils.language.LanguageUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockSitListener implements Listener {

    private final VSit plugin;
    private final Map<UUID, Long> lastInteractionTime = new HashMap<>();
    private static final long INTERACTION_COOLDOWN = 100;

    public BlockSitListener(VSit plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        Long lastTime = lastInteractionTime.get(playerId);
        if (lastTime != null && (currentTime - lastTime) < INTERACTION_COOLDOWN) {
            return;
        }
        
        Block block = event.getClickedBlock();
        if(player.getPersistentDataContainer().getOrDefault(plugin.getSitBlockedKey(), PersistentDataType.BOOLEAN, false)) return;
        if (block == null) return;
        VaxConfiguration configuration = BaseAPI.get().getConfiguration();
        if (!configuration.getBoolean("blocks.enabled")) return;
        if (!configuration.getBoolean("enabled")) return;
        if (configuration.getStringList("blocks.blocked-worlds").contains(block.getWorld().getName())) return;
        if (player.isSneaking()) return;
        if (configuration.getBoolean("blocks.require-empty-hand") && !player.getInventory().getItemInMainHand().isEmpty()) return;

        for (String blockStr : configuration.getStringList("blocks.blocks")) {
            if (block.getType().name().toLowerCase().contains(blockStr.toLowerCase())) {
                if (!configuration.getBoolean("blocks.right-click") && event.getAction() == Action.RIGHT_CLICK_BLOCK) return;
                if (!configuration.getBoolean("blocks.left-click") && event.getAction() == Action.LEFT_CLICK_BLOCK) return;
                
                lastInteractionTime.put(playerId, currentTime);
                
                event.setCancelled(true);
                plugin.sitDown(player, block, false);
                break;
            }
        }
    }

    @EventHandler
    public void onVehicleExit(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDismounted() instanceof ArmorStand stand)) return;
        
        if (stand.getPersistentDataContainer().has(new NamespacedKey(plugin, "player-sit"), PersistentDataType.STRING)) {
            String targetUUID = stand.getPersistentDataContainer().get(new NamespacedKey(plugin, "player-sit"), PersistentDataType.STRING);
            String targetPlayerName = "Unknown Player";
            
            if (targetUUID != null) {
                Player targetPlayer = plugin.getServer().getPlayer(java.util.UUID.fromString(targetUUID));
                if (targetPlayer != null) {
                    targetPlayerName = targetPlayer.getName();
                }
            }
            
            plugin.removeSitStand(player);
            player.sendMessage(LanguageUtils.getMessage("commands.sit.player.stopped").replaceText(net.kyori.adventure.text.TextReplacementConfig.builder().matchLiteral("%player%").replacement(targetPlayerName).build()));
            return;
        }
        
        if (stand.getPersistentDataContainer().has(new NamespacedKey(plugin, "sit"), PersistentDataType.BOOLEAN)) {
            player.sendMessage(LanguageUtils.getMessage("commands.sit.stopped"));
            player.teleportAsync(player.getLocation().add(0, 1, 0));
            stand.remove();
        }
    }


}
