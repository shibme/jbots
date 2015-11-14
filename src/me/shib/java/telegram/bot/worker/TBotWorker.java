package me.shib.java.telegram.bot.worker;

import me.shib.java.telegram.bot.service.TelegramBotService;
import me.shib.java.telegram.bot.types.Message;

public class TBotWorker extends Thread {
	
	private TBotModel tBotModel;
	private TBotConfig tBotConfig;
	private TelegramBotService tBotService;
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
		UpdateReceiver.setBotApiToken(tBotConfig.getBotApiToken());
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
	
	private boolean isSupportMessage(Message message) {
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
				Message adminResponseMessage = null;
				if(tBotConfig.isAdminId(senderId)) {
					adminResponseMessage = tBotModel.onMessageFromAdmin(tBotService, message);
					if((adminResponseMessage == null) && (!defaulModelInUse)) {
						adminResponseMessage = getDefaultModel(tBotConfig).onMessageFromAdmin(tBotService, message);
					}
				}
				Message supporResponseMessage = null;
				if((adminResponseMessage == null) && (isSupportMessage(message))) {
					supporResponseMessage = tBotModel.supportMessageHandler(tBotService, message);
					if((supporResponseMessage == null) && (!defaulModelInUse)) {
						supporResponseMessage = getDefaultModel(tBotConfig).supportMessageHandler(tBotService, message);
					}
				}
				if((adminResponseMessage == null) && (supporResponseMessage == null)) {
					tBotModel.onReceivingMessage(tBotService, message);
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
