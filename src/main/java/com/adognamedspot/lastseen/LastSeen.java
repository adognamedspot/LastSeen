package com.adognamedspot.lastseen;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class LastSeen extends JavaPlugin implements Listener {
	
	public List<String> LastList_Name = new ArrayList<String>();
	public List<Long> LastList_Time = new ArrayList<Long>();
	public String EMPTY = "-EMPTY VOiD-";
	
	private static LastSeen instance;
	
    @Override
    public void onEnable() {
        // TODO Load LastList from disk
    	instance = this;
    	getServer().getPluginManager().registerEvents(this, this);
    	setDefaultLastList();
    }
    
    @Override
    public void onDisable() {
    	// TODO Save LastList to disk
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if (cmd.getName().equalsIgnoreCase("last")) {
    		// doSomething
    		if (args.length != 1) {
    			lastPlayer(sender, "");
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
    	LastList_Name.set(4, LastList_Name.get(3));
    	LastList_Time.set(4, LastList_Time.get(3));
    	LastList_Name.set(3, LastList_Name.get(2));
    	LastList_Time.set(3, LastList_Time.get(2));
    	LastList_Name.set(2, LastList_Name.get(1));
    	LastList_Time.set(2, LastList_Time.get(1));
    	LastList_Name.set(1, LastList_Name.get(0));
    	LastList_Time.set(1, LastList_Time.get(0));
    	LastList_Name.set(0, player.getName());
    	LastList_Time.set(0, System.currentTimeMillis());
    }
    
    public boolean lastPlayer(CommandSender sender, String arg) {
    	if (LastList_Name.get(0) != EMPTY) {
    		sender.sendMessage("These guys left just a bit ago..");
    		sender.sendMessage("----------------------------------------");
    	}
    	else {
    		sender.sendMessage("No one has left since the last reboot.");
    	}
    	
    	for (int i = 4; i >= 0; i--) {
        	if (LastList_Name.get(i) != EMPTY) {
        	    sender.sendMessage((i+1) +". - " + LastList_Name.get(i) + " - " + 
        			wayback(System.currentTimeMillis() - (Long) LastList_Time.get(i)));
        	}    		
    	}
    	return true;
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
    	PlayerData.getInfo(sender, arg);
    	return true;
    }
    
    public void setDefaultLastList() {
    	getLogger().log(Level.INFO, "Setting default Last-on list.");
    	for (int x = 0; x < 5; x++) {
    		LastList_Name.add(EMPTY);
    		LastList_Time.add(0, System.currentTimeMillis());
    	}
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



