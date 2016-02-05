package me.shib.java.lib.jbots;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class JBotSweeper extends Thread {

    private static Map<String, JBotSweeper> tBotSweeperMap;
    private static Logger logger = Logger.getLogger(JBotSweeper.class.getName());

    private JBotDefaultModel defaultModel;
    private JBotConfig jBotConfig;

    private JBotSweeper(JBotDefaultModel defaultModel) {
        this.jBotConfig = defaultModel.getConfig();
        this.defaultModel = defaultModel;
    }

    private static synchronized JBotSweeper getDefaultInstance(JBotDefaultModel defaultModel) {
        String botApiToken = defaultModel.getConfig().getBotApiToken();
        if (botApiToken == null) {
            return null;
        }
        if (tBotSweeperMap == null) {
            tBotSweeperMap = new HashMap<>();
        }
        JBotSweeper tBotSwp = tBotSweeperMap.get(botApiToken);
        if (tBotSwp == null) {
            tBotSwp = new JBotSweeper(defaultModel);
            tBotSweeperMap.put(botApiToken, tBotSwp);
        }
        return tBotSwp;
    }

    protected static synchronized void startDefaultInstance(JBotDefaultModel defaultModel) {
        JBotSweeper defaultSweeper = JBotSweeper.getDefaultInstance(defaultModel);
        if ((!defaultSweeper.isAlive()) && (defaultSweeper.getState() != State.TERMINATED)) {
            defaultSweeper.start();
        }
    }

    private void sendStatusUpdatesOnIntervals() {
        long intervals = this.jBotConfig.getReportIntervalInSeconds() * 1000;
        long[] adminIdList = this.jBotConfig.getAdminIdList();
        if ((intervals > 0) && (adminIdList != null) && (adminIdList.length > 0)) {
            while (true) {
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

    public void run() {
        sendStatusUpdatesOnIntervals();
    }

}
