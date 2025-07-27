package de.varilx.sit.command;

import com.mojang.brigadier.Command;
import de.varilx.BaseAPI;
import de.varilx.configuration.VaxConfiguration;
import de.varilx.sit.VSit;
import de.varilx.utils.language.LanguageUtils;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;


import java.util.concurrent.CompletableFuture;

public class CrawlCommand {

    public CrawlCommand(VSit plugin) {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                    Commands.literal("crawl")
                            .requires(ctx -> ctx.getSender().hasPermission("vsit.crawl"))
                            .requires(ctx -> ctx.getSender() instanceof Player)
                            .executes(ctx -> {
                                VaxConfiguration configuration = BaseAPI.get().getConfiguration();
                                Player player = (Player) ctx.getSource().getSender();
                                
                        
                                if (!configuration.getBoolean("crawl.enabled")) {
                                    player.sendMessage(LanguageUtils.getMessage("commands.crawl.disabled"));
                                    return 0;
                                }
                                
                        
                                for (String world : configuration.getStringList("crawl.blocked-worlds")) {
                                    if (player.getWorld().getName().equals(world)) {
                                        player.sendMessage(LanguageUtils.getMessage("commands.crawl.world-disabled"));
                                        return 0;
                                    }
                                }
                                
                        
                                if (configuration.getBoolean("crawl.require-empty-hand") && !player.getInventory().getItemInMainHand().isEmpty()) {
                                    player.sendMessage(LanguageUtils.getMessage("commands.crawl.empty-hand-required"));
                                    return 0;
                                }
                                
                        
                                if (player.getPersistentDataContainer().getOrDefault(plugin.getCrawlKey(), PersistentDataType.BOOLEAN, false)) {
                        
                                    plugin.stopCrawling(player);
                                } else {
                        
                                    plugin.startCrawling(player);
                                }
                                
                                return 1;
                            })
                            .then(Commands.literal("toggle")
                                .requires(ctx -> ctx.getSender().hasPermission("vsit.crawl.toggle"))
                                .executes(ctx -> {
                                    Player player = (Player) ctx.getSource().getSender();
                                    if(player.getPersistentDataContainer().getOrDefault(plugin.getCrawlBlockedKey(), PersistentDataType.BOOLEAN, false)) {
                                        player.getPersistentDataContainer().set(plugin.getCrawlBlockedKey(), PersistentDataType.BOOLEAN, false);
                                        player.sendMessage(LanguageUtils.getMessage("commands.crawl.toggle.enabled"));
                                    } else {
                                        player.getPersistentDataContainer().set(plugin.getCrawlBlockedKey(), PersistentDataType.BOOLEAN, true);
                                        player.sendMessage(LanguageUtils.getMessage("commands.crawl.toggle.disabled"));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                            )
                            .then(Commands.literal("stop")
                                .requires(ctx -> ctx.getSender().hasPermission("vsit.crawl"))
                                .executes(ctx -> {
                                    Player player = (Player) ctx.getSource().getSender();
                                    if (player.getPersistentDataContainer().getOrDefault(plugin.getCrawlKey(), PersistentDataType.BOOLEAN, false)) {
                                        plugin.stopCrawling(player);
                                    } else {
                                        player.sendMessage(LanguageUtils.getMessage("commands.crawl.not-crawling"));
                                    }
                                    return 1;
                                })
                            )
                            .build()
            );
        });
    }
}