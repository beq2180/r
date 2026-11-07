package com.example.aioeconomy;

import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.*;
import java.util.Base64;

public final class AuctionManager {
    public record Listing(UUID id, UUID seller, ItemStack item, double price) {}

    private final AIOEconomyPlugin plugin;
    private final EconomyManager economy;
    private final Map<UUID, Listing> listings = new LinkedHashMap<>();
    private File file;

    public AuctionManager(AIOEconomyPlugin plugin, EconomyManager economy) {
        this.plugin = plugin; this.economy = economy;
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "auctions.yml");
        if (!file.exists()) return;
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        for (String id : y.getStringList("ids")) {
            try {
                UUID uid = UUID.fromString(id);
                UUID seller = UUID.fromString(y.getString("data." + id + ".seller"));
                ItemStack item = null;

                // New format: exact raw Paper NBT, Base64 encoded.
                String encoded = y.getString("data." + id + ".item-nbt");
                if (encoded != null && !encoded.isBlank()) {
                    item = ItemStack.deserializeBytes(Base64.getDecoder().decode(encoded));
                }

                // Compatibility with old prototype auctions.yml files.
                if (item == null) item = y.getItemStack("data." + id + ".item");

                double price = y.getDouble("data." + id + ".price");
                if (item != null) listings.put(uid, new Listing(uid, seller, item, price));
            } catch (Exception ignored) {}
        }
    }

    public void save() {
        YamlConfiguration y = new YamlConfiguration();
        y.set("ids", listings.keySet().stream().map(UUID::toString).toList());
        for (Listing l : listings.values()) {
            String p = "data." + l.id();
            y.set(p + ".seller", l.seller().toString());
            y.set(p + ".item-nbt", Base64.getEncoder().encodeToString(l.item().serializeAsBytes()));
            y.set(p + ".price", l.price());
        }
        try {
            y.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save auctions: " + e.getMessage());
        }
    }

    public Listing create(UUID seller, ItemStack item, double price) {
        Listing l = new Listing(UUID.randomUUID(), seller, item.clone(), price);
        listings.put(l.id(), l);
        save();
        return l;
    }

    public Collection<Listing> listings() { return listings.values(); }
    public Listing get(UUID id) { return listings.get(id); }

    public void remove(UUID id) { listings.remove(id); save(); }

    public boolean buy(UUID buyer, UUID id) {
        Listing l = listings.get(id);
        if (l == null || buyer.equals(l.seller()) || !economy.withdraw(buyer, l.price())) return false;
        economy.deposit(l.seller(), l.price());
        listings.remove(id);
        save();
        return true;
    }
}
