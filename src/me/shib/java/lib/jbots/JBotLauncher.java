package me.shib.java.lib.jbots;

import me.shib.java.lib.rest.client.ServiceAdapter;
import me.shib.java.lib.rest.client.ServiceResponse;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JBotLauncher {

    private static Logger logger = Logger.getLogger(JBotLauncher.class.getName());

    public static void launchBots(JBotConfig[] configList) {
        if (configList != null) {
            for (JBotConfig conf : configList) {
                int threadCount = conf.getThreadCount();
                BotWorker[] workers = new BotWorker[threadCount];
                for (int i = 0; i < threadCount; i++) {
                    workers[i] = new BotWorker(conf);
                    workers[i].start();
                }
            }
        }
    }

    public static void main(String[] args) {
        if (args.length <= 1) {
            if (args.length == 1) {
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