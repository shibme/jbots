package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.telegram.bot.service.TelegramBot;
import me.shib.java.lib.telegram.bot.types.InlineQuery;
import me.shib.java.lib.telegram.bot.types.Message;
import me.shib.java.lib.telegram.bot.types.Update;

public class BotWorker extends Thread {

    private static int threadCounter = 0;
    private BotConfig botConfig;
    private TelegramBot bot;
    private UpdateReceiver updateReceiver;
    private int threadNumber;
    private BotModel defaultModel;

    public BotWorker(BotModel tBotModel) {
        initTBotWorker(tBotModel);
    }

    public synchronized int getThreadNumber() {
        if (this.threadNumber < 1) {
            BotWorker.threadCounter++;
            this.threadNumber = BotWorker.threadCounter;
        }
        return this.threadNumber;
    }

    private void initTBotWorker(BotModel botModel) {
        this.botConfig = botModel.getBotConfig();
        if ((botConfig.getBotApiToken() != null) && (!botConfig.getBotApiToken().isEmpty())) {
            bot = TelegramBot.getInstance(botConfig.getBotApiToken());
            updateReceiver = UpdateReceiver.getDefaultInstance(this.botConfig.getBotApiToken());
            defaultModel = new DefaultBotModel(botModel);
        }
    }

    public void startBotWork() {
        BotSweeper.startDefaultInstance(defaultModel);
        updateReceiver.onBotStart();
        System.out.println("Starting thread " + getThreadNumber() + " with " + updateReceiver.whoAmI().getUsername());
        while (true) {
            try {
                Update update = updateReceiver.getUpdate();
                if (update.getMessage() != null) {
                    Message message = update.getMessage();
                    long senderId = message.getChat().getId();
                    Message adminResponseMessage = null;
                    if (botConfig.isAdmin(senderId)) {
                        adminResponseMessage = defaultModel.onMessageFromAdmin(bot, message);
                    }
                    Message commandResponseMessage = null;
                    if ((adminResponseMessage == null) && (botConfig.isValidCommand(message.getText()))) {
                        commandResponseMessage = defaultModel.onCommand(bot, message);
                    }
                    if ((adminResponseMessage == null) && (commandResponseMessage == null)) {
                        defaultModel.onReceivingMessage(bot, message);
                    }
                } else if (update.getInline_query() != null) {
                    InlineQuery query = update.getInline_query();
                    defaultModel.onInlineQuery(bot, query);
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
