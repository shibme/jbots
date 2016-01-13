package me.shib.java.lib.telegram.bot.run;

import me.shib.java.lib.telegram.bot.easybot.BotModel;
import me.shib.java.lib.telegram.bot.easybot.BotConfig;
import me.shib.java.lib.telegram.bot.easybot.BotWorker;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class LaunchBots {

    public static void launchBots(BotConfig[] configList) {
        ArrayList<BotWorker> botWorkers = new ArrayList<BotWorker>();
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
                        BotWorker[] workers = new BotWorker[threadCount];
                        for (int i = 0; i < threadCount; i++) {
                            Object object = ctor.newInstance();
                            workers[i] = new BotWorker((BotModel) object);
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