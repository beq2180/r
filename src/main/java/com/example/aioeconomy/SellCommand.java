package com.example.aioeconomy;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class SellCommand implements CommandExecutor {
    private final AIOEconomyPlugin p;
    public SellCommand(AIOEconomyPlugin p) { this.p = p; }

    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!(s instanceof Player player)) return true;
        if (a.length > 0 && (a[0].equalsIgnoreCase("hand") || a[0].equalsIgnoreCase("allhand"))) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType().isAir()) { player.sendMessage("§cHold an item."); return true; }
            double price = price(hand);
            if (price <= 0) { player.sendMessage("§cThat exact item has no sell price."); return true; }
            int amount = a[0].equalsIgnoreCase("hand") ? hand.getAmount() : count(player, hand);
            if (a[0].equalsIgnoreCase("hand")) player.getInventory().setItemInMainHand(null);
            else removeAll(player, hand);
            double total = price * amount;
            p.economy().deposit(player.getUniqueId(), total);
            player.sendMessage("§aSold " + amount + "x " + hand.getType() + " for $" + GuiListener.money(total));
            return true;
        }
        player.sendMessage("§cUsage: /sell <hand|allhand>");
        return true;
    }

    private double price(ItemStack stack) {
        for (String sec : p.shop().sections())
            for (var i : p.shop().items(sec))
                if (stack.isSimilar(i.item())) return i.sell();
        return 0;
    }

    private int count(Player pl, ItemStack template) {
        int n = 0;
        for (ItemStack x : pl.getInventory().getContents())
            if (x != null && x.isSimilar(template)) n += x.getAmount();
        return n;
    }

    private void removeAll(Player pl, ItemStack template) {
        for (int i = 0; i < pl.getInventory().getSize(); i++) {
            ItemStack x = pl.getInventory().getItem(i);
            if (x != null && x.isSimilar(template)) pl.getInventory().setItem(i, null);
        }
    }
}
