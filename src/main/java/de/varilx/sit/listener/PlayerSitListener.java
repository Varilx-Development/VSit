package de.varilx.sit.listener;

import de.varilx.BaseAPI;
import de.varilx.configuration.VaxConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerSitListener implements Listener {


    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractEntityEvent event) {
        VaxConfiguration configuration = BaseAPI.get().getConfiguration();
        if(event.getPlayer().hasMetadata("vsit_blocked")) return;
        if (!configuration.getBoolean("players.enabled")) return;
        if (!configuration.getBoolean("enabled")) return;
        if (configuration.getStringList("players.blocked-worlds").contains(event.getPlayer().getWorld().getName())) return;
        if (event.getPlayer().isSneaking()) return;

        if (!(event.getRightClicked() instanceof Player other)) return;
        other.addPassenger(event.getPlayer());
    }


}
