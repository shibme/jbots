package me.shib.java.lib.jbots;

import me.shib.java.lib.jtelebot.service.TelegramBot;
import me.shib.java.lib.jtelebot.types.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class JBot extends Thread {

    private static final Logger logger = Logger.getLogger(JBot.class.getName());
    private static final Map<String, JBot> botSweeperMap = new HashMap<>();

    private static int threadCounter = 0;

    protected TelegramBot bot;
    protected JBotConfig config;
    private UpdateReceiver updateReceiver;
    private boolean enabled;
    private boolean sweeperMode;
    private int threadNumber;

    public JBot(JBotConfig config) {
        this.config = config;
        this.bot = config.getBot();
        updateReceiver = UpdateReceiver.getDefaultInstance(config);
        enabled = true;
        sweeperMode = false;
        threadNumber = 0;
    }

    private static synchronized int getThreadNumber() {
        threadCounter++;
        return threadCounter;
    }

    public static String getAnalyticsRedirectedURL(TelegramBot bot, long user_id, String url) {
        try {
            AnalyticsBot analyticsBot = (AnalyticsBot) bot;
            String analyticsURL = analyticsBot.getAnalyticsRedirectedURL(user_id, url);
            if (analyticsURL != null) {
                return analyticsURL;
            }
        } catch (Exception e) {
            logger.throwing(JBot.class.getName(), "getAnalyticsRedirectedURL", e);
        }
        return url;
    }

    private static boolean isValidName(String text) {
        return text != null && !text.isEmpty();
    }

    private static String getProperName(String firstName, String lastName, String username) {
        StringBuilder nameBuilder = new StringBuilder();
        if (isValidName(firstName)) {
            nameBuilder.append(firstName);
        }
        if (isValidName(lastName)) {
            if (!nameBuilder.toString().isEmpty()) {
                nameBuilder.append(" ");
            }
            nameBuilder.append(lastName);
        }
        if (nameBuilder.toString().isEmpty() && isValidName(username)) {
            nameBuilder.append(username);
        }
        return nameBuilder.toString();
    }

    public static String getProperName(Chat chat) {
        if (chat != null) {
            return getProperName(chat.getFirst_name(), chat.getLast_name(), chat.getUsername());
        }
        return "";
    }

    public static String getProperName(User user) {
        if (user != null) {
            return getProperName(user.getFirst_name(), user.getLast_name(), user.getUsername());
        }
        return "";
    }

    protected static synchronized JBot startSweeper(JBotConfig config) {
        JBot sweeper = botSweeperMap.get(config.getBotApiToken());
        if (sweeper == null) {
            sweeper = new DefaultJBot(config);
            sweeper.sweeperMode = true;
            botSweeperMap.put(config.getBotApiToken(), sweeper);
            logger.log(Level.INFO, "Starting services for: " + sweeper.bot.getIdentity().getFirst_name() + " (" + sweeper.bot.getIdentity().getUsername() + ")");
            sweeper.start();
        }
        return sweeper;
    }

    protected static synchronized JBot startNewJBot(JBotConfig config) {
        JBot jBot = new DefaultJBot(config);
        jBot.threadNumber = getThreadNumber();
        logger.log(Level.INFO, "Starting thread " + jBot.threadNumber + " with " + jBot.bot.getIdentity().getUsername() + " using the model: " + jBot.getModelClassName());
        jBot.start();
        return jBot;
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

    public Message forwardToAdmins(Message message) {
        try {
            long[] admins = config.getAdminIdList();
            if ((admins != null) && (admins.length > 0)) {
                for (long admin : admins) {
                    try {
                        bot.forwardMessage(new ChatId(admin), new ChatId(message.getFrom().getId()),
                                message.getMessage_id());
                    } catch (IOException e) {
                        logger.throwing(this.getClass().getName(), "forwardToAdmins", e);
                    }
                }
                return bot.sendMessage(new ChatId(message.getChat().getId()),
                        "Your message has been forwarded to the *admin*. It might take quite sometime to get back to you. Please be patient.",
                        false, ParseMode.Markdown, false, message.getMessage_id());
            }
            return bot.sendMessage(new ChatId(message.getChat().getId()),
                    "The support team is unavailable. Please try later.", false, null, false, message.getMessage_id());
        } catch (IOException e) {
            logger.throwing(this.getClass().getName(), "forwardToAdmins", e);
            return null;
        }
    }

    private void sweeperAction() {
        long intervals = config.getReportIntervalInSeconds() * 1000;
        long[] adminIdList = config.getAdminIdList();
        if ((intervals > 0) && (adminIdList != null) && (adminIdList.length > 0)) {
            while (enabled) {
                try {
                    for (long admin : adminIdList) {
                        sendStatusMessage(admin);
                    }
                    Thread.sleep(intervals);
                } catch (Exception e) {
                    logger.throwing(this.getClass().getName(), "sweeperAction", e);
                }
            }
        }
    }

    protected String getModelClassName() {
        return getClass().getSimpleName();
    }

    public void startBot() {
        messageUsersOnDowntimeFailure(updateReceiver.getMissedMessageList());
        while (enabled) {
            try {
                Update update = updateReceiver.getUpdate();
                if (update.getMessage() != null) {
                    Message message = update.getMessage();
                    boolean adminIdValid = (config.isAdmin(message.getChat().getId()) || config.isAdmin(message.getFrom().getId()));
                    Message commandResponseMessage = null;
                    if (config.isValidCommand(message.getText())) {
                        commandResponseMessage = onCommand(message);
                    }
                    Message adminResponseMessage = null;
                    if ((commandResponseMessage == null) && adminIdValid && (!config.isUserMode(message.getFrom().getId()))) {
                        adminResponseMessage = onMessageFromAdmin(message);
                    }
                    if ((adminResponseMessage == null) && (commandResponseMessage == null)) {
                        onReceivingMessage(message);
                    }
                } else if (update.getInline_query() != null) {
                    onInlineQuery(update.getInline_query());
                } else if (update.getChosen_inline_result() != null) {
                    onChosenInlineResult(update.getChosen_inline_result());
                }
            } catch (Exception e) {
                logger.throwing(this.getClass().getName(), "startBot", e);
            }
        }
    }

    public void stopWorker() {
        enabled = false;
    }

    @Override
    public void run() {
        if (sweeperMode) {
            sweeperAction();
        } else {
            startBot();
        }
    }

    public abstract Message onMessageFromAdmin(Message message);

    public abstract Message onCommand(Message message);

    public abstract Message onReceivingMessage(Message message);

    public abstract boolean onInlineQuery(InlineQuery query);

    public abstract boolean onChosenInlineResult(ChosenInlineResult chosenInlineResult);

    public abstract Message sendStatusMessage(long chatId);

}
