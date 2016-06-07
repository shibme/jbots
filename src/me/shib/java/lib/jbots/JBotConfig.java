package me.shib.java.lib.jbots;

import me.shib.java.lib.jbotstats.BotStatsConfig;
import me.shib.java.lib.jbotstats.JBotStats;
import me.shib.java.lib.jtelebot.models.types.User;
import me.shib.java.lib.jtelebot.service.TelegramBot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class JBotConfig {

    private static final Logger logger = Logger.getLogger(JBotConfig.class.getName());
    private static final JsonLib jsonLib = new JsonLib();
    private static final Map<String, JBotConfig> configMap = new HashMap<>();

    private String botModelClassName;
    private String botApiToken;
    private int threadCount;
    private int minRatingAllowed;
    private long[] adminIdList;
    private boolean defaultWorkerDisabled;
    private boolean missedChatHandlingDisabled;
    private long reportIntervalInSeconds;
    private BotStatsConfig botStatsConfig;
    private Map<String, String> constants;
    private Set<String> userModeSet;
    private TelegramBot bot;

    public JBotConfig(String botApiToken, Class<JBot> botModelClass) {
        this.botModelClassName = botModelClass.getName();
        this.botApiToken = botApiToken;
        this.adminIdList = null;
        this.botStatsConfig = null;
        this.defaultWorkerDisabled = false;
        this.missedChatHandlingDisabled = false;
        this.minRatingAllowed = 0;
        initDefaults();
    }

    private static boolean isValidClassName(String className) {
        if (className == null) {
            return true;
        }
        try {
            if (!className.isEmpty()) {
                Class.forName(className);
                return true;
            }
        } catch (ClassNotFoundException e) {
            logger.throwing(JBotConfig.class.getName(), "isValidClassName", e);
        }
        return false;
    }

    public static synchronized JBotConfig[] getAllConfigList() {
        if (configMap == null) {
            return null;
        }
        ArrayList<JBotConfig> configList = new ArrayList<>(configMap.values());
        JBotConfig[] configArray = new JBotConfig[configList.size()];
        return configList.toArray(configArray);
    }

    public static synchronized void addJSONtoConfigList(String json) {
        if (json != null) {
            JBotConfig[] configArray = jsonLib.fromJson(json, JBotConfig[].class);
            if (configArray != null) {
                for (JBotConfig configItem : configArray) {
                    if ((configItem.getBotApiToken() != null)
                            && (!configItem.getBotApiToken().isEmpty())) {
                        if ((configMap.get(configItem.getBotApiToken()) == null)
                                && isValidClassName(configItem.getBotModelClassName())) {
                            configItem.initDefaults();
                            if (configItem.bot != null) {
                                configMap.put(configItem.getBotApiToken(), configItem);
                            } else {
                                logger.log(Level.WARNING, "The bot with API token, \"" + configItem.getBotApiToken() + "\" doesn't seem to work.");
                            }
                        }
                    }
                }
            }
        }
    }

    public static synchronized void addFileToConfigList(File configFile) {
        if ((configFile != null) && configFile.exists()) {
            try {
                StringBuilder jsonBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(configFile));
                String line = br.readLine();
                while (line != null) {
                    jsonBuilder.append(line);
                    line = br.readLine();
                    if (line != null) {
                        jsonBuilder.append("\n");
                    }
                }
                br.close();
                addJSONtoConfigList(jsonBuilder.toString());
            } catch (IOException e) {
                logger.throwing(JBotConfig.class.getName(), "addFileToConfigList", e);
            }
        }
    }

    protected boolean isDefaultWorkerDisabled() {
        return defaultWorkerDisabled;
    }

    protected boolean isMissedChatHandlingDisabled() {
        return missedChatHandlingDisabled;
    }

    private boolean doesStringExistInList(String str, ArrayList<String> list) {
        for (String item : list) {
            if (item.equals(str)) {
                return true;
            }
        }
        return false;
    }

    public BotStatsConfig getBotStatsConfig() {
        return this.botStatsConfig;
    }

    public TelegramBot getBot() {
        return bot;
    }

    protected synchronized void initBot() {
        if ((botApiToken != null) && (!botApiToken.isEmpty())) {
            TelegramBot botService = TelegramBot.getInstance(botApiToken);
            if (botService != null) {
                JBotStats jBotStats = null;
                if (botStatsConfig != null) {
                    try {
                        Class<?> clazz = Class.forName(botStatsConfig.getBotStatsClassName());
                        Constructor<?> ctor = clazz.getConstructor(BotStatsConfig.class, User.class);
                        jBotStats = (JBotStats) ctor.newInstance(botStatsConfig, botService.getIdentity());
                    } catch (Exception e) {
                        logger.throwing(this.getClass().getName(), "initBot", e);
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

    private void initDefaults() {
        this.userModeSet = new HashSet<>();
        if (this.reportIntervalInSeconds < 0) {
            this.reportIntervalInSeconds = 0;
        }
        if (constants == null) {
            constants = new HashMap<>();
        }
        if (this.threadCount < 1) {
            this.threadCount = 1;
        }
        if ((this.minRatingAllowed < 1) || (this.minRatingAllowed > 4)) {
            this.minRatingAllowed = 5;
        }
        initBot();
    }

    public String getConstant(String key) {
        return constants.get(key);
    }

    public String getBotApiToken() {
        return botApiToken;
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

    public long[] getAdminIdList() {
        return adminIdList;
    }

    public boolean isAdmin(long senderId) {
        if (adminIdList != null) {
            for (long adminId : adminIdList) {
                if (senderId == adminId) {
                    return true;
                }
            }
        }
        return false;
    }

    public long getReportIntervalInSeconds() {
        return reportIntervalInSeconds;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public int getMinRatingAllowed() {
        return minRatingAllowed;
    }

    public String getBotModelClassName() {
        return botModelClassName;
    }

}
