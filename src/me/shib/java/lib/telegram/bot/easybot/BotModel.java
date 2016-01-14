package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.telegram.bot.service.TelegramBot;
import me.shib.java.lib.telegram.bot.types.InlineQuery;
import me.shib.java.lib.telegram.bot.types.Message;

public abstract class BotModel {

    private BotConfig botConfig;
    private TelegramBot bot;

    public BotConfig getConfig() {
        if (botConfig == null) {
            botConfig = BotConfig.getConfigForClassName(this.getClass().getName());
        }
        return botConfig;
    }

    public TelegramBot getBot() {
        if (bot == null) {
            bot = TelegramBot.getInstance(getConfig().getBotApiToken());
        }
        return bot;
    }

    public abstract Message onMessageFromAdmin(Message message);

    public abstract Message onCommand(Message message);

    public abstract Message onReceivingMessage(Message message);

    public abstract boolean onInlineQuery(InlineQuery query);

    public abstract Message sendStatusMessage(long chatId);

}
