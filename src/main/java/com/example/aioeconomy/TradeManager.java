package com.example.aioeconomy;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public final class TradeManager {
    private final AIOEconomyPlugin plugin;
    private final EconomyManager economy;
    private final Map<UUID, UUID> requests = new HashMap<>();

    public TradeManager(AIOEconomyPlugin plugin, EconomyManager economy) {
        this.plugin = plugin; this.economy = economy;
    }

    public void request(Player from, Player to) {
        requests.put(to.getUniqueId(), from.getUniqueId());
        to.sendMessage("§e" + from.getName() + " wants to trade with you.");
        to.sendMessage("§7Use §f/trade " + from.getName() + " §7to accept.");
        from.sendMessage("§aTrade request sent to " + to.getName() + ".");
    }

    public boolean hasRequest(Player receiver, Player sender) {
        return Objects.equals(requests.get(receiver.getUniqueId()), sender.getUniqueId());
    }

    public void clear(Player p) { requests.remove(p.getUniqueId()); }

    public void open(Player a, Player b) {
        TradeHolder holder = new TradeHolder(a, b);
        a.openInventory(holder.inventory());
        b.openInventory(holder.inventory());
    }

    public void complete(TradeHolder holder) {
        Player a = holder.a();
        Player b = holder.b();
        ItemStack[] aItems = new ItemStack[21];
        ItemStack[] bItems = new ItemStack[21];
        for (int i = 0; i <= 20; i++) aItems[i] = cloneOrNull(holder.inventory().getItem(i));
        for (int i = 0; i <= 20; i++) bItems[i] = cloneOrNull(holder.inventory().getItem(27 + i));

        for (int i = 0; i <= 20; i++) holder.inventory().setItem(i, null);
        for (int i = 0; i <= 20; i++) holder.inventory().setItem(27 + i, null);
        for (ItemStack item : aItems) if (item != null) b.getInventory().addItem(item);
        for (ItemStack item : bItems) if (item != null) a.getInventory().addItem(item);

        a.closeInventory();
        b.closeInventory();
        a.sendMessage("§aTrade completed with " + b.getName() + ".");
        b.sendMessage("§aTrade completed with " + a.getName() + ".");
    }

    public void cancel(TradeHolder holder) {
        returnItems(holder.a(), holder, 0, 20);
        returnItems(holder.b(), holder, 27, 47);
        holder.a().closeInventory();
        holder.b().closeInventory();
        holder.a().sendMessage("§cTrade cancelled.");
        holder.b().sendMessage("§cTrade cancelled.");
    }

    private void returnItems(Player player, TradeHolder holder, int from, int to) {
        for (int i = from; i <= to; i++) {
            ItemStack item = holder.inventory().getItem(i);
            if (item != null && !item.getType().isAir()) player.getInventory().addItem(item.clone());
            holder.inventory().setItem(i, null);
        }
    }

    private ItemStack cloneOrNull(ItemStack item) { return item == null ? null : item.clone(); }
}
