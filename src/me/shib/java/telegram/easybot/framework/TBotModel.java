package me.shib.java.telegram.easybot.framework;

import me.shib.java.telegram.bot.service.TelegramBotService;
import me.shib.java.telegram.bot.types.Message;

public interface TBotModel {
	
	public Message onMessageFromAdmin(TelegramBotService tBotService, Message message);
	public Message onCommand(TelegramBotService tBotService, Message message);
	public Message onReceivingMessage(TelegramBotService tBotService, Message message);
	
}
