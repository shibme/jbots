package me.shib.java.lib.jbots;

import me.shib.java.lib.jbotstats.AnalyticsBot;
import me.shib.java.lib.jtelebot.models.types.*;
import me.shib.java.lib.jtelebot.models.updates.*;
import me.shib.java.lib.jtelebot.service.TelegramBot;
import org.reflections.Reflections;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class JBot extends Thread {

    private static final Logger logger = Logger.getLogger(JBot.class.getName());
    private static final Map<String, JBot> botSweeperMap = new HashMap<>();
    private static final Date startTime = new Date();
    private static final Reflections reflections = new Reflections("");

    private static int threadCounter = 0;

    protected TelegramBot bot;
    protected JBotConfig config;
    protected String botRatingUrl;
    protected String botReviewMarkdownMessage;
    private UpdateReceiver updateReceiver;
    private boolean enabled;
    private boolean sweeperMode;
    private int threadNumber;

    public JBot(JBotConfig config) {
        this.config = config;
        this.bot = config.getBot();
        this.botRatingUrl = "https://telegram.me/storebot?start=" + bot.getIdentity().getUsername();
        this.botReviewMarkdownMessage = "Please *help us by giving an amazing rating and review*" +
                " for our work in *StoreBot*:\n" + this.botRatingUrl;
        this.updateReceiver = UpdateReceiver.getDefaultInstance(config);
        this.enabled = true;
        this.sweeperMode = false;
        this.threadNumber = 0;
    }

    public static Class[] getAllSubTypes() {
        Set<Class<? extends JBot>> subTypes = reflections.getSubTypesOf(JBot.class);
        return subTypes.toArray(new Class[subTypes.size()]);
    }

    private static synchronized int getThreadNumber() {
        threadCounter++;
        return threadCounter;
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
        JBot sweeper = botSweeperMap.get(config.botApiToken());
        if (sweeper == null) {
            sweeper = new DefaultJBot(config);
            sweeper.sweeperMode = true;
            botSweeperMap.put(config.botApiToken(), sweeper);
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

    private static String getUpTime() {
        long start = startTime.getTime();
        long current = new Date().getTime();
        long timeDiff = current - start;
        timeDiff = timeDiff / 1000;
        int seconds = (int) (timeDiff % 60);
        timeDiff = timeDiff / 60;
        int mins = (int) (timeDiff % 60);
        timeDiff = timeDiff / 60;
        int hours = (int) (timeDiff % 24);
        timeDiff = timeDiff / 24;
        return timeDiff + "d " + hours + "h " + mins + "m " + seconds + "s ";
    }

    private static String getHostInfo() {
        InetAddress ip;
        String hostname;
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
            return hostname + "(" + ip.getHostAddress() + ")";
        } catch (UnknownHostException e) {
            logger.throwing(DefaultJBot.class.getName(), "getHostInfo", e);
            return "Unknown Host";
        }
    }

    public String getAnalyticsRedirectedURL(long user_id, String url) {
        try {
            AnalyticsBot analyticsBot = (AnalyticsBot) bot;
            String analyticsURL = analyticsBot.getAnalyticsRedirectedURL(user_id, url);
            if (analyticsURL != null) {
                return analyticsURL;
            }
        } catch (Exception e) {
            logger.throwing(this.getClass().getName(), "getAnalyticsRedirectedURL", e);
        }
        return url;
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
                            ParseMode.Markdown, false, 0, new ReplyKeyboardHide(false));
                } catch (IOException e) {
                    logger.throwing(this.getClass().getName(), "messageUsersOnDowntimeFailure", e);
                }
            }
        }
    }

    public boolean forwardToAdmins(Message message) {
        try {
            long[] admins = config.admins();
            if ((admins != null) && (admins.length > 0)) {
                for (long admin : admins) {
                    try {
                        bot.forwardMessage(new ChatId(admin), new ChatId(message.getFrom().getId()),
                                message.getMessage_id());
                    } catch (IOException e) {
                        logger.throwing(this.getClass().getName(), "forwardToAdmins", e);
                    }
                }
                bot.sendMessage(new ChatId(message.getChat().getId()),
                        "Your message has been forwarded to the *admin*. It might take quite sometime to get back to you. Please be patient.",
                        ParseMode.Markdown, false, message.getMessage_id());
                return true;
            }
            bot.sendMessage(new ChatId(message.getChat().getId()),
                    "The support team is unavailable. Please try later.", null, false, message.getMessage_id());
        } catch (IOException e) {
            logger.throwing(this.getClass().getName(), "forwardToAdmins", e);
        }
        return false;
    }

    private void sweeperAction() {
        long intervals = config.reportInterval() * 1000;
        long[] adminIdList = config.admins();
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

    private String getCommand(String text) {
        if (text != null) {
            String[] words = text.split("\\s+");
            if (words.length > 0) {
                String command = words[0];
                if (command.startsWith("/")) {
                    return command;
                }
            }
        }
        return null;
    }

    private String getCommandArgument(String command, String text) {
        String commandArgument = text.replaceFirst(command, "");
        return commandArgument.trim();
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
                    boolean adminMode = (config.isAdmin(message.getChat().getId()) || config.isAdmin(message.getFrom().getId())) && (!config.isUserMode(message.getFrom().getId()));
                    MessageHandler messageHandler = onMessage(message);
                    if (messageHandler != null) {
                        String command = getCommand(message.getText());
                        if (command != null) {
                            if (adminMode) {
                                messageHandler.onCommandFromAdmin(command, getCommandArgument(command, message.getText()));
                            } else {
                                messageHandler.onCommandFromUser(command, getCommandArgument(command, message.getText()));
                            }
                        } else {
                            if (adminMode) {
                                messageHandler.onMessageFromAdmin();
                            } else {
                                messageHandler.onMessageFromUser();
                            }
                        }
                    } else {
                        onMessage(message);
                    }
                } else if (update.getInline_query() != null) {
                    onInlineQuery(update.getInline_query());
                } else if (update.getChosen_inline_result() != null) {
                    onChosenInlineResult(update.getChosen_inline_result());
                } else if (update.getCallback_query() != null) {
                    onCallbackQuery(update.getCallback_query());
                }
            } catch (Exception e) {
                logger.throwing(this.getClass().getName(), "startBot", e);
            }
        }
    }

    @Override
    public void run() {
        if (sweeperMode) {
            sweeperAction();
        } else {
            startBot();
        }
    }

    public abstract MessageHandler onMessage(Message message);

    public abstract void onInlineQuery(InlineQuery query);

    public abstract void onChosenInlineResult(ChosenInlineResult chosenInlineResult);

    public abstract void onCallbackQuery(CallbackQuery callbackQuery);

    public void sendStatusMessage(long chatId) {
        if (config.defaultWorker()) {
            try {
                bot.sendMessage(new ChatId(chatId),
                        "Reporting status:\nHost: " + getHostInfo() + "\nUp Time: " + getUpTime());
            } catch (IOException e) {
                logger.throwing(this.getClass().getName(), "sendStatusMessage", e);
            }
        }
    }

}
