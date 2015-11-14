package me.shib.java.telegram.bot.worker;

import java.io.IOException;

import me.shib.java.telegram.bot.service.TelegramBotService;
import me.shib.java.telegram.bot.types.ChatId;
import me.shib.java.telegram.bot.types.Message;
import me.shib.java.telegram.bot.types.ParseMode;

public class DefaultBotModel implements TBotModel {
	
	private TBotConfig tBotConfig;
	
	protected DefaultBotModel(TBotConfig tBotConfig) {
		this.tBotConfig = tBotConfig;
	}

	@Override
	public Message onReceivingMessage(TelegramBotService tBotService, Message message) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message onMessageFromAdmin(TelegramBotService tBotService, Message message) {
		try {
			long replyToUser = message.getReply_to_message().getForward_from().getId();
			if(replyToUser > 0) {
				return tBotService.sendMessage(new ChatId(replyToUser), message.getText());
			}
		} catch (Exception e) {}
		return null;
	}

	@Override
	public Message supportMessageHandler(TelegramBotService tBotService, Message message) {
		try {
			String text = message.getText();
			String[] words = text.split("\\s+");
			if(words.length == 1) {
				return tBotService.sendMessage(new ChatId(message.getChat().getId()), "Please provide a valid message following the \"" + tBotConfig.getSupportCommand() + "\" keyword.");
			}
			else {
				long[] admins = tBotConfig.getAdminIdList();
				if((admins != null) && (admins.length > 0)) {
					for(long admin : admins) {
						try {
							tBotService.forwardMessage(new ChatId(admin), new ChatId(message.getFrom().getId()), message.getMessage_id());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					return tBotService.sendMessage(new ChatId(message.getChat().getId()), "Your request was duly noted.", ParseMode.None, false, message.getMessage_id());
				}
				else {
					return tBotService.sendMessage(new ChatId(message.getChat().getId()), "The support team is unavailable. Please try later.", ParseMode.None, false, message.getMessage_id());
				}
			}
		} catch (IOException e) {
			return null;
		}
	}

}
