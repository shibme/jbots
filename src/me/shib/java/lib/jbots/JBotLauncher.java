package me.shib.java.lib.jbots;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class JBotLauncher {

    private static final Logger logger = Logger.getLogger(JBotLauncher.class.getName());

    private static void launchBots(JBotConfig[] configList) {
        List<JBot> jBots = new ArrayList<>();
        if (configList != null) {
            for (JBotConfig conf : configList) {
                jBots.add(JBot.startSweeper(conf));
                int threadCount = conf.threadCount();
                for (int i = 0; i < threadCount; i++) {
                    jBots.add(JBot.startNewJBot(conf));
                }
            }
        }
        for (JBot jBot : jBots) {
            try {
                jBot.join();
            } catch (InterruptedException e) {
                logger.throwing(JBotLauncher.class.getName(), "launchBots", e);
            }
        }
    }

    public static void main(String[] args) {
        JBotConfig[] configList = JBotConfig.getAllConfigList();
        launchBots(configList);
    }
}