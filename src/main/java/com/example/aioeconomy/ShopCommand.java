package com.example.aioeconomy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class ShopCommand implements CommandExecutor {
    public static final int PAGE_SIZE = 45;
    public static final int BACK_SLOT = 45;
    public static final int PREVIOUS_SLOT = 48;
    public static final int PAGE_INFO_SLOT = 49;
    public static final int NEXT_SLOT = 50;

    public static final class ShopPageHolder implements InventoryHolder {
        private final String section;
        private final int page;
        private final int pageCount;
        private Inventory inventory;

        private ShopPageHolder(String section, int page, int pageCount) {
            this.section = section;
            this.page = page;
            this.pageCount = pageCount;
        }

        public String section() { return section; }
        public int page() { return page; }
        public int pageCount() { return pageCount; }
        @Override public Inventory getInventory() { return inventory; }
    }

    private final AIOEconomyPlugin plugin;
    public ShopCommand(AIOEconomyPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (args.length == 0) openSections(player);
        else openSection(player, String.join(" ", args));
        return true;
    }

    public void openSections(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Shop");
        int slot = 0;
        for (String section : plugin.shop().sections()) {
            if (slot >= 45) break;
            ItemStack icon = new ItemStack(iconFor(section));
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName("§6" + section);
            meta.setLore(List.of("§7Click to browse this section."));
            icon.setItemMeta(meta);
            inv.setItem(slot++, icon);
        }
        player.openInventory(inv);
    }

    public void openSection(Player player, String section) {
        openSection(player, section, 0);
    }

    public void openSection(Player player, String section, int requestedPage) {
        if (!plugin.shop().sections().contains(section)) {
            player.sendMessage("§cShop section not found. Use /shop to see the available sections.");
            return;
        }
        List<ShopManager.ShopItem> items = plugin.shop().items(section);
        int pageCount = Math.max(1, (items.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.max(0, Math.min(requestedPage, pageCount - 1));
        ShopPageHolder holder = new ShopPageHolder(section, page, pageCount);
        Inventory inv = Bukkit.createInventory(holder, 54, "Shop: " + section);
        holder.inventory = inv;

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, items.size());
        int slot = 0;
        for (int i = start; i < end; i++) {
            ShopManager.ShopItem item = items.get(i);
            ItemStack display = item.item().clone();
            display.setAmount(1);
            ItemMeta meta = display.getItemMeta();
            meta.setDisplayName("§f" + pretty(item.material()));
            List<String> lore = new ArrayList<>();
            lore.add("§aBuy: §f$" + GuiListener.money(item.buy()));
            lore.add("§cSell: §f$" + GuiListener.money(item.sell()));
            lore.add("§7Left-click: buy 1");
            lore.add("§7Right-click: sell 1");
            lore.add("§7Shift-left: buy 16");
            lore.add("§7Shift-right: sell 16");
            meta.setLore(lore);
            display.setItemMeta(meta);
            inv.setItem(slot++, display);
        }

        inv.setItem(BACK_SLOT, button(Material.BARRIER, "§cBack to sections", List.of()));
        Material previousMaterial = page > 0 ? Material.ARROW : Material.GRAY_STAINED_GLASS_PANE;
        String previousName = page > 0 ? "§ePrevious page" : "§7No previous page";
        inv.setItem(PREVIOUS_SLOT, button(previousMaterial, previousName, List.of()));
        inv.setItem(PAGE_INFO_SLOT, button(Material.PAPER, "§ePage " + (page + 1) + "/" + pageCount, List.of("§7Shift-click items to trade 16.")));
        Material nextMaterial = page + 1 < pageCount ? Material.ARROW : Material.GRAY_STAINED_GLASS_PANE;
        String nextName = page + 1 < pageCount ? "§eNext page" : "§7No next page";
        inv.setItem(NEXT_SLOT, button(nextMaterial, nextName, List.of()));
        player.openInventory(inv);
    }

    private ItemStack button(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (!lore.isEmpty()) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private Material iconFor(String section) {
        var items = plugin.shop().items(section);
        return items.isEmpty() ? Material.CHEST : items.get(0).material();
    }

    public static String pretty(Material m) {
        String[] parts = m.name().toLowerCase().split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
        return out.toString().trim();
    }
}
