package me.shib.java.lib.jbots;


import me.shib.java.lib.jtelebot.service.TelegramBot;
import me.shib.java.lib.jtelebot.types.Message;
import me.shib.java.lib.jtelebot.types.Update;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class UpdateReceiver {

    private static final Logger logger = Logger.getLogger(UpdateReceiver.class.getName());
    private static final int allowRequestsBeforeInterval = 10;

    private static Map<String, UpdateReceiver> updateReceiverMap;

    private Queue<Update> updatesQueue;
    private TelegramBot bot;
    private JBotConfig config;
    private boolean botStarted;
    private long startTime;

    private UpdateReceiver(JBotConfig config) {
        this.startTime = (new Date().getTime() / 1000) - allowRequestsBeforeInterval;
        this.updatesQueue = new LinkedList<>();
        this.bot = BotProvider.getInstance(config);
        this.botStarted = false;
        this.config = config;
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

    protected long getStartTime() {
        return startTime;
    }

    protected synchronized List<Message> getMissedMessageList() {
        List<Message> missedMessages = new ArrayList<>();
        Set<Long> missedChatIds = new HashSet<>();
        if (!config.isMissedChatHandlingDisabled()) {
            try {
                Update[] updates = bot.getUpdatesImmediately();
                while ((updates != null) && (updates.length > 0)) {
                    for (Update update : updates) {
                        if ((update.getMessage() != null) && (update.getMessage().getDate() < startTime)) {
                            if (!missedChatIds.contains(update.getMessage().getChat().getId())) {
                                missedChatIds.add(update.getMessage().getChat().getId());
                                missedMessages.add(update.getMessage());
                            }
                        } else {
                            updatesQueue.add(update);
                        }
                    }
                    updates = bot.getUpdatesImmediately();
                }
            } catch (IOException e) {
                logger.throwing(this.getClass().getName(), "getMissedMessageList", e);
            }
        }
        return missedMessages;
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
