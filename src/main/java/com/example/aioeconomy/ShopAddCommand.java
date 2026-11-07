package com.example.aioeconomy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ShopAddCommand implements CommandExecutor {
    private final AIOEconomyPlugin plugin;

    public ShopAddCommand(AIOEconomyPlugin plugin) {
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

        // /shopadd section <name>
        if (args.length >= 2 && args[0].equalsIgnoreCase("section")) {
            String section = join(args, 1);
            plugin.shop().addSection(section);
            plugin.shop().save();
            player.sendMessage("§aCreated shop section: §f" + section);
            return true;
        }

        // /shopadd item <buyprice> <sellprice> [section]
        // If section is omitted, the most recently created section is used.
        if (args.length >= 3 && args[0].equalsIgnoreCase("item")) {
            try {
                double buy = Double.parseDouble(args[1]);
                double sell = Double.parseDouble(args[2]);

                if (buy < 0 || sell < 0) {
                    player.sendMessage("§cPrices cannot be negative.");
                    return true;
                }

                String section = args.length >= 4 ? join(args, 3) : plugin.shop().lastSection();
                if (section == null || section.isBlank()) {
                    player.sendMessage("§cCreate a section first, or specify one at the end of the command.");
                    player.sendMessage("§7Example: /shopadd item 100 50 Ores");
                    return true;
                }

                if (!plugin.shop().hasSection(section)) {
                    player.sendMessage("§cThat shop section doesn't exist: §f" + section);
                    return true;
                }

                if (plugin.shop().addItem(player.getInventory().getItemInMainHand(), buy, sell, section)) {
                    plugin.shop().save();
                    player.sendMessage("§aAdded §f" + player.getInventory().getItemInMainHand().getType().name()
                            + " §ato shop section §f" + section + "§a.");
                } else {
                    player.sendMessage("§cHold an item in your main hand first.");
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid prices.");
            }
            return true;
        }

        player.sendMessage("§c/shopadd section <name>");
        player.sendMessage("§c/shopadd item <buyprice> <sellprice> [section]");
        player.sendMessage("§7Example: /shopadd item 100 50 Ores");
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
