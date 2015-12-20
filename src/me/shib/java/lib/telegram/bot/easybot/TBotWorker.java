package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.telegram.bot.service.TelegramBot;
import me.shib.java.lib.telegram.bot.types.Message;

public class TBotWorker extends Thread {

    private TBotConfig tBotConfig;
    private TelegramBot bot;
    private UpdateReceiver updateReceiver;
    private int threadNumber;
    private TBotModel defaultModel;

    private static int threadCounter = 0;

    public synchronized int getThreadNumber() {
        if (this.threadNumber < 1) {
            TBotWorker.threadCounter++;
            this.threadNumber = TBotWorker.threadCounter;
        }
        return this.threadNumber;
    }

    public TBotWorker(TBotModel tBotModel, TBotConfig tBotConfig) {
        initTBotWorker(tBotModel, tBotConfig);
    }

    private void initTBotWorker(TBotModel tBotModel, TBotConfig tBotConfig) {
        this.tBotConfig = tBotConfig;
        if ((tBotConfig.getBotApiToken() != null) && (!tBotConfig.getBotApiToken().isEmpty())) {
            bot = TelegramBot.getInstance(tBotConfig.getBotApiToken());
            updateReceiver = UpdateReceiver.getDefaultInstance(bot);
            defaultModel = new DefaultBotModel(tBotConfig, tBotModel, bot);
        }
    }

    public void startBotWork() {
        TBotSweeper.startDefaultInstance(defaultModel, tBotConfig, bot);
        updateReceiver.onBotStart();
        System.out.println("Starting thread " + getThreadNumber() + " with " + updateReceiver.whoAmI().getUsername());
        while (true) {
            try {
                Message message = updateReceiver.getUpdate().getMessage();
                long senderId = message.getChat().getId();
                Message adminResponseMessage = null;
                if (tBotConfig.isAdmin(senderId)) {
                    adminResponseMessage = defaultModel.onMessageFromAdmin(bot, message);
                }
                Message commandResponseMessage = null;
                if ((adminResponseMessage == null) && (tBotConfig.isValidCommand(message.getText()))) {
                    commandResponseMessage = defaultModel.onCommand(bot, message);
                }
                if ((adminResponseMessage == null) && (commandResponseMessage == null)) {
                    defaultModel.onReceivingMessage(bot, message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        startBotWork();
    }

}
