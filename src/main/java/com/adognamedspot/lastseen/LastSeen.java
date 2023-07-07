package com.adognamedspot.lastseen;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public final class LastSeen extends JavaPlugin implements Listener {
	public static int LastListOccupants = 100;
	public static Object[][] LastList = new Object[LastListOccupants][2];
	
	private static LastSeen instance;
	
    @Override
    public void onEnable() {
    	instance = this;
    	getServer().getPluginManager().registerEvents(this, this);
    	PlayerData.loadLastList();
    }
    
    @Override
    public void onDisable() {
    	PlayerData.saveLastList();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if (cmd.getName().equalsIgnoreCase("last")) {
    		if (args.length != 1) {
    			lastPlayer(sender, "10");	// Default LastList Length
    		} else {
    			lastPlayer(sender, args[0]);
    		}
    		return true;
    	} else if (cmd.getName().equalsIgnoreCase("seen")) {
    	    if (args.length != 1) {
    	        return false;
    	    }
    	    return seenPlayer(sender, args[0]);
    	} else if (cmd.getName().equalsIgnoreCase("info")) {
	        if (args.length != 1) {
	            return false;
	        }
	        return PlayerInfo(sender, args[0]);
	    }
    	return false;
    }
    
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		// Called when a player leaves a server
		PlayerData.Quit(event.getPlayer());
    	HandleLastList(event.getPlayer());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		// TODO Insert logic to be performed when the Player joins the server
		PlayerData.Join(event.getPlayer());
	}

    public static LastSeen getInstance() {
    	return instance;
    }
 
    public void HandleLastList(Player player) {
    	// Add Player to beginning of list, cycle everyone else down one spot
       	for (int x = (LastListOccupants - 1); x > 0; x--) {
       		if (LastList[x-1][0] != null) {
       			LastList[x][0] = LastList[x-1][0];
       			LastList[x][1] = LastList[x-1][1];
       		}
       	}
       	LastList[0][0] = player.getName();
       	LastList[0][1] = System.currentTimeMillis();
    }
    
    public boolean lastPlayer(CommandSender sender, String arg) {
    	int length = 0;
    	
    	if (isInteger(arg)) {
    		length = Integer.parseInt(arg);
    	} 
    	if (length > LastListOccupants) {
    		length = LastListOccupants;		// Maximum LastList Length
    		sender.sendMessage("Giving you the maximum (" + LastListOccupants + ") number of entries.");
    	} else if (length < 1) {
    		sender.sendMessage("How do you expect me to do that? Here's a random amount.");
    		Random rn = new Random();
    		length = rn.nextInt(10) + 1;
    	}
    	
		sender.sendMessage(ChatColor.YELLOW + "--------------------" 
				+ ChatColor.BLUE + "Last Online" + ChatColor.YELLOW + "---------------------");
    	if (LastList[0][0] == null) {
    		sender.sendMessage(ChatColor.RED + "No one has left since the last reboot.");
    	} else {
    		for (int i = 0; i < length; i++) {
    			if (LastList[i][0] != null) {
    				sender.sendMessage(" " + (i+1) + ". " + LastList[i][0] + " - " + ChatColor.GRAY 
    						+ wayback(System.currentTimeMillis() - (long)LastList[i][1]) + " ago.");
    			}
    		}
    	}
		sender.sendMessage(ChatColor.YELLOW + "--------------------------------------------------");
    	return true;
    }

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
    
    public boolean seenPlayer(CommandSender sender, String arg) {
    	Player target = Bukkit.getServer().getPlayer(arg);

    	// Make sure the player is online.
    	if (target == null) {
    		// Player is not currently online
    		Long LastOnline = PlayerData.lastSeen(arg);
    		if (LastOnline == 0) {
        		sender.sendMessage(arg + " has never been seen.");
        		return true;    				
    		}
    		sender.sendMessage(arg + " was last seen " + wayback(System.currentTimeMillis() - LastOnline) + " ago.");
    		return true;
    	} else {
    		// Player is currently online
    		sender.sendMessage(target.getName() + " is currently online!");
    		return true;
    	}
    }
    
    public boolean PlayerInfo(CommandSender sender, String arg) {
    	PlayerData.processStats(arg, 0);
    	PlayerData.getInfo(sender, arg);
    	return true;
    }
    
    /**
      * Convert a millisecond duration to a string format
      * 
      * @param millis A duration to convert to a string form
      * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
      */
    public static String wayback(long millis) {
        if(millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        if (days > 1) {
        sb.append(days);
        sb.append(" Days ");
        }
        if (days == 1) {
        sb.append(days);
        sb.append(" Day ");
        }
        if (hours > 1) {
        sb.append(hours);
        sb.append(" Hours ");
        }
        if (hours == 1) {
        sb.append(hours);
        sb.append(" Hour ");
        }
        if (minutes > 1) {
        sb.append(minutes);
        sb.append(" Minutes ");
        }
        if (minutes == 1) {
        sb.append(minutes);
        sb.append(" Minute ");
        }
        if (seconds == 1) {
        sb.append(seconds);
        sb.append(" Second");
        } else {
        sb.append(seconds);
        sb.append(" Seconds");
        }

        return(sb.toString());
        }
}



