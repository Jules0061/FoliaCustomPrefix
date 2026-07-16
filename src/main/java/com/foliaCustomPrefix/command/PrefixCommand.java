package com.foliaCustomPrefix.command;

import com.foliaCustomPrefix.hooks.LuckPermsHook;
import com.foliaCustomPrefix.manager.ConfigManager;
import com.foliaCustomPrefix.manager.CooldownManager;
import com.foliaCustomPrefix.manager.DisplayManager;
import com.foliaCustomPrefix.manager.PrefixManager;
import com.foliaCustomPrefix.util.MiniMessageUtil;
import com.foliaCustomPrefix.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class PrefixCommand implements TabExecutor {

    private static final String PERMISSION_USE = "foliacustomprefix.use";
    private static final String PERMISSION_BYPASS = "foliacustomprefix.bypass";
    private static final String PERMISSION_RELOAD = "foliacustomprefix.reload";
    private static final String PERMISSION_RESET_OTHERS = "foliacustomprefix.reset.others";
    private static final String PERMISSION_SET_OTHERS = "foliacustomprefix.set.others";

    private final ConfigManager configManager;
    private final PrefixManager prefixManager;
    private final CooldownManager cooldownManager;
    private final DisplayManager displayManager;
    private final LuckPermsHook luckPermsHook;

    public PrefixCommand(ConfigManager configManager, PrefixManager prefixManager,
                         CooldownManager cooldownManager, DisplayManager displayManager,
                         LuckPermsHook luckPermsHook) {
        this.configManager = configManager;
        this.prefixManager = prefixManager;
        this.cooldownManager = cooldownManager;
        this.displayManager = displayManager;
        this.luckPermsHook = luckPermsHook;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            sender.sendMessage(configManager.message("usage"));
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload" -> handleReload(sender);
            case "reset" -> handleResetCommand(sender, args);
            case "set" -> handleSetCommand(sender, args);
            default -> sender.sendMessage(configManager.message("usage"));
        }
        return true;
    }

    private void handleResetCommand(CommandSender sender, String[] args) {
        if (args.length == 2) {
            handleResetOther(sender, args[1]);
            return;
        }
        if (args.length != 1) {
            sender.sendMessage(configManager.message("usage"));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(configManager.message("players-only"));
            return;
        }
        handleReset(player);
    }

    private void handleSetCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(configManager.message("usage"));
            return;
        }
        if (args.length >= 3 && sender.hasPermission(PERMISSION_SET_OTHERS)) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target != null) {
                handleSetOther(sender, target, join(args, 2));
                return;
            }
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(configManager.message("players-only"));
            return;
        }
        handleSet(player, join(args, 1));
    }

    private String join(String[] args, int from) {
        return String.join(" ", Arrays.copyOfRange(args, from, args.length));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission(PERMISSION_RELOAD)) {
            sender.sendMessage(configManager.message("no-permission"));
            return;
        }
        configManager.reload();
        sender.sendMessage(configManager.message("reloaded"));
    }

    private void handleReset(Player player) {
        if (!player.hasPermission(PERMISSION_USE)) {
            player.sendMessage(configManager.message("no-permission"));
            return;
        }
        if (checkCooldown(player)) {
            return;
        }
        prefixManager.resetPrefix(player.getUniqueId());
        luckPermsHook.clearUserPrefix(player.getUniqueId())
            .thenRun(() -> displayManager.updateDisplay(player));
        player.sendMessage(configManager.message("removed"));
    }

    private void handleResetOther(CommandSender sender, String targetName) {
        if (!sender.hasPermission(PERMISSION_RESET_OTHERS)) {
            sender.sendMessage(configManager.message("no-permission"));
            return;
        }
        Player online = Bukkit.getPlayerExact(targetName);
        if (online != null) {
            resetTarget(sender, online.getUniqueId(), online.getName(), online);
            return;
        }
        luckPermsHook.lookupUniqueId(targetName).thenAccept(uuid -> {
            if (uuid == null) {
                sender.sendMessage(configManager.message("player-not-found", "%player%", targetName));
                return;
            }
            resetTarget(sender, uuid, targetName, null);
        });
    }

    private void resetTarget(CommandSender sender, UUID uuid, String name, Player online) {
        prefixManager.removeEntry(uuid);
        luckPermsHook.clearUserPrefix(uuid).thenRun(() -> {
            if (online != null && online.isOnline()) {
                displayManager.updateDisplay(online);
            }
        });
        sender.sendMessage(configManager.message("reset-other", "%player%", name));
    }

    private void handleSet(Player player, String input) {
        if (!player.hasPermission(PERMISSION_USE)) {
            player.sendMessage(configManager.message("no-permission"));
            return;
        }
        if (checkCooldown(player)) {
            return;
        }
        String prefix = validate(player, input);
        if (prefix == null) {
            return;
        }
        applyPrefix(player, prefix);
        player.sendMessage(configManager.message("changed"));
    }

    private void handleSetOther(CommandSender sender, Player target, String input) {
        String prefix = validate(sender, input);
        if (prefix == null) {
            return;
        }
        applyPrefix(target, prefix);
        sender.sendMessage(configManager.message("set-other", "%player%", target.getName()));
    }

    private String validate(CommandSender sender, String input) {
        String prefix = input.trim();
        if (prefix.isEmpty()) {
            sender.sendMessage(configManager.message("empty"));
            return null;
        }
        if (MiniMessageUtil.containsIllegalCharacters(prefix)) {
            sender.sendMessage(configManager.message("invalid"));
            return null;
        }
        if (!MiniMessageUtil.isValid(prefix)) {
            sender.sendMessage(configManager.message("invalid"));
            return null;
        }
        String plainText = MiniMessageUtil.plainText(prefix);
        if (plainText.isEmpty()) {
            sender.sendMessage(configManager.message("empty"));
            return null;
        }
        if (plainText.length() > configManager.getMaxLength()) {
            sender.sendMessage(configManager.message("too-long"));
            return null;
        }
        if (configManager.isBlacklisted(plainText)) {
            sender.sendMessage(configManager.message("blacklisted"));
            return null;
        }
        return prefix;
    }

    private void applyPrefix(Player target, String prefix) {
        prefixManager.setPrefix(target.getUniqueId(), prefix);
        luckPermsHook.setUserPrefix(target.getUniqueId(), MiniMessageUtil.toLegacy(prefix))
            .thenRun(() -> displayManager.updateDisplay(target));
    }

    private boolean checkCooldown(Player player) {
        if (player.hasPermission(PERMISSION_BYPASS)) {
            return false;
        }
        long remaining = cooldownManager.getRemainingMillis(player.getUniqueId());
        if (remaining <= 0L) {
            return false;
        }
        player.sendMessage(configManager.message("cooldown", "%time%", TimeUtil.formatDuration(remaining)));
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                               @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(3);
            String current = args[0].toLowerCase(Locale.ROOT);
            if (sender.hasPermission(PERMISSION_USE) && "set".startsWith(current)) {
                completions.add("set");
            }
            if (sender.hasPermission(PERMISSION_USE) && "reset".startsWith(current)) {
                completions.add("reset");
            }
            if (sender.hasPermission(PERMISSION_RELOAD) && "reload".startsWith(current)) {
                completions.add("reload");
            }
            return completions;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            if (!sender.hasPermission(PERMISSION_RESET_OTHERS)) {
                return List.of();
            }
            return matchingPlayerNames(args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            if (!sender.hasPermission(PERMISSION_USE)) {
                return List.of();
            }
            List<String> completions = new ArrayList<>();
            completions.add("<prefix>");
            if (sender.hasPermission(PERMISSION_SET_OTHERS)) {
                completions.addAll(matchingPlayerNames(args[1]));
            }
            return completions;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("set")
            && sender.hasPermission(PERMISSION_SET_OTHERS)
            && Bukkit.getPlayerExact(args[1]) != null) {
            return List.of("<prefix>");
        }
        return List.of();
    }

    private List<String> matchingPlayerNames(String current) {
        String lower = current.toLowerCase(Locale.ROOT);
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase(Locale.ROOT).startsWith(lower)) {
                names.add(player.getName());
            }
        }
        return names;
    }
}
