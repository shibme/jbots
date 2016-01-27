package me.shib.java.lib.jbots;

import me.shib.java.lib.jtelebot.service.TelegramBot;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class JBots {

    private static final String botanProxyClass = "me.shib.java.lib.jbotan.JBotan";
    private static final String jBotAnalyticsClass = "me.shib.java.lib.jbotstats.JBotStats";

    public static Map<String, TelegramBot> jBotsMap;

    private static synchronized boolean isProxyModelInClassPath(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static synchronized TelegramBot getInstance(JBotConfig config) {
        String botApiToken = config.getBotApiToken();
        if ((botApiToken == null) || (botApiToken.isEmpty())) {
            return null;
        }
        if (jBotsMap == null) {
            jBotsMap = new HashMap<>();
        }
        TelegramBot bot = jBotsMap.get(botApiToken);
        if (bot == null) {
            if (config.isAnalyticsEnabled()) {
                String botanProxyToken = config.getBotanProxyToken();
                try {
                    if ((botanProxyToken != null) && isProxyModelInClassPath(botanProxyToken)) {
                        Class<?> clazz = Class.forName(config.getBotModelClassName());
                        Constructor<?> ctor = clazz.getConstructor(String.class, String.class);
                        bot = (TelegramBot) ctor.newInstance(botApiToken, botanProxyToken);
                    } else {
                        Class<?> clazz = Class.forName(config.getBotModelClassName());
                        Constructor<?> ctor = clazz.getConstructor(String.class, String.class);
                        bot = (TelegramBot) ctor.newInstance(botApiToken, botanProxyToken);
                    }
                } catch (Exception ignored) {
                }
            }
            if (bot == null) {
                bot = TelegramBot.getInstance(botApiToken);
            }
            if (bot != null) {
                jBotsMap.put(botApiToken, bot);
            }
        }
        return bot;
    }

}
