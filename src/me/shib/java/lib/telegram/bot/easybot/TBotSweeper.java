package me.shib.java.lib.telegram.bot.easybot;

import java.util.HashMap;
import java.util.Map;

import me.shib.java.lib.telegram.bot.service.TelegramBotService;

public class TBotSweeper extends Thread {
	
	private static Map<String, TBotSweeper> tBotSweeperMap;
	
	private TBotModel defaultModel;
	private TBotConfig tBotConfig;
	private TelegramBotService sweeperTelegramBotService;
	
	private TBotSweeper(TBotModel defaultModel, TBotConfig tBotConfig) {
		this.tBotConfig = tBotConfig;
		this.defaultModel = defaultModel;
		this.sweeperTelegramBotService = UpdateReceiver.getDefaultInstance(tBotConfig.getBotApiToken()).getTelegramBotService();
	}
	
	private static synchronized TBotSweeper getDefaultInstance(TBotModel defaultModel, TBotConfig tBotConfig) {
		String botApiToken = tBotConfig.getBotApiToken();
		if(botApiToken == null) {
			return null;
		}
		if(tBotSweeperMap == null) {
			tBotSweeperMap = new HashMap<String, TBotSweeper>();
		}
		TBotSweeper tBotSwp = tBotSweeperMap.get(botApiToken);
		if(tBotSwp == null) {
			tBotSwp = new TBotSweeper(defaultModel, tBotConfig);
			tBotSweeperMap.put(botApiToken, tBotSwp);
		}
		return tBotSwp;
	}
	
	protected static synchronized void startDefaultInstance(TBotModel defaultModel, TBotConfig tBotConfig) {
		TBotSweeper defaultSweeper = TBotSweeper.getDefaultInstance(defaultModel, tBotConfig);
		if((!defaultSweeper.isAlive()) && (defaultSweeper.getState() != State.TERMINATED)) {
			defaultSweeper.start();
		}
	}
	
	private void sendStatusUpdatesOnIntervals() {
		long intervals = this.tBotConfig.getReportIntervalInSeconds() * 1000;
		long[] adminIdList = this.tBotConfig.getAdminIdList();
		if((intervals > 0) && (adminIdList != null) && (adminIdList.length > 0)) {
			while(true) {
				try {
					for(long admin : adminIdList) {
						this.defaultModel.sendStatusMessage(sweeperTelegramBotService, admin);
					}
					Thread.sleep(intervals);
				} catch (Exception e) {}
			}
		}
	}
	
	public void run() {
		sendStatusUpdatesOnIntervals();
	}
	
}
