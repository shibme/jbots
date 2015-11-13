package me.shib.java.telegram.bot.worker;

import java.io.IOException;

import me.shib.java.telegram.bot.service.TelegramBotService;
import me.shib.java.telegram.bot.types.ChatId;
import me.shib.java.telegram.bot.types.Message;

public class TBotWorker extends Thread {
	
	private TBotModel tBotAct;
	private TBotConfig tBotConfig;
	private TelegramBotService tBotService;
	private int threadNumber;
	
	private static int threadCounter = 0;
	
	private static synchronized int getThreadNumber() {
		TBotWorker.threadCounter++;
		return TBotWorker.threadCounter;
	}
	
	public TBotWorker(TBotModel tBotAct, TBotConfig tBotConfig) {
		initTBotWorker(tBotAct, tBotConfig);
	}
	
	public TBotWorker(TBotModel tBotAct) {
		tBotConfig = TBotConfig.getFileConfig();
		initTBotWorker(tBotAct, tBotConfig);
	}
	
	private void initTBotWorker(TBotModel tBotAct, TBotConfig tBotConfig) {
		threadNumber = TBotWorker.getThreadNumber();
		UpdateReceiver.setBotApiToken(tBotConfig.getBotApiToken());
		this.tBotAct = tBotAct;
		this.tBotConfig = tBotConfig;
		if((tBotConfig.getBotApiToken() != null) && (!tBotConfig.getBotApiToken().isEmpty())) {
			tBotService = new TelegramBotService(tBotConfig.getBotApiToken());
		}
	}
	
	private boolean handleIfSupportMessage(TelegramBotService tBotService, Message message) throws IOException {
		String text = message.getText();
		if(text != null) {
			String[] words = text.split("\\s+");
			if(words.length > 0) {
				String supportCommand = tBotConfig.getSupportCommand();
				if(supportCommand != null) {
					supportCommand = supportCommand.replace("\\s+", "");
				}
				if(supportCommand.isEmpty()) {
					supportCommand = null;
				}
				if((supportCommand != null) &&(words[0].equalsIgnoreCase(tBotConfig.getSupportCommand()))) {
					Message responseMessage = tBotAct.customSupportHandler(tBotService, message);
					if(responseMessage == null) {
						if(words.length == 1) {
							tBotService.sendMessage(new ChatId(message.getChat().getId()), "Please provide a valid message following the \"" + tBotConfig.getSupportCommand() + "\" keyword.");
						}
						else {
							long[] admins = tBotConfig.getAdminIdList();
							if((admins != null) && (admins.length > 0)) {
								for(long admin : admins) {
									tBotService.forwardMessage(new ChatId(admin), new ChatId(message.getFrom().getId()), message.getMessage_id());
								}
							}
							else {
								return false;
							}
						}
					}
					return true;
				}
			}
		}
		return false;
	}
	
	public void startBotWork() {
		CommonMethods.onThreadStart(threadNumber);
		while(true) {
			try {
				Message message = UpdateReceiver.getUpdate().getMessage();
				CommonMethods.logNewMessage(message);
				long senderId = message.getChat().getId();
				Message adminMessage = null;
				if(tBotConfig.isAdminId(senderId)) {
					adminMessage = tBotAct.onMessageFromAdmin(tBotService, message);
				}
				if((adminMessage == null) && (!handleIfSupportMessage(tBotService, message))) {
					tBotAct.onReceivingMessage(tBotService, message);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		startBotWork();
	}
	
}
