package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.telegram.bot.service.TelegramBot;

import java.util.HashMap;
import java.util.Map;

public class TBotSweeper extends Thread {

    private static Map<String, TBotSweeper> tBotSweeperMap;

    private BotModel defaultModel;
    private BotConfig botConfig;
    private TelegramBot sweeperTelegramBot;

    private TBotSweeper(BotModel defaultModel) {
        this.botConfig = defaultModel.thisConfig();
        this.defaultModel = defaultModel;
        this.sweeperTelegramBot = TelegramBot.getInstance(this.botConfig.getBotApiToken());
    }

    private static synchronized TBotSweeper getDefaultInstance(BotModel defaultModel) {
        String botApiToken = defaultModel.thisConfig().getBotApiToken();
        if (botApiToken == null) {
            return null;
        }
        if (tBotSweeperMap == null) {
            tBotSweeperMap = new HashMap<>();
        }
        TBotSweeper tBotSwp = tBotSweeperMap.get(botApiToken);
        if (tBotSwp == null) {
            tBotSwp = new TBotSweeper(defaultModel);
            tBotSweeperMap.put(botApiToken, tBotSwp);
        }
        return tBotSwp;
    }

    protected static synchronized void startDefaultInstance(BotModel defaultModel) {
        TBotSweeper defaultSweeper = TBotSweeper.getDefaultInstance(defaultModel);
        if ((!defaultSweeper.isAlive()) && (defaultSweeper.getState() != State.TERMINATED)) {
            defaultSweeper.start();
        }
    }

    private void sendStatusUpdatesOnIntervals() {
        long intervals = this.botConfig.getReportIntervalInSeconds() * 1000;
        long[] adminIdList = this.botConfig.getAdminIdList();
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
