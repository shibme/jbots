package me.shib.java.telegram.bot.worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import me.shib.java.rest.client.lib.JsonLib;

public class TBotConfig {
	
	private static final long oneDayInSeconds = 86400;
	private static final String configFilePath = "TBotConfig.json";
	private static TBotConfig fileConfig;
	
	private String botApiToken;
	private long[] adminIdList;
	private boolean supportMessageFilter;
	private boolean reportStatus;
	private long statusReportInterval;
	private String supportCommand;
	private String statusReportCommand;
	
	public TBotConfig(String botApiToken, long[] adminIdList, boolean supportMessageFilter, boolean reportStatus, long statusReportInterval) {
		initTBotConfig(botApiToken, adminIdList, supportMessageFilter, reportStatus, statusReportInterval);
	}
	
	private void initTBotConfig(String botApiToken, long[] adminIdList, boolean supportMessageFilter, boolean reportStatus, long statusReportInterval) {
		this.botApiToken = botApiToken;
		this.adminIdList = adminIdList;
		this.supportMessageFilter = supportMessageFilter;
		this.reportStatus = reportStatus;
		this.statusReportInterval = statusReportInterval;
	}
	
	protected static synchronized TBotConfig getFileConfig() {
		if(fileConfig == null) {
			File configFile = new File(TBotConfig.configFilePath);
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
		if(this.supportCommand == null) {
			this.supportCommand = "/support";
		}
		if(this.statusReportCommand == null) {
			this.statusReportCommand = "/status";
		}
		if(this.statusReportInterval < 1) {
			this.statusReportInterval = oneDayInSeconds;
		}
	}
	
	protected String getBotApiToken() {
		return botApiToken;
	}

	protected long[] getAdminIdList() {
		return adminIdList;
	}

	protected boolean isAdminId(long id) {
		if((adminIdList != null) && (adminIdList.length > 0)) {
			for(long adminId : adminIdList) {
				if(id == adminId) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean isSupportMessageFilter() {
		return supportMessageFilter;
	}

	protected boolean isReportStatus() {
		return reportStatus;
	}

	protected long getStatusReportInterval() {
		return statusReportInterval;
	}

	protected String getSupportCommand() {
		return supportCommand;
	}

	protected void setSupportCommand(String supportCommand) {
		this.supportCommand = supportCommand;
	}

	protected String getStatusReportCommand() {
		return statusReportCommand;
	}

	protected void setStatusReportCommand(String statusReportCommand) {
		this.statusReportCommand = statusReportCommand;
	}
	
}
