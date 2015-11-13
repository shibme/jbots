package me.shib.java.telegram.bot.worker;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import me.shib.java.telegram.bot.service.TelegramBotService;
import me.shib.java.telegram.bot.types.Update;
import me.shib.java.telegram.bot.types.User;

public class UpdateReceiver {
	
	private static Queue<Update> updatesQueue = new LinkedList<Update>();
	private static TelegramBotService updateReceiver;
	private static User thisBot;
	
	protected static synchronized void setBotApiToken(String botApiToken) {
		if((updateReceiver == null) && (botApiToken != null) && (!botApiToken.isEmpty())) {
			updateReceiver = new TelegramBotService(botApiToken);
		}
	}
	
	private static synchronized void fillUpdatesQueue() {
		try {
			Update[] updates = updateReceiver.getUpdates();
			for(Update u : updates) {
				updatesQueue.add(u);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected static synchronized Update getUpdate() {
		Update update = updatesQueue.poll();
		while(update == null) {
			fillUpdatesQueue();
			update = updatesQueue.poll();
		}
		return update;
	}
	
	protected static synchronized User getMe() throws IOException {
		if(thisBot == null) {
			thisBot = updateReceiver.getMe();
		}
		return thisBot;
	}
	
}
