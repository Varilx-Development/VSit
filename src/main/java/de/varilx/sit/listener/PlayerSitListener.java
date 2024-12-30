package de.varilx.sit.listener;

import de.varilx.BaseAPI;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerSitListener implements Listener {

    private final YamlConfiguration configuration;

    public PlayerSitListener() {
        this.configuration = BaseAPI.getBaseAPI().getConfiguration().getConfig();
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractEntityEvent event) {
        if (!configuration.getBoolean("players.enabled")) return;
        if (!configuration.getBoolean("enabled")) return;
        if (!(event.getRightClicked() instanceof Player other)) return;
        other.addPassenger(event.getPlayer());
    }


}
