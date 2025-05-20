package de.varilx.sit.listener;

import de.varilx.sit.VSit;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.persistence.PersistentDataType;

/**
 * Project: VSit
 * Package: de.varilx.sit.listener
 * <p>
 * Author: ShadowDev1929
 * Created on: 20.05.2025
 */
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConnectionListener implements Listener {

    VSit plugin;

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if(!player.getPersistentDataContainer().has(plugin.getSitBlockedKey(), PersistentDataType.BOOLEAN)) {
            player.getPersistentDataContainer().set(plugin.getSitBlockedKey(), PersistentDataType.BOOLEAN, false);
        }
    }

}
