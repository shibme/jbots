package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.common.utils.JsonLib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BotConfig {

    private static final String defaultConfigFilePath = "easy-tbot-config.json";
    private static final String[] defaultCommands = {"/start", "/status", "/scr"};

    private static Map<File, BotConfig[]> fileConfigListMap;

    private String botModelClassName;
    private String botApiToken;
    private String[] commandList;
    private int threadCount;
    private long[] adminIdList;
    private long reportIntervalInSeconds;
    private Map<String, String> constants;

    public BotConfig(String botModelClassName, String botApiToken, String[] commandList, long[] adminIdList,
                     long reportIntervalInSeconds, int threadCount) {
        initTBotConfig(botModelClassName, botApiToken, commandList, adminIdList, reportIntervalInSeconds,
                threadCount);
    }

    public BotConfig(String botModelClassName, String botApiToken, String[] commandList, long[] adminIdList,
                     long reportIntervalInSeconds) {
        initTBotConfig(botModelClassName, botApiToken, commandList, adminIdList, reportIntervalInSeconds, 0);
    }

    public BotConfig(String botModelClassName, String botApiToken, String[] commandList, long[] adminIdList) {
        initTBotConfig(botModelClassName, botApiToken, commandList, adminIdList, 0, 0);
    }

    public BotConfig(String botModelClassName, String botApiToken, String[] commandList) {
        initTBotConfig(botModelClassName, botApiToken, commandList, null, 0, 0);
    }

    public BotConfig(String botModelClassName, String botApiToken) {
        initTBotConfig(botModelClassName, botApiToken, null, null, 0, 0);
    }

    private static boolean isBotAlreadyInUse(ArrayList<BotConfig> fileConfigs, String botApiToken) {
        for (int i = 0; i < fileConfigs.size(); i++) {
            if (fileConfigs.get(i).getBotApiToken().equals(botApiToken)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isClassExistInClassPath(String className) {
        try {
            if ((className != null) && (!className.isEmpty())) {
                Class.forName(className);
                return true;
            }
        } catch (ClassNotFoundException e) {
        }
        return false;
    }

    public static synchronized BotConfig getConfigForClassName(String className) {
        return getConfigForClassName(new File(BotConfig.defaultConfigFilePath), className);
    }

    public static synchronized BotConfig[] reloadFileConfigList() {
        return reloadFileConfigList(new File(BotConfig.defaultConfigFilePath));
    }

    public static synchronized BotConfig[] getFileConfigList() {
        return getFileConfigList(new File(BotConfig.defaultConfigFilePath));
    }

    private static synchronized BotConfig getConfigForClassName(File configFile, String className) {
        BotConfig[] configList = getFileConfigList(configFile);
        if (configList != null) {
            for (BotConfig config : configList) {
                if (config.getBotModelClassName().equals(className)) {
                    return config;
                }
            }
        }
        return null;
    }

    private static synchronized BotConfig[] reloadFileConfigList(File configFile) {
        fileConfigListMap.put(configFile, null);
        return getFileConfigList(configFile);
    }

    private static synchronized BotConfig[] getFileConfigList(File configFile) {
        if (fileConfigListMap == null) {
            fileConfigListMap = new HashMap<>();
        }
        BotConfig[] fileConfigArray = fileConfigListMap.get(configFile);
        if (configFile.exists()) {
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
                fileConfigArray = JsonLib.getDefaultInstance().fromJson(jsonBuilder.toString(), BotConfig[].class);
                ArrayList<BotConfig> fileConfigList = new ArrayList<>();
                if (fileConfigArray != null) {
                    for (int i = 0; i < fileConfigArray.length; i++) {
                        if ((fileConfigArray[i].getBotApiToken() != null)
                                && (!fileConfigArray[i].getBotApiToken().isEmpty())
                                && (!isBotAlreadyInUse(fileConfigList, fileConfigArray[i].getBotApiToken()))
                                && isClassExistInClassPath(fileConfigArray[i].getBotModelClassName())) {
                            fileConfigList.add(fileConfigArray[i]);
                            fileConfigArray[i].initDefaults();
                        }
                    }
                    fileConfigArray = new BotConfig[fileConfigList.size()];
                    fileConfigArray = fileConfigList.toArray(fileConfigArray);
                    fileConfigListMap.put(configFile, fileConfigArray);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileConfigArray;
    }

    private void initTBotConfig(String botLauncherclassName, String botApiToken, String[] commandList,
                                long[] adminIdList, long reportIntervalInSeconds, int threadCount) {
        this.botModelClassName = botLauncherclassName;
        this.botApiToken = botApiToken;
        this.commandList = commandList;
        this.adminIdList = adminIdList;
        this.reportIntervalInSeconds = reportIntervalInSeconds;
        this.threadCount = threadCount;
        initDefaults();
    }

    private boolean doesStringExistInList(String str, ArrayList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(str)) {
                return true;
            }
        }
        return false;
    }

    private void initDefaults() {
        if (this.commandList == null) {
            this.commandList = defaultCommands;
        } else {
            ArrayList<String> newCommandList = new ArrayList<String>();
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
            constants = new HashMap<String, String>();
        }
        if (this.threadCount < 1) {
            this.threadCount = 1;
        }
    }

    public void setValueForKey(String key, String value) {
        constants.put(key, value);
    }

    public String getValueForKey(String key) {
        return constants.get(key);
    }

    public String getBotApiToken() {
        return botApiToken;
    }

    public String[] getCommandList() {
        return commandList;
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
