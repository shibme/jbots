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

    protected DefaultBotModel(BotModel appModel) {
        this.appModel = appModel;
        this.botConfig = appModel.thisConfig();
        this.myIdentity = UpdateReceiver.getDefaultInstance(this.botConfig.getBotApiToken()).whoAmI();
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
        String upTime = timeDiff + "d " + hours + "h " + mins + "m " + seconds + "s ";
        return upTime;
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
    public BotConfig thisConfig() {
        return appModel.thisConfig();
    }

    public Message onReceivingMessage(TelegramBot bot, Message message) {
        Message appModelReponseMessage = appModel.onReceivingMessage(bot, message);
        if (appModelReponseMessage != null) {
            return appModelReponseMessage;
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

    @Override
    public boolean onInlineQuery(TelegramBot bot, InlineQuery query) {
        return appModel.onInlineQuery(bot, query);
    }

    public Message onMessageFromAdmin(TelegramBot bot, Message message) {
        Message appModelReponseMessage = appModel.onMessageFromAdmin(bot, message);
        if (appModelReponseMessage != null) {
            return appModelReponseMessage;
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
        } catch (Exception e) {
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

    private Message onStartAndHelp(TelegramBot tBotService, Message message) throws IOException {
        tBotService.sendChatAction(new ChatId(message.getChat().getId()), ChatAction.typing);
        return tBotService.sendMessage(new ChatId(message.getChat().getId()),
                "Hi " + message.getFrom().getFirst_name() + ". My name is *" + myIdentity.getFirst_name() + "* (@"
                        + myIdentity.getUsername() + "). I'll try to serve you the best" + " with all my efforts. Welcome!",
                ParseMode.Markdown);
    }

    public Message onCommand(TelegramBot bot, Message message) {
        Message appModelReponseMessage = appModel.onCommand(bot, message);
        if (appModelReponseMessage != null) {
            return appModelReponseMessage;
        }
        switch (message.getText()) {
            case "/start":
                if (botConfig.isValidCommand("/start")) {
                    try {
                        return onStartAndHelp(bot, message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case "/help":
                if (botConfig.isValidCommand("/help")) {
                    try {
                        return onStartAndHelp(bot, message);
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
                            Message returMessage = bot.sendDocument(new ChatId(message.getChat().getId()),
                                    new TelegramFile(screenShotFile));
                            screenShotFile.delete();
                            return returMessage;
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
                        return sendStatusMessage(bot, message.getChat().getId());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public Message sendStatusMessage(TelegramBot bot, long chatId) {
        Message appModelReponseMessage = appModel.sendStatusMessage(bot, chatId);
        if (appModelReponseMessage != null) {
            return appModelReponseMessage;
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
