package me.shib.java.lib.jbots;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class BotSweeper extends Thread {

    private static final Logger logger = Logger.getLogger(BotSweeper.class.getName());
    private static final Map<String, BotSweeper> tBotSweeperMap = new HashMap<>();

    private DefaultJBot defaultModel;
    private JBotConfig jBotConfig;
    private boolean enabled;

    private BotSweeper(DefaultJBot defaultModel) {
        this.enabled = true;
        this.jBotConfig = defaultModel.getConfig();
        this.defaultModel = defaultModel;
    }

    private static synchronized BotSweeper getDefaultInstance(DefaultJBot defaultModel) {
        String botApiToken = defaultModel.getConfig().getBotApiToken();
        if (botApiToken == null) {
            return null;
        }
        BotSweeper botSweeper = tBotSweeperMap.get(botApiToken);
        if (botSweeper == null) {
            botSweeper = new BotSweeper(defaultModel);
            tBotSweeperMap.put(botApiToken, botSweeper);
        }
        return botSweeper;
    }

    protected static synchronized void startDefaultInstance(DefaultJBot defaultModel) {
        BotSweeper defaultSweeper = BotSweeper.getDefaultInstance(defaultModel);
        if ((defaultSweeper != null) && (!defaultSweeper.isAlive()) && (defaultSweeper.getState() != State.TERMINATED)) {
            defaultSweeper.start();
        }
    }

    private void sendStatusUpdatesOnIntervals() {
        long intervals = this.jBotConfig.getReportIntervalInSeconds() * 1000;
        long[] adminIdList = this.jBotConfig.getAdminIdList();
        if ((intervals > 0) && (adminIdList != null) && (adminIdList.length > 0)) {
            while (enabled) {
                try {
                    for (long admin : adminIdList) {
                        this.defaultModel.sendStatusMessage(admin);
                    }
                    Thread.sleep(intervals);
                } catch (Exception e) {
                    logger.throwing(this.getClass().getName(), "sendStatusUpdatesOnIntervals", e);
                }
            }
        }
    }

    public void stopSweeper() {
        enabled = false;
    }

    public void run() {
        sendStatusUpdatesOnIntervals();
    }

}
