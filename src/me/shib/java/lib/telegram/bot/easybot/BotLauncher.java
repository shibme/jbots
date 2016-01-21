package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.rest.client.ServiceAdapter;
import me.shib.java.lib.rest.client.ServiceResponse;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class BotLauncher {

    public static void launchBots(BotConfig[] configList) {
        ArrayList<TBotWorker> botWorkers = new ArrayList<>();
        if (configList != null) {
            for (BotConfig conf : configList) {
                try {
                    Class<?> clazz = Class.forName(conf.getBotModelClassName());
                    Constructor<?> ctor = null;
                    if (clazz != null) {
                        ctor = clazz.getConstructor();
                    }
                    if (ctor != null) {
                        int threadCount = conf.getThreadCount();
                        TBotWorker[] workers = new TBotWorker[threadCount];
                        for (int i = 0; i < threadCount; i++) {
                            Object object = ctor.newInstance();
                            workers[i] = new TBotWorker((BotModel) object);
                            botWorkers.add(workers[i]);
                            workers[i].start();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
                BotConfig.addJSONtoConfig(configJson);
            }
            BotConfig[] configList = BotConfig.getAllConfigList();
            launchBots(configList);
        } else {
            System.out.println("Please provide valid arguments.");
        }
    }
}