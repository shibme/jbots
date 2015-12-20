package me.shib.java.lib.telegram.bot.easybot;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import me.shib.java.lib.telegram.bot.service.TelegramBot;
import me.shib.java.lib.telegram.bot.types.Update;
import me.shib.java.lib.telegram.bot.types.User;

public class UpdateReceiver {

    private static Map<String, UpdateReceiver> updateReceiverMap;

    private Queue<Update> updatesQueue;
    private TelegramBot bot;
    private User myIdentity;
    private boolean botStarted;

    private UpdateReceiver(TelegramBot tBot) {
        this.updatesQueue = new LinkedList<>();
        this.bot = tBot;
        botStarted = false;
    }

    protected static synchronized UpdateReceiver getDefaultInstance(TelegramBot bot) {
        if (updateReceiverMap == null) {
            updateReceiverMap = new HashMap<>();
        }
        UpdateReceiver updateReceiver = updateReceiverMap.get(bot.getBotApiToken());
        if (updateReceiver == null) {
            updateReceiver = new UpdateReceiver(bot);
            updateReceiverMap.put(bot.getBotApiToken(), updateReceiver);
        }
        return updateReceiver;
    }

    private synchronized void fillUpdatesQueue() {
        try {
            Update[] updates = bot.getUpdates();
            for (Update u : updates) {
                updatesQueue.add(u);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected synchronized Update getUpdate() {
        Update update = updatesQueue.poll();
        while (update == null) {
            fillUpdatesQueue();
            update = updatesQueue.poll();
        }
        return update;
    }

    protected synchronized void onBotStart() {
        if (!botStarted) {
            whoAmI();
            if (myIdentity != null) {
                System.out.println(
                        "Starting services for: " + myIdentity.getFirst_name() + " (" + myIdentity.getUsername() + ")");
                botStarted = true;
            }
        }
    }

    public User whoAmI() {
        if (myIdentity == null) {
            try {
                myIdentity = bot.getMe();
            } catch (IOException e) {
                myIdentity = null;
            }
        }
        return myIdentity;
    }

}
