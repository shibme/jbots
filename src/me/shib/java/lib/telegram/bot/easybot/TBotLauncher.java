package me.shib.java.lib.telegram.bot.easybot;

public interface TBotLauncher {
	public TBotWorker[] launchBot(TBotConfig tBotConfig, int threadCount);
}
