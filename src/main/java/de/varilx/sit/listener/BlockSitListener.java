package de.varilx.sit.listener;

import de.varilx.BaseAPI;
import de.varilx.configuration.VaxConfiguration;
import de.varilx.sit.VSit;
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

public class BlockSitListener implements Listener {

    private final VSit plugin;

    public BlockSitListener(VSit plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        VaxConfiguration configuration = BaseAPI.get().getConfiguration();
        if (!configuration.getBoolean("blocks.enabled")) return;
        if (!configuration.getBoolean("enabled")) return;
        
        Block block = event.getClickedBlock();
        if (configuration.getStringList("blocks.blocked-worlds").contains(block.getWorld().getName())) return;
        Player player = event.getPlayer();
        if (player.isSneaking()) return;
        
        for (String block : configuration.getStringList("blocks.blocks")) {
            if (block.getType().name().toLowerCase().contains(block.toLowerCase())) {
                if (!configuration.getBoolean("blocks.right-click") && event.getAction() == Action.RIGHT_CLICK_BLOCK) return;
                if (!configuration.getBoolean("blocks.left-click") && event.getAction() == Action.LEFT_CLICK_BLOCK) return;
                plugin.sitDown(player, block, false);
            }
        }
    }

    @EventHandler
    public void onVehicleExit(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDismounted() instanceof ArmorStand stand)) return;
        player.teleportAsync(player.getLocation().add(0, 1, 0));
        stand.remove();
    }


}
