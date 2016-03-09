package me.shib.java.lib.jbots;


import me.shib.java.lib.jtelebot.service.TelegramBot;
import me.shib.java.lib.jtelebot.types.*;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BotWorker extends Thread {

    private static final Logger logger = Logger.getLogger(BotWorker.class.getName());

    private static int threadCounter = 0;

    private JBotConfig config;
    private TelegramBot bot;
    private UpdateReceiver updateReceiver;
    private DefaultJBot defaultModel;
    private boolean enabled;
    private int threadNumber;

    public BotWorker(JBotConfig config) {
        this.config = config;
        if (config != null) {
            if ((config.getBotApiToken() != null) && (!config.getBotApiToken().isEmpty())) {
                updateReceiver = UpdateReceiver.getDefaultInstance(this.config);
                defaultModel = new DefaultJBot(config);
                bot = defaultModel.bot;
                enabled = true;
            }
        }
        threadNumber = 0;
    }

    private static synchronized int getThisThreadNumber(BotWorker worker) {
        if (worker.threadNumber == 0) {
            threadCounter++;
            worker.threadNumber = threadCounter;
        }
        return worker.threadNumber;
    }

    private String getRoundedDowntime(long timeDiff) {
        if (timeDiff < 60) {
            if (timeDiff > 1) {
                return timeDiff + " seconds";
            }
            return timeDiff + " second";
        }
        timeDiff /= 60;
        if (timeDiff < 60) {
            if (timeDiff > 1) {
                return timeDiff + " minutes";
            }
            return timeDiff + " minute";
        }
        timeDiff /= 60;
        if (timeDiff < 24) {
            if (timeDiff > 1) {
                return timeDiff + " hours";
            }
            return timeDiff + " hour";
        }
        timeDiff /= 24;
        if (timeDiff > 1) {
            return timeDiff + " days";
        }
        return timeDiff + " day";
    }

    private void messageUsersOnDowntimeFailure(List<Message> missedMessages) {
        for (Message message : missedMessages) {
            if (message.getDate() > 0) {
                try {
                    StringBuilder messageBuilder = new StringBuilder();
                    String name = JBot.getProperName(message.getFrom());
                    if (name.isEmpty()) {
                        messageBuilder.append("Hi. ");
                    } else {
                        messageBuilder.append("Hi *").append(name).append("*. ");
                    }
                    messageBuilder.append("\nWe regret that the service has been down for *")
                            .append(getRoundedDowntime(updateReceiver.getStartTime() - message.getDate()))
                            .append("* for maintenance.\nWe'll try to make sure this doesn't happen again.");
                    bot.sendMessage(new ChatId(message.getChat().getId()), messageBuilder.toString(),
                            false, ParseMode.Markdown, false, 0, new ReplyKeyboardHide());
                } catch (IOException e) {
                    logger.throwing(this.getClass().getName(), "messageUsersOnDowntimeFailure", e);
                }
            }
        }
    }

    public void startBotWork() {
        if (defaultModel != null) {
            BotSweeper.startDefaultInstance(defaultModel);
            updateReceiver.onBotStart();
            logger.log(Level.INFO, "Starting thread " + getThisThreadNumber(this) + " with " + bot.getIdentity().getUsername() + " using the model: " + defaultModel.getModelClassName());
            messageUsersOnDowntimeFailure(updateReceiver.getMissedMessageList());
            while (enabled) {
                try {
                    Update update = updateReceiver.getUpdate();
                    if (update.getMessage() != null) {
                        Message message = update.getMessage();
                        boolean adminIdValid = (config.isAdmin(message.getChat().getId()) || config.isAdmin(message.getFrom().getId()));
                        Message commandResponseMessage = null;
                        if (config.isValidCommand(message.getText())) {
                            commandResponseMessage = defaultModel.onCommand(message);
                        }
                        Message adminResponseMessage = null;
                        if ((commandResponseMessage == null) && adminIdValid && (!config.isUserMode(message.getFrom().getId()))) {
                            adminResponseMessage = defaultModel.onMessageFromAdmin(message);
                        }
                        if ((adminResponseMessage == null) && (commandResponseMessage == null)) {
                            defaultModel.onReceivingMessage(message);
                        }
                    } else if (update.getInline_query() != null) {
                        defaultModel.onInlineQuery(update.getInline_query());
                    } else if (update.getChosen_inline_result() != null) {
                        defaultModel.onChosenInlineResult(update.getChosen_inline_result());
                    }
                } catch (Exception e) {
                    logger.throwing(this.getClass().getName(), "startBotWork", e);
                }
            }
        }
    }

    public void stopWorker() {
        enabled = false;
    }

    @Override
    public void run() {
        startBotWork();
    }

}
