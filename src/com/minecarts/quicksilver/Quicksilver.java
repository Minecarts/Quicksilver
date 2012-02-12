package com.minecarts.quicksilver;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Quicksilver extends JavaPlugin implements Listener {
    private ArrayList<Player> deagroedPlayers = new ArrayList<Player>();
    private ArrayList<Player> vanishedPlayers = new ArrayList<Player>();
    public void onEnable(){

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("vanish").setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                //Determine if they're vanishing themselves or someone else
                if(!sender.hasPermission("quicksilver.vanish.self") || !sender.hasPermission("quicksilver.vanish.other")) return true;
                Player playerToVanish = null;
                switch(args.length){
                    case 0:
                        if(!(sender instanceof Player)){
                            sender.sendMessage("You cannot vanish yourself when you're not a player.");
                            return true;
                        }
                        playerToVanish = (Player)sender;
                        break;
                    case 1:
                        if(args[0].equalsIgnoreCase("list")){
                            sender.sendMessage("---Vanished players---");
                            for(Player p : vanishedPlayers){
                                sender.sendMessage(p.getDisplayName());
                            }
                            return true;
                        }

                        List<Player> players = Bukkit.matchPlayer(args[0]);
                        if(players.size() != 1){
                            sender.sendMessage("Could not vanish " + ChatColor.YELLOW + args[0] + ChatColor.WHITE + ". " +  players.size()  + " players matched.");
                            return true;
                        }

                        playerToVanish = players.get(0);
                }

                //Hide the player (depending upon self or someone else)
                if(playerToVanish == null) return false;
                if(!vanishedPlayers.contains(playerToVanish)){
                    //Hide this player from all players
                    for(Player p : Bukkit.getOnlinePlayers()){
                        if(p.hasPermission("quicksilver.vanish.see")) continue;
                        p.hidePlayer(playerToVanish);
                    }
                    vanishedPlayers.add(playerToVanish);
                    if(!deagroedPlayers.contains(playerToVanish)) deagroedPlayers.add(playerToVanish);
                    sender.sendMessage(playerToVanish.getDisplayName() + " is now vanished.");
                    if(!playerToVanish.getName().equalsIgnoreCase(sender.getName())){
                        playerToVanish.sendMessage(sender.getName() + " has made you invisible.");
                    }
                } else {
                    for(Player p : Bukkit.getOnlinePlayers()){
                        p.showPlayer(playerToVanish);
                    }
                    sender.sendMessage(playerToVanish.getDisplayName() + " is now visible.");
                    if(!playerToVanish.getName().equalsIgnoreCase(sender.getName())){
                        playerToVanish.sendMessage(sender.getName() + " has made you visible.");
                    }
                    vanishedPlayers.remove(playerToVanish);
                    if(deagroedPlayers.contains(playerToVanish)) deagroedPlayers.remove(playerToVanish);

                }
                return true;
            }
        });

        getCommand("agro").setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if(!sender.hasPermission("quicksilver.agro.self") || !sender.hasPermission("quicksilver.agro.other")) return true;

                Player playerToDeagro = null;
                switch(args.length){
                    case 0:
                        if(!(sender instanceof Player)){
                            sender.sendMessage("You cannot deagro yourself when you're not a player.");
                            return true;
                        }
                        playerToDeagro = (Player)sender;
                        break;
                    case 1:
                        if(args[0].equalsIgnoreCase("list")){
                            sender.sendMessage("---Deagroed players---");
                            for(Player p : deagroedPlayers){
                                sender.sendMessage(p.getDisplayName());
                            }
                            return true;
                        }

                        List<Player> players = Bukkit.matchPlayer(args[0]);
                        if(players.size() != 1){
                            sender.sendMessage("Could not deagro " + ChatColor.YELLOW + args[0] + ChatColor.WHITE + ". " +  players.size()  + " players matched.");
                            return true;
                        }

                        playerToDeagro = players.get(0);
                }

                if(deagroedPlayers.contains(playerToDeagro)){
                    deagroedPlayers.remove(playerToDeagro);
                    sender.sendMessage(playerToDeagro.getDisplayName() + " will now agro mobs.");
                    if(!playerToDeagro.getName().equalsIgnoreCase(sender.getName())){
                        playerToDeagro.sendMessage(sender.getName() + " has made it so you will agro mobs.");
                    }
                } else {
                    deagroedPlayers.add(playerToDeagro);
                    if(!playerToDeagro.getName().equalsIgnoreCase(sender.getName())){
                        playerToDeagro.sendMessage(sender.getName() + " has made it so you will no long agro mobs.");
                    }
                }
                return true;
            }
        });
    }

    @EventHandler
    public void playerAgroListener(EntityTargetEvent event){
        if(deagroedPlayers.contains(event.getTarget())){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void playerPickupListener(PlayerPickupItemEvent event){
        if(vanishedPlayers.contains(event.getPlayer())){
            event.setCancelled(true);
        }
    }
}
