package me.mrmaga.playertrafficsource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
		TextComponent[] shouldSend = new TextComponent[getAnswerVariants().size()];
		int i = 0;
		for (String s : msg.getStringList("Messages.MessagesBeforeAnswerVariants")) {
			player.sendMessage(color(s));
		}
		for (String variant : getAnswerVariants()) {
			shouldSend[i] = new TextComponent(color("&c&l⚪&a" + config.getString("AnswerVariants." + variant)));
			shouldSend[i].setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pts answer " + variant));
			shouldSend[i].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(color("&cНажми, чтобы выбрать &b&l" + config.getString("AnswerVariants." + variant))).create()));
			player.spigot().sendMessage(shouldSend[i]);
			i++;
		}
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
		FileConfiguration msg = Main.settings.getMsg();
		for (String caseName : getAnsweredPlayers()) {
			if (caseName.equalsIgnoreCase(name)) {
				sender.sendMessage(ChatColor.YELLOW + caseName + " " + ChatColor.WHITE + msg.getString("Messages.Answered") + " \"" + ChatColor.GREEN + config.getString("AnswerVariants." + data.getString("AnsweredPlayers." + caseName)) + ChatColor.WHITE + "\"");
			}
		}
	}
	
	public static void removePlayerAnswer(CommandSender sender, String name) {
		Main.settings.reloadData();
		FileConfiguration data = Main.settings.getData();
		FileConfiguration msg = Main.settings.getMsg();
		boolean isSuccessful = false;
		if (getAnsweredPlayers() != null) {
			for (String key : getAnsweredPlayers()) {
				if (key.equalsIgnoreCase(name)) {
					data.set("AnsweredPlayers." + key, null);
					Main.settings.saveData();
					isSuccessful = true;
				}
			}
		}
		if (isSuccessful) {
			sender.sendMessage(color(msg.getString("Messages.SuccessfulRemoveAnswer").replaceAll("%player%", name)));
		} else {
			sender.sendMessage(color(msg.getString("Messages.PlayerHasNotAnsweredYet").replaceAll("%player%", name)));
		}
	}
	
	public static int getVariantCount(String variant) {
		if (getAnsweredPlayers() != null) {
			FileConfiguration data = Main.settings.getData();
			if (containsIgnoreCase(variant, getAnswerVariants())) {
				int count = 0;
				for (String key : getAnsweredPlayers()) {
					if (data.getString("AnsweredPlayers." + key).equalsIgnoreCase(variant)) {
						count++;
					}
				}
				return count;
			} else {
				return -1;
			}
		} else {
			return -2;
		}
	}
	
	public static void sendResults(CommandSender sender) {
		Main.settings.reloadData();
		FileConfiguration msg = Main.settings.getMsg();
		if (getAnsweredPlayers() == null) {
			sender.sendMessage(color(msg.getString("Messages.NoResults")));
		} else {
			sender.sendMessage(color(msg.getString("Messages.Results") + ": "));
			for (String variant : getAnswerVariants()) {
				int count = getVariantCount(variant);
				int percent;
				if (count == 0) {
					percent = 0;
				} else {
					percent = (count*100)/getAnsweredPlayers().size();
				}
				sender.sendMessage(ChatColor.GREEN + variant + ChatColor.GREEN + ": " + ChatColor.WHITE + count + " - " + ChatColor.RED + percent + "%");
			}
			sender.sendMessage(color(msg.getString("Messages.Total") + ": " + ChatColor.WHITE + getAnsweredPlayers().size()));
		}
	}
	
	public static void sendVariantList(CommandSender sender, String variant, int page) {
		Main.settings.reloadData();
		FileConfiguration msg = Main.settings.getMsg();
		FileConfiguration config = Main.settings.getConfig();
		int count = getVariantCount(variant);
		int number = config.getInt("PlayerOnOnePage");
		int maxPage = count%number == 0 ? count/number : count/number + 1;
		if (count == -2) {
			sender.sendMessage(color(msg.getString("Messages.NoResults")));
		} else if (count == -1) {
			sender.sendMessage(color(msg.getString("Messages.IncorrectVariant")));
		} else {
			if (page > maxPage || page <= 0) {
				sender.sendMessage(color(msg.getString("Messages.UnknownPage")));
			} else {
				TreeSet<String> players = new TreeSet<String>();
				for (String s : Methods.getAnsweredPlayers()) {
					if (Main.settings.getData().getString("AnsweredPlayers." + s).equalsIgnoreCase(variant)) {
						players.add(s);
					}	
				}
				sender.sendMessage(ChatColor.WHITE + "Игроки, которые ответили \"" + ChatColor.YELLOW + config.getString("AnswerVariants." + variant.toLowerCase()) + ChatColor.WHITE + "\"");
				sender.sendMessage("");
				StringBuilder list = new StringBuilder();
				for (int i = (page-1)*number; i < (page==maxPage ? count : (page*number)); i++) {
					list.append(ChatColor.GREEN + "" + (getNth(players, i)));
					if (i < (page==maxPage ? count-1 : (page*number)-1)) {
						list.append(ChatColor.WHITE + ", ");
					} else {
						if (page < maxPage) {
							list.append(ChatColor.WHITE + "...");
						} else {
							list.append(ChatColor.WHITE + ".");
						}
					}
				}
				sender.sendMessage(list.toString());
				sender.sendMessage("");
				sender.sendMessage(color(msg.getString("Messages.CurrentPage")
						.replaceAll("%page%", page+"").replaceAll("%maxpage%", maxPage+"")));
				if (page < maxPage) {
					sender.sendMessage(color(msg.getString("Messages.NextPage")
							.replaceAll("%type%", variant+"").replaceAll("%next%", page+1+"")));
				}	
				players = null;
				list = null;
			}
		}
	}
	
	public static String getNth(TreeSet<String> treeset, int n) {
		List<String> list = new ArrayList<String>(treeset);
		if (list.size()-1 >= n) {
			return list.get(n);
		} else {
			return list.get(list.size()-1);
		}
	}
	
	public static List<String> getCommandArgs(CommandSender sender) {
		List<String> args = new ArrayList<>();
		FileConfiguration msg = Main.settings.getMsg();
		for (String cmd : msg.getConfigurationSection("Messages.CommandsHelp").getKeys(false)) {
			if (sender.hasPermission("playertrafficsource." + cmd) || sender.hasPermission("playertrafficsource.admin")) {
				args.add(cmd);
			}
		}
		return args;
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
				sender.sendMessage(color(msg.getString("Messages.CommandsHelp." + key)));
			}
		}
	}
	
	public static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}
}
