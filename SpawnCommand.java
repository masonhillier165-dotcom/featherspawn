package com.featherffa.spawntp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpawnCommand implements CommandExecutor, TabCompleter {

    public static final TextColor PINK = TextColor.color(0xFF3FA4);

    private final SpawnTP plugin;
    private final Map<UUID, BukkitTask> pending = new HashMap<>();
    private final Map<UUID, Location> startLocations = new HashMap<>();

    public SpawnCommand(SpawnTP plugin) {
        this.plugin = plugin;
    }

    public Map<UUID, BukkitTask> getPending() {
        return pending;
    }

    public Map<UUID, Location> getStartLocations() {
        return startLocations;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("set")) {
            return handleSet(sender);
        }
        return handleTeleport(sender);
    }

    private boolean handleSet(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can set the spawn point.").color(NamedTextColor.RED));
            return true;
        }
        if (!player.hasPermission("spawntp.set")) {
            player.sendMessage(prefixed(Component.text("You don't have permission to do that.").color(NamedTextColor.RED)));
            return true;
        }

        plugin.setSpawnLocation(player.getLocation());
        player.sendMessage(prefixed(
                Component.text("\u2605 ").color(PINK)
                        .append(Component.text("Spawn point updated").color(PINK).decorate(TextDecoration.BOLD))
                        .append(Component.text(" to your current location.").color(NamedTextColor.GRAY))
        ));
        return true;
    }

    private boolean handleTeleport(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
            return true;
        }
        if (!player.hasPermission("spawntp.use")) {
            player.sendMessage(prefixed(Component.text("You don't have permission to do that.").color(NamedTextColor.RED)));
            return true;
        }
        if (!plugin.isSpawnSet()) {
            player.sendMessage(prefixed(
                    Component.text("Spawn hasn't been set yet. Ask an admin to run ").color(NamedTextColor.GRAY)
                            .append(Component.text("/spawn set").color(PINK).decorate(TextDecoration.BOLD))
            ));
            return true;
        }

        Location destination = plugin.getSpawnLocation();
        if (destination == null) {
            player.sendMessage(prefixed(Component.text("Spawn's world isn't loaded right now — try again in a moment.").color(NamedTextColor.RED)));
            return true;
        }

        UUID uuid = player.getUniqueId();
        if (pending.containsKey(uuid)) {
            player.sendMessage(prefixed(Component.text("You're already being teleported!").color(NamedTextColor.YELLOW)));
            return true;
        }

        startLocations.put(uuid, player.getLocation());
        player.sendMessage(prefixed(
                Component.text("Teleporting to ").color(NamedTextColor.GRAY)
                        .append(Component.text("spawn").color(PINK).decorate(TextDecoration.BOLD))
                        .append(Component.text(" in ").color(NamedTextColor.GRAY))
                        .append(Component.text("5").color(PINK).decorate(TextDecoration.BOLD))
                        .append(Component.text(" seconds. Don't move!").color(NamedTextColor.GRAY))
        ));

        BukkitTask task = new CountdownTask(this, player, destination).runTaskTimer(plugin, 20L, 20L);
        pending.put(uuid, task);
        return true;
    }

    public void cancel(Player player, Component reason) {
        UUID uuid = player.getUniqueId();
        BukkitTask task = pending.remove(uuid);
        startLocations.remove(uuid);
        if (task != null) {
            task.cancel();
            player.sendMessage(prefixed(reason));
        }
    }

    public Component prefixed(Component msg) {
        return Component.text("[").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("Feather FFA").color(PINK).decorate(TextDecoration.BOLD))
                .append(Component.text("] ").color(NamedTextColor.DARK_GRAY))
                .append(msg);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("spawntp.set") && "set".startsWith(args[0].toLowerCase())) {
            return Collections.singletonList("set");
        }
        return Collections.emptyList();
    }
}
