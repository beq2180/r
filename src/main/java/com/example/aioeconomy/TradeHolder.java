package com.example.aioeconomy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class TradeHolder implements InventoryHolder {
    private final Player a, b;
    private final Inventory inv;
    private boolean aAccepted, bAccepted;
    private boolean finished;

    public TradeHolder(Player a, Player b) {
        this.a = a; this.b = b;
        this.inv = Bukkit.createInventory(this, 54, "Trade: " + a.getName() + " ↔ " + b.getName());
        refreshButtons();
    }

    public Player a() { return a; }
    public Player b() { return b; }
    public Inventory inventory() { return inv; }
    public boolean isA(Player p) { return p.getUniqueId().equals(a.getUniqueId()); }
    public boolean isB(Player p) { return p.getUniqueId().equals(b.getUniqueId()); }
    public boolean acceptedA() { return aAccepted; }
    public boolean acceptedB() { return bAccepted; }
    public void toggleAccept(Player p) {
        if (isA(p)) aAccepted = !aAccepted;
        if (isB(p)) bAccepted = !bAccepted;
        refreshButtons();
    }
    public void resetAccepts() { aAccepted = false; bAccepted = false; refreshButtons(); }
    public boolean bothAccepted() { return aAccepted && bAccepted; }
    public boolean finished() { return finished; }
    public void markFinished() { finished = true; }

    public boolean ownSlot(Player p, int slot) {
        return isA(p) ? slot >= 0 && slot <= 20 : isB(p) && slot >= 27 && slot <= 47;
    }

    public void refreshButtons() {
        inv.setItem(22, button(aAccepted ? "§aAccepted" : "§eAccept", aAccepted ? Material.LIME_WOOL : Material.GREEN_WOOL));
        inv.setItem(31, button(bAccepted ? "§aAccepted" : "§eAccept", bAccepted ? Material.LIME_WOOL : Material.GREEN_WOOL));
        inv.setItem(49, button("§cCancel Trade", Material.BARRIER));
    }

    private ItemStack button(String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of("§7Click to change your trade status."));
        item.setItemMeta(meta);
        return item;
    }

    @Override public Inventory getInventory() { return inv; }
}
