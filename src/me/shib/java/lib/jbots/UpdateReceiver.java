package me.shib.java.lib.jbots;


import me.shib.java.lib.jtelebot.models.updates.Message;
import me.shib.java.lib.jtelebot.models.updates.Update;
import me.shib.java.lib.jtelebot.service.TelegramBot;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

final class UpdateReceiver {

    private static final Logger logger = Logger.getLogger(UpdateReceiver.class.getName());
    private static final int allowRequestsBeforeInterval = 10;
    private static final Map<String, UpdateReceiver> updateReceiverMap = new HashMap<>();

    private Queue<Update> updatesQueue;
    private TelegramBot bot;
    private boolean missedChatHandlerEnabled;
    private long startTime;

    private UpdateReceiver(JBotConfig config) {
        this.startTime = (new Date().getTime() / 1000) - allowRequestsBeforeInterval;
        this.updatesQueue = new LinkedList<>();
        this.missedChatHandlerEnabled = config.handleMissedChats();
        this.bot = config.getBot();
    }

    static synchronized UpdateReceiver getDefaultInstance(JBotConfig config) {
        String botApiToken = config.botApiToken();
        UpdateReceiver updateReceiver = updateReceiverMap.get(botApiToken);
        if (updateReceiver == null) {
            updateReceiver = new UpdateReceiver(config);
            updateReceiverMap.put(botApiToken, updateReceiver);
        }
        return updateReceiver;
    }

    long getStartTime() {
        return startTime;
    }

    synchronized List<Message> getMissedMessageList() {
        List<Message> missedMessages = new ArrayList<>();
        Set<Long> missedChatIds = new HashSet<>();
        if (missedChatHandlerEnabled) {
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
            Collections.addAll(updatesQueue, bot.getUpdates());
        } catch (IOException e) {
            logger.throwing(this.getClass().getName(), "fillUpdatesQueue", e);
        }
    }

    synchronized Update getUpdate() {
        Update update = updatesQueue.poll();
        if (update == null) {
            fillUpdatesQueue();
            update = updatesQueue.poll();
        }
        return update;
    }

}
