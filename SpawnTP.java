package com.featherffa.spawntp;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnTP extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        SpawnCommand spawnCommand = new SpawnCommand(this);
        getCommand("spawn").setExecutor(spawnCommand);
        getCommand("spawn").setTabCompleter(spawnCommand);
        getServer().getPluginManager().registerEvents(new CancelListener(spawnCommand), this);

        getLogger().info(isSpawnSet()
                ? "KittySpawn enabled — spawn point loaded from config."
                : "KittySpawn enabled — no spawn set yet. Run /spawn set as an admin.");
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    public boolean isSpawnSet() {
        return getConfig().contains("spawn.world");
    }

    public Location getSpawnLocation() {
        if (!isSpawnSet()) return null;

        FileConfiguration cfg = getConfig();
        World world = getServer().getWorld(cfg.getString("spawn.world"));
        if (world == null) return null;

        double x = cfg.getDouble("spawn.x");
        double y = cfg.getDouble("spawn.y");
        double z = cfg.getDouble("spawn.z");
        float yaw = (float) cfg.getDouble("spawn.yaw");
        float pitch = (float) cfg.getDouble("spawn.pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public void setSpawnLocation(Location loc) {
        FileConfiguration cfg = getConfig();
        cfg.set("spawn.world", loc.getWorld().getName());
        cfg.set("spawn.x", loc.getX());
        cfg.set("spawn.y", loc.getY());
        cfg.set("spawn.z", loc.getZ());
        cfg.set("spawn.yaw", (double) loc.getYaw());
        cfg.set("spawn.pitch", (double) loc.getPitch());
        saveConfig();
    }
}
