package com.starry.plotarmor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlotArmor extends JavaPlugin implements Listener, TabCompleter {

    private final Set<UUID> protectedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadProtectedPlayers();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("plotarmor").setTabCompleter(this);
        getLogger().info("Plot Armor enabled! " + protectedPlayers.size() + " player(s) protected.");
    }

    @Override
    public void onDisable() {
        saveProtectedPlayers();
        getLogger().info("Plot Armor disabled.");
    }

    // ─── Config Persistence ──────────────────────────────────────────────

    private void loadProtectedPlayers() {
        protectedPlayers.clear();
        List<String> uuids = getConfig().getStringList("protected-players");
        for (String uuid : uuids) {
            try {
                protectedPlayers.add(UUID.fromString(uuid));
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid UUID in config: " + uuid);
            }
        }
    }

    private void saveProtectedPlayers() {
        List<String> uuids = protectedPlayers.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        getConfig().set("protected-players", uuids);
        saveConfig();
    }

    // ─── Command Handling ────────────────────────────────────────────────

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> handleAdd(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "list" -> handleList(sender);
            default -> sendUsage(sender);
        }

        return true;
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /plotarmor add <player>", NamedTextColor.RED));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found: ", NamedTextColor.RED)
                    .append(Component.text(args[1], NamedTextColor.WHITE)));
            return;
        }

        if (protectedPlayers.contains(target.getUniqueId())) {
            sender.sendMessage(Component.text(target.getName(), NamedTextColor.GOLD)
                    .append(Component.text(" already has plot armor.", NamedTextColor.YELLOW)));
            return;
        }

        protectedPlayers.add(target.getUniqueId());
        saveProtectedPlayers();

        sender.sendMessage(Component.text("✦ ", NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text(target.getName(), NamedTextColor.GOLD))
                .append(Component.text(" now has plot armor!", NamedTextColor.GREEN)));

        target.sendMessage(Component.text("✦ ", NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text("You have been granted plot armor!", NamedTextColor.GREEN)));
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /plotarmor remove <player>", NamedTextColor.RED));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found: ", NamedTextColor.RED)
                    .append(Component.text(args[1], NamedTextColor.WHITE)));
            return;
        }

        if (!protectedPlayers.contains(target.getUniqueId())) {
            sender.sendMessage(Component.text(target.getName(), NamedTextColor.GOLD)
                    .append(Component.text(" doesn't have plot armor.", NamedTextColor.YELLOW)));
            return;
        }

        protectedPlayers.remove(target.getUniqueId());
        saveProtectedPlayers();

        sender.sendMessage(Component.text("✦ ", NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text(target.getName(), NamedTextColor.GOLD))
                .append(Component.text(" no longer has plot armor.", NamedTextColor.RED)));

        target.sendMessage(Component.text("✦ ", NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text("Your plot armor has been removed!", NamedTextColor.RED)));
    }

    private void handleList(CommandSender sender) {
        if (protectedPlayers.isEmpty()) {
            sender.sendMessage(Component.text("No players currently have plot armor.", NamedTextColor.YELLOW));
            return;
        }

        sender.sendMessage(Component.text("✦ Plot Armor List ", NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text("(" + protectedPlayers.size() + ")", NamedTextColor.GRAY)
                        .decoration(TextDecoration.BOLD, false)));

        for (UUID uuid : protectedPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            String name = player != null ? player.getName() : uuid.toString();
            NamedTextColor color = player != null ? NamedTextColor.GREEN : NamedTextColor.GRAY;
            String status = player != null ? " ●" : " ○";

            sender.sendMessage(Component.text(" - ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(name, color))
                    .append(Component.text(status, player != null ? NamedTextColor.GREEN : NamedTextColor.GRAY)));
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text("✦ Plot Armor Commands", NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.text(" /plotarmor add <player>", NamedTextColor.YELLOW)
                .append(Component.text(" - Grant plot armor", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text(" /plotarmor remove <player>", NamedTextColor.YELLOW)
                .append(Component.text(" - Revoke plot armor", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text(" /plotarmor list", NamedTextColor.YELLOW)
                .append(Component.text(" - View protected players", NamedTextColor.GRAY)));
    }

    // ─── Tab Completion ──────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("add", "remove", "list").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                // Suggest online players not already protected
                return Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !protectedPlayers.contains(p.getUniqueId()))
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("remove")) {
                // Suggest only protected players who are online
                return Bukkit.getOnlinePlayers().stream()
                        .filter(p -> protectedPlayers.contains(p.getUniqueId()))
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }

    // ─── Damage Prevention ───────────────────────────────────────────────

    /**
     * Prevents any damage from killing a protected player.
     * If a hit would reduce their health below half a heart (1.0 HP),
     * the event is cancelled and health is set to exactly half a heart.
     * If the player is already at half a heart or below, all damage is negated.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;

        // Only protect players on the list
        if (!protectedPlayers.contains(player.getUniqueId()))
            return;

        double health = player.getHealth();

        if (health <= 1.0) {
            event.setCancelled(true);
            player.playHurtAnimation(0);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
            applyExplosionKnockback(event, player);
            logIntervention(player, event.getCause(), event.getFinalDamage(), health, "BLOCKED (at half heart)");
            return;
        }

        double finalDamage = event.getFinalDamage();
        double absorption = player.getAbsorptionAmount();
        double healthDamage = Math.max(0.0, finalDamage - absorption);
        double healthAfterDamage = health - healthDamage;

        if (healthAfterDamage < 1.0) {
            event.setCancelled(true);
            player.setHealth(1.0);
            if (absorption > 0) {
                player.setAbsorptionAmount(0);
            }
            player.playHurtAnimation(0);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
            applyExplosionKnockback(event, player);
            logIntervention(player, event.getCause(), finalDamage, health, "SAVED (would have died)");
        }
    }

    /**
     * If the damage was from an explosion, manually apply knockback
     * since cancelling the event also cancels the vanilla knockback.
     */
    private void applyExplosionKnockback(EntityDamageEvent event, Player player) {
        DamageCause cause = event.getCause();
        if (cause != DamageCause.ENTITY_EXPLOSION && cause != DamageCause.BLOCK_EXPLOSION) {
            return;
        }

        Location explosionSource = null;

        // Try to get the source entity's location (creeper, tnt, etc.)
        if (event instanceof EntityDamageByEntityEvent entityEvent) {
            Entity damager = entityEvent.getDamager();
            if (damager != null) {
                explosionSource = damager.getLocation();
            }
        }

        if (explosionSource == null)
            return;

        // Calculate knockback direction from explosion to player
        Vector knockback = player.getLocation().toVector()
                .subtract(explosionSource.toVector());

        double distance = knockback.length();
        if (distance == 0)
            return;

        // Normalize and scale — closer = stronger knockback
        knockback.normalize();
        double strength = Math.max(0.3, 1.5 - (distance * 0.15));
        knockback.multiply(strength);
        knockback.setY(Math.max(0.4, knockback.getY())); // Always launch upward a bit

        player.setVelocity(knockback);
    }

    /**
     * Logs a plot armor intervention to the console with details
     * for timestamp cross-referencing with recordings.
     */
    private void logIntervention(Player player, DamageCause cause, double damage, double healthBefore, String action) {
        Location loc = player.getLocation();
        getLogger().info(String.format(
                "[Plot Armor] %s | %s | Cause: %s | Damage: %.1f | Health: %.1f -> 0.5❤ | Pos: %s %d %d %d",
                action,
                player.getName(),
                cause.name(),
                damage,
                healthBefore / 2.0,
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()));
    }
}
