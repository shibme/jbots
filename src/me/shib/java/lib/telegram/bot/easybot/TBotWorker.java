package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.telegram.bot.types.Message;
import me.shib.java.lib.telegram.bot.types.Update;

public class TBotWorker extends Thread {

    private static int threadCounter = 0;

    private BotConfig config;
    private UpdateReceiver updateReceiver;
    private DefaultBotModel defaultModel;
    private boolean enabled;

    public TBotWorker(BotConfig config) {
        this.config = config;
        if (config != null) {
            if ((config.getBotApiToken() != null) && (!config.getBotApiToken().isEmpty())) {
                updateReceiver = UpdateReceiver.getDefaultInstance(this.config.getBotApiToken());
                defaultModel = new DefaultBotModel(config);
            }
        }
    }

    public static synchronized int getNewThreadNumber() {
        threadCounter++;
        return threadCounter;
    }

    public void startBotWork() {
        if (defaultModel != null) {
            TBotSweeper.startDefaultInstance(defaultModel);
            updateReceiver.onBotStart();
            System.out.println("Starting thread " + getNewThreadNumber() + " with " + updateReceiver.whoAmI().getUsername() + " using the model: " + defaultModel.getModelClassName());
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
                    e.printStackTrace();
                }
            }
        }
    }

    public void stopBotThread() {
        enabled = false;
    }

    @Override
    public void run() {
        startBotWork();
    }

}
