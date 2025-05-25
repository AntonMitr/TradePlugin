package by.kotor.trade.plugin.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.UUID;

public class TradeOperation {
    private UUID senderId;
    private UUID receiverId;
    private String senderItemBase64;
    private String receiverItemBase64;

    public TradeOperation(UUID senderId, UUID receiverId, String senderItemBase64, String receiverItemBase64) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderItemBase64 = senderItemBase64;
        this.receiverItemBase64 = receiverItemBase64;
    }

    public void execute() throws IOException, ClassNotFoundException {
        Player sender = Bukkit.getPlayer(senderId);
        Player receiver = Bukkit.getPlayer(receiverId);

        if (sender == null || !sender.isOnline() || receiver == null || !receiver.isOnline()) {
            return;
        }

        ItemStack senderItem = ItemStackSerializer.deserialize(senderItemBase64);
        ItemStack receiverItem = ItemStackSerializer.deserialize(receiverItemBase64);

        if (senderItem == null || receiverItem == null) {
            return;
        }

        sender.getInventory().setItemInMainHand(receiverItem);
        receiver.getInventory().setItemInMainHand(senderItem);

        sender.sendMessage("Trade operation completed!");
        receiver.sendMessage("Trade operation completed!");
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public UUID getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(UUID receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderItemBase64() {
        return senderItemBase64;
    }

    public void setSenderItemBase64(String senderItemBase64) {
        this.senderItemBase64 = senderItemBase64;
    }

    public String getReceiverItemBase64() {
        return receiverItemBase64;
    }

    public void setReceiverItemBase64(String receiverItemBase64) {
        this.receiverItemBase64 = receiverItemBase64;
    }
}
