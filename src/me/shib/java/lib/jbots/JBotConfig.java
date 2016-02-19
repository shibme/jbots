package me.shib.java.lib.jbots;

import me.shib.java.lib.common.utils.JsonLib;
import me.shib.java.lib.jbotstats.BotStatsConfig;
import me.shib.java.lib.jtelebot.service.TelegramBot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class JBotConfig {

    private static Logger logger = Logger.getLogger(JBotConfig.class.getName());
    private static JsonLib jsonLib = new JsonLib();
    private static Map<String, JBotConfig> configMap;

    private String botModelClassName;
    private String botApiToken;
    private String[] commandList;
    private int threadCount;
    private long[] adminIdList;
    private boolean defaultWorkerDisabled;
    private long reportIntervalInSeconds;
    private BotStatsConfig botStatsConfig;
    private Map<String, String> constants;
    private Set<String> userModeSet;

    public JBotConfig(String botApiToken, Class<JBot> botModelClass) {
        this.botModelClassName = botModelClass.getName();
        this.botApiToken = botApiToken;
        this.adminIdList = null;
        this.botStatsConfig = null;
        this.defaultWorkerDisabled = false;
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

    public static void addConfigToList(JBotConfig config) {
        if (configMap == null) {
            configMap = new HashMap<>();
        }
        configMap.put(config.getBotApiToken(), config);
    }

    public static synchronized void addJSONtoConfigList(String json) {
        if (json != null) {
            JBotConfig[] configArray = jsonLib.fromJson(json, JBotConfig[].class);
            if (configArray != null) {
                for (JBotConfig configItem : configArray) {
                    if ((configItem.getBotApiToken() != null)
                            && (!configItem.getBotApiToken().isEmpty())
                            && isValidClassName(configItem.getBotModelClassName())) {
                        configItem.initDefaults();
                        TelegramBot bot = BotProvider.getInstance(configItem);
                        if (bot != null) {
                            addConfigToList(configItem);
                        } else {
                            logger.log(Level.WARNING, "The bot with API token, \"" + configItem.getBotApiToken() + "\" doesn't seem to work.");
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
                e.printStackTrace();
            }
        }
    }

    public void setCommandList(String[] commandList) {
        this.commandList = commandList;
    }

    public boolean isDefaultWorkerDisabled() {
        return defaultWorkerDisabled;
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
    }

    public String getConstant(String key) {
        return constants.get(key);
    }

    public String getBotApiToken() {
        return botApiToken;
    }

    public boolean isValidCommand(String messageText) {
        if ((messageText != null) && (commandList != null)) {
            String[] words = messageText.split("\\s+");
            if (words.length > 0) {
                String possibleCommand = words[0];
                for (String command : commandList) {
                    if (command.equals(possibleCommand)) {
                        return true;
                    }
                }
            }
        }
        return false;
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

    public String getBotModelClassName() {
        return botModelClassName;
    }

}
