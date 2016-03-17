package me.shib.java.lib.jbots;

import me.shib.java.lib.jtelebot.types.Message;

public abstract class MessageHandler {

    protected Message message;

    public MessageHandler(Message message) {
        this.message = message;
    }

    public abstract boolean onCommandFromAdmin(String command);

    public abstract boolean onCommandFromUser(String command);

    public abstract boolean onMessageFromAdmin();

    public abstract boolean onMessageFromUser();

}
