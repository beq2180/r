package com.example.aioeconomy;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;

public final class EconomyManager {
    private final AIOEconomyPlugin plugin;
    private final Map<UUID, Double> balances = new HashMap<>();
    private File file;

    public EconomyManager(AIOEconomyPlugin plugin) { this.plugin = plugin; }

    public void load() {
        file = new File(plugin.getDataFolder(), "balances.yml");
        if (!file.exists()) return;
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        for (String key : y.getKeys(false)) {
            try { balances.put(UUID.fromString(key), y.getDouble(key)); } catch (Exception ignored) {}
        }
    }

    public void save() {
        if (file == null) return;
        YamlConfiguration y = new YamlConfiguration();
        balances.forEach((uuid, amount) -> y.set(uuid.toString(), amount));
        try { y.save(file); } catch (IOException e) { plugin.getLogger().warning("Could not save balances: " + e.getMessage()); }
    }

    public double get(UUID uuid) { return balances.getOrDefault(uuid, plugin.getConfig().getDouble("starting-balance", 100.0)); }

    public void set(UUID uuid, double amount) { balances.put(uuid, Math.max(0, amount)); }

    public boolean has(UUID uuid, double amount) {
        return amount >= 0 && get(uuid) >= amount;
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (amount < 0 || get(uuid) < amount) return false;
        set(uuid, get(uuid) - amount);
        return true;
    }

    public void deposit(UUID uuid, double amount) {
        if (amount > 0) set(uuid, get(uuid) + amount);
    }

    public Map<UUID, Double> top() { return Collections.unmodifiableMap(balances); }
}
