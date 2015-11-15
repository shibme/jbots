package me.shib.java.telegram.easybot.framework;

import me.shib.java.telegram.bot.service.TelegramBotService;
import me.shib.java.telegram.bot.types.Message;

public class TBotWorker extends Thread {
	
	private TBotModel tBotModel;
	private TBotConfig tBotConfig;
	private TelegramBotService tBotService;
	private UpdateReceiver updateReceiver;
	private int threadNumber;
	private TBotModel defaultModel;
	private boolean defaulModelInUse;
	
	private static int threadCounter = 0;
	
	private static synchronized int getThreadNumber() {
		TBotWorker.threadCounter++;
		return TBotWorker.threadCounter;
	}
	
	public TBotWorker(TBotModel tBotModel, TBotConfig tBotConfig) {
		initTBotWorker(tBotModel, tBotConfig);
	}
	
	public TBotWorker(TBotModel tBotModel) {
		tBotConfig = TBotConfig.getFileConfig();
		initTBotWorker(tBotModel, tBotConfig);
	}
	
	private TBotModel getDefaultModel(TBotConfig tBotConfig) {
		if(defaultModel == null) {
			defaultModel = new DefaultBotModel(tBotConfig);
		}
		return defaultModel;
	}
	
	private void initTBotWorker(TBotModel tBotModel, TBotConfig tBotConfig) {
		threadNumber = TBotWorker.getThreadNumber();
		updateReceiver = UpdateReceiver.getDefaultInstance(tBotConfig.getBotApiToken());
		this.tBotConfig = tBotConfig;
		if((tBotConfig.getBotApiToken() != null) && (!tBotConfig.getBotApiToken().isEmpty())) {
			tBotService = new TelegramBotService(tBotConfig.getBotApiToken());
		}
		if(tBotModel != null) {
			this.tBotModel = tBotModel;
			defaulModelInUse = false;
		}
		else {
			this.tBotModel = getDefaultModel(tBotConfig);
			defaulModelInUse = true;
		}
	}
	
	private boolean isAdmin(long senderId) {
		long[] adminList = tBotConfig.getAdminIdList();
		if(adminList != null) {
			for(long adminId : adminList) {
				if(senderId == adminId) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isValidCommand(String messageText) {
		String[] commandList = tBotConfig.getCommandList();
		if((messageText != null) && (commandList != null)) {
			String[] words = messageText.split("\\s+");
			if(words.length > 0) {
				String possibleCommand = words[0];
				for(String command : commandList) {
					if(command.equals(possibleCommand)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void startBotWork() {
		updateReceiver.onBotStart();
		System.out.println("Starting thread " + threadNumber + " of " + updateReceiver.whoAmI().getUsername());
		while(true) {
			try {
				Message message = updateReceiver.getUpdate().getMessage();
				long senderId = message.getChat().getId();
				Message adminResponseMessage = null;
				if(isAdmin(senderId)) {
					adminResponseMessage = tBotModel.onMessageFromAdmin(tBotService, message);
					if((adminResponseMessage == null) && (!defaulModelInUse)) {
						adminResponseMessage = getDefaultModel(tBotConfig).onMessageFromAdmin(tBotService, message);
					}
				}
				Message commandResponseMessage = null;
				if((adminResponseMessage == null) && (isValidCommand(message.getText()))) {
					commandResponseMessage = tBotModel.onCommand(tBotService, message);
					if((commandResponseMessage == null) && (!defaulModelInUse)) {
						commandResponseMessage = getDefaultModel(tBotConfig).onCommand(tBotService, message);
					}
				}
				if((adminResponseMessage == null) && (commandResponseMessage == null)) {
					if(tBotModel.onReceivingMessage(tBotService, message) == null) {
						getDefaultModel(tBotConfig).onReceivingMessage(tBotService, message);
					}
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
