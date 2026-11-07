package com.example.aioeconomy;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;

public final class ShopRemoveCommand implements CommandExecutor {
    private final AIOEconomyPlugin plugin;

    public ShopRemoveCommand(AIOEconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by a player.");
            return true;
        }

        if (!player.hasPermission("aioeconomy.admin")) {
            player.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§c/shopremove <item> <section>");
            player.sendMessage("§7Example: /shopremove diamond Ores");
            return true;
        }

        Material material = Material.matchMaterial(args[0]);
        if (material == null || material.isAir()) {
            player.sendMessage("§cUnknown item: §f" + args[0]);
            return true;
        }

        String section = join(args, 1);
        if (!plugin.shop().hasSection(section)) {
            player.sendMessage("§cThat shop section doesn't exist: §f" + section);
            return true;
        }

        Iterator<ShopManager.ShopItem> iterator = plugin.shop().items(section).iterator();
        while (iterator.hasNext()) {
            ShopManager.ShopItem shopItem = iterator.next();
            if (shopItem.material() == material) {
                iterator.remove();
                plugin.shop().save();
                player.sendMessage("§aRemoved §f" + material.name() + " §afrom shop section §f" + section + "§a.");
                return true;
            }
        }

        player.sendMessage("§cThat item isn't in shop section §f" + section + "§c.");
        return true;
    }

    private static String join(String[] args, int start) {
        StringBuilder result = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) result.append(' ');
            result.append(args[i]);
        }
        return result.toString().trim();
    }
}
