package me.shib.java.lib.jbots;

import me.shib.java.lib.jtelebot.service.TelegramBot;
import me.shib.java.lib.jtelebot.types.ChosenInlineResult;
import me.shib.java.lib.jtelebot.types.InlineQuery;
import me.shib.java.lib.jtelebot.types.Message;

public abstract class BotModel {

    private TelegramBot bot;

    public BotModel(BotConfig config) {
        this.bot = TelegramBot.getInstance(config.getBotApiToken());
    }

    public TelegramBot getBot() {
        return bot;
    }

    public abstract Message onMessageFromAdmin(Message message);

    public abstract Message onCommand(Message message);

    public abstract Message onReceivingMessage(Message message);

    public abstract boolean onInlineQuery(InlineQuery query);

    public abstract boolean onChosenInlineResult(ChosenInlineResult chosenInlineResult);

    public abstract Message sendStatusMessage(long chatId);

}
