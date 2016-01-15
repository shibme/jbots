package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.telegram.bot.types.InlineQuery;
import me.shib.java.lib.telegram.bot.types.Message;
import me.shib.java.lib.telegram.bot.types.Update;

public class TBotWorker extends Thread {

    private static int threadCounter = 0;
    private BotConfig botConfig;
    private UpdateReceiver updateReceiver;
    private int threadNumber;
    private BotModel defaultModel;
    private boolean enabled;

    public TBotWorker(BotModel botModel) {
        initTBotWorker(botModel);
    }

    public synchronized int getThreadNumber() {
        this.enabled = true;
        if (this.threadNumber < 1) {
            TBotWorker.threadCounter++;
            this.threadNumber = TBotWorker.threadCounter;
        }
        return this.threadNumber;
    }

    private void initTBotWorker(BotModel botModel) {
        this.botConfig = botModel.getConfig();
        if ((botConfig.getBotApiToken() != null) && (!botConfig.getBotApiToken().isEmpty())) {
            updateReceiver = UpdateReceiver.getDefaultInstance(this.botConfig.getBotApiToken());
            defaultModel = new DefaultBotModel(botModel);
        }
    }

    public void startBotWork() {
        TBotSweeper.startDefaultInstance(defaultModel);
        updateReceiver.onBotStart();
        System.out.println("Starting thread " + getThreadNumber() + " with " + updateReceiver.whoAmI().getUsername());
        while (enabled) {
            try {
                Update update = updateReceiver.getUpdate();
                if (update.getMessage() != null) {
                    Message message = update.getMessage();
                    boolean adminIdValid = (botConfig.isAdmin(message.getChat().getId()) || botConfig.isAdmin(message.getFrom().getId()));
                    Message commandResponseMessage = null;
                    if (botConfig.isValidCommand(message.getText())) {
                        commandResponseMessage = defaultModel.onCommand(message);
                    }
                    Message adminResponseMessage = null;
                    if ((commandResponseMessage == null) && adminIdValid && (!botConfig.isUserMode(message.getFrom().getId()))) {
                        adminResponseMessage = defaultModel.onMessageFromAdmin(message);
                    }
                    if ((adminResponseMessage == null) && (commandResponseMessage == null)) {
                        defaultModel.onReceivingMessage(message);
                    }
                } else if (update.getInline_query() != null) {
                    defaultModel.onInlineQuery(update.getInline_query());
                }
                else if(update.getChosen_inline_result() != null) {
                    defaultModel.onChosenInlineResult(update.getChosen_inline_result());
                }
            } catch (Exception e) {
                e.printStackTrace();
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
