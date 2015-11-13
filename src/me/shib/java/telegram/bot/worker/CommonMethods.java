package me.shib.java.telegram.bot.worker;

import java.io.IOException;

import me.shib.java.telegram.bot.types.Message;
import me.shib.java.telegram.bot.types.User;

public class CommonMethods {
	
	private static User thisBot;
	
	private static synchronized boolean onProcessStart() {
		if(thisBot == null) {
			try {
				thisBot = UpdateReceiver.getMe();
				System.out.println("Now starting services for: " + thisBot.getFirst_name() +  " (" + thisBot.getUsername() + ")");
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return (thisBot != null);
	}
	
	protected static boolean onThreadStart(int threadNumber) {
		if(onProcessStart()) {
			System.out.println("Starting thread " + threadNumber + " of " + thisBot.getUsername());
			return true;
		}
		return false;
	}
	
	protected static void logNewMessage(Message message) {
		
	}
	
}
