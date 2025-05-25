package by.kotor.trade.plugin.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TradeRequest {

    private final Player sender;
    private final Player receiver;
    private final ItemStack senderItem;

    public TradeRequest(Player sender, Player receiver, ItemStack senderItem) {
        this.sender = sender;
        this.receiver = receiver;
        this.senderItem = senderItem;
    }

    public Player getSender() {
        return sender;
    }

    public Player getReceiver() {
        return receiver;
    }

    public ItemStack getSenderItem() {
        return senderItem;
    }

}
