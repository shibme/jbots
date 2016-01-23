package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.common.utils.JsonLib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BotConfig {

    private static final File defaultConfigFile = new File("easy-tbot-config.json");
    private static final String[] defaultCommands = {"/start", "/status", "/scr", "/usermode", "/adminmode"};

    private static Map<String, BotConfig> configMap;

    private String botModelClassName;
    private String botApiToken;
    private String[] commandList;
    private int threadCount;
    private long[] adminIdList;
    private long reportIntervalInSeconds;
    private Map<String, String> constants;
    private Set<String> userModeSet;

    public BotConfig(String botApiToken, Class<BotModel> botModelClass) {
        this.botModelClassName = botModelClass.getName();
        this.botApiToken = botApiToken;
        adminIdList = null;
        initDefaults();
    }

    private static boolean isValidClassName(String className) {
        try {
            if ((className != null) && (!className.isEmpty())) {
                Class.forName(className);
                return true;
            }
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    public static synchronized BotConfig[] getAllConfigList() {
        if (configMap == null) {
            addFileToConfigList(defaultConfigFile);
        }
        if (configMap == null) {
            return null;
        }
        ArrayList<BotConfig> configList = new ArrayList<>(configMap.values());
        BotConfig[] configArray = new BotConfig[configList.size()];
        return configList.toArray(configArray);
    }

    public static void addConfigToList(BotConfig config) {
        if (configMap == null) {
            configMap = new HashMap<>();
        }
        configMap.put(config.getBotApiToken(), config);
    }

    public static synchronized void addJSONtoConfigList(String json) {
        if (json != null) {
            BotConfig[] configArray = JsonLib.getDefaultInstance().fromJson(json, BotConfig[].class);
            if (configArray != null) {
                for (BotConfig configItem : configArray) {
                    if ((configItem.getBotApiToken() != null)
                            && (!configItem.getBotApiToken().isEmpty())
                            && isValidClassName(configItem.getBotModelClassName())) {
                        configItem.initDefaults();
                        addConfigToList(configItem);
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
                e.printStackTrace();
            }
        }
    }

    private boolean doesStringExistInList(String str, ArrayList<String> list) {
        for (String item : list) {
            if (item.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private void initDefaults() {
        this.userModeSet = new HashSet<>();
        if (this.commandList == null) {
            this.commandList = defaultCommands;
        } else {
            ArrayList<String> newCommandList = new ArrayList<>();
            for (String command : this.commandList) {
                if (!doesStringExistInList(command, newCommandList)) {
                    newCommandList.add(command);
                }
            }
            if (defaultCommands != null) {
                for (String command : defaultCommands) {
                    if (!doesStringExistInList(command, newCommandList)) {
                        newCommandList.add(command);
                    }
                }
            }
            this.commandList = new String[newCommandList.size()];
            this.commandList = newCommandList.toArray(this.commandList);
        }
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
