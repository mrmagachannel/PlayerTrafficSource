package me.mrmaga.playertrafficsource;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	
	public static Settings settings = new Settings();
	
	@Override
	public void onEnable() {
		settings.setup(this);
		getCommand("playertrafficsource").setExecutor(this);
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
	
}