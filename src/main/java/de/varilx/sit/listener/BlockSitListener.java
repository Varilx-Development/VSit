package de.varilx.sit.listener;

import de.varilx.BaseAPI;
import de.varilx.sit.VSit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class BlockSitListener implements Listener {

    private final YamlConfiguration configuration;
    private final VSit plugin;

    public BlockSitListener(VSit plugin) {
        this.plugin = plugin;
        this.configuration = BaseAPI.getBaseAPI().getConfiguration().getConfig();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (!configuration.getBoolean("blocks.enabled")) return;
        if (!configuration.getBoolean("enabled")) return;
        for (String block : configuration.getStringList("blocks.blocks")) {
            if (event.getClickedBlock().getType().name().toLowerCase().contains(block.toLowerCase())) {
                plugin.sitDown(event.getPlayer(), event.getClickedBlock(), false);
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