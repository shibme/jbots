package me.shib.java.lib.jbots;


import me.shib.java.lib.jtelebot.types.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DefaultJBot extends JBot {

    private static final Logger logger = Logger.getLogger(DefaultJBot.class.getName());
    private static final Date startTime = new Date();

    private JBotConfig config;
    private JBot appModel;

    protected DefaultJBot(JBotConfig config) {
        super(config);
        this.config = config;
        try {
            Class<?> clazz = Class.forName(config.getBotModelClassName());
            Constructor<?> ctor = clazz.getConstructor(JBotConfig.class);
            appModel = (JBot) ctor.newInstance(config);
        } catch (Exception e) {
            logger.throwing(this.getClass().getName(), "DefaultJBot", e);
            appModel = null;
        }
    }

    private static String getUpTime() {
        long start = startTime.getTime();
        long current = new Date().getTime();
        long timeDiff = current - start;
        timeDiff = timeDiff / 1000;
        int seconds = (int) (timeDiff % 60);
        timeDiff = timeDiff / 60;
        int mins = (int) (timeDiff % 60);
        timeDiff = timeDiff / 60;
        int hours = (int) (timeDiff % 24);
        timeDiff = timeDiff / 24;
        return timeDiff + "d " + hours + "h " + mins + "m " + seconds + "s ";
    }

    private static String getHostInfo() {
        InetAddress ip;
        String hostname;
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
            return hostname + "(" + ip.getHostAddress() + ")";
        } catch (UnknownHostException e) {
            logger.throwing(DefaultJBot.class.getName(), "getHostInfo", e);
            return "Unknown Host";
        }
    }

    protected String getModelClassName() {
        if (appModel != null) {
            return appModel.getClass().getSimpleName();
        }
        return this.getClass().getSimpleName();
    }

    protected JBotConfig getConfig() {
        return config;
    }

    public Message onReceivingMessage(Message message) {
        Message appModelResponseMessage = null;
        if (appModel != null) {
            appModelResponseMessage = appModel.onReceivingMessage(message);
        }
        if (appModelResponseMessage != null) {
            return appModelResponseMessage;
        } else if (!config.isDefaultWorkerDisabled()) {
            return forwardToAdmins(message);
        }
        return null;
    }

    public Message onMessageFromAdmin(Message message) {
        Message appModelResponseMessage = null;
        if (appModel != null) {
            appModelResponseMessage = appModel.onMessageFromAdmin(message);
        }
        if (appModelResponseMessage != null) {
            return appModelResponseMessage;
        } else if (!config.isDefaultWorkerDisabled()) {
            try {
                long replyToUser = message.getReply_to_message().getForward_from().getId();
                if (replyToUser > 0) {
                    if (message.getText() != null) {
                        return bot.sendMessage(new ChatId(replyToUser), message.getText());
                    } else if (message.getDocument() != null) {
                        return bot.sendDocument(new ChatId(replyToUser),
                                new TelegramFile(message.getDocument().getFile_id()));
                    } else if (message.getVideo() != null) {
                        return bot.sendVideo(new ChatId(replyToUser),
                                new TelegramFile(message.getVideo().getFile_id()));
                    } else if (message.getPhoto() != null) {
                        return bot.sendPhoto(new ChatId(replyToUser),
                                new TelegramFile(message.getPhoto()[message.getPhoto().length - 1].getFile_id()));
                    } else if (message.getAudio() != null) {
                        return bot.sendDocument(new ChatId(replyToUser),
                                new TelegramFile(message.getDocument().getFile_id()));
                    } else if (message.getVoice() != null) {
                        return bot.sendDocument(new ChatId(replyToUser),
                                new TelegramFile(message.getDocument().getFile_id()));
                    } else if (message.getSticker() != null) {
                        return bot.sendDocument(new ChatId(replyToUser),
                                new TelegramFile(message.getDocument().getFile_id()));
                    }
                }
            } catch (Exception e) {
                logger.throwing(this.getClass().getName(), "onMessageFromAdmin", e);
            }
        }
        return null;
    }

    private File getCurrentScreenShotFile() {
        try {
            File screenShotFile = new File("screenshots" + File.separator + new Date().getTime() + ".png");
            File parentDir = screenShotFile.getParentFile();
            if ((!parentDir.exists()) || (!parentDir.isDirectory())) {
                if (!parentDir.mkdirs()) {
                    logger.log(Level.WARNING, "Failed to create directory tree: " + parentDir.getAbsolutePath());
                }
            }
            if (parentDir.exists() && parentDir.isDirectory()) {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Rectangle screenRectangle = new Rectangle(screenSize);
                Robot robot = new Robot();
                BufferedImage image = robot.createScreenCapture(screenRectangle);
                ImageIO.write(image, "png", screenShotFile);
            }
            return screenShotFile;
        } catch (Exception e) {
            logger.throwing(this.getClass().getName(), "getCurrentScreenShotFile", e);
            return null;
        }
    }

    private Message onStartAndHelp(Message message) throws IOException {
        bot.sendChatAction(new ChatId(message.getChat().getId()), ChatAction.typing);
        return bot.sendMessage(new ChatId(message.getChat().getId()),
                "Hi " + message.getFrom().getFirst_name() + ". My name is *" + bot.getIdentity().getFirst_name() + "* (@"
                        + bot.getIdentity().getUsername() + "). I'll try to serve you the best" + " with all my efforts. Welcome!",
                false, ParseMode.Markdown);
    }

    public Message onCommand(Message message) {
        Message appModelResponseMessage = null;
        if (appModel != null) {
            appModelResponseMessage = appModel.onCommand(message);
        }
        if (appModelResponseMessage != null) {
            return appModelResponseMessage;
        }
        switch (message.getText()) {
            case "/start":
                if (config.isValidCommand("/start")) {
                    try {
                        return onStartAndHelp(message);
                    } catch (IOException e) {
                        logger.throwing(this.getClass().getName(), "onCommand", e);
                    }
                }
                break;
            case "/help":
                if (config.isValidCommand("/help")) {
                    try {
                        return onStartAndHelp(message);
                    } catch (IOException e) {
                        logger.throwing(this.getClass().getName(), "onCommand", e);
                    }
                }
                break;
            case "/scr":
                if (config.isValidCommand("/scr") && config.isAdmin(message.getChat().getId())) {
                    try {
                        bot.sendChatAction(new ChatId(message.getChat().getId()), ChatAction.upload_document);
                        File screenShotFile = getCurrentScreenShotFile();
                        if (screenShotFile != null) {
                            Message returnMessage = bot.sendDocument(new ChatId(message.getChat().getId()),
                                    new TelegramFile(screenShotFile));
                            if (!screenShotFile.delete()) {
                                logger.log(Level.FINE, "Screenshot file, \"" + screenShotFile.getAbsolutePath() + "\" was not deleted.");
                            }
                            return returnMessage;
                        }
                        return bot.sendMessage(new ChatId(message.getChat().getId()),
                                "I couldn't take a screenshot right now. I'm sorry.");
                    } catch (IOException e) {
                        logger.throwing(this.getClass().getName(), "onCommand", e);
                    }
                }
                break;
            case "/status":
                if (config.isValidCommand("/status") && config.isAdmin(message.getChat().getId())) {
                    try {
                        bot.sendChatAction(new ChatId(message.getChat().getId()), ChatAction.typing);
                        return sendStatusMessage(message.getChat().getId());
                    } catch (IOException e) {
                        logger.throwing(this.getClass().getName(), "onCommand", e);
                    }
                }
                break;
            case "/usermode":
                if (config.isValidCommand("/usermode") && config.isAdmin(message.getChat().getId())) {
                    config.setUserMode(message.getFrom().getId());
                    try {
                        return bot.sendMessage(new ChatId(message.getChat().getId()), "Switched to *User Mode*", false, ParseMode.Markdown);
                    } catch (IOException e) {
                        logger.throwing(this.getClass().getName(), "onCommand", e);
                    }
                }
                break;
            case "/adminmode":
                if (config.isValidCommand("/adminmode") && config.isAdmin(message.getChat().getId())) {
                    config.setAdminMode(message.getFrom().getId());
                    try {
                        return bot.sendMessage(new ChatId(message.getChat().getId()), "Switched to *Admin Mode*", false, ParseMode.Markdown);
                    } catch (IOException e) {
                        logger.throwing(this.getClass().getName(), "onCommand", e);
                    }
                }
                break;
        }
        return null;
    }

    @Override
    public boolean onInlineQuery(InlineQuery query) {
        boolean appModelResponse = false;
        if (appModel != null) {
            appModelResponse = appModel.onInlineQuery(query);
        }
        return appModelResponse;
    }

    @Override
    public boolean onChosenInlineResult(ChosenInlineResult chosenInlineResult) {
        boolean appModelResponse = false;
        if (appModel != null) {
            appModelResponse = appModel.onChosenInlineResult(chosenInlineResult);
        }
        return appModelResponse;
    }

    @Override
    public Message sendStatusMessage(long chatId) {
        Message appModelResponseMessage = null;
        if (appModel != null) {
            appModelResponseMessage = appModel.sendStatusMessage(chatId);
        }
        if (appModelResponseMessage != null) {
            return appModelResponseMessage;
        } else if (!config.isDefaultWorkerDisabled()) {
            try {
                return bot.sendMessage(new ChatId(chatId),
                        "Reporting status:\nHost: " + getHostInfo() + "\nUp Time: " + getUpTime());
            } catch (IOException e) {
                logger.throwing(this.getClass().getName(), "sendStatusMessage", e);
            }
        }
        return null;
    }

}
