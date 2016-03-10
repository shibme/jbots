package me.shib.java.lib.jbots;

import me.shib.java.lib.rest.client.ServiceAdapter;
import me.shib.java.lib.rest.client.ServiceResponse;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class JBotLauncher {

    private static final Logger logger = Logger.getLogger(JBotLauncher.class.getName());

    public static void launchBots(JBotConfig[] configList) {
        if (configList != null) {
            for (JBotConfig conf : configList) {
                JBot sweeperBot = new DefaultJBot(conf);
                sweeperBot.setAsSweeperThread();
                sweeperBot.start();
                while (!sweeperBot.isAlive()) {
                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException e) {
                        logger.throwing(JBotLauncher.class.getName(), "launchBots", e);
                    }
                }
                int threadCount = conf.getThreadCount();
                JBot[] bots = new DefaultJBot[threadCount];
                for (int i = 0; i < threadCount; i++) {
                    bots[i] = new DefaultJBot(conf);
                    bots[i].start();
                }
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