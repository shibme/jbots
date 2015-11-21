package me.shib.java.lib.telegram.bot.easybot;

import java.util.HashMap;
import java.util.Map;

import me.shib.java.lib.telegram.bot.service.TelegramBotService;
import me.shib.java.lib.telegram.bot.types.ChatId;

public class TBotSweeper extends Thread {
	
	private static Map<String, TBotSweeper> tBotSweeperMap;
	
	private TBotModel tBotModel;
	private TBotModel defaultModel;
	private TBotConfig tBotConfig;
	private TelegramBotService sweeperTelegramBotService;
	
	private TBotSweeper(TBotModel tBotModel, TBotConfig tBotConfig, TBotModel defaultModel) {
		this.tBotModel = tBotModel;
		this.tBotConfig = tBotConfig;
		this.defaultModel = defaultModel;
		this.sweeperTelegramBotService = UpdateReceiver.getDefaultInstance(tBotConfig.getBotApiToken()).getTelegramBotService();
	}
	
	private static synchronized TBotSweeper getDefaultInstance(TBotModel tBotModel, TBotConfig tBotConfig, TBotModel defaultModel) {
		String botApiToken = tBotConfig.getBotApiToken();
		if(botApiToken == null) {
			return null;
		}
		if(tBotSweeperMap == null) {
			tBotSweeperMap = new HashMap<String, TBotSweeper>();
		}
		TBotSweeper tBotSwp = tBotSweeperMap.get(botApiToken);
		if(tBotSwp == null) {
			tBotSwp = new TBotSweeper(tBotModel, tBotConfig, defaultModel);
			tBotSweeperMap.put(botApiToken, tBotSwp);
		}
		return tBotSwp;
	}
	
	protected static synchronized void startDefaultInstance(TBotModel tBotModel, TBotConfig tBotConfig, TBotModel defaultModel) {
		TBotSweeper defaultSweeper = TBotSweeper.getDefaultInstance(tBotModel, tBotConfig, defaultModel);
		if((!defaultSweeper.isAlive()) && (defaultSweeper.getState() != State.TERMINATED)) {
			defaultSweeper.start();
		}
	}
	
	private void shootStatusUpdatesOnIntervals() {
		long intervals = this.tBotConfig.getReportIntervalInSeconds() * 1000;
		long[] adminIdList = this.tBotConfig.getAdminIdList();
		if((intervals > 0) && (adminIdList != null) && (adminIdList.length > 0)) {
			while(true) {
				try {
					String statusMessage = this.tBotModel.getStatusMessage();
					if((statusMessage == null) && (defaultModel != null)) {
						statusMessage = this.defaultModel.getStatusMessage();
					}
					if(statusMessage != null) {
						for(long admin : adminIdList) {
							sweeperTelegramBotService.sendMessage(new ChatId(admin), statusMessage);
						}
					}
					Thread.sleep(intervals);
				} catch (Exception e) {}
			}
		}
	}
	
	public void run() {
		shootStatusUpdatesOnIntervals();
	}
	
}
