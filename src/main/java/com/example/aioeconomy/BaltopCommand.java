package com.example.aioeconomy;
import org.bukkit.Bukkit; import org.bukkit.command.*; import java.util.*;
public final class BaltopCommand implements CommandExecutor{
 private final AIOEconomyPlugin p; public BaltopCommand(AIOEconomyPlugin p){this.p=p;}
 public boolean onCommand(CommandSender s,Command c,String l,String[]a){
  var list=new ArrayList<>(p.economy().top().entrySet()); list.sort((x,y)->Double.compare(y.getValue(),x.getValue()));
  s.sendMessage("§6§lBalance Top"); int i=1; for(var e:list){s.sendMessage("§e"+i+". §f"+Bukkit.getOfflinePlayer(e.getKey()).getName()+" §7$"+GuiListener.money(e.getValue()));if(i++>=10)break;} return true;
 }
}
