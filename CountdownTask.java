package com.featherffa.spawntp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CountdownTask extends BukkitRunnable {

    private final SpawnCommand command;
    private final Player player;
    private final Location destination;
    private int secondsLeft = 5;

    public CountdownTask(SpawnCommand command, Player player, Location destination) {
        this.command = command;
        this.player = player;
        this.destination = destination;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cleanup();
            cancel();
            return;
        }

        // secondsLeft starts at 5 because the "Teleporting in 5 seconds" message
        // was already sent when the command was run, so we count down from 4.
        secondsLeft--;

        if (secondsLeft <= 0) {
            cleanup();
            player.teleport(destination);
            player.sendMessage(command.prefixed(
                    Component.text("\u2713 ").color(NamedTextColor.GREEN)
                            .append(Component.text("Teleported to spawn!").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD))
            ));
            cancel();
            return;
        }

        player.sendMessage(command.prefixed(
                Component.text("Teleporting in ").color(NamedTextColor.GRAY)
                        .append(Component.text(secondsLeft).color(SpawnCommand.PINK).decorate(TextDecoration.BOLD))
                        .append(Component.text("...").color(NamedTextColor.GRAY))
        ));
    }

    private void cleanup() {
        command.getPending().remove(player.getUniqueId());
        command.getStartLocations().remove(player.getUniqueId());
    }
}
