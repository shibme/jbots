package me.shib.java.lib.jbots;

import me.shib.java.lib.rest.client.ServiceAdapter;
import me.shib.java.lib.rest.client.ServiceResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class JBotLauncher {

    private static final Logger logger = Logger.getLogger(JBotLauncher.class.getName());

    private static void launchBots(JBotConfig[] configList) {
        List<JBot> jBots = new ArrayList<>();
        if (configList != null) {
            for (JBotConfig conf : configList) {
                jBots.add(JBot.startSweeper(conf));
                int threadCount = conf.getThreadCount();
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
        if (args.length <= 1) {
            if (args.length == 1) {
                File possibleConfigFile = new File(args[0]);
                if (possibleConfigFile.exists()) {
                    JBotConfig.addFileToConfigList(possibleConfigFile);
                } else {
                    String configJson = null;
                    try {
                        ServiceAdapter adapter = new ServiceAdapter(args[0]);
                        ServiceResponse response = adapter.get(null);
                        if (response.getStatusCode() == 200) {
                            configJson = response.getResponse();
                        }
                    } catch (IOException e) {
                        logger.throwing(JBotLauncher.class.getName(), "sendStatusMessage", e);
                    }
                    JBotConfig.addJSONtoConfigList(configJson);
                }
            } else {
                JBotConfig.addFileToConfigList(new File("jbots-config.json"));
            }
            JBotConfig[] configList = JBotConfig.getAllConfigList();
            launchBots(configList);
        } else {
            logger.log(Level.SEVERE, "Please provide valid arguments");
        }
    }
}