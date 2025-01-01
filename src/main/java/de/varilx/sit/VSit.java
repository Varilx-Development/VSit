package de.varilx.sit;

import de.varilx.BaseAPI;
import de.varilx.sit.command.SitCommand;
import de.varilx.sit.listener.BlockSitListener;
import de.varilx.sit.listener.PlayerSitListener;
import de.varilx.utils.language.LanguageUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public final class VSit extends JavaPlugin {

    @Override
    public void onEnable() {
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand armorStand : world.getEntitiesByClass(ArmorStand.class)) {
                if (!armorStand.getPersistentDataContainer().has(new NamespacedKey(this, "sit"))) continue;
                armorStand.remove();
            }
        }

        new BaseAPI(this, 24310).enable();

        YamlConfiguration config = BaseAPI.getBaseAPI().getConfiguration().getConfig();

        Bukkit.getPluginManager().registerEvents(new BlockSitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerSitListener(), this);

        new SitCommand(this);

        Bukkit.getServer().sendMessage(LanguageUtils.getMessage("startup"));
    }

    public void sitDown(Player player, Block block, boolean command) {
        if (block.getRelative(BlockFace.UP).getType().isCollidable()) return;
        ArmorStand armorStand = block.getWorld().spawn(block.getLocation()
                .add(0.5, command ? 0.2 : 0, 0.5)
                .subtract(0, command ? 0 :getHeight(block), 0), ArmorStand.class, (stand) -> {
            stand.setCanMove(false);
            stand.setInvisible(true);
            stand.setInvulnerable(true);
            stand.setSmall(true);
            stand.getPersistentDataContainer().set(new NamespacedKey(this, "sit"), PersistentDataType.BOOLEAN, true);
        });
        armorStand.addPassenger(player);
    }

    private double getHeight(@Nullable Block clickedBlock) {
        if (clickedBlock.getBoundingBox().getHeight() >= 0.5) return 0.6;
        return 0.9;
    }

}