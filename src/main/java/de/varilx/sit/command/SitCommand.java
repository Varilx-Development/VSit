package de.varilx.sit.command;

import com.mojang.brigadier.Command;
import de.varilx.BaseAPI;
import de.varilx.configuration.VaxConfiguration;
import de.varilx.sit.VSit;
import de.varilx.utils.language.LanguageUtils;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

import javax.naming.Name;
import java.util.concurrent.CompletableFuture;

public class SitCommand {

    public SitCommand(VSit plugin) {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                    Commands.literal("sit")
                            .requires(ctx -> ctx.getSender().hasPermission("vsit.sit"))
                            .requires(ctx -> ctx.getSender() instanceof Player)
                            .executes(ctx -> {
                                VaxConfiguration configuration = BaseAPI.get().getConfiguration();
                                Player player = (Player) ctx.getSource().getSender();
                                Block blockBelow = getBlockBelow(player);

                                if (configuration.getBoolean("blocks.require-empty-hand") && !player.getInventory().getItemInMainHand().isEmpty()) {
                                    player.sendMessage(LanguageUtils.getMessage("commands.sit.empty-hand-required"));
                                    return 0;
                                }

                                boolean isSpecialBlock = false;
                                for (String block : configuration.getStringList("blocks.blocks")) {
                                    if (blockBelow.getType().name().toLowerCase().contains(block.toLowerCase())) {
                                        plugin.sitDown(player, blockBelow, false);
                                        isSpecialBlock = true;
                                        break;
                                    }
                                }

                                if (!isSpecialBlock) {
                                    plugin.sitDown(player, blockBelow, true);
                                }

                                player.sendMessage(LanguageUtils.getMessage("commands.sit.success"));
                                return 1;
                            })
                            .then(Commands.literal("toggle")
                                .requires(ctx -> ctx.getSender().hasPermission("vsit.toggle"))
                                .executes(ctx -> {
                                    Player player = (Player) ctx.getSource().getSender();
                                    if(player.getPersistentDataContainer().getOrDefault(plugin.getSitBlockedKey(), PersistentDataType.BOOLEAN, false)) {
                                        player.getPersistentDataContainer().set(plugin.getSitBlockedKey(), PersistentDataType.BOOLEAN, false);
                                        player.sendMessage(LanguageUtils.getMessage("commands.toggle.enabled"));
                                    } else {
                                        player.getPersistentDataContainer().set(plugin.getSitBlockedKey(), PersistentDataType.BOOLEAN, true);
                                        player.sendMessage(LanguageUtils.getMessage("commands.toggle.disabled"));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                            )
                            .then(Commands.literal("reload")
                                    .requires(ctx -> ctx.getSender().hasPermission("vsit.reload"))
                                    .executes(ctx -> {
                                        CompletableFuture.runAsync(() -> {
                                            BaseAPI.get().getConfiguration().reload();
                                            BaseAPI.get().getDatabaseConfiguration().reload();
                                            BaseAPI.get().getCurrentLanguageConfiguration().reload();
                                            ctx.getSource().getSender().sendMessage(LanguageUtils.getMessage("commands.reload"));
                                        });
                                        return 1;
                                    })
                            )
                            .build()
            );
        });
    }

    public static Block getBlockBelow(Player player) {
        Location location = player.getLocation();

        for (int y = location.getBlockY(); y > location.getBlockY() - 5; y--) {
            Location checkLocation = new Location(
                    location.getWorld(),
                    location.getBlockX(),
                    y,
                    location.getBlockZ()
            );

            Block block = checkLocation.getBlock();

            if (!block.getType().isCollidable()) continue;
            if (!block.getType().isAir()) return block;
        }

        return null;
    }
}
