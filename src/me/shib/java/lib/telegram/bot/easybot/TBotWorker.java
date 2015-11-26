package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.telegram.bot.service.TelegramBotService;
import me.shib.java.lib.telegram.bot.types.Message;

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
	
	public void startBotWork() {
		if(defaulModelInUse) {
			TBotSweeper.startDefaultInstance(tBotModel, tBotConfig, null);
		}
		else {
			TBotSweeper.startDefaultInstance(tBotModel, tBotConfig, getDefaultModel(tBotConfig));
		}
		updateReceiver.onBotStart();
		System.out.println("Starting thread " + threadNumber + " with " + updateReceiver.whoAmI().getUsername());
		while(true) {
			try {
				Message message = updateReceiver.getUpdate().getMessage();
				long senderId = message.getChat().getId();
				Message adminResponseMessage = null;
				if(tBotConfig.isAdmin(senderId)) {
					adminResponseMessage = tBotModel.onMessageFromAdmin(tBotService, message);
					if((adminResponseMessage == null) && (!defaulModelInUse)) {
						adminResponseMessage = getDefaultModel(tBotConfig).onMessageFromAdmin(tBotService, message);
					}
				}
				Message commandResponseMessage = null;
				if((adminResponseMessage == null) && (tBotConfig.isValidCommand(message.getText()))) {
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
