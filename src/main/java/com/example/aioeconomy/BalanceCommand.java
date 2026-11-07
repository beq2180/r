package com.example.aioeconomy;
import org.bukkit.command.*; import org.bukkit.entity.Player;
public final class BalanceCommand implements CommandExecutor{
 private final AIOEconomyPlugin p; public BalanceCommand(AIOEconomyPlugin p){this.p=p;}
 public boolean onCommand(CommandSender s,Command c,String l,String[]a){if(s instanceof Player x)x.sendMessage("§aBalance: §f$"+GuiListener.money(p.economy().get(x.getUniqueId())));return true;}
}
