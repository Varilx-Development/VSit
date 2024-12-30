package de.varilx.sit.command;

import de.varilx.BaseAPI;
import de.varilx.config.Configuration;
import de.varilx.sit.VSit;
import de.varilx.utils.language.LanguageUtils;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class SitCommand {

    public SitCommand(VSit plugin) {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                    Commands.literal("sit")
                            .requires(ctx -> ctx.getSender().hasPermission("vsit.sit"))
                            .requires(ctx -> ctx.getSender() instanceof Player)
                            .executes(ctx -> {
                                Player player = (Player) ctx.getSource().getSender();
                                plugin.sitDown(player, getBlockBelow(player), true);
                                player.sendMessage(LanguageUtils.getMessage("commands.sit"));
                                return 1;
                            })
                            .then(Commands.literal("reload")
                                    .requires(ctx -> ctx.getSender().hasPermission("vsit.reload"))
                                    .executes(ctx -> {
                                        CompletableFuture.runAsync(() -> {
                                            BaseAPI.getBaseAPI().getConfiguration().reload();
                                            BaseAPI.getBaseAPI().getDatabaseConfiguration().reload();
                                            BaseAPI.getBaseAPI().getLanguageConfigurations().values().forEach(Configuration::reload);
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
