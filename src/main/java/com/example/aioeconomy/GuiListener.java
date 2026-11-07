package com.example.aioeconomy;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class GuiListener implements Listener {
    private final AIOEconomyPlugin plugin;
    private final ShopCommand shopCommand;
    public GuiListener(AIOEconomyPlugin plugin) {
        this.plugin = plugin;
        this.shopCommand = new ShopCommand(plugin);
    }

    @EventHandler
    public void click(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();
        Inventory top = e.getView().getTopInventory();

        if (top.getHolder() instanceof TradeHolder trade) {
            handleTradeClick(e, p, trade);
            return;
        }

        if (top.getHolder() instanceof ShopCommand.ShopPageHolder shopPage) {
            handleShopPageClick(e, p, shopPage);
            return;
        }

        if (title.equals("Shop")) {
            e.setCancelled(true);
            if (e.getClickedInventory() != top) return;
            int slot = e.getRawSlot();
            if (slot < 0 || slot >= 45) return;
            List<String> sections = new ArrayList<>(plugin.shop().sections());
            if (slot >= sections.size()) return;
            shopCommand.openSection(p, sections.get(slot));
            return;
        }

        if (title.startsWith("Auction House")) {
            if (e.getClickedInventory() != top) return;
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item == null || item.getType().isAir()) return;
            ItemMeta meta = item.getItemMeta();
            if (meta == null || meta.getLore() == null) return;
            for (String line : meta.getLore()) {
                if (!line.startsWith("§0ID:")) continue;
                try {
                    UUID id = UUID.fromString(line.substring(5));
                    var listing = plugin.auction().get(id);
                    if (listing == null) { p.sendMessage("§cThat listing no longer exists."); return; }

                    if (listing.seller().equals(p.getUniqueId())) {
                        if (p.getInventory().firstEmpty() == -1) {
                            p.sendMessage("§cYou need an empty inventory slot to take this listing off the AH."); return;
                        }
                        p.getInventory().addItem(listing.item());
                        plugin.auction().remove(id);
                        p.sendMessage("§aListing removed and item returned to you.");
                        p.closeInventory();
                    } else if (plugin.auction().buy(p.getUniqueId(), id)) {
                        p.getInventory().addItem(listing.item());
                        p.sendMessage("§aPurchased the item for $" + money(listing.price()));
                        p.closeInventory();
                    } else {
                        p.sendMessage("§cYou cannot afford this listing or it no longer exists.");
                    }
                } catch (Exception ignored) {}
                break;
            }
        }
    }

    private void handleShopPageClick(InventoryClickEvent e, Player player, ShopCommand.ShopPageHolder page) {
        e.setCancelled(true);
        if (e.getClickedInventory() != e.getView().getTopInventory()) return;
        int slot = e.getRawSlot();
        if (slot == ShopCommand.BACK_SLOT) {
            shopCommand.openSections(player);
            return;
        }
        if (slot == ShopCommand.PREVIOUS_SLOT) {
            if (page.page() > 0) shopCommand.openSection(player, page.section(), page.page() - 1);
            return;
        }
        if (slot == ShopCommand.NEXT_SLOT) {
            if (page.page() + 1 < page.pageCount()) shopCommand.openSection(player, page.section(), page.page() + 1);
            return;
        }
        if (slot < 0 || slot >= ShopCommand.PAGE_SIZE) return;
        int itemIndex = page.page() * ShopCommand.PAGE_SIZE + slot;
        List<ShopManager.ShopItem> items = plugin.shop().items(page.section());
        if (itemIndex >= items.size()) return;
        ShopManager.ShopItem shopItem = items.get(itemIndex);

        int amount;
        boolean buying;
        switch (e.getClick()) {
            case SHIFT_LEFT -> { amount = 16; buying = true; }
            case SHIFT_RIGHT -> { amount = 16; buying = false; }
            case LEFT -> { amount = 1; buying = true; }
            case RIGHT -> { amount = 1; buying = false; }
            default -> { return; }
        }
        if (buying) buyItems(player, shopItem, amount);
        else sellItems(player, shopItem, amount);
    }

    private void buyItems(Player player, ShopManager.ShopItem shopItem, int amount) {
        double total = shopItem.buy() * amount;
        if (!plugin.economy().has(player.getUniqueId(), total)) {
            player.sendMessage("§cYou cannot afford " + amount + "x that item.");
            return;
        }
        if (!canFit(player, shopItem.item(), amount)) {
            player.sendMessage("§cYour inventory doesn't have room for " + amount + " items.");
            return;
        }
        if (!plugin.economy().withdraw(player.getUniqueId(), total)) {
            player.sendMessage("§cYou cannot afford that purchase.");
            return;
        }
        if (!insertItems(player, shopItem.item(), amount)) {
            plugin.economy().deposit(player.getUniqueId(), total);
            player.sendMessage("§cYour inventory doesn't have room for those items.");
            return;
        }
        player.sendMessage("§aBought " + amount + "x " + ShopCommand.pretty(shopItem.material())
                + " for $" + money(total));
    }

    private void sellItems(Player player, ShopManager.ShopItem shopItem, int amount) {
        int available = countMatching(player, shopItem.item());
        if (available < amount) {
            player.sendMessage("§cYou need " + amount + " of that exact item to sell this amount (you have " + available + ").");
            return;
        }
        if (!removeMatching(player, shopItem.item(), amount)) {
            player.sendMessage("§cCould not remove the items from your inventory.");
            return;
        }
        double total = shopItem.sell() * amount;
        plugin.economy().deposit(player.getUniqueId(), total);
        player.sendMessage("§aSold " + amount + "x " + ShopCommand.pretty(shopItem.material())
                + " for $" + money(total));
    }

    private boolean canFit(Player player, ItemStack template, int amount) {
        int remaining = amount;
        int inventoryLimit = player.getInventory().getMaxStackSize();
        for (ItemStack current : player.getInventory().getStorageContents()) {
            if (current == null || current.getType().isAir() || !current.isSimilar(template)) continue;
            int limit = Math.min(inventoryLimit, current.getMaxStackSize());
            remaining -= Math.max(0, limit - current.getAmount());
        }
        int emptySlotCapacity = Math.min(inventoryLimit, template.getMaxStackSize());
        for (ItemStack current : player.getInventory().getStorageContents()) {
            if (remaining <= 0) break;
            if (current == null || current.getType().isAir()) remaining -= emptySlotCapacity;
        }
        return remaining <= 0;
    }

    private boolean insertItems(Player player, ItemStack template, int amount) {
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) contents[i] = contents[i].clone();
        }
        int remaining = amount;
        int inventoryLimit = player.getInventory().getMaxStackSize();

        for (ItemStack current : contents) {
            if (current == null || current.getType().isAir() || !current.isSimilar(template)) continue;
            int limit = Math.min(inventoryLimit, current.getMaxStackSize());
            int moved = Math.min(remaining, Math.max(0, limit - current.getAmount()));
            if (moved > 0) {
                current.setAmount(current.getAmount() + moved);
                remaining -= moved;
            }
            if (remaining == 0) break;
        }
        int emptySlotCapacity = Math.min(inventoryLimit, template.getMaxStackSize());
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            if (contents[i] != null && !contents[i].getType().isAir()) continue;
            int moved = Math.min(remaining, emptySlotCapacity);
            ItemStack addition = template.clone();
            addition.setAmount(moved);
            contents[i] = addition;
            remaining -= moved;
        }
        if (remaining > 0) return false;
        player.getInventory().setStorageContents(contents);
        return true;
    }

    private int countMatching(Player player, ItemStack template) {
        int amount = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item != null && item.isSimilar(template)) amount += item.getAmount();
        }
        return amount;
    }

    private boolean removeMatching(Player player, ItemStack template, int amount) {
        if (countMatching(player, template) < amount) return false;
        int remaining = amount;
        for (int slot = 0; slot < player.getInventory().getStorageContents().length && remaining > 0; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null || !item.isSimilar(template)) continue;
            int removed = Math.min(remaining, item.getAmount());
            if (removed == item.getAmount()) player.getInventory().setItem(slot, null);
            else item.setAmount(item.getAmount() - removed);
            remaining -= removed;
        }
        return remaining == 0;
    }

    private void handleTradeClick(InventoryClickEvent e, Player p, TradeHolder trade) {
        if (e.getClickedInventory() != e.getView().getTopInventory()) {
            if (e.isShiftClick()) e.setCancelled(true);
            return;
        }
        int slot = e.getRawSlot();
        e.setCancelled(true);

        if (slot == 22 || slot == 31) {
            if ((trade.isA(p) && slot != 22) || (trade.isB(p) && slot != 31)) {
                p.sendMessage("§cThat's the other player's accept button."); return;
            }
            trade.toggleAccept(p);
            if (trade.bothAccepted()) {
                trade.markFinished();
                plugin.trades().complete(trade);
            } else {
                p.sendMessage("§aYou accepted the trade.");
            }
            return;
        }
        if (slot == 49) { trade.markFinished(); plugin.trades().cancel(trade); return; }

        if (trade.ownSlot(p, slot)) {
            // We cancelled the normal event, so manually support simple pickup/place.
            ItemStack cursor = e.getCursor();
            ItemStack clicked = trade.inventory().getItem(slot);
            trade.inventory().setItem(slot, cursor == null || cursor.getType().isAir() ? null : cursor.clone());
            e.getWhoClicked().setItemOnCursor(clicked == null ? null : clicked.clone());
            trade.resetAccepts();
        } else {
            p.sendMessage("§cYou can only edit your side of the trade.");
        }
    }

    @EventHandler
    public void drag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();
        if (title.equals("Shop") || e.getView().getTopInventory().getHolder() instanceof ShopCommand.ShopPageHolder) {
            for (int slot : e.getRawSlots()) {
                if (slot < e.getView().getTopInventory().getSize()) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
        if (!(e.getView().getTopInventory().getHolder() instanceof TradeHolder trade)) return;
        for (int slot : e.getRawSlots()) {
            if (slot < e.getView().getTopInventory().getSize()) {
                e.setCancelled(true);
                p.sendMessage("§cClick individual slots to place items in a trade.");
                return;
            }
        }
    }

    @EventHandler
    public void close(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        if (!(e.getInventory().getHolder() instanceof TradeHolder trade)) return;
        if (!trade.finished()) {
            trade.markFinished();
            plugin.trades().cancel(trade);
        }
    }

    private ItemStack findOne(Player p, ItemStack template) {
        for (ItemStack item : p.getInventory().getContents())
            if (item != null && item.getAmount() > 0 && item.isSimilar(template)) return item;
        return null;
    }

    private void removeOne(Player p, ItemStack template) {
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack item = p.getInventory().getItem(i);
            if (item == null || !item.isSimilar(template)) continue;
            if (item.getAmount() == 1) p.getInventory().setItem(i, null);
            else item.setAmount(item.getAmount() - 1);
            return;
        }
    }

    static String money(double n) { return String.format("%.2f", n); }
}
