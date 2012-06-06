package com.minecarts.quicksilver;

import java.util.logging.Level;
import java.text.MessageFormat;

import java.util.Set;
import java.util.HashSet;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import com.minecarts.dbpermissions.PermissionsCalculated;

public class Quicksilver extends JavaPlugin implements Listener {
    protected final Set<OfflinePlayer> deaggroedPlayers = new HashSet<OfflinePlayer>();
    protected final Set<OfflinePlayer> vanishedPlayers = new HashSet<OfflinePlayer>();
    
    @Override
    public void onEnable() {       
        getServer().getPluginManager().registerEvents(this, this);
        
        getCommand("vanish").setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                //Determine if they're vanishing themselves or someone else
                if(!sender.hasPermission("quicksilver.vanish.self") && !sender.hasPermission("quicksilver.vanish.other")) return true;
                
                Player playerToVanish = null;
                switch(args.length) {
                    case 0:
                        if(!(sender instanceof Player)) {
                            sender.sendMessage("You cannot vanish yourself when you're not a player.");
                            return true;
                        }
                        playerToVanish = (Player) sender;
                        break;
                        
                    case 1:
                        if(args[0].equalsIgnoreCase("list")) {
                            sender.sendMessage("---Vanished players---");
                            for(OfflinePlayer player : vanishedPlayers) {
                                sender.sendMessage(player.getName());
                            }
                            return true;
                        }

                        List<Player> players = Bukkit.matchPlayer(args[0]);
                        if(players.size() != 1) {
                            sender.sendMessage("Could not vanish " + ChatColor.YELLOW + args[0] + ChatColor.WHITE + ". " +  players.size()  + " players matched.");
                            return true;
                        }

                        playerToVanish = players.get(0);
                }
                if(playerToVanish == null) return false;
                
                
                if(!vanishedPlayers.contains(playerToVanish)) {
                    //Hide this player from all players
                    vanishedPlayers.add(playerToVanish);
                    deaggroedPlayers.add(playerToVanish);
                    updatePlayer(playerToVanish);
                    
                    sender.sendMessage(playerToVanish.getName() + " is now vanished.");
                    if(!playerToVanish.equals(sender)) {
                        playerToVanish.sendMessage(sender.getName() + " has made you invisible.");
                    }
                    log("{0} VANISHED {1}", sender.getName(), playerToVanish.getName());
                } else {
                    vanishedPlayers.remove(playerToVanish);
                    deaggroedPlayers.remove(playerToVanish);
                    updatePlayer(playerToVanish);
                    
                    sender.sendMessage(playerToVanish.getName() + " is now visible.");
                    if(!playerToVanish.equals(sender)) {
                        playerToVanish.sendMessage(sender.getName() + " has made you visible.");
                    }
                    log("{0} UNVANISHED {1}", sender.getName(), playerToVanish.getName());
                }
                
                return true;
            }
        });

        getCommand("aggro").setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if(!sender.hasPermission("quicksilver.aggro.self") && !sender.hasPermission("quicksilver.aggro.other")) return true;

                Player playerToDeaggro = null;
                switch(args.length) {
                    case 0:
                        if(!(sender instanceof Player)) {
                            sender.sendMessage("You cannot deaggro yourself when you're not a player.");
                            return true;
                        }
                        playerToDeaggro = (Player) sender;
                        break;
                        
                    case 1:
                        if(args[0].equalsIgnoreCase("list")) {
                            sender.sendMessage("---Deaggroed players---");
                            for(OfflinePlayer player : deaggroedPlayers) {
                                sender.sendMessage(player.getName());
                            }
                            return true;
                        }

                        List<Player> players = Bukkit.matchPlayer(args[0]);
                        if(players.size() != 1) {
                            sender.sendMessage("Could not deaggro " + ChatColor.YELLOW + args[0] + ChatColor.WHITE + ". " +  players.size()  + " players matched.");
                            return true;
                        }

                        playerToDeaggro = players.get(0);
                }

                if(deaggroedPlayers.contains(playerToDeaggro)) {
                    deaggroedPlayers.remove(playerToDeaggro);

                    sender.sendMessage(playerToDeaggro.getName() + " will now aggro mobs.");
                    if(!playerToDeaggro.equals(sender)) {
                        playerToDeaggro.sendMessage(sender.getName() + " has made it so you will aggro mobs.");
                    }
                    log("{0} AGGROED {1}", sender.getName(), playerToDeaggro.getName());

                } else {
                    deaggroedPlayers.add(playerToDeaggro);

                    sender.sendMessage(playerToDeaggro.getName() + " will no longer aggro mobs.");
                    if(!playerToDeaggro.equals(sender)) {
                        playerToDeaggro.sendMessage(sender.getName() + " has made it so you will no long aggro mobs.");
                    }
                    log("{0} DEAGGROED {1}", sender.getName(), playerToDeaggro.getName());

                }
                return true;
            }
        });


        log("{0} enabled.", getDescription().getVersion());
    }
    
    @Override
    public void onDisable() {
        for(Player player : getOnlinePlayers(vanishedPlayers)) {
            player.sendMessage(ChatColor.YELLOW + "You are no longer vanished (plugin disabled).");
        }
        for(Player player : getOnlinePlayers(deaggroedPlayers)) {
            player.sendMessage(ChatColor.YELLOW + "Mobs will now attack you (plugin disabled).");
        }
    }


    @EventHandler
    public void playerAggro(EntityTargetEvent event) {
        if(event.getTarget() instanceof Player && deaggroedPlayers.contains((Player) event.getTarget())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void playerPickupItem(PlayerPickupItemEvent event) {
        if(vanishedPlayers.contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void permissionsUpdate(PermissionsCalculated event) {
        if(event.getPermissible() instanceof Player) {
            Player player = (Player) event.getPermissible();
            if(player == null) return; // shouldn't happen, but something has been throwing NPEs
            
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
    public void playerConnect(PlayerJoinEvent event) {
        updatePlayer(event.getPlayer());
    }
    
    
    protected Set<Player> getOnlinePlayers(Set<OfflinePlayer> players) {
        Set<Player> online = new HashSet<Player>();
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(players.contains(player)) {
                online.add(player);
            }
        }
        return online;
    }
    
    
    protected void updatePlayer(Player target) {
        if(target == null) return; // shouldn't happen, but something has been throwing NPEs
        
        // player is vanished
        if(vanishedPlayers.contains(target)) {
            // hide player from online players
            for(Player p : Bukkit.getOnlinePlayers()) {
                if(target.equals(p)) continue; // skip self
                
                // unless they can see vanished players
                if(p.hasPermission("quicksilver.vanish.see")) {
                    p.showPlayer(target);
                }
                else {
                    p.hidePlayer(target);
                }
            }
        }
        // player is NOT vanished
        else {
            // show player to online players
            for(Player p : Bukkit.getOnlinePlayers()) {
                if(target.equals(p)) continue; // skip self
                
                p.showPlayer(target);
            }
        }
        
        // player can see vanished players
        if(target.hasPermission("quicksilver.vanish.see")) {
            // show all vanished players to player
            for(Player player : getOnlinePlayers(vanishedPlayers)) {
                if(target.equals(player)) continue; // skip self
                
                target.showPlayer(player);
            }
        }
        // player can't see vanished players
        else {
            // hide all vanished players from player
            for(Player player : getOnlinePlayers(vanishedPlayers)) {
                if(target.equals(player)) continue; // skip self
                
                target.hidePlayer(player);
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
