package me.shib.java.lib.jbots;

import me.shib.java.lib.rest.client.ServiceAdapter;
import me.shib.java.lib.rest.client.ServiceResponse;

import java.io.IOException;

public class JBotLauncher {

    public static void launchBots(JBotConfig[] configList) {
        if (configList != null) {
            for (JBotConfig conf : configList) {
                int threadCount = conf.getThreadCount();
                JBotWorker[] workers = new JBotWorker[threadCount];
                for (int i = 0; i < threadCount; i++) {
                    workers[i] = new JBotWorker(conf);
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
                } catch (IOException ignored) {
                }
                JBotConfig.addJSONtoConfigList(configJson);
            }
            JBotConfig[] configList = JBotConfig.getAllConfigList();
            launchBots(configList);
        } else {
            System.out.println("Please provide valid arguments.");
        }
    }
}