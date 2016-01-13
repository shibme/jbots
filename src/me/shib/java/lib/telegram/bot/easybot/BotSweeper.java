package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.telegram.bot.service.TelegramBot;

import java.util.HashMap;
import java.util.Map;

public class BotSweeper extends Thread {

    private static Map<String, BotSweeper> tBotSweeperMap;

    private BotModel defaultModel;
    private BotConfig botConfig;
    private TelegramBot sweeperTelegramBot;

    private BotSweeper(BotModel defaultModel) {
        this.botConfig = defaultModel.getBotConfig();
        this.defaultModel = defaultModel;
        this.sweeperTelegramBot = TelegramBot.getInstance(this.botConfig.getBotApiToken());
    }

    private static synchronized BotSweeper getDefaultInstance(BotModel defaultModel) {
        String botApiToken = defaultModel.getBotConfig().getBotApiToken();
        if (botApiToken == null) {
            return null;
        }
        if (tBotSweeperMap == null) {
            tBotSweeperMap = new HashMap<>();
        }
        BotSweeper tBotSwp = tBotSweeperMap.get(botApiToken);
        if (tBotSwp == null) {
            tBotSwp = new BotSweeper(defaultModel);
            tBotSweeperMap.put(botApiToken, tBotSwp);
        }
        return tBotSwp;
    }

    protected static synchronized void startDefaultInstance(BotModel defaultModel) {
        BotSweeper defaultSweeper = BotSweeper.getDefaultInstance(defaultModel);
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
