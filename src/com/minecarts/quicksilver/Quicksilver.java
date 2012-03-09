package com.minecarts.quicksilver;

import java.util.logging.Level;
import java.text.MessageFormat;

import java.util.Set;
import java.util.HashSet;
import java.util.List;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.minecarts.dbpermissions.PermissionsCalculated;

public class Quicksilver extends JavaPlugin implements Listener {
    protected final Set<Player> deaggroedPlayers = new HashSet<Player>();
    protected final Set<Player> vanishedPlayers = new HashSet<Player>();
    
    @Override
    public void onEnable(){       
        getServer().getPluginManager().registerEvents(this, this);
        
        getCommand("vanish").setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                //Determine if they're vanishing themselves or someone else
                if(!sender.hasPermission("quicksilver.vanish.self") && !sender.hasPermission("quicksilver.vanish.other")) return true;
                
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
                                if(!p.isOnline()) continue;
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
                if(playerToVanish == null) return false;
                
                
                if(!vanishedPlayers.contains(playerToVanish)){
                    //Hide this player from all players
                    vanishedPlayers.add(playerToVanish);
                    deaggroedPlayers.add(playerToVanish);
                    updatePlayer(playerToVanish);
                    
                    sender.sendMessage(playerToVanish.getDisplayName() + " is now vanished.");
                    if(!playerToVanish.getName().equalsIgnoreCase(sender.getName())){
                        playerToVanish.sendMessage(sender.getName() + " has made you invisible.");
                    }
                    getLogger().log(Level.INFO,sender.getName()  + " VANISHED " + playerToVanish.getName());
                } else {
                    vanishedPlayers.remove(playerToVanish);
                    deaggroedPlayers.remove(playerToVanish);
                    updatePlayer(playerToVanish);
                    
                    sender.sendMessage(playerToVanish.getDisplayName() + " is now visible.");
                    if(!playerToVanish.getName().equalsIgnoreCase(sender.getName())){
                        playerToVanish.sendMessage(sender.getName() + " has made you visible.");
                    }
                    getLogger().log(Level.INFO,sender.getName()  + " APPEARED " + playerToVanish.getName());
                }
                
                return true;
            }
        });

        getCommand("aggro").setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if(!sender.hasPermission("quicksilver.aggro.self") && !sender.hasPermission("quicksilver.aggro.other")) return true;

                Player playerToDeaggro = null;
                switch(args.length){
                    case 0:
                        if(!(sender instanceof Player)){
                            sender.sendMessage("You cannot deaggro yourself when you're not a player.");
                            return true;
                        }
                        playerToDeaggro = (Player)sender;
                        break;
                    case 1:
                        if(args[0].equalsIgnoreCase("list")){
                            sender.sendMessage("---Deaggroed players---");
                            for(Player p : deaggroedPlayers){
                                sender.sendMessage(p.getDisplayName());
                            }
                            return true;
                        }

                        List<Player> players = Bukkit.matchPlayer(args[0]);
                        if(players.size() != 1){
                            sender.sendMessage("Could not deaggro " + ChatColor.YELLOW + args[0] + ChatColor.WHITE + ". " +  players.size()  + " players matched.");
                            return true;
                        }

                        playerToDeaggro = players.get(0);
                }

                if(deaggroedPlayers.contains(playerToDeaggro)) {
                    deaggroedPlayers.remove(playerToDeaggro);

                    sender.sendMessage(playerToDeaggro.getDisplayName() + " will now aggro mobs.");
                    if(!playerToDeaggro.getName().equalsIgnoreCase(sender.getName())){
                        playerToDeaggro.sendMessage(sender.getName() + " has made it so you will aggro mobs.");
                    }
                    getLogger().log(Level.INFO,sender.getName()  + " AGGROED " + playerToDeaggro.getName());

                } else {
                    deaggroedPlayers.add(playerToDeaggro);

                    sender.sendMessage(playerToDeaggro.getDisplayName() + " will no longer aggro mobs.");
                    if(!playerToDeaggro.getName().equalsIgnoreCase(sender.getName())){
                        playerToDeaggro.sendMessage(sender.getName() + " has made it so you will no long aggro mobs.");
                    }
                    getLogger().log(Level.INFO,sender.getName()  + " DEAGGROED " + playerToDeaggro.getName());

                }
                return true;
            }
        });


        getLogger().log(Level.INFO,getDescription().getVersion() + " enabled.");
    }
    
    @Override
    public void onDisable(){
        for(Player p : vanishedPlayers){
            p.sendMessage(ChatColor.YELLOW + "You are no longer vanished (plugin disabled).");
        }
        for(Player p : deaggroedPlayers){
            if(vanishedPlayers.contains(p)) continue;
            p.sendMessage(ChatColor.YELLOW + "Mobs will now attack you (plugin disabled).");
        }
    }


    @EventHandler
    public void playerAggro(EntityTargetEvent event){
        if(event.getTarget() instanceof Player && deaggroedPlayers.contains((Player) event.getTarget())){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void playerPickupItem(PlayerPickupItemEvent event){
        if(vanishedPlayers.contains(event.getPlayer())){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void permissionsUpdate(PermissionsCalculated event){
        if(event.getPermissible() instanceof Player) {
            Player player = (Player) event.getPermissible();
            if(player.hasPermission("quicksilver.vanish.always")) {
                vanishedPlayers.add(player);
                deaggroedPlayers.add(player);
            }
            else if(player.hasPermission("quicksilver.deaggro.always")) {
                deaggroedPlayers.add(player);
            }
            updatePlayer(player);
        }
    }

    @EventHandler
    public void playerConnect(PlayerJoinEvent event){
        updatePlayer(event.getPlayer());
    }
    
    
    protected void updatePlayer(Player player) {
        // player is vanished
        if(vanishedPlayers.contains(player)) {
            // hide player from online players
            for(Player p : Bukkit.getOnlinePlayers()) {
                // unless they can see vanished players
                if(p.hasPermission("quicksilver.vanish.see")) {
                    p.showPlayer(player);
                }
                else {
                    p.hidePlayer(player);
                }
            }
        }
        // player is NOT vanished
        else {
            // show player to online players
            for(Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(player);
            }
        }
        
        // player can see vanished players
        if(player.hasPermission("quicksilver.vanish.see")) {
            // show all vanished players to player
            for(Player p : vanishedPlayers) {
                player.showPlayer(p);
            }
        }
        // player can't see vanished players
        else {
            // hide all vanished players from player
            for(Player p : vanishedPlayers) {
                player.hidePlayer(p);
            }
        }
    }
    
    
    
    
    public void log(String message) {
        log(Level.INFO, message);
    }
    public void log(Level level, String message) {
        getLogger().log(level, message);
    }
    public void log(String message, Object... args) {
        log(MessageFormat.format(message, args));
    }
    public void log(Level level, String message, Object... args) {
        log(level, MessageFormat.format(message, args));
    }
    
    public void debug(String message) {
        log(Level.FINE, message);
    }
    public void debug(String message, Object... args) {
        debug(MessageFormat.format(message, args));
    }
    
}
