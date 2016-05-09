package me.shib.java.lib.jbots;

import me.shib.java.lib.jtelebot.models.updates.Message;

public abstract class MessageHandler {

    protected Message message;

    public MessageHandler(Message message) {
        this.message = message;
    }

    public abstract boolean onCommandFromAdmin(String command, String argument);

    public abstract boolean onCommandFromUser(String command, String argument);

    public abstract boolean onMessageFromAdmin();

    public abstract boolean onMessageFromUser();

}
