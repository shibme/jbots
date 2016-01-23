package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.rest.client.ServiceAdapter;
import me.shib.java.lib.rest.client.ServiceResponse;

import java.io.IOException;

public class BotLauncher {

    public static void launchBots(BotConfig[] configList) {
        if (configList != null) {
            for (BotConfig conf : configList) {
                int threadCount = conf.getThreadCount();
                TBotWorker[] workers = new TBotWorker[threadCount];
                for (int i = 0; i < threadCount; i++) {
                    workers[i] = new TBotWorker(conf);
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
                BotConfig.addJSONtoConfigList(configJson);
            }
            BotConfig[] configList = BotConfig.getAllConfigList();
            launchBots(configList);
        } else {
            System.out.println("Please provide valid arguments.");
        }
    }
}