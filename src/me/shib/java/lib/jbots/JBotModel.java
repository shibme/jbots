package me.shib.java.lib.jbots;

import me.shib.java.lib.jtelebot.service.TelegramBot;
import me.shib.java.lib.jtelebot.types.*;

import java.io.IOException;
import java.util.logging.Logger;

public abstract class JBotModel {

    private static Logger logger = Logger.getLogger(JBotModel.class.getName());

    private TelegramBot bot;
    private JBotConfig config;

    public JBotModel(JBotConfig config) {
        this.bot = JBots.getInstance(config);
        this.config = config;
    }

    public TelegramBot getBot() {
        return bot;
    }

    public Message forwardToAdmins(Message message) {
        try {
            long[] admins = config.getAdminIdList();
            if ((admins != null) && (admins.length > 0)) {
                for (long admin : admins) {
                    try {
                        bot.forwardMessage(new ChatId(admin), new ChatId(message.getFrom().getId()),
                                message.getMessage_id());
                    } catch (IOException e) {
                        logger.throwing(JBotLauncher.class.getName(), "forwardToAdmins", e);
                    }
                }
                return bot.sendMessage(new ChatId(message.getChat().getId()),
                        "Your message has been forwarded to the *admin*. It might take quite sometime to get back to you. Please be patient.",
                        ParseMode.Markdown, false, message.getMessage_id());
            }
            return bot.sendMessage(new ChatId(message.getChat().getId()),
                    "The support team is unavailable. Please try later.", null, false, message.getMessage_id());
        } catch (IOException e) {
            logger.throwing(JBotLauncher.class.getName(), "forwardToAdmins", e);
            return null;
        }
    }

    public abstract Message onMessageFromAdmin(Message message);

    public abstract Message onCommand(Message message);

    public abstract Message onReceivingMessage(Message message);

    public abstract boolean onInlineQuery(InlineQuery query);

    public abstract boolean onChosenInlineResult(ChosenInlineResult chosenInlineResult);

    public abstract Message sendStatusMessage(long chatId);

}
