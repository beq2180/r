package com.example.aioeconomy;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public final class TradeCommand implements CommandExecutor {
    private final AIOEconomyPlugin p;
    public TradeCommand(AIOEconomyPlugin p){this.p=p;}

    public boolean onCommand(CommandSender s,Command c,String l,String[]a){
        if(!(s instanceof Player from)||a.length<1){
            if(s instanceof Player) s.sendMessage("§cUsage: /trade <player>");
            return true;
        }
        Player to=Bukkit.getPlayerExact(a[0]);
        if(to==null||to==from){from.sendMessage("§cPlayer not found.");return true;}
        if(p.trades().hasRequest(from,to)){
            p.trades().clear(from);
            p.trades().open(from,to);
        } else {
            p.trades().request(from,to);
        }
        return true;
    }
}
