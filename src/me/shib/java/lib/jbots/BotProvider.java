package me.shib.java.lib.jbots;

import me.shib.java.lib.jbotstats.BotStatsConfig;
import me.shib.java.lib.jbotstats.JBotStats;
import me.shib.java.lib.jtelebot.service.TelegramBot;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class BotProvider {

    private static Logger logger = Logger.getLogger(BotProvider.class.getName());
    private static Map<String, TelegramBot> jBotsMap;

    protected static synchronized TelegramBot getInstance(JBotConfig config) {
        String botApiToken = config.getBotApiToken();
        if ((botApiToken == null) || (botApiToken.isEmpty())) {
            return null;
        }
        if (jBotsMap == null) {
            jBotsMap = new HashMap<>();
        }
        TelegramBot bot = jBotsMap.get(botApiToken);
        if (bot == null) {
            TelegramBot botService = TelegramBot.getInstance(botApiToken);
            if (botService != null) {
                JBotStats jBotStats = null;
                BotStatsConfig botStatsConfig = config.getBotStatsConfig();
                if (botStatsConfig != null) {
                    try {
                        Class<?> clazz = Class.forName(botStatsConfig.getBotStatsClassName());
                        Constructor<?> ctor = clazz.getConstructor(BotStatsConfig.class);
                        jBotStats = (JBotStats) ctor.newInstance(botStatsConfig);
                    } catch (Exception e) {
                        logger.throwing(BotProvider.class.getName(), "getInstance", e);
                    }
                }
                if (jBotStats != null) {
                    bot = new AnalyticsBot(jBotStats, botService);
                } else {
                    bot = botService;
                }
                jBotsMap.put(botApiToken, bot);
            }
        }
        return bot;
    }

}
