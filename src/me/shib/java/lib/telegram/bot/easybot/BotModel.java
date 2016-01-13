package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.telegram.bot.service.TelegramBot;
import me.shib.java.lib.telegram.bot.types.InlineQuery;
import me.shib.java.lib.telegram.bot.types.InlineQueryResult;
import me.shib.java.lib.telegram.bot.types.Message;

public abstract class BotModel {

    private BotConfig botConfig;

    public BotConfig getBotConfig() {
        if (botConfig == null) {
            botConfig = BotConfig.getConfigForClassName(this.getClass().getName());
        }
        return botConfig;
    }

    public abstract Message onMessageFromAdmin(TelegramBot bot, Message message);

    public abstract Message onCommand(TelegramBot bot, Message message);

    public abstract Message onReceivingMessage(TelegramBot bot, Message message);

    public abstract boolean onInlineQuery(TelegramBot bot, InlineQuery query);

    public abstract Message sendStatusMessage(TelegramBot bot, long chatId);

}
