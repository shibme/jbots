package me.shib.java.lib.jbots;


import me.shib.java.lib.jtelebot.service.TelegramBot;
import me.shib.java.lib.jtelebot.types.Update;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class UpdateReceiver {

    private static Map<String, UpdateReceiver> updateReceiverMap;
    private static Logger logger = Logger.getLogger(UpdateReceiver.class.getName());

    private Queue<Update> updatesQueue;
    private TelegramBot bot;
    private boolean botStarted;

    private UpdateReceiver(JBotConfig config) {
        this.updatesQueue = new LinkedList<>();
        this.bot = BotProvider.getInstance(config);
        botStarted = false;
    }

    protected static synchronized UpdateReceiver getDefaultInstance(JBotConfig config) {
        if (updateReceiverMap == null) {
            updateReceiverMap = new HashMap<>();
        }
        String botAPItoken = config.getBotApiToken();
        UpdateReceiver updateReceiver = updateReceiverMap.get(botAPItoken);
        if (updateReceiver == null) {
            updateReceiver = new UpdateReceiver(config);
            updateReceiverMap.put(botAPItoken, updateReceiver);
        }
        return updateReceiver;
    }

    private synchronized void fillUpdatesQueue() {
        try {
            Update[] updates = bot.getUpdates();
            Collections.addAll(updatesQueue, updates);
        } catch (IOException e) {
            logger.throwing(this.getClass().getName(), "fillUpdatesQueue", e);
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
            logger.log(Level.INFO, "Starting services for: " + bot.getIdentity().getFirst_name() + " (" + bot.getIdentity().getUsername() + ")");
            botStarted = true;
        }
    }

}
