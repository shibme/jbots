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
	
	private static final String defaultConfigFilePath = "TBotConfig.json";
	private static final String[] defaultCommands = {"/start", "/status", "/scr"};
	
	private static Map<File, TBotConfig> fileConfigMap;
	
	private String botApiToken;
	private String[] commandList;
	private long[] adminIdList;
	private long reportIntervalInSeconds;
	private Map<String, String> constants;
	
	public TBotConfig(String botApiToken, String[] commandList, long[] adminIdList, long reportIntervalInSeconds) {
		initTBotConfig(botApiToken, commandList, adminIdList, reportIntervalInSeconds);
	}
	
	public TBotConfig(String botApiToken, String[] commandList, long[] adminIdList) {
		initTBotConfig(botApiToken, commandList, adminIdList, 0);
	}
	
	public TBotConfig(String botApiToken, String[] commandList) {
		initTBotConfig(botApiToken, commandList, null, 0);
	}
	
	public TBotConfig(String botApiToken) {
		initTBotConfig(botApiToken, null, null, 0);
	}
	
	private void initTBotConfig(String botApiToken, String[] commandList, long[] adminIdList, long reportIntervalInSeconds) {
		this.botApiToken = botApiToken;
		this.commandList = commandList;
		this.adminIdList = adminIdList;
		this.reportIntervalInSeconds = reportIntervalInSeconds;
		initDefaults();
	}
	
	public static synchronized TBotConfig getFileConfig() {
		return getFileConfig(new File(TBotConfig.defaultConfigFilePath));
	}
	
	public static synchronized TBotConfig getFileConfig(File configFile) {
		if(fileConfigMap == null) {
			fileConfigMap = new HashMap<File, TBotConfig>();
		}
		TBotConfig fileConfig = fileConfigMap.get(configFile);
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
				fileConfig = JsonLib.getDefaultInstance().fromJson(jsonBuilder.toString(), TBotConfig.class);
				if(fileConfig != null) {
					if(fileConfig.getBotApiToken() == null) {
						fileConfig = null;
					}
					else {
						fileConfig.initDefaults();
					}
				}
				if(fileConfig != null) {
					fileConfigMap.put(configFile, fileConfig);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return fileConfig;
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
	
}
