package me.shib.java.lib.jbots;


import me.shib.java.lib.jtelebot.types.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URLDecoder;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DefaultJBot extends JBot {

    private static final Logger logger = Logger.getLogger(DefaultJBot.class.getName());
    private static final String starEmoji = "%E2%AD%90%EF%B8%8F";

    private JBotConfig config;
    private JBot appModel;

    DefaultJBot(JBotConfig config) {
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
        if (appModel != null) {
            this.botReviewMarkdownMessage = appModel.botReviewMarkdownMessage;
        }
    }

    private static String getStars(int count) throws UnsupportedEncodingException {
        StringBuilder starBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            starBuilder.append(URLDecoder.decode(starEmoji, "UTF-8"));
        }
        return starBuilder.toString();
    }

    protected String getModelClassName() {
        if (appModel != null) {
            return appModel.getClass().getSimpleName();
        }
        return this.getClass().getSimpleName();
    }

    private void invalidMessageResponse(ChatId chatId, boolean appHandled) {
        if ((!appHandled) && (!config.isDefaultWorkerDisabled())) {
            try {
                bot.sendMessage(chatId, "Invalid Message.");
            } catch (IOException e) {
                logger.throwing(this.getClass().getName(), "invalidMessageResponse", e);
            }
        }
    }

    @Override
    public MessageHandler onMessage(Message message) {
        if (processIfReview(message)) {
            return null;
        }

        abstract class DefaultMessageHandler extends MessageHandler {
            MessageHandler appMessageHandler;

            DefaultMessageHandler(MessageHandler appMessageHandler, Message message) {
                super(message);
                this.appMessageHandler = appMessageHandler;
            }
        }

        MessageHandler appMessageHandler = null;
        if (appModel != null) {
            appMessageHandler = appModel.onMessage(message);
        }
        return new DefaultMessageHandler(appMessageHandler, message) {
            @Override
            public boolean onCommandFromAdmin(String command) {
                boolean appHandled = (appMessageHandler != null) && appMessageHandler.onCommandFromAdmin(command);
                if ((!appHandled) && (!config.isDefaultWorkerDisabled())) {
                    switch (command) {
                        case "/scr":
                            if (message.getText().equalsIgnoreCase("/scr")) {
                                try {
                                    bot.sendChatAction(new ChatId(message.getChat().getId()), ChatAction.upload_document);
                                    File screenShotFile = getCurrentScreenShotFile();
                                    if (screenShotFile != null) {
                                        bot.sendDocument(new ChatId(message.getChat().getId()),
                                                new TelegramFile(screenShotFile));
                                        if (!screenShotFile.delete()) {
                                            logger.log(Level.FINE, "Screenshot file, \"" + screenShotFile.getAbsolutePath() + "\" was not deleted.");
                                        }
                                    } else {
                                        bot.sendMessage(new ChatId(message.getChat().getId()),
                                                "I couldn't take a screenshot right now. I'm sorry.");
                                    }
                                    return true;
                                } catch (IOException e) {
                                    logger.throwing(this.getClass().getName(), "onCommandFromAdmin", e);
                                }
                            }
                            break;
                        case "/status":
                            if (message.getText().equalsIgnoreCase("/status")) {
                                try {
                                    bot.sendChatAction(new ChatId(message.getChat().getId()), ChatAction.typing);
                                    sendStatusMessage(message.getChat().getId());
                                    return true;
                                } catch (IOException e) {
                                    logger.throwing(this.getClass().getName(), "onCommandFromAdmin", e);
                                }
                            }
                            break;
                        case "/usermode":
                            if (message.getText().equalsIgnoreCase("/usermode") && config.isAdmin(message.getChat().getId())) {
                                config.setUserMode(message.getFrom().getId());
                                try {
                                    bot.sendMessage(new ChatId(message.getChat().getId()), "Switched to *User Mode*", false, ParseMode.Markdown);
                                    return true;
                                } catch (IOException e) {
                                    logger.throwing(this.getClass().getName(), "onCommandFromUser", e);
                                }
                            }
                            break;
                    }
                }
                invalidMessageResponse(new ChatId(message.getChat().getId()), appHandled);
                return appHandled;
            }

            @Override
            public boolean onCommandFromUser(String command) {
                boolean appHandled = (appMessageHandler != null) && appMessageHandler.onCommandFromUser(command);
                if ((!appHandled) && (!config.isDefaultWorkerDisabled())) {
                    switch (command) {
                        case "/start":
                            if (message.getText().equalsIgnoreCase("/start")) {
                                try {
                                    onStartAndHelp(message);
                                    return true;
                                } catch (IOException e) {
                                    logger.throwing(this.getClass().getName(), "onCommandFromUser", e);
                                }
                            } else if (message.getText().toLowerCase().startsWith("/start")) {
                                String linkValueText = message.getText().replace("/start", "").trim();
                                if (linkValueText.equalsIgnoreCase("review") || linkValueText.equalsIgnoreCase("rating")) {
                                    return onReviewAndRating(message);
                                }
                            }
                            break;
                        case "/help":
                            try {
                                onStartAndHelp(message);
                                return true;
                            } catch (IOException e) {
                                logger.throwing(this.getClass().getName(), "onCommandFromUser", e);
                            }
                            break;
                        case "/review":
                            if (onReviewAndRating(message)) {
                                return true;
                            }
                            break;
                        case "/rating":
                            if (onReviewAndRating(message)) {
                                return true;
                            }
                            break;
                        case "/adminmode":
                            if (message.getText().equalsIgnoreCase("/adminmode") && config.isAdmin(message.getChat().getId())) {
                                config.setAdminMode(message.getFrom().getId());
                                try {
                                    bot.sendMessage(new ChatId(message.getChat().getId()), "Switched to *Admin Mode*", false, ParseMode.Markdown);
                                    return true;
                                } catch (IOException e) {
                                    logger.throwing(this.getClass().getName(), "onCommandFromUser", e);
                                }
                            }
                            break;
                    }
                }
                invalidMessageResponse(new ChatId(message.getChat().getId()), appHandled);
                return appHandled;
            }

            @Override
            public boolean onMessageFromAdmin() {
                boolean appHandled = (appMessageHandler != null) && appMessageHandler.onMessageFromAdmin();
                if ((!appHandled) && (!config.isDefaultWorkerDisabled())) {
                    try {
                        long replyToUser = message.getReply_to_message().getForward_from().getId();
                        if (replyToUser > 0) {
                            if (message.getText() != null) {
                                bot.sendMessage(new ChatId(replyToUser), message.getText());
                                return true;
                            } else if (message.getDocument() != null) {
                                bot.sendDocument(new ChatId(replyToUser),
                                        new TelegramFile(message.getDocument().getFile_id()));
                                return true;
                            } else if (message.getVideo() != null) {
                                bot.sendVideo(new ChatId(replyToUser),
                                        new TelegramFile(message.getVideo().getFile_id()));
                                return true;
                            } else if (message.getPhoto() != null) {
                                bot.sendPhoto(new ChatId(replyToUser),
                                        new TelegramFile(message.getPhoto()[message.getPhoto().length - 1].getFile_id()));
                                return true;
                            } else if (message.getAudio() != null) {
                                bot.sendDocument(new ChatId(replyToUser),
                                        new TelegramFile(message.getDocument().getFile_id()));
                                return true;
                            } else if (message.getVoice() != null) {
                                bot.sendDocument(new ChatId(replyToUser),
                                        new TelegramFile(message.getDocument().getFile_id()));
                                return true;
                            } else if (message.getSticker() != null) {
                                bot.sendDocument(new ChatId(replyToUser),
                                        new TelegramFile(message.getDocument().getFile_id()));
                                return true;
                            }
                        }
                    } catch (Exception e) {
                        logger.throwing(this.getClass().getName(), "onMessageFromAdmin", e);
                    }
                }
                invalidMessageResponse(new ChatId(message.getChat().getId()), appHandled);
                return appHandled;
            }

            @Override
            public boolean onMessageFromUser() {
                boolean appHandled = (appMessageHandler != null) && appMessageHandler.onMessageFromUser();
                if ((!appHandled) && (!config.isDefaultWorkerDisabled())) {
                    return forwardToAdmins(message);
                }
                invalidMessageResponse(new ChatId(message.getChat().getId()), appHandled);
                return appHandled;
            }
        };
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
                        + bot.getIdentity().getUsername() + "). I'll try to serve you the best way I can.\n\n*Welcome!*",
                false, ParseMode.Markdown);
    }

    private boolean onReviewAndRating(Message message) {
        try {
            if (appModel != null) {
                showReviewMessage(new ChatId(message.getChat().getId()));
                return true;
            }
        } catch (IOException e) {
            logger.throwing(this.getClass().getName(), "onReviewAndRating", e);
        }
        return false;
    }

    private boolean processIfReview(Message message) {
        try {
            if ((message.getText() != null) && (message.getText().startsWith(getStars(1)))
                    && (message.getText().replace(getStars(1), "").isEmpty())) {
                int minRating = config.getMinRatingAllowed() - 1;
                String reviewText = message.getText();
                for (int i = 0; i < minRating; i++) {
                    reviewText = reviewText.replaceFirst(getStars(1), "");
                }
                if (!reviewText.isEmpty()) {
                    bot.sendMessage(new ChatId(message.getChat().getId()), botReviewMarkdownMessage, false,
                            ParseMode.Markdown, true, 0, new ReplyKeyboardHide());
                } else {
                    bot.sendMessage(new ChatId(message.getChat().getId()), "Thanks for your rating.", false,
                            ParseMode.Markdown, false, 0, new ReplyKeyboardHide());
                }
                return true;
            }
        } catch (IOException e) {
            logger.throwing(this.getClass().getName(), "processIfReview", e);
        }
        return false;
    }

    private void showReviewMessage(ChatId chatId) throws IOException {
        String[][] keyboard = new String[][]{{getStars(1), getStars(2)},
                {getStars(3), getStars(4)},
                {getStars(5)}};
        bot.sendMessage(chatId, "Please *give us " + getStars(5) + " rating* and an *amazing review*",
                false, ParseMode.Markdown, false, 0, new ReplyKeyboardMarkup(keyboard));
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

}
