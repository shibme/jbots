package me.shib.java.telegram.bot.worker;

import me.shib.java.telegram.bot.service.TelegramBotService;
import me.shib.java.telegram.bot.types.Message;

public interface TBotModel {
	
	public Message onReceivingMessage(TelegramBotService tBotService, Message message);
	public Message onMessageFromAdmin(TelegramBotService tBotService, Message message);
	public Message customSupportHandler(TelegramBotService tBotService, Message message);
	
}
