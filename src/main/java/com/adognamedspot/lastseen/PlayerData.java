package com.adognamedspot.lastseen;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class PlayerData {
	
	public static File getFile(Player player) {
        File userdata = new File(Bukkit.getServer().getPluginManager().getPlugin("LastSeen").getDataFolder(), File.separator + "PlayerData");
        File f = new File(userdata, File.separator + player.getUniqueId().toString() + ".yml");
		return f;
	}
	
	public static File getFile(String UUID) {
        File userdata = new File(Bukkit.getServer().getPluginManager().getPlugin("LastSeen").getDataFolder(), File.separator + "PlayerData");
        File f = new File(userdata, File.separator + UUID + ".yml");
		return f;
	}
	
	public static File getIndex() {
        File f = new File(Bukkit.getServer().getPluginManager().getPlugin("LastSeen").getDataFolder(), File.separator + "UserIndex.yml");
		return f;
	}
	
	public static FileConfiguration getConfig(File file, Player player) {
		FileConfiguration config;
		
		if (!file.exists())
			config = createFile(file, player);
		else
			config = YamlConfiguration.loadConfiguration(file);

		return config;
	}
	
	public static void Join(Player player) {
		File File = getFile(player);
		FileConfiguration Config = getConfig(File, player);
		
		processIndex(player);
		
		if (Config == null)
			return;
		
		Config.set("Last.In", System.currentTimeMillis());
		save(Config, File);
	}

	public static void Quit(Player player) {
		File File = getFile(player);
		FileConfiguration Config = getConfig(File, player);
		long TimeIn, TimeOut, Total;
		
		if (Config == null)
			return;
		
		TimeIn = (long)Config.getDouble("Last.In");
		TimeOut = System.currentTimeMillis();
		Total = TimeOut - TimeIn;
		
		Config.set("Last.Out", TimeOut);
		save(Config, File);

		processStats(player, Total);
	}
	
	public static FileConfiguration createFile(File file, Player player) {
        try {
            FileConfiguration playerData = YamlConfiguration.loadConfiguration(file);
            List<String> Names = new ArrayList<String>();
            List<String> IPs = new ArrayList<String>();
            Names.add(player.getName());
            IPs.add(player.getAddress().toString());
            
            playerData.createSection("Data");
            playerData.set("Data.Name", player.getName());
            playerData.set("Data.UUID", player.getUniqueId().toString());
            playerData.set("Data.Join Date.Month", getMonth());
            playerData.set("Data.Join Date.Day", getDay());
            playerData.set("Data.Join Date.Year", getYear());
            playerData.createSection("Last");
            playerData.set("Last.In", 0);
            playerData.set("Last.Out", 0);
            playerData.createSection("Stats");
            playerData.createSection("Stats.Yearly");
            playerData.set("Stats.Yearly.Year", getYear());
            playerData.set("Stats.Yearly.Total", 0);
            playerData.set("Stats.Yearly.AllTime", 0);
            playerData.createSection("Stats.Monthly");
            playerData.set("Stats.Monthly.Month", getMonth());
            playerData.set("Stats.Monthly.Total", 0);
            playerData.set("Stats.Monthly.AllTime", 0);
            playerData.createSection("Stats.Weekly");
            playerData.set("Stats.Weekly.Week", getWeekNumber());
            playerData.set("Stats.Weekly.Total", 0);
            playerData.set("Stats.Weekly.AllTime", 0);
            playerData.createSection("Stats.Daily");
            playerData.set("Stats.Daily.Day", getDay());
            playerData.set("Stats.Daily.Total", 0);
            playerData.set("Stats.Daily.AllTime", 0);


            playerData.save(file);
            return playerData;
        } catch (IOException exception) {

            exception.printStackTrace();
        }
		return null;
	}
	
	public static void save(FileConfiguration playerData, File file) {
		try {
			playerData.save(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void processIndex(Player player) {
		File file = getIndex();
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		try {
			config.set(player.getName(), player.getUniqueId().toString());
			config.save(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void processStats(Player player, long sessionTotal) {
		File file = getFile(player);
		FileConfiguration Config = getConfig(file, player);
		int currentYear = Integer.parseInt(getYear());
		int currentMonth = Integer.parseInt(getMonth());
		int currentWeek = getWeekNumber();
		int currentDay = Integer.parseInt(getDay());
		// [Year, Month, Week, Day][Y/M/W/D, Total]
		double[][] Stats = new double[4][2]; 
		
		if (Config == null)
			return;
		
		Stats[0][0] = Config.getDouble("Stats.Yearly.Year");
		Stats[0][1] = Config.getDouble("Stats.Yearly.Total");
		Stats[1][0] = Config.getDouble("Stats.Monthly.Month");
		Stats[1][1] = Config.getDouble("Stats.Monthly.Total");
		Stats[2][0] = Config.getDouble("Stats.Weekly.Week");
		Stats[2][1] = Config.getDouble("Stats.Weekly.Total");
		Stats[3][0] = Config.getDouble("Stats.Daily.Day");
		Stats[3][1] = Config.getDouble("Stats.Daily.Total");

		
		if (Stats[0][0] == currentYear) {
			// Still the same year, add on sessionTotal
			Stats[0][1] = Stats[0][1] + sessionTotal;
		} else {
			// New year, reset all stats
			Stats[0][0] = Integer.parseInt(getYear());
			Stats[0][1] = sessionTotal;
			Stats[1][0] = Integer.parseInt(getMonth());
			Stats[1][1] = 0;
			Stats[2][0] = getWeekNumber();
			Stats[2][1] = 0;
			Stats[3][0] = Integer.parseInt(getDay());
			Stats[3][1] = 0;
		}
		if (Stats[1][0] == currentMonth) {
			// Still the same month, add on sessionTotal
			Stats[1][1] = Stats[1][1] + sessionTotal;
		} else {
			// New month, reset Month, Week, Day stats
			Stats[1][0] = Integer.parseInt(getMonth());
			Stats[1][1] = sessionTotal;
			Stats[3][0] = Integer.parseInt(getDay());
			Stats[3][1] = 0;
		}
		if (Stats[2][0] == currentWeek) {
			// Still the same week, add on sessionTotal
			Stats[2][1] =  Stats[2][1] + sessionTotal;
		} else {
			// New week, reset Week, Day stats
			Stats[2][0] = getWeekNumber();
			Stats[2][1] = sessionTotal;
			Stats[3][0] = Integer.parseInt(getDay());
			Stats[3][1] = 0;
		}
		if (Stats[3][0] == currentDay) {
			// Still the same day, add on sessionTotal
			Stats[3][1] = Stats[3][1] + sessionTotal;
		} else {
			// New day, reset day stats
			Stats[3][0] = Integer.parseInt(getDay());
			Stats[3][1] = sessionTotal;
		}
		Config.set("Stats.Yearly.Year",   Stats[0][0]);
		Config.set("Stats.Yearly.Total",  Stats[0][1]);
		Config.set("Stats.Monthly.Month", Stats[1][0]);
		Config.set("Stats.Monthly.Total", Stats[1][1]);
		Config.set("Stats.Weekly.Week",   Stats[2][0]);
		Config.set("Stats.Weekly.Total",  Stats[2][1]);
		Config.set("Stats.Daily.Day",     Stats[3][0]);
		Config.set("Stats.Daily.Total",   Stats[3][1]);
		
		// All-Time Highs
		Stats[0][0] = Config.getDouble("Stats.Yearly.AllTime");
		Stats[1][0] = Config.getDouble("Stats.Monthly.AllTime");
		Stats[2][0] = Config.getDouble("Stats.Weekly.AllTime");
		Stats[3][0] = Config.getDouble("Stats.Daily.AllTime");
		
		if (Stats[0][0] < Stats [0][1])
			Config.set("Stats.Yearly.AllTime", Stats[0][1]);
		if (Stats[1][0] < Stats [1][1])
			Config.set("Stats.Monthly.AllTime", Stats[1][1]);
		if (Stats[2][0] < Stats [2][1])
			Config.set("Stats.Weekly.AllTime", Stats[2][1]);
		if (Stats[3][0] < Stats [3][1])
			Config.set("Stats.Daily.AllTime", Stats[3][1]);
		
		save(Config, file);
	}
	
	public static String getUUIDbyName(String name) {
		File file = getIndex();
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		if (!file.exists())
			return null;
		
		return config.getString(name);
	}
	
	public static long lastSeen(String name) {
		File file = getFile(getUUIDbyName(name));
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		if (config == null)
			return 0;
		
		return (long)config.getDouble(name + ".Out");
	}

	public static void getInfo(CommandSender sender, String name) {
		File file = getFile(getUUIDbyName(name));
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		Player target = Bukkit.getServer().getPlayer(name);
		
		if (config == null)
			return;
		
		sender.sendMessage("--------------------------------------------------");
		sender.sendMessage(" Name: " + name);
		sender.sendMessage(" UUiD: " + config.getString("Data.UUID"));
		sender.sendMessage("--------------------------------------------------");
		if (target != null) {
			sender.sendMessage(ChatColor.GREEN + "Player is online!");
			sender.sendMessage(" Total Playtime: " 
					+ LastSeen.wayback((long)((System.currentTimeMillis() - config.getDouble(name + ".In")) + config.getDouble(name + ".Total"))));
		} else {
			sender.sendMessage(" Last seen: " + LastSeen.wayback(System.currentTimeMillis() - lastSeen(name)) + " ago.");
			sender.sendMessage(" Total Playtime: " + LastSeen.wayback((long)config.getDouble(name + ".Total")));
		}
		sender.sendMessage("--------------------------------------------------");	
	}
	
	public static String getYear() {
	    LocalDateTime myDateObj = LocalDateTime.now();
	    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy");
	    String formattedDate = myDateObj.format(myFormatObj);
	    return formattedDate;
	}
	
	public static String getMonth() {
	    LocalDateTime myDateObj = LocalDateTime.now();
	    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("MM");
	    String formattedDate = myDateObj.format(myFormatObj);
	    return formattedDate;
	}
	
	public static String getDay() {
	    LocalDateTime myDateObj = LocalDateTime.now();
	    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd");
	    String formattedDate = myDateObj.format(myFormatObj);
	    return formattedDate;
	}
	
	public static int getWeekNumber() {
        Calendar calendar = Calendar.getInstance();
        int weekNumber = calendar.get(Calendar.WEEK_OF_YEAR);
        return weekNumber;
	}
	
	public List<String> CSVtoList(String line) {
	    if (line != null) {
	        String[] arr = line.split(",");
	        List<String> list = new ArrayList<>();
	        for (String s : arr)
	          list.add(s.trim()); 
	        return list;
	      } 
	      return new ArrayList<>();	}
	
	public static String ListtoCSV(List<String> list) {
		if (list != null) {
		    String result = list.stream()
		    	      .map(n -> String.valueOf(n))
		    	      .collect(Collectors.joining(", "));
		    return result;
		}		
		return null;
	}
}
