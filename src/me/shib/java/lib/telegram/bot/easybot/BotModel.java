package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.telegram.bot.service.TelegramBot;
import me.shib.java.lib.telegram.bot.types.InlineQuery;
import me.shib.java.lib.telegram.bot.types.Message;

public abstract class BotModel {

    private BotConfig botConfig;
    private TelegramBot bot;

    public BotConfig thisConfig() {
        if (botConfig == null) {
            botConfig = BotConfig.getConfigForClassName(this.getClass().getName());
        }
        return botConfig;
    }

    public BotConfig getConfig() {
        return thisConfig();
    }

    public TelegramBot thisBot() {
        if (bot == null) {
            bot = TelegramBot.getInstance(thisConfig().getBotApiToken());
        }
        return bot;
    }

    public TelegramBot getBot() {
        return thisBot();
    }

    public abstract Message onMessageFromAdmin(TelegramBot bot, Message message);

    public abstract Message onCommand(TelegramBot bot, Message message);

    public abstract Message onReceivingMessage(TelegramBot bot, Message message);

    public abstract boolean onInlineQuery(TelegramBot bot, InlineQuery query);

    public abstract Message sendStatusMessage(TelegramBot bot, long chatId);

}
