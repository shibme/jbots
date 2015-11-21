package me.shib.java.lib.telegram.bot.easybot;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import me.shib.java.lib.telegram.bot.service.TelegramBotService;
import me.shib.java.lib.telegram.bot.types.Update;
import me.shib.java.lib.telegram.bot.types.User;

public class UpdateReceiver {
	
	private static final long myIdentityCheckInterval = 300000;
	
	private static Map<String, UpdateReceiver> updateReceiverMap;
	
	private Queue<Update> updatesQueue = new LinkedList<Update>();
	private TelegramBotService tBotUpdateService;
	private User myIdentity;
	private long lastMyIdentityCheck;
	private boolean botStarted;
	
	private UpdateReceiver(String botApiToken) {
		this.updatesQueue = new LinkedList<Update>();
		this.tBotUpdateService = new TelegramBotService(botApiToken);
		this.lastMyIdentityCheck = 0;
		botStarted = false;
	}
	
	protected static synchronized UpdateReceiver getDefaultInstance(String botApiToken) {
		if(updateReceiverMap == null) {
			updateReceiverMap = new HashMap<String, UpdateReceiver>();
		}
		UpdateReceiver updRecvr = updateReceiverMap.get(botApiToken);
		if(updRecvr == null) {
			updRecvr = new UpdateReceiver(botApiToken);
			updateReceiverMap.put(botApiToken, updRecvr);
		}
		return updRecvr;
	}
	
	private synchronized void fillUpdatesQueue() {
		try {
			Update[] updates = tBotUpdateService.getUpdates();
			for(Update u : updates) {
				updatesQueue.add(u);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected synchronized Update getUpdate() {
		Update update = updatesQueue.poll();
		while(update == null) {
			fillUpdatesQueue();
			update = updatesQueue.poll();
		}
		return update;
	}
	
	protected synchronized void onBotStart() {
		if(!botStarted) {
			whoAmI();
			if(myIdentity != null) {
				System.out.println("Starting services for: " + myIdentity.getFirst_name() +  " (" + myIdentity.getUsername() + ")");
				botStarted = true;
			}
		}
	}
	
	private void updateMyIdentity() {
		try {
			myIdentity = tBotUpdateService.getMe();
			lastMyIdentityCheck = new Date().getTime();
		} catch (IOException e) {
			lastMyIdentityCheck = 0;
		}
	}
	
	public User whoAmI() {
		if(myIdentity == null) {
			updateMyIdentity();
		}
		long currentTime = new Date().getTime();
		if((currentTime - lastMyIdentityCheck) > UpdateReceiver.myIdentityCheckInterval) {
			updateMyIdentity();
		}
		return myIdentity;
	}
	
	protected TelegramBotService getTelegramBotService() {
		return this.tBotUpdateService;
	}
	
}
