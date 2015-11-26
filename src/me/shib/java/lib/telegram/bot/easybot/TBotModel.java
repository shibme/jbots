package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.telegram.bot.service.TelegramBotService;
import me.shib.java.lib.telegram.bot.types.Message;

public interface TBotModel {
	public Message onMessageFromAdmin(TelegramBotService tBotService, Message message);
	public Message onCommand(TelegramBotService tBotService, Message message);
	public Message onReceivingMessage(TelegramBotService tBotService, Message message);
	public Message sendStatusMessage(TelegramBotService tBotService, long chatId);
}
