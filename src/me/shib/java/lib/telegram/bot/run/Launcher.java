package me.shib.java.lib.telegram.bot.run;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import me.shib.java.lib.telegram.bot.easybot.TBotConfig;
import me.shib.java.lib.telegram.bot.easybot.TBotWorker;

public class Launcher {

    public static void launchBots(TBotConfig[] configList) {
        ArrayList<TBotWorker> botWorkers = new ArrayList<TBotWorker>();
        if (configList != null) {
            for (TBotConfig conf : configList) {
                try {
                    Class<?> clazz = Class.forName(conf.getBotLauncherclassName());
                    Constructor<?> ctor = clazz.getConstructor();
                    Object object = ctor.newInstance();
                    TBotLauncherModel botLauncher = (TBotLauncherModel) object;
                    TBotWorker[] workers = botLauncher.launchBot(conf);
                    if (workers != null) {
                        for (int i = 0; i < workers.length; i++) {
                            botWorkers.add(workers[i]);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        TBotConfig[] configList = TBotConfig.getFileConfigList();
        launchBots(configList);
    }
}