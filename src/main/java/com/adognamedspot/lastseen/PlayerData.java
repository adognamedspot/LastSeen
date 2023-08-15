package com.adognamedspot.lastseen;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
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
	
	public static File getLastList() {
        File f = new File(Bukkit.getServer().getPluginManager().getPlugin("LastSeen").getDataFolder(), File.separator + "LastList.yml");
		return f;
	}
	
	public static File getServerStats() {
        File f = new File(Bukkit.getServer().getPluginManager().getPlugin("LastSeen").getDataFolder(), File.separator + "ServerStats.yml");
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
	
	public static FileConfiguration getServerConfig(File file) {
		FileConfiguration config;
		
		if (!file.exists())
			config = createServerFile(file);
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
		
		processStats(player, 0);
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
            
            playerData.createSection("Data");
            playerData.set("Data.Name", player.getName());
            playerData.set("Data.UUID", player.getUniqueId().toString());
            playerData.set("Data.Join Date.Month", Integer.parseInt(getMonth()));
            playerData.set("Data.Join Date.Day", Integer.parseInt(getDay()));
            playerData.set("Data.Join Date.Year", Integer.parseInt(getYear()));
            playerData.set("Data.First Played", player.getFirstPlayed());
            playerData.createSection("Last");
            playerData.set("Last.In", 0);
            playerData.set("Last.Out", 0);
            playerData.createSection("Stats");
            playerData.createSection("Stats.Yearly");
            playerData.set("Stats.Yearly.Year", Integer.parseInt(getYear()));
            playerData.set("Stats.Yearly.Total", 0);
            playerData.set("Stats.Yearly.AllTime", 0);
            playerData.createSection("Stats.Monthly");
            playerData.set("Stats.Monthly.Month", Integer.parseInt(getMonth()));
            playerData.set("Stats.Monthly.Total", 0);
            playerData.set("Stats.Monthly.AllTime", 0);
            playerData.createSection("Stats.Weekly");
            playerData.set("Stats.Weekly.Week", getWeekNumber());
            playerData.set("Stats.Weekly.Total", 0);
            playerData.set("Stats.Weekly.AllTime", 0);
            playerData.createSection("Stats.Daily");
            playerData.set("Stats.Daily.Day", Integer.parseInt(getDay()));
            playerData.set("Stats.Daily.Total", 0);
            playerData.set("Stats.Daily.AllTime", 0);

            playerData.save(file);
            return playerData;
        } catch (IOException exception) {

            exception.printStackTrace();
        }
		return null;
	}
	
	public static FileConfiguration createServerFile(File file) {
        try {
            FileConfiguration serverData = YamlConfiguration.loadConfiguration(file);
            
            serverData.createSection("Stats");
            serverData.createSection("Stats.Yearly");
            serverData.set("Stats.Yearly.Month", Integer.parseInt(getMonth()));
            serverData.set("Stats.Yearly.Day", Integer.parseInt(getDay()));
            serverData.set("Stats.Yearly.Year", Integer.parseInt(getYear()));
            serverData.set("Stats.Yearly.Total", 0);
            serverData.set("Stats.Yearly.Player", "");
            serverData.createSection("Stats.Monthly");
            serverData.set("Stats.Monthly.Month", Integer.parseInt(getMonth()));
            serverData.set("Stats.Monthly.Day", Integer.parseInt(getDay()));
            serverData.set("Stats.Monthly.Year", Integer.parseInt(getYear()));
            serverData.set("Stats.Monthly.Total", 0);
            serverData.set("Stats.Monthly.Player", "");
            serverData.createSection("Stats.Weekly");
            serverData.set("Stats.Weekly.Month", Integer.parseInt(getMonth()));
            serverData.set("Stats.Weekly.Day", Integer.parseInt(getDay()));
            serverData.set("Stats.Weekly.Year", Integer.parseInt(getYear()));
            serverData.set("Stats.Weekly.Total", 0);
            serverData.set("Stats.Weekly.Player", "");
            serverData.createSection("Stats.Daily");
            serverData.set("Stats.Daily.Month", Integer.parseInt(getMonth()));
            serverData.set("Stats.Daily.Day", Integer.parseInt(getDay()));
            serverData.set("Stats.Daily.Year", Integer.parseInt(getYear()));
            serverData.set("Stats.Daily.Total", 0);
            serverData.set("Stats.Daily.Player", "");

            serverData.save(file);
            return serverData;
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
	
	public static void saveLastList( ) {
		File file = getLastList();
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		file.delete();

		Bukkit.getLogger().info("Saving Last list to disk.");
		
		for (int i = 0; i < LastSeen.LastListOccupants; i++) {
			if (LastSeen.LastList[i][0] != null) {
				config.set(i + ".Name", LastSeen.LastList[i][0]);
				config.set(i + ".Time", LastSeen.LastList[i][1]);
			}
		}
		
		save(config, file);
	}
	
	public static void loadLastList( ) {
		File file = getLastList();
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		if (!file.exists())
			return;
		
		Bukkit.getLogger().info("Loading Last list from disk.");
		
		for (int i = 0; i < LastSeen.LastListOccupants; i++) {
			if (config.getDouble(i + ".Time") != 0) {
				LastSeen.LastList[i][0] = config.get(i + ".Name");
				LastSeen.LastList[i][1] = (long)config.getDouble(i + ".Time");
			}
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
	
	public static void processStats(String name, long sessionTotal) {
		File file = getFile(getUUIDbyName(name));
		if (!file.exists())
			return;
		processStats(file, getUUIDbyName(name), sessionTotal);
	}
	
	public static void processStats(Player player, long sessionTotal) {
		File file = getFile(player);
		processStats(file, player.getUniqueId().toString(), sessionTotal);
	}
	
	public static void processStats(File file, String uuid, long sessionTotal) {
		FileConfiguration Config = YamlConfiguration.loadConfiguration(file);
		FileConfiguration Server = getServerConfig(getServerStats());
		int currentYear = Integer.parseInt(getYear());
		int currentMonth = Integer.parseInt(getMonth());
		int currentWeek = getWeekNumber();
		int currentDay = Integer.parseInt(getDay());
		// [Year, Month, Week, Day][Y/M/W/D, Total]
		double[][] Stats = new double[4][2];
		double[] SrvStats = new double[4];
		
		
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
			// New month, reset Month, Day stats
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
		
		// Player All-Time Highs
		Stats[0][0] = Config.getDouble("Stats.Yearly.AllTime");
		Stats[1][0] = Config.getDouble("Stats.Monthly.AllTime");
		Stats[2][0] = Config.getDouble("Stats.Weekly.AllTime");
		Stats[3][0] = Config.getDouble("Stats.Daily.AllTime");
		
		if (Stats[0][0] < Stats [0][1]) {
			Config.set("Stats.Yearly.AllTime", Stats[0][1]);
			Stats[0][0] = Stats[0][1];
		}
		if (Stats[1][0] < Stats [1][1]) {
			Config.set("Stats.Monthly.AllTime", Stats[1][1]);
			Stats[1][0] = Stats[1][1];
		}
		if (Stats[2][0] < Stats [2][1]) {
			Config.set("Stats.Weekly.AllTime", Stats[2][1]);
			Stats[2][0] = Stats[2][1];
		}
		if (Stats[3][0] < Stats [3][1]) {
			Config.set("Stats.Daily.AllTime", Stats[3][1]);
			Stats[3][0] = Stats[3][1];
		}
		
		// Server All-Time Highs
		SrvStats[0] = Server.getDouble("Stats.Yearly.Total");
		SrvStats[1] = Server.getDouble("Stats.Monthly.Total");
		SrvStats[2] = Server.getDouble("Stats.Weekly.Total");
		SrvStats[3] = Server.getDouble("Stats.Daily.Total");

		if (SrvStats[0] < Stats[0][0]) {
            Server.set("Stats.Yearly.Month", Integer.parseInt(getMonth()));
            Server.set("Stats.Yearly.Day", Integer.parseInt(getDay()));
            Server.set("Stats.Yearly.Year", Integer.parseInt(getYear()));
            Server.set("Stats.Yearly.Total", Stats[0][0]);
            Server.set("Stats.Yearly.Player", Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName());
		}
		if (SrvStats[1] < Stats[1][0]) {
            Server.set("Stats.Monthly.Month", Integer.parseInt(getMonth()));
            Server.set("Stats.Monthly.Day", Integer.parseInt(getDay()));
            Server.set("Stats.Monthly.Year", Integer.parseInt(getYear()));
            Server.set("Stats.Monthly.Total", Stats[1][0]);
            Server.set("Stats.Monthly.Player", Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName());
		}
		if (SrvStats[2] < Stats[2][0]) {
            Server.set("Stats.Weekly.Month", Integer.parseInt(getMonth()));
            Server.set("Stats.Weekly.Day", Integer.parseInt(getDay()));
            Server.set("Stats.Weekly.Year", Integer.parseInt(getYear()));
            Server.set("Stats.Weekly.Total", Stats[2][0]);
            Server.set("Stats.Weekly.Player", Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName());
		}
		if (SrvStats[3] < Stats[3][0]) {
            Server.set("Stats.Daily.Month", Integer.parseInt(getMonth()));
            Server.set("Stats.Daily.Day", Integer.parseInt(getDay()));
            Server.set("Stats.Daily.Year", Integer.parseInt(getYear()));
            Server.set("Stats.Daily.Total", Stats[3][0]);
            Server.set("Stats.Daily.Player", Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName());
		}

		save(Server, getServerStats());
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
		
		return (long)config.getDouble("Last.Out");
	}
	
	public static void hardJoin() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			Join(p);
		}
	}
	
	public static void hardQuit() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			Quit(p);
			LastSeen.HandleLastList(p);
		}
	}

	public static void getInfo(CommandSender sender, String name) {
		File file = getFile(getUUIDbyName(name));
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		Player target = Bukkit.getServer().getPlayer(name);
		
		if (!file.exists()) {
			sender.sendMessage(ChatColor.GRAY + "The player " 
					+ ChatColor.WHITE + name + ChatColor.GRAY + " does not exist.");	
			return;
		}
		if (config == null)
			return;
		
		sender.sendMessage(ChatColor.AQUA + "--------------------------------------------------");
		sender.sendMessage(ChatColor.GRAY + "      Name: " + ChatColor.WHITE + name);
		sender.sendMessage(ChatColor.GRAY + "      UUiD: " + ChatColor.WHITE + config.getString("Data.UUID"));
		sender.sendMessage(ChatColor.GRAY + " Join Date: " + ChatColor.WHITE
				+ millisToDate((long) config.getDouble("Data.First Played")));
//				+ MonthName(config.getInt("Data.Join Date.Month")) + " " 
//				+ config.getInt("Data.Join Date.Day") + ", " 
//				+ config.getInt("Data.Join Date.Year"));
		if (target != null) {
			sender.sendMessage(ChatColor.AQUA + "-----------------------" 
					+ ChatColor.GREEN + "ONLiNE" + ChatColor.AQUA + "----------------------");
			sender.sendMessage(ChatColor.GRAY + " Session Playtime: " + ChatColor.WHITE
					+ LastSeen.wayback((long)((System.currentTimeMillis() - config.getDouble("Last.In")))));
		} else {
			sender.sendMessage(ChatColor.AQUA + "----------------------" 
					+ ChatColor.RED + "OFFLiNE" + ChatColor.AQUA + "----------------------");
			sender.sendMessage(ChatColor.GRAY + " Last seen: " + ChatColor.WHITE
				+ LastSeen.wayback(System.currentTimeMillis() - lastSeen(name)) + " ago.");
		}
		sender.sendMessage(ChatColor.AQUA + "-------------------" 
			+ ChatColor.YELLOW + "Total Playtime" + ChatColor.AQUA + "--------------------");	
		sender.sendMessage(ChatColor.GRAY + " This Year: " + ChatColor.WHITE
			+ LastSeen.wayback((long)config.getDouble("Stats.Yearly.Total")));
		sender.sendMessage(ChatColor.GRAY + "This Month: " + ChatColor.WHITE
			+ LastSeen.wayback((long)config.getDouble("Stats.Monthly.Total")));
		sender.sendMessage(ChatColor.GRAY + " This Week: " + ChatColor.WHITE
			+ LastSeen.wayback((long)config.getDouble("Stats.Weekly.Total")));
		sender.sendMessage(ChatColor.GRAY + "     Today: " + ChatColor.WHITE
			+ LastSeen.wayback((long)config.getDouble("Stats.Daily.Total")));
		sender.sendMessage(ChatColor.AQUA + "-------------------" 
			+ ChatColor.DARK_AQUA + "All-Time Stats" + ChatColor.AQUA + "--------------------");	
		sender.sendMessage(ChatColor.GRAY + " Best Year: " + ChatColor.WHITE
			+ LastSeen.wayback((long)config.getDouble("Stats.Yearly.AllTime")));
		sender.sendMessage(ChatColor.GRAY + "Best Month: " + ChatColor.WHITE
			+ LastSeen.wayback((long)config.getDouble("Stats.Monthly.AllTime")));
		sender.sendMessage(ChatColor.GRAY + " Best Week: " + ChatColor.WHITE
			+ LastSeen.wayback((long)config.getDouble("Stats.Weekly.AllTime")));
		sender.sendMessage(ChatColor.GRAY + "  Best Day: " + ChatColor.WHITE
			+ LastSeen.wayback((long)config.getDouble("Stats.Daily.AllTime")));
		sender.sendMessage(ChatColor.AQUA + "--------------------------------------------------");	
	}

	public static void serverStats(CommandSender sender) {
		FileConfiguration Server = getServerConfig(getServerStats());
		sender.sendMessage(ChatColor.WHITE + "--------------------------------------------------");
		sender.sendMessage(ChatColor.GREEN + "            Server All-Time Records");
		sender.sendMessage(ChatColor.WHITE + "--------------------------------------------------");
		sender.sendMessage(ChatColor.GRAY + "Best Year: " + ChatColor.YELLOW
				+ LastSeen.wayback((long)Server.getDouble("Stats.Yearly.Total")));
		sender.sendMessage(ChatColor.GRAY + "Set by: " + ChatColor.YELLOW
				+ Server.getString("Stats.Yearly.Player") + ChatColor.GRAY + " on " + ChatColor.YELLOW
				+ MonthName(Server.getInt("Stats.Yearly.Month")) + " " + Server.getInt("Stats.Yearly.Day")
				+ ", " + Server.getInt("Stats.Yearly.Year"));
		sender.sendMessage(ChatColor.WHITE + "--------------------------------------------------");
		sender.sendMessage(ChatColor.GRAY + "Best Month: " + ChatColor.YELLOW
				+ LastSeen.wayback((long)Server.getDouble("Stats.Monthly.Total")));
		sender.sendMessage(ChatColor.GRAY + "Set by: " + ChatColor.YELLOW
				+ Server.getString("Stats.Monthly.Player") + ChatColor.GRAY + " on " + ChatColor.YELLOW
				+ MonthName(Server.getInt("Stats.Monthly.Month")) + " " + Server.getInt("Stats.Monthly.Day")
				+ ", " + Server.getInt("Stats.Monthly.Year"));
		sender.sendMessage(ChatColor.WHITE + "--------------------------------------------------");
		sender.sendMessage(ChatColor.GRAY + "Best Week: " + ChatColor.YELLOW
				+ LastSeen.wayback((long)Server.getDouble("Stats.Weekly.Total")));
		sender.sendMessage(ChatColor.GRAY + "Set by: " + ChatColor.YELLOW
				+ Server.getString("Stats.Weekly.Player") + ChatColor.GRAY + " on " + ChatColor.YELLOW
				+ MonthName(Server.getInt("Stats.Weekly.Month")) + " " + Server.getInt("Stats.Weekly.Day")
				+ ", " + Server.getInt("Stats.Weekly.Year"));
		sender.sendMessage(ChatColor.WHITE + "--------------------------------------------------");
		sender.sendMessage(ChatColor.GRAY + "Best Day: " + ChatColor.YELLOW
				+ LastSeen.wayback((long)Server.getDouble("Stats.Daily.Total")));
		sender.sendMessage(ChatColor.GRAY + "Set by: " + ChatColor.YELLOW
				+ Server.getString("Stats.Daily.Player") + ChatColor.GRAY + " on " + ChatColor.YELLOW
				+ MonthName(Server.getInt("Stats.Daily.Month")) + " " + Server.getInt("Stats.Daily.Day")
				+ ", " + Server.getInt("Stats.Daily.Year"));
		sender.sendMessage(ChatColor.WHITE + "--------------------------------------------------");
	}
	
	public static String millisToDate(Long millis) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEEEE, MMMMM dd yyyy");
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(millis);
		return sdf.format(calendar.getTime());
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
	
	public static String MonthName(int month) {
		switch (month) {
		case 1:
			return "January";
		case 2:
			return "February";
		case 3:
			return "March";
		case 4:
			return "April";
		case 5:
			return "May";
		case 6:
			return "June";
		case 7:
			return "July";
		case 8:
			return "August";
		case 9:
			return "September";
		case 10:
			return "October";
		case 11:
			return "November";
		case 12:
			return "December";
		}
		return null;
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
