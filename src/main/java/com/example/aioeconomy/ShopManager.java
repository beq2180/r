package com.example.aioeconomy;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Base64;

public final class ShopManager {
    /**
     * A shop entry stores the complete item template, including Paper's item NBT/data components.
     */
    public record ShopItem(String section, ItemStack item, double buy, double sell) {
        public ShopItem {
            item = item.clone();
            item.setAmount(1);
        }

        public Material material() {
            return item.getType();
        }
    }

    private final AIOEconomyPlugin plugin;
    private final Map<String, List<ShopItem>> sections = new LinkedHashMap<>();
    private File file;
    private String lastSection;

    public ShopManager(AIOEconomyPlugin plugin) { this.plugin = plugin; }

    public void load() {
        file = new File(plugin.getDataFolder(), "shop.yml");
        if (!file.exists()) return;

        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection s = y.getConfigurationSection("sections");
        if (s == null) return;

        for (String section : s.getKeys(false)) {
            ConfigurationSection sectionConfig = s.getConfigurationSection(section);
            if (sectionConfig == null) continue;

            List<ShopItem> list = new ArrayList<>();
            for (String key : sectionConfig.getKeys(false)) {
                String path = "sections." + section + "." + key;
                ItemStack item = null;

                // New format: raw Paper NBT, Base64 encoded.
                String encoded = y.getString(path + ".item-nbt");
                if (encoded != null && !encoded.isBlank()) {
                    try {
                        item = ItemStack.deserializeBytes(Base64.getDecoder().decode(encoded));
                        item.setAmount(1);
                    } catch (Exception ex) {
                        plugin.getLogger().warning("Could not load NBT shop item " + path + ": " + ex.getMessage());
                    }
                }

                // Compatibility with older prototype shop.yml files.
                if (item == null) {
                    item = y.getItemStack(path + ".item");
                }
                if (item == null) {
                    Material m = Material.matchMaterial(y.getString(path + ".material", ""));
                    if (m != null) item = new ItemStack(m, 1);
                }

                if (item != null && !item.getType().isAir()) {
                    list.add(new ShopItem(section, item, y.getDouble(path + ".buy"), y.getDouble(path + ".sell")));
                }
            }
            sections.put(section, list);
            lastSection = section;
        }
    }

    public void save() {
        YamlConfiguration y = new YamlConfiguration();
        for (var e : sections.entrySet()) {
            int i = 0;
            for (ShopItem shopItem : e.getValue()) {
                String path = "sections." + e.getKey() + ".item" + i++;
                ItemStack item = shopItem.item().clone();
                item.setAmount(1);
                y.set(path + ".item-nbt", Base64.getEncoder().encodeToString(item.serializeAsBytes()));
                // Keep a readable material field for easier manual inspection.
                y.set(path + ".material", item.getType().name());
                y.set(path + ".buy", shopItem.buy());
                y.set(path + ".sell", shopItem.sell());
            }
        }
        try {
            y.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save shop: " + e.getMessage());
        }
    }

    public void addSection(String name) {
        sections.putIfAbsent(name, new ArrayList<>());
        lastSection = name;
    }

    public boolean addItem(ItemStack stack, double buy, double sell) {
        return addItem(stack, buy, sell, lastSection);
    }

    public boolean addItem(ItemStack stack, double buy, double sell, String section) {
        if (section == null || !sections.containsKey(section) || stack == null || stack.getType().isAir()) return false;
        ItemStack template = stack.clone();
        template.setAmount(1);
        sections.get(section).add(new ShopItem(section, template, buy, sell));
        lastSection = section;
        return true;
    }

    public boolean hasSection(String section) {
        return section != null && sections.containsKey(section);
    }

    public Set<String> sections() { return sections.keySet(); }
    public List<ShopItem> items(String section) { return sections.getOrDefault(section, List.of()); }
    public String lastSection() { return lastSection; }
}
