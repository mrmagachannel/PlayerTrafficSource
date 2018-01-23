package me.mrmaga.playertrafficsource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
	
	public static Settings settings = new Settings();
	public static List<String> args1 = new ArrayList<>();
	
	@Override
	public void onEnable() {
		settings.setup(this);
		getCommand("playertrafficsource").setExecutor(this);
		getCommand("playertrafficsource").setTabCompleter(this);
		Bukkit.getPluginManager().registerEvents(this, this);;
	}
	
	@Override
	public void onDisable() {
		
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if (getConfig().getBoolean("EnableAutoQuestionSending")) {
			Player player = e.getPlayer();
			if (Methods.needSendQuestion(player)) {
				Methods.sendQuestionAfterTicks(100L, player);
			}
		}
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("playertrafficsource")) {
			FileConfiguration msg = settings.getMsg();
			
			if (args.length == 0) {
				Methods.sendHelp(sender);
				return true;
			}
			
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					if (sender.hasPermission("playertrafficsource.reload") || sender.hasPermission("playertrafficsource.admin")) {
					settings.reloadMsg();
					settings.reloadConfig();
					settings.reloadData();
					sender.sendMessage(Methods.color(msg.getString("Messages.Reload")));
					return true;
					} else {
						sender.sendMessage(Methods.color(msg.getString("Messages.NoPermission")));
						return true;
					}
				} else if (args[0].equalsIgnoreCase("results")) {
					if (sender.hasPermission("playertrafficsource.results") || sender.hasPermission("playertrafficsource.admin")) {
						Methods.sendResults(sender);
						return true;
					} else {
					sender.sendMessage(Methods.color(msg.getString("Messages.NoPermission")));
					return true;
					}
				} else if (args[0].equalsIgnoreCase("seen") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("list")) {
					if (sender.hasPermission("playertrafficsource." + args[0]) || sender.hasPermission("playertrafficsource.admin")) {
						sender.sendMessage(Methods.color(msg.getString("Messages.CommandsHelp." + args[0])));
					}
				} else {
					Methods.sendHelp(sender);
					return true;
				}
			}	
			if (args.length >= 2) {
				if (args[0].equalsIgnoreCase("seen")) {
					if (sender.hasPermission("playertrafficsource.seen") || sender.hasPermission("playertrafficsource.admin")) {
						if (Methods.getAnsweredPlayers() != null && Methods.containsIgnoreCase(args[1], Methods.getAnsweredPlayers())) {
							Methods.getPlayerAnswer(sender, args[1]);
							return true;
						} else {
							sender.sendMessage(Methods.color(msg.getString("Messages.PlayerHasNotAnsweredYet").replaceAll("%player%", args[1])));
							return true;
						}
					} else {
						sender.sendMessage(Methods.color(msg.getString("Messages.NoPermission")));
						return true;
					}
				} else if (args[0].equalsIgnoreCase("answer")) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						if (Methods.getAnswerVariants() != null && Methods.containsIgnoreCase(args[1], Methods.getAnswerVariants())) {
							Methods.setAnswer(player, args[1]);
							return true;
						} else {
							player.sendMessage(Methods.color(msg.getString("Messages.IncorrectVariant")));
							return true;
						}
					} else {
						sender.sendMessage(Methods.color(msg.getString("Messages.PlayerOnly")));
						return true;
					}
				} else if (args[0].equalsIgnoreCase("sendquestion")) {
					if (sender.hasPermission("playertrafficsource.admin")) {
						Player player = Bukkit.getPlayer(args[1]);
						if (player != null) {
							if (Methods.needSendQuestion(player)) {
								Methods.sendQuestionAfterTicks(100L, player);
								return true;
							}
						} else {
							sender.sendMessage(ChatColor.RED + "»грок " + ChatColor.YELLOW + player + ChatColor.RED + " не в сети!");
							return true;
						}
					} else {
					sender.sendMessage(Methods.color(msg.getString("Messages.NoPermission")));
					return true;
					}
				} else if (args[0].equalsIgnoreCase("remove")) {
					if (sender.hasPermission("playertrafficsource.remove") || sender.hasPermission("playertrafficsource.admin")) {
						Methods.removePlayerAnswer(sender, args[1]);
						return true;
					} else {
						sender.sendMessage(Methods.color(msg.getString("Messages.NoPermission")));
						return true;
					}
				} else if (args[0].equalsIgnoreCase("list")) {
					if (sender.hasPermission("playertrafficsource.list") || sender.hasPermission("playertrafficsource.admin")) {
						if (args.length == 2) {
							Methods.sendVariantList(sender, args[1], 1);
						} else {
							if (Methods.isInt(args[2])) {
								Methods.sendVariantList(sender, args[1], Integer.parseInt(args[2]));
							} else {
								sender.sendMessage(Methods.color(msg.getString("Messages.IncorrectPageFormat").replaceAll("%arg%", args[2])));
							}
						}
					} else {
						sender.sendMessage(Methods.color(msg.getString("Messages.NoPermission")));
						return true;
					}
				} else {
					Methods.sendHelp(sender);
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("playertrafficsource")) {
			List<String> result = new ArrayList<>();
			if (args.length == 1) {
				if (!args[0].equals("")) {
					for (String arg : Methods.getCommandArgs(sender)) {
						if (arg.toLowerCase().startsWith(args[0].toLowerCase())) {
							result.add(arg);
						}
					}
				} else {
					for (String arg : Methods.getCommandArgs(sender)) {
						result.add(arg);
					}
				}
			}
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("answer") || args[0].equalsIgnoreCase("list")) {
					if (!args[1].equals("")) {
						for (String var : Methods.getAnswerVariants()) {
							if (var.toLowerCase().startsWith(args[1].toLowerCase())) {
								result.add(var);
							}
						}
					} else {
						for (String var : Methods.getAnswerVariants()) {
							result.add(var);
						}
					}
				}
			}
			Collections.sort(result);
			return result;
		}
		return null;
	}
}