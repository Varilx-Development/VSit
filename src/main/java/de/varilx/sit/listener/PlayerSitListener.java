package de.varilx.sit.listener;

import de.varilx.BaseAPI;
import de.varilx.configuration.VaxConfiguration;
import de.varilx.sit.VSit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class PlayerSitListener implements Listener {

    private final VSit plugin;

    public PlayerSitListener(VSit plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractEntityEvent event) {
        VaxConfiguration configuration = BaseAPI.get().getConfiguration();
        if(event.getPlayer().getPersistentDataContainer().getOrDefault(plugin.getSitBlockedKey(), PersistentDataType.BOOLEAN, false)) return;
        if (!configuration.getBoolean("players.enabled")) return;
        if (!configuration.getBoolean("enabled")) return;
        if (configuration.getStringList("players.blocked-worlds").contains(event.getPlayer().getWorld().getName())) return;
        if (event.getPlayer().isSneaking()) return;

        if (!(event.getRightClicked() instanceof Player other)) return;
        other.addPassenger(event.getPlayer());
    }


}
