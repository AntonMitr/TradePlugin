package by.kotor.trade.plugin.util;

import by.kotor.trade.plugin.TradePlugin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class TradeManager {
    private final int tradeTimeoutSeconds;
    private final Map<UUID, TradeRequest> pendingTrades;
    private final Queue<TradeOperation> tradeOperations;
    private final File tradesFile = new File(TradePlugin.instance.getDataFolder(), "trades.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Logger log = TradePlugin.instance.getLogger();
    private boolean isOperationTradeRunning = false;


    public TradeManager(int tradeTimeoutSecond) {
        this.tradeTimeoutSeconds = tradeTimeoutSecond;
        pendingTrades = new HashMap<>();
        tradeOperations = new LinkedList<>();
    }

    public void startOperationTrade() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isOperationTradeRunning) return;

                isOperationTradeRunning = true;
                if (!tradeOperations.isEmpty()) {
                    TradeOperation tradeOperation = tradeOperations.peek();
                    if (tradeOperation != null) {
                        try {
                            tradeOperation.execute();
                            removeFirstTradeAndSave();
                            pendingTrades.remove(tradeOperation.getSenderId());
                            pendingTrades.remove(tradeOperation.getReceiverId());
                        } catch (IOException | ClassNotFoundException e) {
                            log.warning("Deserialization error");
                            e.printStackTrace();
                        }
                    }
                }
                if (tradeOperations.isEmpty()) {
                    isOperationTradeRunning = false;
                    cancel();
                }
            }
        }.runTaskTimer(TradePlugin.instance, 0L, 1L);
    }

    public void saveTradesToFile() {
        try (FileWriter writer = new FileWriter(tradesFile)) {
            gson.toJson(tradeOperations, writer);
        } catch (IOException e) {
            log.warning("Failed to save trades to file");
            e.printStackTrace();
        }
    }

    public void loadTradesFromFile() {
        if (!tradesFile.exists()) return;

        try (FileReader reader = new FileReader(tradesFile)) {
            TradeOperation[] loaded = gson.fromJson(reader, TradeOperation[].class);
            if (loaded != null) {
                tradeOperations.clear();
                tradeOperations.addAll(Arrays.asList(loaded));
            }
        } catch (IOException e) {
            log.warning("Failed to load trades from file");
            e.printStackTrace();
        }
    }

    private void removeFirstTradeAndSave() {
        tradeOperations.poll();
        saveTradesToFile();
    }

    public void proposeTrade(Player sender, Player receiver) {
        if (sender == null) {
            receiver.sendMessage("Sender not found");
            return;
        }

        if (receiver == null) {
            sender.sendMessage("Receiver not found");
            return;
        }

        if (sender.equals(receiver)) {
            sender.sendMessage("You cannot trade with yourself!");
            return;
        }

        if (pendingTrades.containsKey(sender.getUniqueId())) {
            sender.sendMessage("You have an open trade");
            return;
        }

        if (pendingTrades.containsKey(receiver.getUniqueId())) {
            sender.sendMessage("Receiver has an open trade");
            return;
        }

        ItemStack senderItem = sender.getInventory().getItemInMainHand();
        if (senderItem == null || senderItem.getType().isAir()) {
            sender.sendMessage("You must hold an item to trade");
            return;
        }

        TradeRequest tradeRequest = new TradeRequest(sender, receiver, senderItem);
        pendingTrades.put(sender.getUniqueId(), tradeRequest);
        pendingTrades.put(receiver.getUniqueId(), tradeRequest);

        sender.sendMessage("Trade propose to " + receiver.getName() + " with " + senderItem.getType().name());
        receiver.sendMessage(sender.getName() + " wants to trade their " + senderItem.getType().name() +
                ". Use /trade accept or /trade deny within " + tradeTimeoutSeconds + " seconds.");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (pendingTrades.containsKey(sender.getUniqueId())) {
                    pendingTrades.remove(sender.getUniqueId());
                    pendingTrades.remove(receiver.getUniqueId());
                    sender.sendMessage("Trade with " + receiver.getName() + " has timed out");
                    receiver.sendMessage("Trade with " + sender.getName() + " has timed out");
                }
            }
        }.runTaskLater(TradePlugin.instance, tradeTimeoutSeconds * 20L);
    }

    public void accept(Player receiver) {
        TradeRequest tradeRequest = pendingTrades.get(receiver.getUniqueId());
        if (tradeRequest == null) {
            receiver.sendMessage("No pending trade request");
            return;
        }

        Player sender = tradeRequest.getSender();
        ItemStack senderItem = tradeRequest.getSenderItem();
        ItemStack receiverItem = receiver.getInventory().getItemInMainHand();

        if (receiverItem == null || receiverItem.getType().isAir()) {
            sender.sendMessage("You must hold an item to trade");
            return;
        }

        UUID uuidSender = sender.getUniqueId();
        UUID uuidReceiver = receiver.getUniqueId();
        try {
            String SenderItemBase64 = ItemStackSerializer.serialize(senderItem);
            String ReceiverItemBase64 = ItemStackSerializer.serialize(receiverItem);
            tradeOperations.add(new TradeOperation(uuidSender, uuidReceiver, SenderItemBase64, ReceiverItemBase64));
            startOperationTrade();
        } catch (IOException e) {
            TradePlugin.instance.getLogger().warning("Problems with serialization of items");
            e.printStackTrace();
        }
        saveTradesToFile();

        sender.sendMessage("Trade with " + receiver.getName() + " completed!");
        receiver.sendMessage("Trade with " + sender.getName() + " completed!");
        }

    public void deny(Player receiver) {
        TradeRequest tradeRequest = pendingTrades.get(receiver.getUniqueId());
        if (tradeRequest == null) {
            receiver.sendMessage("No pending trade request");
            return;
        }

        Player sender = tradeRequest.getSender();
        pendingTrades.remove(sender.getUniqueId());
        pendingTrades.remove(receiver.getUniqueId());

        sender.sendMessage(receiver.getName() + " denied your trade request.");
        receiver.sendMessage("Trade request from " + sender.getName() + " denied.");
    }

}
