package me.mrmaga.playertrafficsource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Methods {
	
	public static Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("PlayerTrafficSource");
	
	public static String color(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
	
	public static boolean containsIgnoreCase(String str, Set<String> section) {
		for (String s : section) {
			if (s.equalsIgnoreCase(str)) {
				return true;
			}
		}
		return false;
	}
	
	public static void sendQuestionAfterTicks(long ticks, Player player) {
		BukkitScheduler scheduler = plugin.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				sendQuestion(player);
			}
		}, ticks );
	}
	
	public static void sendQuestion(Player player) {
		FileConfiguration msg = Main.settings.getMsg();
		FileConfiguration config = Main.settings.getConfig();
		for (String s : msg.getStringList("Messages.MessagesBeforeAnswerVariants")) {
			player.sendMessage(color(s));
		}
		Map<String, TextComponent> map = new HashMap<String, TextComponent>();
		for (String key : getAnswerVariants()) {
			String str = config.getConfigurationSection("AnswerVariants").getString(key);
			map.put(key, new TextComponent(color("&c&l⚪&a" + str)));
		}
		TextComponent[] shouldSend = new TextComponent[getAnswerVariants().size()];
		int i = 0;
		for (String key : getAnswerVariants()) {
			shouldSend[i] = map.get(key);
			shouldSend[i].setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pts answer " + key));
			shouldSend[i].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(color("&cНажми, чтобы выбрать &b&l" + config.getConfigurationSection("AnswerVariants").getString(key))).create()));
			i++;
		}
		for (int j = 0; j < shouldSend.length; j++) {
			player.spigot().sendMessage(shouldSend[j]);
		}
		map = null;
		shouldSend = null;
	}
	
	public static boolean needSendQuestion(Player player) {
		if (getAnsweredPlayers() == null) {
			return true;
		} else {
			for (String s : Methods.getAnsweredPlayers()) {
				if (player.getName().equalsIgnoreCase(s)) {
					return false;
				}
			}
			return true;
		}
	}
	
	public static void setAnswer(Player player, String val) {
		boolean needSet = true;
		FileConfiguration data = Main.settings.getData();
		FileConfiguration msg = Main.settings.getMsg();
		try {
			for (String s : getAnsweredPlayers()) {
				if (s.equalsIgnoreCase(player.getName())) {
					needSet = false;
				}
			}
		} catch (NullPointerException e) {
			needSet = true;
		}
		if (needSet) {
			data.set("AnsweredPlayers." + player.getName(), val);
			Main.settings.saveData();
			player.sendMessage(color(msg.getString("Messages.SuccessfulAnswer")));
		} else {
			player.sendMessage(color(msg.getString("Messages.AlreadyAnswered")));
		}
	}
	
	public static void getPlayerAnswer(CommandSender sender, String name) {
		Main.settings.reloadData();
		FileConfiguration data = Main.settings.getData();
		FileConfiguration config = Main.settings.getConfig();
		for (String caseName : getAnsweredPlayers()) {
			if (caseName.equalsIgnoreCase(name)) {
				sender.sendMessage(ChatColor.YELLOW + caseName + " " + ChatColor.WHITE + config.getConfigurationSection("AnswerVariants").getString(data.getConfigurationSection("AnsweredPlayers").getString(caseName)));
			}
		}
	}
	
	public static void getResults(CommandSender sender) {
		Main.settings.reloadData();
		FileConfiguration data = Main.settings.getData();
		FileConfiguration msg = Main.settings.getMsg();
		Integer i = 0;
		boolean needGet = true;
		try{
			data.getConfigurationSection("AnsweredPlayers").getKeys(false);
		} catch (NullPointerException e) {
			needGet = false;
		}
		if (needGet) {
			Map<String, Integer> map = new HashMap<String, Integer>();
			for (String s : getAnswerVariants()) {
				for (String key : data.getConfigurationSection("AnsweredPlayers").getKeys(false)) {
					if (data.getConfigurationSection("AnsweredPlayers").getString(key).equalsIgnoreCase(s)) {
						i++;
						map.put(s, i);
					}
				}
				i = 0;
			}
			sender.sendMessage(color(msg.getString("Messages.Results")));
			for (String s : getAnswerVariants()) {
				int count;
				int percent;
				if (map.get(s) == null) {
					count = 0;
				} else {
					count = map.get(s);
				}
				if (getAnsweredPlayers().size() > 0) {
					percent = (count*100)/getAnsweredPlayers().size();
				} else {
					percent = 0;
				}
				sender.sendMessage(ChatColor.GREEN + s + ChatColor.GREEN + ": " + ChatColor.WHITE +  count + " - " + ChatColor.RED + percent + "%");
			}
			sender.sendMessage(color(msg.getString("Messages.Total") + ChatColor.WHITE + getAnsweredPlayers().size()));
			map = null;
		} else {
			sender.sendMessage(color(msg.getString("Messages.NoResults")));
		}
	}
	
	public static Set<String> getAnswerVariants() {
		FileConfiguration config = Main.settings.getConfig();
		try {
			return config.getConfigurationSection("AnswerVariants").getKeys(false);
		} catch (NullPointerException npe) {
			return null;
		}
	}
	
	public static Set<String> getAnsweredPlayers() {
		FileConfiguration data = Main.settings.getData();
		try {
			return data.getConfigurationSection("AnsweredPlayers").getKeys(false);
		} catch (NullPointerException npe) {
			return null;
		}
	}
	
	public static void sendHelp(CommandSender sender) {
		FileConfiguration msg = Main.settings.getMsg();
		for (String key : msg.getConfigurationSection("Messages.CommandsHelp").getKeys(false)) {
			if (sender.hasPermission("playertrafficsource." + key) || sender.hasPermission("playertrafficsource.admin")) {
				sender.sendMessage(color(msg.getConfigurationSection("Messages.CommandsHelp").getString(key)));
			}
		}
	}
}
