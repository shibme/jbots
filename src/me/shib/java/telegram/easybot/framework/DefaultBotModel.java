package me.shib.java.telegram.easybot.framework;

import java.io.IOException;

import me.shib.java.telegram.bot.service.TelegramBotService;
import me.shib.java.telegram.bot.types.ChatId;
import me.shib.java.telegram.bot.types.Message;
import me.shib.java.telegram.bot.types.ParseMode;
import me.shib.java.telegram.bot.types.TelegramFile;
import me.shib.java.telegram.bot.types.User;

public class DefaultBotModel implements TBotModel {
	
	private TBotConfig tBotConfig;
	private UpdateReceiver updRecvr;
	
	protected DefaultBotModel(TBotConfig tBotConfig) {
		this.tBotConfig = tBotConfig;
		this.updRecvr = UpdateReceiver.getDefaultInstance(tBotConfig.getBotApiToken());
	}

	public Message onReceivingMessage(TelegramBotService tBotService, Message message) {
		try {
			long[] admins = tBotConfig.getAdminIdList();
			if((admins != null) && (admins.length > 0)) {
				for(long admin : admins) {
					try {
						tBotService.forwardMessage(new ChatId(admin), new ChatId(message.getFrom().getId()), message.getMessage_id());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return tBotService.sendMessage(new ChatId(message.getChat().getId()), "Hey " + message.getFrom().getFirst_name() + ",Your request was taken. I'll get back to you ASAP.", ParseMode.None, false, message.getMessage_id());
			}
			else {
				return tBotService.sendMessage(new ChatId(message.getChat().getId()), "The support team is unavailable. Please try later.", ParseMode.None, false, message.getMessage_id());
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Message onMessageFromAdmin(TelegramBotService tBotService, Message message) {
		try {
			long replyToUser = message.getReply_to_message().getForward_from().getId();
			if(replyToUser > 0) {
				if(message.getText() != null) {
					return tBotService.sendMessage(new ChatId(replyToUser), message.getText());
				}
				else if(message.getDocument() != null) {
					return tBotService.sendDocument(new ChatId(replyToUser), new TelegramFile(message.getDocument().getFile_id()));
				}
				else if(message.getVideo() != null) {
					return tBotService.sendVideo(new ChatId(replyToUser), new TelegramFile(message.getVideo().getFile_id()));
				}
				else if(message.getPhoto() != null) {
					return tBotService.sendPhoto(new ChatId(replyToUser), new TelegramFile(message.getPhoto()[message.getPhoto().length - 1].getFile_id()));
				}
				else if(message.getAudio() != null) {
					return tBotService.sendDocument(new ChatId(replyToUser), new TelegramFile(message.getDocument().getFile_id()));
				}
				else if(message.getVoice() != null) {
					return tBotService.sendDocument(new ChatId(replyToUser), new TelegramFile(message.getDocument().getFile_id()));
				}
				else if(message.getSticker() != null) {
					return tBotService.sendDocument(new ChatId(replyToUser), new TelegramFile(message.getDocument().getFile_id()));
				}
			}
		} catch (Exception e) {}
		return null;
	}

	public Message onCommand(TelegramBotService tBotService, Message message) {
		if((message.getText() != null) && message.getText().equals("/start")) {
			User myself = updRecvr.whoAmI();
			try {
				return tBotService.sendMessage(new ChatId(message.getChat().getId()), "Hi " + message.getFrom().getFirst_name()
						+ ". My name is *" + myself.getFirst_name() + "* (@" + myself.getUsername() + ")."
								+ " I'll try to serve you the best with all my efforts. Welcome!", ParseMode.Markdown);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
