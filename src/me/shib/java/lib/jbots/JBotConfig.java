package me.shib.java.lib.jbots;

import me.shib.java.lib.jbotstats.AnalyticsBot;
import me.shib.java.lib.jbotstats.BotStatsConfig;
import me.shib.java.lib.jbotstats.JBotStats;
import me.shib.java.lib.jtelebot.models.types.User;
import me.shib.java.lib.jtelebot.service.TelegramBot;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.Logger;

public abstract class JBotConfig {

    private static final Logger logger = Logger.getLogger(JBotConfig.class.getName());
    private static final Map<String, JBotConfig> configMap = new HashMap<>();
    private static final Reflections reflections = new Reflections("");

    private static Class[] getAllSubTypes() {
        Set<Class<? extends JBotConfig>> subTypes = reflections.getSubTypesOf(JBotConfig.class);
        return subTypes.toArray(new Class[subTypes.size()]);
    }

    private Set<String> userModeSet;
    private TelegramBot bot;

    public abstract String botApiToken();

    public abstract Class<JBot> botModelClass();

    public int threadCount() {
        return 1;
    }

    public int reportInterval() {
        return 86400;
    }

    public int minimumAllowedRating() {
        return 1;
    }

    public boolean handleMissedChats() {
        return true;
    }

    public boolean defaultWorker() {
        return true;
    }

    protected long[] admins() {
        return new long[0];
    }

    protected HashMap<String, String> constants() {
        return new HashMap<>();
    }

    public BotStatsConfig botStatsConfig() {
        return null;
    }

    public JBotConfig() {
        this.userModeSet = new HashSet<>();
        if ((botApiToken() != null) && (!botApiToken().isEmpty())) {
            TelegramBot botService = TelegramBot.getInstance(botApiToken());
            if (botService != null) {
                JBotStats jBotStats = null;
                if (botStatsConfig() != null) {
                    try {
                        Class<?> clazz = Class.forName(botStatsConfig().getBotStatsClass().getName());
                        Constructor<?> ctor = clazz.getConstructor(BotStatsConfig.class, User.class);
                        jBotStats = (JBotStats) ctor.newInstance(botStatsConfig(), botService.getIdentity());
                    } catch (Exception e) {
                        logger.throwing(this.getClass().getName(), "JBotConfig", e);
                    }
                }
                if (jBotStats != null) {
                    bot = new AnalyticsBot(botService, jBotStats);
                } else {
                    bot = botService;
                }
            }
        }
    }

    public static synchronized JBotConfig[] getAllConfigList() {
        Class[] configClasses = getAllSubTypes();
        for (Class configClass : configClasses) {
            try {
                Class<?> clazz = Class.forName(configClass.getName());
                Constructor<?> ctor = clazz.getConstructor(JBotConfig.class);
                JBotConfig config = (JBotConfig) ctor.newInstance();
                if (configMap.get(config.botApiToken()) == null) {
                    configMap.put(config.botApiToken(), config);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ArrayList<JBotConfig> configList = new ArrayList<>(configMap.values());
        JBotConfig[] configArray = new JBotConfig[configList.size()];
        return configList.toArray(configArray);
    }

    public TelegramBot getBot() {
        return bot;
    }

    public String getConstant(String key) {
        return constants().get(key);
    }

    protected boolean isUserMode(long userId) {
        return userModeSet.contains(userId + "");
    }

    protected void setAdminMode(long userId) {
        userModeSet.remove(userId + "");
    }

    protected void setUserMode(long userId) {
        userModeSet.add(userId + "");
    }

    public boolean isAdmin(long senderId) {
        if (admins() != null) {
            for (long adminId : admins()) {
                if (senderId == adminId) {
                    return true;
                }
            }
        }
        return false;
    }

}
