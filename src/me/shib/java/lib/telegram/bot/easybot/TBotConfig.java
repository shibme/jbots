package me.shib.java.lib.telegram.bot.easybot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.shib.java.lib.common.utils.JsonLib;

public class TBotConfig {
	
	private static final String defaultConfigFilePath = "easy-bot-config.json";
	private static final String[] defaultCommands = {"/start", "/status", "/scr"};
	
	private static Map<File, TBotConfig[]> fileConfigListMap;
	
	private String botLauncherclassName;
	private String botApiToken;
	private String[] commandList;
	private int threadCount;
	private long[] adminIdList;
	private long reportIntervalInSeconds;
	private Map<String, String> constants;
	
	public TBotConfig(String botLauncherclassName, String botApiToken, String[] commandList, long[] adminIdList, long reportIntervalInSeconds, int threadCount) {
		initTBotConfig(botLauncherclassName, botApiToken, commandList, adminIdList, reportIntervalInSeconds, threadCount);
	}
	
	public TBotConfig(String botLauncherclassName, String botApiToken, String[] commandList, long[] adminIdList, long reportIntervalInSeconds) {
		initTBotConfig(botLauncherclassName, botApiToken, commandList, adminIdList, reportIntervalInSeconds, 0);
	}
	
	public TBotConfig(String botLauncherclassName, String botApiToken, String[] commandList, long[] adminIdList) {
		initTBotConfig(botLauncherclassName, botApiToken, commandList, adminIdList, 0, 0);
	}
	
	public TBotConfig(String botLauncherclassName, String botApiToken, String[] commandList) {
		initTBotConfig(botLauncherclassName, botApiToken, commandList, null, 0, 0);
	}
	
	public TBotConfig(String botLauncherclassName, String botApiToken) {
		initTBotConfig(botLauncherclassName, botApiToken, null, null, 0, 0);
	}
	
	private void initTBotConfig(String botLauncherclassName, String botApiToken, String[] commandList, long[] adminIdList, long reportIntervalInSeconds, int threadCount) {
		this.botLauncherclassName = botLauncherclassName;
		this.botApiToken = botApiToken;
		this.commandList = commandList;
		this.adminIdList = adminIdList;
		this.reportIntervalInSeconds = reportIntervalInSeconds;
		this.threadCount = threadCount;
		initDefaults();
	}
	
	public static synchronized TBotConfig[] getFileConfigList() {
		return getFileConfigList(new File(TBotConfig.defaultConfigFilePath));
	}
	
	public static synchronized TBotConfig[] getFileConfigList(File configFile) {
		if(fileConfigListMap == null) {
			fileConfigListMap = new HashMap<File, TBotConfig[]>();
		}
		TBotConfig[] fileConfigList = fileConfigListMap.get(configFile);
		if(configFile.exists()) {
			try {
				StringBuilder jsonBuilder = new StringBuilder();
				BufferedReader br = new BufferedReader(new FileReader(configFile));
				String line = br.readLine();
				while(line != null) {
					jsonBuilder.append(line);
					line = br.readLine();
					if(line != null) {
						jsonBuilder.append("\n");
					}
				}
				br.close();
				fileConfigList = JsonLib.getDefaultInstance().fromJson(jsonBuilder.toString(), TBotConfig[].class);
				if(fileConfigList != null) {
					for(int i = 0; i < fileConfigList.length; i++) {
						if(fileConfigList[i].getBotApiToken() == null) {
							fileConfigList[i] = null;
						}
						else {
							fileConfigList[i].initDefaults();
						}
					}
					fileConfigListMap.put(configFile, fileConfigList);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return fileConfigList;
	}
	
	private boolean doesStringExistInList(String str, ArrayList<String> list) {
		for(int i = 0; i < list.size(); i++) {
			if(list.get(i).equals(str)) {
				return true;
			}
		}
		return false;
	}
	
	private void initDefaults() {
		if(this.commandList == null) {
			this.commandList = defaultCommands;
		}
		else {
			ArrayList<String> newCommandList = new ArrayList<String>();
			for(String command : this.commandList) {
				if(!doesStringExistInList(command, newCommandList)) {
					newCommandList.add(command);
				}
			}
			if(defaultCommands != null) {
				for(String command : defaultCommands) {
					if(!doesStringExistInList(command, newCommandList)) {
						newCommandList.add(command);
					}
				}
			}
			this.commandList = new String[newCommandList.size()];
			this.commandList = newCommandList.toArray(this.commandList);
		}
		if(this.reportIntervalInSeconds < 0) {
			this.reportIntervalInSeconds = 0;
		}
		if(constants == null) {
			constants = new HashMap<String, String>();
		}
		if(this.threadCount < 1) {
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
		if((messageText != null) && (commandList != null)) {
			String[] words = messageText.split("\\s+");
			if(words.length > 0) {
				String possibleCommand = words[0];
				for(String command : commandList) {
					if(command.equals(possibleCommand)) {
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
		if(adminIdList != null) {
			for(long adminId : adminIdList) {
				if(senderId == adminId) {
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

	public String getBotLauncherclassName() {
		return botLauncherclassName;
	}
	
}
