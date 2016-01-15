package me.shib.java.lib.telegram.bot.easybot;

import me.shib.java.lib.telegram.bot.service.TelegramBot;
import me.shib.java.lib.telegram.bot.service.TelegramBot.ChatAction;
import me.shib.java.lib.telegram.bot.types.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

public class DefaultBotModel extends BotModel {

    private static final Date startTime = new Date();

    private BotConfig botConfig;
    private User myIdentity;
    private BotModel appModel;
    private TelegramBot bot;

    protected DefaultBotModel(BotModel appModel) {
        this.appModel = appModel;
        this.botConfig = appModel.getConfig();
        this.myIdentity = UpdateReceiver.getDefaultInstance(this.botConfig.getBotApiToken()).whoAmI();
        bot = getBot();
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
            return "Unknown Host";
        }
    }

    @Override
    public BotConfig getConfig() {
        return appModel.getConfig();
    }

    public Message onReceivingMessage(Message message) {
        Message appModelResponseMessage = appModel.onReceivingMessage(message);
        if (appModelResponseMessage != null) {
            return appModelResponseMessage;
        }
        try {
            long[] admins = botConfig.getAdminIdList();
            if ((admins != null) && (admins.length > 0)) {
                for (long admin : admins) {
                    try {
                        bot.forwardMessage(new ChatId(admin), new ChatId(message.getFrom().getId()),
                                message.getMessage_id());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return bot.sendMessage(new ChatId(message.getChat().getId()),
                        "Hey " + message.getFrom().getFirst_name()
                                + ",Your request was taken. I'll get back to you ASAP.",
                        ParseMode.None, false, message.getMessage_id());
            } else {
                return bot.sendMessage(new ChatId(message.getChat().getId()),
                        "The support team is unavailable. Please try later.", ParseMode.None, false,
                        message.getMessage_id());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Message onMessageFromAdmin(Message message) {
        Message appModelResponseMessage = appModel.onMessageFromAdmin(message);
        if (appModelResponseMessage != null) {
            return appModelResponseMessage;
        }
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
        } catch (Exception ignored) {
        }
        return null;
    }

    private File getCurrentScreenShotFile() {
        try {
            File screenShotFile = new File("screenshots" + File.separator + new Date().getTime() + ".png");
            File parentDir = screenShotFile.getParentFile();
            if ((!parentDir.exists()) || (!parentDir.isDirectory())) {
                parentDir.mkdirs();
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
            return null;
        }
    }

    private Message onStartAndHelp(Message message) throws IOException {
        bot.sendChatAction(new ChatId(message.getChat().getId()), ChatAction.typing);
        return bot.sendMessage(new ChatId(message.getChat().getId()),
                "Hi " + message.getFrom().getFirst_name() + ". My name is *" + myIdentity.getFirst_name() + "* (@"
                        + myIdentity.getUsername() + "). I'll try to serve you the best" + " with all my efforts. Welcome!",
                ParseMode.Markdown);
    }

    public Message onCommand(Message message) {
        Message appModelResponseMessage = appModel.onCommand(message);
        if (appModelResponseMessage != null) {
            return appModelResponseMessage;
        }
        switch (message.getText()) {
            case "/start":
                if (botConfig.isValidCommand("/start")) {
                    try {
                        return onStartAndHelp(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case "/help":
                if (botConfig.isValidCommand("/help")) {
                    try {
                        return onStartAndHelp(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case "/scr":
                if (botConfig.isValidCommand("/scr") && botConfig.isAdmin(message.getChat().getId())) {
                    try {
                        bot.sendChatAction(new ChatId(message.getChat().getId()), ChatAction.upload_document);
                        File screenShotFile = getCurrentScreenShotFile();
                        if (screenShotFile != null) {
                            Message returnMessage = bot.sendDocument(new ChatId(message.getChat().getId()),
                                    new TelegramFile(screenShotFile));
                            screenShotFile.delete();
                            return returnMessage;
                        }
                        return bot.sendMessage(new ChatId(message.getChat().getId()),
                                "I couldn't take a screenshot right now. I'm sorry.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case "/status":
                if (botConfig.isValidCommand("/status") && botConfig.isAdmin(message.getChat().getId())) {
                    try {
                        bot.sendChatAction(new ChatId(message.getChat().getId()), ChatAction.typing);
                        return sendStatusMessage(message.getChat().getId());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case "/usermode":
                if (botConfig.isValidCommand("/usermode") && botConfig.isAdmin(message.getChat().getId())) {
                    botConfig.setUserMode(message.getFrom().getId());
                    try {
                        return bot.sendMessage(new ChatId(message.getChat().getId()), "Switched to *User Mode*", ParseMode.Markdown);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case "/adminmode":
                if (botConfig.isValidCommand("/adminmode") && botConfig.isAdmin(message.getChat().getId())) {
                    botConfig.setAdminMode(message.getFrom().getId());
                    try {
                        return bot.sendMessage(new ChatId(message.getChat().getId()), "Switched to *Admin Mode*", ParseMode.Markdown);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
        return null;
    }

    @Override
    public boolean onInlineQuery(InlineQuery query) {
        return appModel.onInlineQuery(query);
    }

    @Override
    public boolean onChosenInlineResult(ChosenInlineResult chosenInlineResult) {
        return appModel.onChosenInlineResult(chosenInlineResult);
    }

    @Override
    public Message sendStatusMessage(long chatId) {
        Message appModelResponseMessage = appModel.sendStatusMessage(chatId);
        if (appModelResponseMessage != null) {
            return appModelResponseMessage;
        }
        try {
            return bot.sendMessage(new ChatId(chatId),
                    "Reporting status:\nHost: " + getHostInfo() + "\nUp Time: " + getUpTime());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
