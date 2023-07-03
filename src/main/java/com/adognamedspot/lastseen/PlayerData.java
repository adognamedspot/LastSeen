package com.adognamedspot.lastseen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
		
		Config.set(player.getName() + ".In", System.currentTimeMillis());
		save(Config, File);
	}

	public static void Quit(Player player) {
		File File = getFile(player);
		FileConfiguration Config = getConfig(File, player);
		long TimeIn, TimeOut, Total;
		
		if (Config == null)
			return;
		
		TimeIn = (long)Config.getDouble(player.getName() + ".In");
		TimeOut = System.currentTimeMillis();
		Total = TimeOut - TimeIn;

		Config.set(player.getName() + ".In", 0);
		Config.set(player.getName() + ".Out", TimeOut);
		Config.set(player.getName() + ".Total", Config.getDouble(player.getName() + ".Total") + Total);
		save(Config, File);
	}
	
	public static FileConfiguration createFile(File file, Player player) {
        try {
            FileConfiguration playerData = YamlConfiguration.loadConfiguration(file);
            List<String> Names = new ArrayList<String>();
            List<String> IPs = new ArrayList<String>();
            Names.add(player.getName());
            IPs.add(player.getAddress().toString());
            
            playerData.createSection("Data");
            playerData.set("Data.UUID", player.getUniqueId().toString());
            playerData.setComments("Data.Name", Names);
            playerData.setComments("Data.IP", IPs);
            playerData.createSection(player.getName());
            playerData.set(player.getName() + ".In", 0);
            playerData.set(player.getName() + ".Out", 0);
            playerData.set(player.getName() + ".Total", 0);

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
}
