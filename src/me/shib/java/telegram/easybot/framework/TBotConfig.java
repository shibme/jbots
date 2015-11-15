package me.shib.java.telegram.easybot.framework;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import me.shib.java.rest.client.lib.JsonLib;

public class TBotConfig {
	
	private static final long oneDayInSeconds = 86400;
	private static final String defaultConfigFilePath = "TBotConfig.json";
	private static TBotConfig fileConfig;
	
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
		return getFileConfig(TBotConfig.defaultConfigFilePath);
	}
	
	public static synchronized TBotConfig getFileConfig(String configFilePath) {
		if(fileConfig == null) {
			File configFile = new File(configFilePath);
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
					fileConfig = JsonLib.fromJson(jsonBuilder.toString(), TBotConfig.class);
					if(fileConfig != null) {
						if(fileConfig.getBotApiToken() == null) {
							fileConfig = null;
						}
						else {
							fileConfig.initDefaults();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return fileConfig;
	}
	
	private void initDefaults() {
		if(this.commandList == null) {
			this.commandList = new String[1];
			this.commandList[0] = "/start";
		}
		if(this.reportIntervalInSeconds < 1) {
			this.reportIntervalInSeconds = oneDayInSeconds;
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

	public long[] getAdminIdList() {
		return adminIdList;
	}

	public long getReportIntervalInSeconds() {
		return reportIntervalInSeconds;
	}
	
}
