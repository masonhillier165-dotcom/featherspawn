package com.featherffa.spawntp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class CancelListener implements Listener {

    private final SpawnCommand command;

    public CancelListener(SpawnCommand command) {
        this.command = command;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!command.getPending().containsKey(uuid)) return;

        Location start = command.getStartLocations().get(uuid);
        Location to = event.getTo();
        if (start == null || to == null) return;

        // Ignore pure head-turns; only cancel on an actual position change.
        if (start.getWorld().equals(to.getWorld()) && start.distanceSquared(to) < 0.04) {
            return;
        }

        command.cancel(player, Component.text("\u2717 Teleport cancelled \u2014 you moved!").color(NamedTextColor.RED));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!command.getPending().containsKey(player.getUniqueId())) return;

        command.cancel(player, Component.text("\u2717 Teleport cancelled \u2014 you took damage!").color(NamedTextColor.RED));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        var task = command.getPending().remove(uuid);
        command.getStartLocations().remove(uuid);
        if (task != null) task.cancel();
    }
}
