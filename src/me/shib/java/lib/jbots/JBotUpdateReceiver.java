package me.shib.java.lib.jbots;


import me.shib.java.lib.jtelebot.service.TelegramBot;
import me.shib.java.lib.jtelebot.types.Update;

import java.io.IOException;
import java.util.*;

public class JBotUpdateReceiver {

    private static Map<String, JBotUpdateReceiver> updateReceiverMap;

    private Queue<Update> updatesQueue;
    private TelegramBot bot;
    private boolean botStarted;

    private JBotUpdateReceiver(JBotConfig config) {
        this.updatesQueue = new LinkedList<>();
        this.bot = JBots.getInstance(config);
        botStarted = false;
    }

    protected static synchronized JBotUpdateReceiver getDefaultInstance(JBotConfig config) {
        if (updateReceiverMap == null) {
            updateReceiverMap = new HashMap<>();
        }
        String botAPItoken = config.getBotApiToken();
        JBotUpdateReceiver JBotUpdateReceiver = updateReceiverMap.get(botAPItoken);
        if (JBotUpdateReceiver == null) {
            JBotUpdateReceiver = new JBotUpdateReceiver(config);
            updateReceiverMap.put(botAPItoken, JBotUpdateReceiver);
        }
        return JBotUpdateReceiver;
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
