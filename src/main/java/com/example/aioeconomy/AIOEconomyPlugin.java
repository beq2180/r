package com.example.aioeconomy;

import org.bukkit.plugin.java.JavaPlugin;

public final class AIOEconomyPlugin extends JavaPlugin {
    private EconomyManager economy;
    private ShopManager shop;
    private AuctionManager auction;
    private TradeManager trades;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        economy = new EconomyManager(this);
        shop = new ShopManager(this);
        auction = new AuctionManager(this, economy);
        trades = new TradeManager(this, economy);

        economy.load();
        shop.load();
        auction.load();

        getCommand("sell").setExecutor(new SellCommand(this));
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("ah").setExecutor(new AuctionCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("bal").setExecutor(new BalanceCommand(this));
        getCommand("baltop").setExecutor(new BaltopCommand(this));
        getCommand("trade").setExecutor(new TradeCommand(this));
        getCommand("shopadd").setExecutor(new ShopAddCommand(this));
        getCommand("shopremove").setExecutor(new ShopRemoveCommand(this));
        getCommand("balset").setExecutor(new BalSetCommand(this));

        getServer().getPluginManager().registerEvents(new GuiListener(this), this);

        getLogger().info("AIOEconomy prototype enabled.");
    }

    @Override
    public void onDisable() {
        if (economy != null) economy.save();
        if (shop != null) shop.save();
        if (auction != null) auction.save();
    }

    public EconomyManager economy() { return economy; }
    public ShopManager shop() { return shop; }
    public AuctionManager auction() { return auction; }
    public TradeManager trades() { return trades; }
}
