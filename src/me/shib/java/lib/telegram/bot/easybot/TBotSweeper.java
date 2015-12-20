package me.shib.java.lib.telegram.bot.easybot;

import java.util.HashMap;
import java.util.Map;

import me.shib.java.lib.telegram.bot.service.TelegramBot;

public class TBotSweeper extends Thread {

    private static Map<String, TBotSweeper> tBotSweeperMap;

    private TBotModel defaultModel;
    private TBotConfig tBotConfig;
    private TelegramBot sweeperTelegramBot;

    private TBotSweeper(TBotModel defaultModel, TBotConfig tBotConfig, TelegramBot tBot) {
        this.tBotConfig = tBotConfig;
        this.defaultModel = defaultModel;
        this.sweeperTelegramBot = tBot;
    }

    private static synchronized TBotSweeper getDefaultInstance(TBotModel defaultModel, TBotConfig tBotConfig, TelegramBot tBot) {
        String botApiToken = tBot.getBotApiToken();
        if (botApiToken == null) {
            return null;
        }
        if (tBotSweeperMap == null) {
            tBotSweeperMap = new HashMap<>();
        }
        TBotSweeper tBotSwp = tBotSweeperMap.get(botApiToken);
        if (tBotSwp == null) {
            tBotSwp = new TBotSweeper(defaultModel, tBotConfig, tBot);
            tBotSweeperMap.put(botApiToken, tBotSwp);
        }
        return tBotSwp;
    }

    protected static synchronized void startDefaultInstance(TBotModel defaultModel, TBotConfig tBotConfig, TelegramBot tBot) {
        TBotSweeper defaultSweeper = TBotSweeper.getDefaultInstance(defaultModel, tBotConfig, tBot);
        if ((!defaultSweeper.isAlive()) && (defaultSweeper.getState() != State.TERMINATED)) {
            defaultSweeper.start();
        }
    }

    private void sendStatusUpdatesOnIntervals() {
        long intervals = this.tBotConfig.getReportIntervalInSeconds() * 1000;
        long[] adminIdList = this.tBotConfig.getAdminIdList();
        if ((intervals > 0) && (adminIdList != null) && (adminIdList.length > 0)) {
            while (true) {
                try {
                    for (long admin : adminIdList) {
                        this.defaultModel.sendStatusMessage(sweeperTelegramBot, admin);
                    }
                    Thread.sleep(intervals);
                } catch (Exception e) {
                }
            }
        }
    }

    public void run() {
        sendStatusUpdatesOnIntervals();
    }

}
