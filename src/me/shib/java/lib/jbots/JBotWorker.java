package me.shib.java.lib.jbots;


import me.shib.java.lib.jtelebot.service.TelegramBot;
import me.shib.java.lib.jtelebot.types.Message;
import me.shib.java.lib.jtelebot.types.Update;

public class JBotWorker extends Thread {

    private static int threadCounter = 0;

    private JBotConfig config;
    private TelegramBot bot;
    private JBotUpdateReceiver jBotUpdateReceiver;
    private JBotDefaultModel defaultModel;
    private boolean enabled;
    private int threadNumber;

    public JBotWorker(JBotConfig config) {
        this.config = config;
        if (config != null) {
            if ((config.getBotApiToken() != null) && (!config.getBotApiToken().isEmpty())) {
                jBotUpdateReceiver = JBotUpdateReceiver.getDefaultInstance(this.config);
                defaultModel = new JBotDefaultModel(config);
                bot = defaultModel.getBot();
                enabled = true;
            }
        }
        threadNumber = 0;
    }

    private static synchronized int getThisThreadNumber(JBotWorker worker) {
        if (worker.threadNumber == 0) {
            threadCounter++;
            worker.threadNumber = threadCounter;
        }
        return worker.threadNumber;
    }

    public void startBotWork() {
        if (defaultModel != null) {
            JBotSweeper.startDefaultInstance(defaultModel);
            jBotUpdateReceiver.onBotStart();
            System.out.println("Starting thread " + getThisThreadNumber(this) + " with " + bot.getIdentity().getUsername() + " using the model: " + defaultModel.getModelClassName());
            while (enabled) {
                try {
                    Update update = jBotUpdateReceiver.getUpdate();
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

    public void stopWorker() {
        enabled = false;
    }

    @Override
    public void run() {
        startBotWork();
    }

}
