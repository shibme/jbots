package me.shib.java.lib.telegram.bot.easybot;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class BotLauncher {

    public static void launchBots(BotConfig[] configList) {
        ArrayList<TBotWorker> botWorkers = new ArrayList<TBotWorker>();
        if (configList != null) {
            for (BotConfig conf : configList) {
                try {
                    Class<?> clazz = Class.forName(conf.getBotModelclassName());
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
        BotConfig[] configList = BotConfig.getFileConfigList();
        launchBots(configList);
    }
}