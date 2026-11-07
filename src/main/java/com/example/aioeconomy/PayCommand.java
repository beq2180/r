package com.example.aioeconomy;
import org.bukkit.Bukkit; import org.bukkit.command.*; import org.bukkit.entity.Player;
public final class PayCommand implements CommandExecutor{
 private final AIOEconomyPlugin p; public PayCommand(AIOEconomyPlugin p){this.p=p;}
 public boolean onCommand(CommandSender s,Command c,String l,String[]a){
  if(!(s instanceof Player from)||a.length<2)return false;
  Player to=Bukkit.getPlayerExact(a[0]); if(to==null){from.sendMessage("§cPlayer not found.");return true;}
  try{double n=Double.parseDouble(a[1]); if(n<=0||!p.economy().withdraw(from.getUniqueId(),n)){from.sendMessage("§cInvalid amount or insufficient funds.");return true;}
   p.economy().deposit(to.getUniqueId(),n); from.sendMessage("§aPaid "+to.getName()+" $"+GuiListener.money(n)+"."); to.sendMessage("§aReceived $"+GuiListener.money(n)+" from "+from.getName()+".");
  }catch(Exception e){from.sendMessage("§cInvalid amount.");} return true;
 }
}
