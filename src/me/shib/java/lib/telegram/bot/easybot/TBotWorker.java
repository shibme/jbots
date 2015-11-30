package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.telegram.bot.service.TelegramBot;
import me.shib.java.lib.telegram.bot.types.Message;

public class TBotWorker extends Thread {

	private TBotConfig tBotConfig;
	private TelegramBot tBotService;
	private UpdateReceiver updateReceiver;
	private int threadNumber;
	private TBotModel defaultModel;

	private static int threadCounter = 0;

	public synchronized int getThreadNumber() {
		if (this.threadNumber < 1) {
			TBotWorker.threadCounter++;
			this.threadNumber = TBotWorker.threadCounter;
		}
		return this.threadNumber;
	}

	public TBotWorker(TBotModel tBotModel, TBotConfig tBotConfig) {
		initTBotWorker(tBotModel, tBotConfig);
	}

	private void initTBotWorker(TBotModel tBotModel, TBotConfig tBotConfig) {
		updateReceiver = UpdateReceiver.getDefaultInstance(tBotConfig.getBotApiToken());
		this.tBotConfig = tBotConfig;
		if ((tBotConfig.getBotApiToken() != null) && (!tBotConfig.getBotApiToken().isEmpty())) {
			tBotService = new TelegramBot(tBotConfig.getBotApiToken());
			defaultModel = new DefaultBotModel(tBotConfig, tBotModel);
		}
	}

	public void startBotWork() {
		TBotSweeper.startDefaultInstance(defaultModel, tBotConfig);
		updateReceiver.onBotStart();
		System.out.println("Starting thread " + getThreadNumber() + " with " + updateReceiver.whoAmI().getUsername());
		while (true) {
			try {
				Message message = updateReceiver.getUpdate().getMessage();
				long senderId = message.getChat().getId();
				Message adminResponseMessage = null;
				if (tBotConfig.isAdmin(senderId)) {
					adminResponseMessage = defaultModel.onMessageFromAdmin(tBotService, message);
				}
				Message commandResponseMessage = null;
				if ((adminResponseMessage == null) && (tBotConfig.isValidCommand(message.getText()))) {
					commandResponseMessage = defaultModel.onCommand(tBotService, message);
				}
				if ((adminResponseMessage == null) && (commandResponseMessage == null)) {
					defaultModel.onReceivingMessage(tBotService, message);
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
