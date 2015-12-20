package me.shib.java.lib.telegram.bot.run;

import me.shib.java.lib.telegram.bot.easybot.TBotConfig;
import me.shib.java.lib.telegram.bot.easybot.TBotWorker;

public interface TBotLauncherModel {
    public TBotWorker[] launchBot(TBotConfig tBotConfig);
}
