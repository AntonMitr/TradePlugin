package by.kotor.trade.plugin;

import by.kotor.trade.plugin.command.TradeCommand;
import by.kotor.trade.plugin.util.TradeManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TradePlugin extends JavaPlugin {

    public static TradePlugin instance;

    private TradeManager tradeManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        int tradeTimeoutSeconds = getConfig().getInt("trade-timeout-seconds", 30);
        if (tradeTimeoutSeconds <= 0) {
            getLogger().info("Trade-timeout-seconds configuration value in the file is negative");
            getConfig().set("trade-timeout-seconds", 30);
            saveConfig();
        }

        tradeManager = new TradeManager(tradeTimeoutSeconds);
        tradeManager.loadTradesFromFile();
        getLogger().info("Trade-loaded from trades.json");
        tradeManager.startOperationTrade();

        getCommand("trade").setExecutor(new TradeCommand());
    }

    @Override
    public void onDisable() {
        getLogger().info("QuickTrade plugin disabled!");
    }

    public TradeManager getTradeManager() {
        return tradeManager;
    }
}
