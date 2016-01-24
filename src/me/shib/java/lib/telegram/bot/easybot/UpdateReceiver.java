package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.telegram.bot.service.TelegramBot;
import me.shib.java.lib.telegram.bot.types.Update;

import java.io.IOException;
import java.util.*;

public class UpdateReceiver {

    private static Map<String, UpdateReceiver> updateReceiverMap;

    private Queue<Update> updatesQueue;
    private TelegramBot bot;
    private boolean botStarted;

    private UpdateReceiver(String botAPItoken) {
        this.updatesQueue = new LinkedList<>();
        this.bot = TelegramBot.getInstance(botAPItoken);
        botStarted = false;
    }

    protected static synchronized UpdateReceiver getDefaultInstance(String botAPItoken) {
        if (updateReceiverMap == null) {
            updateReceiverMap = new HashMap<>();
        }
        UpdateReceiver updateReceiver = updateReceiverMap.get(botAPItoken);
        if (updateReceiver == null) {
            updateReceiver = new UpdateReceiver(botAPItoken);
            updateReceiverMap.put(botAPItoken, updateReceiver);
        }
        return updateReceiver;
    }

    private synchronized void fillUpdatesQueue() {
        try {
            Update[] updates = bot.getUpdates();
            Collections.addAll(updatesQueue, updates);
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
            System.out.println("Starting services for: " + bot.getIdentity().getFirst_name() + " (" + bot.getIdentity().getUsername() + ")");
            botStarted = true;
        }
    }

}
