package com.example.aioeconomy;
import org.bukkit.Bukkit; import org.bukkit.command.*; import org.bukkit.entity.Player;
public final class BalSetCommand implements CommandExecutor{
 private final AIOEconomyPlugin p; public BalSetCommand(AIOEconomyPlugin p){this.p=p;}
 public boolean onCommand(CommandSender s,Command c,String l,String[]a){
  if(!s.hasPermission("aioeconomy.admin")||a.length<2)return false; Player x=Bukkit.getPlayerExact(a[0]); if(x==null){s.sendMessage("§cPlayer not found.");return true;}
  try{double n=Double.parseDouble(a[1]);if(n<0)throw new Exception();p.economy().set(x.getUniqueId(),n);p.economy().save();s.sendMessage("§aSet "+x.getName()+" balance to $"+GuiListener.money(n));}catch(Exception e){s.sendMessage("§cInvalid amount.");}return true;
 }
}
