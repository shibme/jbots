package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.telegram.bot.service.TelegramBot;
import me.shib.java.lib.telegram.bot.types.Message;

public interface TBotModel {
    public Message onMessageFromAdmin(TelegramBot tBotService, Message message);

    public Message onCommand(TelegramBot tBotService, Message message);

    public Message onReceivingMessage(TelegramBot tBotService, Message message);

    public Message sendStatusMessage(TelegramBot tBotService, long chatId);
}
