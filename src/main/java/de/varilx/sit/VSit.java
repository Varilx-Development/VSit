package de.varilx.sit;

import de.varilx.BaseAPI;
import de.varilx.BaseSpigotAPI;
import de.varilx.configuration.VaxConfiguration;
import de.varilx.sit.command.CrawlCommand;
import de.varilx.sit.command.SitCommand;
import de.varilx.sit.listener.BlockSitListener;
import de.varilx.sit.listener.ConnectionListener;
import de.varilx.sit.listener.CrawlListener;
import de.varilx.sit.listener.PlayerSitListener;
import de.varilx.utils.language.LanguageUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.Nullable;

@Getter
public final class VSit extends JavaPlugin {

    private NamespacedKey sitBlockedKey;
    private NamespacedKey crawlKey;
    private NamespacedKey crawlBlockedKey;

    private final Map<UUID, Location> crawlingBarriers = new HashMap<>();
    public final Map<UUID, ArmorStand> playerSitStands = new HashMap<>();
    private final Map<UUID, Long> sitCooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand armorStand : world.getEntitiesByClass(ArmorStand.class)) {
                if (!armorStand.getPersistentDataContainer().has(new NamespacedKey(this, "sit"))) continue;
                armorStand.remove();
            }
        }
        

        playerSitStands.clear();
        crawlingBarriers.clear();
        sitCooldowns.clear();

        new BaseSpigotAPI(this, 24310).enable();

        this.sitBlockedKey = new NamespacedKey(this, "vsit_blocked");
        this.crawlKey = new NamespacedKey(this, "vcrawl_active");
        this.crawlBlockedKey = new NamespacedKey(this, "vcrawl_blocked");

        Bukkit.getPluginManager().registerEvents(new BlockSitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerSitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CrawlListener(this), this);

        new SitCommand(this);
        new CrawlCommand(this);


        Bukkit.getScheduler().runTaskTimer(this, this::cleanupExpiredCooldowns, 600L, 600L);

        initializeConfigWithDefaults();
        updateLanguageFiles();

        Bukkit.getServer().getConsoleSender().sendMessage(LanguageUtils.getMessage("startup"));
    }

    public void sitDown(Player player, Block block, boolean command) {
        if (block.getRelative(BlockFace.UP).getType().isCollidable()) return;
        
        if (isBlockOccupied(block, player)) {
            return;
        }
        if (block.getType().name().contains("SLAB")) {
            if (block.getBlockData() instanceof org.bukkit.block.data.type.Slab) {
                org.bukkit.block.data.type.Slab slab = (org.bukkit.block.data.type.Slab) block.getBlockData();
                if (slab.getType() == org.bukkit.block.data.type.Slab.Type.DOUBLE) {
                    return;
                }
            }
        }
        
        double armorStandHeight = command ? 0.2 : (getHeight(block) - 0.5);
        Location sitLocation = block.getLocation().add(0.5, armorStandHeight, 0.5);
        float playerYaw = player.getLocation().getYaw();
        
        sitLocation.setYaw(playerYaw);
        
        ArmorStand armorStand = block.getWorld().spawn(sitLocation, ArmorStand.class, (stand) -> {
            stand.setCanMove(false);
            stand.setInvisible(true);
            stand.setInvulnerable(true);
            stand.setSmall(true);
            stand.setRotation(playerYaw, 0);
            stand.getPersistentDataContainer().set(new NamespacedKey(this, "sit"), PersistentDataType.BOOLEAN, true);
        });
        armorStand.addPassenger(player);
    }
    
    public void sitOnPlayer(Player sittingPlayer, Player targetPlayer) {
        if (sittingPlayer.getVehicle() != null) {
            return;
        }
        
        if (!targetPlayer.getPassengers().isEmpty()) {
            return;
        }
        if (isCrawling(targetPlayer)) {
            return;
        }
        

        long currentTime = System.currentTimeMillis();
        Long lastSitTime = sitCooldowns.get(sittingPlayer.getUniqueId());
        if (lastSitTime != null && (currentTime - lastSitTime) < 3000) {
            sittingPlayer.sendMessage(LanguageUtils.getMessage("commands.sit.player.cooldown"));
            return;
        }
        
        VaxConfiguration configuration = BaseAPI.get().getConfiguration();
        double sitHeight = configuration.getDouble("players.sit-height", 1.5);
        

        sitCooldowns.put(sittingPlayer.getUniqueId(), currentTime);


        removeSitStand(sittingPlayer);
        
        ArmorStand armorStand = targetPlayer.getWorld().spawn(
            targetPlayer.getLocation().add(0, sitHeight, 0), 
            ArmorStand.class, (stand) -> {
                stand.setCanMove(false);
                stand.setInvisible(true);
                stand.setInvulnerable(true);
                stand.setSmall(true);
                stand.setGravity(false);
                stand.getPersistentDataContainer().set(new NamespacedKey(this, "sit"), PersistentDataType.BOOLEAN, true);
                stand.getPersistentDataContainer().set(new NamespacedKey(this, "player-sit"), PersistentDataType.STRING, targetPlayer.getUniqueId().toString());
            }
        );
        
        targetPlayer.addPassenger(armorStand);
        armorStand.addPassenger(sittingPlayer);
        playerSitStands.put(sittingPlayer.getUniqueId(), armorStand);
        
        sittingPlayer.sendMessage(LanguageUtils.getMessage("commands.sit.player.success").replaceText(net.kyori.adventure.text.TextReplacementConfig.builder().matchLiteral("%player%").replacement(targetPlayer.getName()).build()));
    }
    
    public void removeSitStand(Player player) {
        ArmorStand stand = playerSitStands.remove(player.getUniqueId());
        if (stand != null) {
            stand.remove();
        }
        

    }
    
    public void kickPassenger(Player targetPlayer) {

        for (org.bukkit.entity.Entity passenger : targetPlayer.getPassengers()) {
            if (passenger instanceof ArmorStand armorStand) {
        
                for (org.bukkit.entity.Entity sittingEntity : armorStand.getPassengers()) {
                    if (sittingEntity instanceof Player sittingPlayer) {
                        armorStand.removePassenger(sittingPlayer);
                        playerSitStands.remove(sittingPlayer.getUniqueId());
        
                    }
                }
        
                targetPlayer.removePassenger(armorStand);
                armorStand.remove();
            }
        }
    }

    private boolean isBlockOccupied(Block block, Player currentPlayer) {
        Location blockCenter = block.getLocation().add(0.5, 0, 0.5);
        

        for (ArmorStand armorStand : block.getWorld().getEntitiesByClass(ArmorStand.class)) {
            if (armorStand.getPersistentDataContainer().has(new NamespacedKey(this, "sit"), PersistentDataType.BOOLEAN)) {
                Location standLocation = armorStand.getLocation();
                
                
                if (Math.abs(standLocation.getX() - blockCenter.getX()) < 0.6 && 
                    Math.abs(standLocation.getZ() - blockCenter.getZ()) < 0.6 &&
                    Math.abs(standLocation.getY() - block.getY()) < 2.0) {
                    
        
                    if (armorStand.getPassengers().contains(currentPlayer)) {
                        return false;
                    }
                    
   
                    currentPlayer.sendMessage(LanguageUtils.getMessage("commands.sit.occupied"));
                    currentPlayer.playSound(currentPlayer.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return true;
                }
            }
        }
        return false;
    }
    
    private double getHeight(@Nullable Block clickedBlock) {
        if (clickedBlock == null) return 0.3;
        
        Material blockType = clickedBlock.getType();
        double blockHeight = clickedBlock.getBoundingBox().getHeight();
        
        if (blockType.name().contains("CARPET")) {
            return 0.04;
        }
        

        if (clickedBlock.getBlockData() instanceof org.bukkit.block.data.type.Stairs) {
            org.bukkit.block.data.type.Stairs stairs = (org.bukkit.block.data.type.Stairs) clickedBlock.getBlockData();
            
            if (stairs.getHalf() == org.bukkit.block.data.type.Stairs.Half.TOP) {
                return 0.5;
            } else {
                org.bukkit.block.data.type.Stairs.Shape shape = stairs.getShape();
                if (shape == org.bukkit.block.data.type.Stairs.Shape.STRAIGHT) {
                    return 0.25;
                } else {
                    return 0.2;
                }
            }
        }
        

        if (clickedBlock.getBlockData() instanceof org.bukkit.block.data.type.Slab) {
            org.bukkit.block.data.type.Slab slab = (org.bukkit.block.data.type.Slab) clickedBlock.getBlockData();
            if (slab.getType() == org.bukkit.block.data.type.Slab.Type.BOTTOM) {
                return 0.3;
            } else if (slab.getType() == org.bukkit.block.data.type.Slab.Type.TOP) {
                return 0.8;
            }
        }
        
        return 0.3;
    }

    public void startCrawling(Player player) {
        if (isCrawling(player)) {
            return;
        }
        
        player.getPersistentDataContainer().set(crawlKey, PersistentDataType.BOOLEAN, true);
        player.sendMessage(LanguageUtils.getMessage("commands.crawl.started"));

        updateCrawlingBarrier(player);
    }
    
    public void updateCrawlingBarrier(Player player) {

        Location oldBarrierLocation = crawlingBarriers.get(player.getUniqueId());
        if (oldBarrierLocation != null) {
            Block oldBlock = oldBarrierLocation.getBlock();
            if (oldBlock.getType() == Material.BARRIER) {
                oldBlock.setType(Material.AIR);
            }
        }
        

        Location headLocation = player.getLocation().add(0, 1, 0);
        Block headBlock = headLocation.getBlock();
        

        if (headBlock.getType() == Material.AIR) {

            headBlock.setType(Material.BARRIER);
            

            crawlingBarriers.put(player.getUniqueId(), headLocation);
        } else {

            crawlingBarriers.remove(player.getUniqueId());
        }
    }
    
    public void stopCrawling(Player player) {
        if (!isCrawling(player)) {
            return;
        }
        player.getPersistentDataContainer().remove(crawlKey);
        player.sendMessage(LanguageUtils.getMessage("commands.crawl.stopped"));
        

        Location barrierLocation = crawlingBarriers.remove(player.getUniqueId());
        if (barrierLocation != null) {
            Block barrierBlock = barrierLocation.getBlock();
            if (barrierBlock.getType() == Material.BARRIER) {
                barrierBlock.setType(Material.AIR);
            }
        }
    }
    
    public boolean isCrawling(Player player) {
        return crawlingBarriers.containsKey(player.getUniqueId());
    }
    
    private void cleanupExpiredCooldowns() {
        long currentTime = System.currentTimeMillis();
        sitCooldowns.entrySet().removeIf(entry -> (currentTime - entry.getValue()) >= 3000);
    }
    
    private void initializeConfigWithDefaults() {
        saveDefaultConfig();
        

        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
            new java.io.InputStreamReader(getResource("config.yml"))
        );
        YamlConfiguration currentConfig = (YamlConfiguration) getConfig();
        
        boolean hasNewOptions = false;
        for (String key : defaultConfig.getKeys(true)) {
            if (!currentConfig.contains(key)) {
                currentConfig.set(key, defaultConfig.get(key));
                hasNewOptions = true;
                getLogger().info("Added new configuration option: " + key + " = " + defaultConfig.get(key));
            }
        }
        
        if (hasNewOptions) {
            try {
                currentConfig.save(new java.io.File(getDataFolder(), "config.yml"));
                getLogger().info("Configuration updated with new options!");
            } catch (java.io.IOException e) {
                getLogger().warning("Could not save updated configuration: " + e.getMessage());
            }
        }
    }
    
    private void updateLanguageFiles() {
        String[] languages = {"en", "de", "es", "fr", "it", "pt", "ru", "zh", "ja", "ko"};
        boolean anyUpdated = false;
        
        for (String lang : languages) {
            String fileName = "lang/" + lang + ".yml";
            java.io.InputStream defaultLangStream = getResource(fileName);
            
            if (defaultLangStream != null) {
                java.io.File langFile = new java.io.File(getDataFolder(), fileName);
                
                if (!langFile.exists()) {
        
                    langFile.getParentFile().mkdirs();
                    try (java.io.FileOutputStream out = new java.io.FileOutputStream(langFile)) {
                        defaultLangStream.transferTo(out);
                        getLogger().info("Created new language file: " + fileName);
                        anyUpdated = true;
                    } catch (java.io.IOException e) {
                        getLogger().warning("Could not create language file " + fileName + ": " + e.getMessage());
                    }
                } else {
        
                    try {
                        YamlConfiguration defaultLang = YamlConfiguration.loadConfiguration(
                            new java.io.InputStreamReader(getResource(fileName))
                        );
                        YamlConfiguration currentLang = YamlConfiguration.loadConfiguration(langFile);
                        
                        boolean hasNewKeys = false;
                        for (String key : defaultLang.getKeys(true)) {
                            if (!currentLang.contains(key)) {
                                currentLang.set(key, defaultLang.get(key));
                                hasNewKeys = true;
                                getLogger().info("Added new language key to " + fileName + ": " + key);
                            }
                        }
                        
                        if (hasNewKeys) {
                            currentLang.save(langFile);
                            getLogger().info("Updated language file: " + fileName);
                            anyUpdated = true;
                        }
                    } catch (java.io.IOException e) {
                        getLogger().warning("Could not update language file " + fileName + ": " + e.getMessage());
                    }
                }
            }
        }
        
        
        if (anyUpdated) {
            try {
                BaseAPI.get().getCurrentLanguageConfiguration().reload();
                getLogger().info("Language configuration reloaded after updates");
            } catch (Exception e) {
                getLogger().warning("Could not reload language configuration: " + e.getMessage());
            }
        }
    }
}