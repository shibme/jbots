package me.shib.java.lib.jbots;


import me.shib.java.lib.jtelebot.service.TelegramBot;
import me.shib.java.lib.jtelebot.types.Message;
import me.shib.java.lib.jtelebot.types.Update;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BotWorker extends Thread {

    private static int threadCounter = 0;
    private static Logger logger = Logger.getLogger(BotWorker.class.getName());

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
                bot = defaultModel.getBot();
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

    public void startBotWork() {
        if (defaultModel != null) {
            BotSweeper.startDefaultInstance(defaultModel);
            updateReceiver.onBotStart();
            logger.log(Level.INFO, "Starting thread " + getThisThreadNumber(this) + " with " + bot.getIdentity().getUsername() + " using the model: " + defaultModel.getModelClassName());
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
