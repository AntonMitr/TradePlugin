package by.kotor.trade.plugin.command;

import by.kotor.trade.plugin.TradePlugin;
import by.kotor.trade.plugin.util.TradeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TradeCommand implements CommandExecutor {
    private TradeManager tradeManager = TradePlugin.instance.getTradeManager();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }
        if (args.length != 1) return false;

        Player player = (Player) sender;

        if (args[0].equalsIgnoreCase("accept")) {
            tradeManager.accept(player);
        } else if (args[0].equalsIgnoreCase("deny")) {
            tradeManager.deny(player);
        } else {
            Player receiver = player.getServer().getPlayer(args[0]);
            tradeManager.proposeTrade(player,receiver);
        }
        return true;
    }
}
