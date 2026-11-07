package com.example.aioeconomy;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public final class AuctionCommand implements CommandExecutor {
    private final AIOEconomyPlugin p;
    public AuctionCommand(AIOEconomyPlugin p){this.p=p;}

    public boolean onCommand(CommandSender s, Command c, String l, String[] a){
        if(!(s instanceof Player pl))return true;
        if(a.length>=1&&a[0].equalsIgnoreCase("sell")){
            ItemStack hand=pl.getInventory().getItemInMainHand();
            if(hand.getType().isAir()){pl.sendMessage("§cHold an item.");return true;}
            double price;
            try{price=a.length>=2?Double.parseDouble(a[1]):p.getConfig().getDouble("auction.default-price",100);}catch(Exception e){pl.sendMessage("§cInvalid price.");return true;}
            if(price<=0){pl.sendMessage("§cPrice must be positive.");return true;}
            p.auction().create(pl.getUniqueId(),hand,price);
            pl.getInventory().setItemInMainHand(null);
            pl.sendMessage("§aListed your item for $"+GuiListener.money(price)+".");
            return true;
        }
        String search=a.length>=2&&a[0].equalsIgnoreCase("search")?String.join(" ", Arrays.copyOfRange(a,1,a.length)):"";
        Inventory inv=Bukkit.createInventory(null,54,"Auction House"+(search.isEmpty()?"":" — "+search));
        int slot=0;
        for(var x:p.auction().listings()){
            if(slot>=54)break;
            if(!search.isEmpty()&&!search.equalsIgnoreCase("hand")&&!x.item().getType().name().toLowerCase().contains(search.toLowerCase()))continue;
            if(search.equalsIgnoreCase("hand")){
                ItemStack held = pl.getInventory().getItemInMainHand();
                if(held.getType().isAir() || !x.item().isSimilar(held)) continue;
            }
            ItemStack display=x.item().clone();
            ItemMeta meta=display.getItemMeta();
            List<String> lore=meta.hasLore()?new ArrayList<>(meta.getLore()):new ArrayList<>();
            lore.add("§aPrice: $"+GuiListener.money(x.price()));
            lore.add("§7Seller: "+Optional.ofNullable(Bukkit.getOfflinePlayer(x.seller()).getName()).orElse("Unknown"));
            if(x.seller().equals(pl.getUniqueId())) {
                lore.add("§eClick to take this listing off the AH");
            } else {
                lore.add("§eClick to buy this listing");
            }
            lore.add("§0ID:"+x.id());
            meta.setLore(lore); display.setItemMeta(meta);
            inv.setItem(slot++,display);
        }
        pl.openInventory(inv); return true;
    }
}
