package me.mrmaga.playertrafficsource;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class Settings {

	FileConfiguration config;
	File cfile;
	
	FileConfiguration msg;
	File mfile;
	
	FileConfiguration data;
	File dfile;
	
	public void setup(Plugin plugin) {
		cfile = new File(plugin.getDataFolder(), "config.yml");
			if (!cfile.exists()) {
				plugin.getConfig().options().copyDefaults(true);
				plugin.saveDefaultConfig();
			}
		config = YamlConfiguration.loadConfiguration(cfile);
		
		mfile = new File(plugin.getDataFolder(), "messages.yml");
		mfile.getParentFile().mkdir();
		if (!mfile.exists()) {
			plugin.saveResource("messages.yml", false);
		}
		msg = YamlConfiguration.loadConfiguration(mfile);
		
		dfile = new File(plugin.getDataFolder(), "data.yml");
		dfile.getParentFile().mkdir();
		if (!dfile.exists()) {
			plugin.saveResource("data.yml", false);
		}
		data = YamlConfiguration.loadConfiguration(dfile);
	}
	
	public FileConfiguration getData() {
		return data;
	}
	public FileConfiguration getMsg() {
		return msg;
	}
	public FileConfiguration getConfig() {
		return config;
	}
	public void saveData() {
		try {
			data.save(dfile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger()
					.severe(ChatColor.RED + "Не удалось сохранить data.yml");
		}
	}
	public void saveMsg() {
		try {
			msg.save(mfile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger()
					.severe(ChatColor.RED + "Не удалось сохранить messages.yml");
		}
	}
	public void reloadConfig() {
		config = YamlConfiguration.loadConfiguration(cfile);
	}
	public void reloadData() {
		data = YamlConfiguration.loadConfiguration(dfile);
	}
	public void reloadMsg() {
		msg = YamlConfiguration.loadConfiguration(mfile);
	}
}
