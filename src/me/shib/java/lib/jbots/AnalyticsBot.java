package me.shib.java.lib.jbots;

import me.shib.java.lib.jbotstats.AnalyticsData;
import me.shib.java.lib.jbotstats.AnalyticsWorker;
import me.shib.java.lib.jbotstats.JBotStats;
import me.shib.java.lib.jtelebot.service.TelegramBot;
import me.shib.java.lib.jtelebot.types.*;
import me.shib.java.lib.rest.client.HTTPFileDownloader;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class AnalyticsBot extends TelegramBot {

    private TelegramBot bot;
    private AnalyticsWorker analyticsWorker;
    private long lastKnownUpdateId;

    protected AnalyticsBot(JBotStats jBotStats, TelegramBot bot) {
        this.bot = bot;
        this.analyticsWorker = new AnalyticsWorker(jBotStats);
        this.lastKnownUpdateId = 0;
    }

    @Override
    public String getBotApiToken() {
        return bot.getBotApiToken();
    }

    @Override
    public User getIdentity() {
        return bot.getIdentity();
    }

    @Override
    public User getMe() throws IOException {
        AnalyticsData data = new AnalyticsData("getMe");
        IOException ioException = null;
        User me = null;
        try {
            me = bot.getMe();
            data.setReturned(me);
        } catch (IOException e) {
            ioException = e;
            data.setIoException(ioException);
        }
        analyticsWorker.putData(data);
        if (ioException != null) {
            throw ioException;
        }
        return me;
    }

    @Override
    public UserProfilePhotos getUserProfilePhotos(long user_id, int offset, int limit) throws IOException {
        AnalyticsData data = new AnalyticsData("getUserProfilePhotos");
        IOException ioException = null;
        UserProfilePhotos photos = null;
        data.setValue("user_id", user_id);
        data.setValue("offset", offset);
        data.setValue("limit", limit);
        try {
            photos = bot.getUserProfilePhotos(user_id, offset, limit);
            data.setReturned(photos);
        } catch (IOException e) {
            ioException = e;
            data.setIoException(ioException);
        }
        analyticsWorker.putData(data);
        if (ioException != null) {
            throw ioException;
        }
        return photos;
    }

    @Override
    public TelegramFile getFile(String file_id) throws IOException {
        AnalyticsData data = new AnalyticsData("getFile");
        IOException ioException = null;
        TelegramFile telegramFile = null;
        data.setValue("file_id", file_id);
        try {
            telegramFile = bot.getFile(file_id);
            data.setReturned(telegramFile);
        } catch (IOException e) {
            ioException = e;
            data.setIoException(ioException);
        }
        analyticsWorker.putData(data);
        if (ioException != null) {
            throw ioException;
        }
        return telegramFile;
    }

    @Override
    public HTTPFileDownloader.DownloadProgress downloadToFile(String file_id, File downloadToFile, boolean waitForCompletion) throws IOException {
        AnalyticsData data = new AnalyticsData("downloadToFile");
        IOException ioException = null;
        HTTPFileDownloader.DownloadProgress progress = null;
        data.setValue("file_id", file_id);
        data.setValue("downloadToFile", downloadToFile);
        data.setValue("waitForCompletion", waitForCompletion);
        try {
            progress = bot.downloadToFile(file_id, downloadToFile, waitForCompletion);
            data.setReturned(progress);
        } catch (IOException e) {
            ioException = e;
            data.setIoException(ioException);
        }
        analyticsWorker.putData(data);
        if (ioException != null) {
            throw ioException;
        }
        return progress;
    }

    @Override
    public File downloadFile(String file_id, File downloadToFile) throws IOException {
        AnalyticsData data = new AnalyticsData("downloadFile");
        IOException ioException = null;
        File file = null;
        data.setValue("file_id", file_id);
        data.setValue("downloadToFile", downloadToFile);
        try {
            file = bot.downloadFile(file_id, downloadToFile);
            data.setReturned(file);
        } catch (IOException e) {
            ioException = e;
            data.setIoException(ioException);
        }
        analyticsWorker.putData(data);
        if (ioException != null) {
            throw ioException;
        }
        return file;
    }

    private AnalyticsData getNewUpdateAnalyticsData(int timeout, int limit, long offset) {
        AnalyticsData data = new AnalyticsData("getUpdates");
        data.setValue("timeout", timeout);
        data.setValue("limit", limit);
        if (offset >= 0) {
            data.setValue("offset", offset);
        }
        return data;
    }

    @Override
    public synchronized Update[] getUpdates(int timeout, int limit, long offset) throws IOException {
        Date accessTime = new Date();
        AnalyticsData data = getNewUpdateAnalyticsData(timeout, limit, offset);
        IOException ioException = null;
        Update[] updates = null;
        try {
            updates = bot.getUpdates(timeout, limit, offset);
        } catch (IOException e) {
            ioException = e;
            data.setIoException(ioException);
        }
        if (updates != null) {
            for (Update update : updates) {
                if (update.getUpdate_id() > lastKnownUpdateId) {
                    lastKnownUpdateId = update.getUpdate_id();
                    data = getNewUpdateAnalyticsData(timeout, limit, offset);
                    data.setReturned(update);
                    data.setAccessTime(accessTime);
                    analyticsWorker.putData(data);
                }
            }
        } else {
            data.setAccessTime(accessTime);
            analyticsWorker.putData(data);
        }
        if (ioException != null) {
            throw ioException;
        }
        return updates;
    }

    @Override
    public synchronized Update[] getUpdates(int timeout, int limit) throws IOException {
        Date accessTime = new Date();
        AnalyticsData data;
        IOException ioException = null;
        Update[] updates = null;
        try {
            updates = bot.getUpdates(timeout, limit);
        } catch (IOException e) {
            ioException = e;
        }
        if (updates != null) {
            for (Update update : updates) {
                if (update.getUpdate_id() > lastKnownUpdateId) {
                    lastKnownUpdateId = update.getUpdate_id();
                    data = getNewUpdateAnalyticsData(timeout, limit, -1);
                    data.setReturned(update);
                    data.setAccessTime(accessTime);
                    analyticsWorker.putData(data);
                }
            }
        } else {
            data = getNewUpdateAnalyticsData(timeout, limit, -1);
            data.setIoException(ioException);
            data.setAccessTime(accessTime);
            analyticsWorker.putData(data);
        }
        if (ioException != null) {
            throw ioException;
        }
        return updates;
    }

    @Override
    public Message sendMessage(ChatId chat_id, String text, ParseMode parse_mode, boolean disable_web_page_preview, long reply_to_message_id, ReplyMarkup reply_markup) throws IOException {
        AnalyticsData data = new AnalyticsData("sendMessage");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("text", text);
        data.setValue("parse_mode", parse_mode);
        data.setValue("disable_web_page_preview", disable_web_page_preview);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.sendMessage(chat_id, text, parse_mode, disable_web_page_preview, reply_to_message_id, reply_markup);
            data.setReturned(message);
        } catch (IOException e) {
            ioException = e;
            data.setIoException(ioException);
        }
        analyticsWorker.putData(data);
        if (ioException != null) {
            throw ioException;
        }
        return message;
    }

    @Override
    public Message forwardMessage(ChatId chat_id, ChatId from_chat_id, long message_id) throws IOException {
        AnalyticsData data = new AnalyticsData("forwardMessage");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("from_chat_id", from_chat_id);
        data.setValue("message_id", message_id);
        try {
            message = bot.forwardMessage(chat_id, from_chat_id, message_id);
            data.setReturned(message);
        } catch (IOException e) {
            ioException = e;
        }
        analyticsWorker.putData(data);
        if (ioException != null) {
            throw ioException;
        }
        return message;
    }

    @Override
    public Message sendPhoto(ChatId chat_id, TelegramFile photo, String caption, long reply_to_message_id, ReplyMarkup reply_markup) throws IOException {
        AnalyticsData data = new AnalyticsData("sendPhoto");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("photo", photo);
        data.setValue("caption", caption);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.sendPhoto(chat_id, photo, caption, reply_to_message_id, reply_markup);
            data.setReturned(message);
        } catch (IOException e) {
            ioException = e;
            data.setIoException(ioException);
        }
        analyticsWorker.putData(data);
        if (ioException != null) {
            throw ioException;
        }
        return message;
    }

    @Override
    public Message sendAudio(ChatId chat_id, TelegramFile audio, int duration, String performer, String title, long reply_to_message_id, ReplyMarkup reply_markup) throws IOException {
        AnalyticsData data = new AnalyticsData("sendAudio");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("audio", audio);
        data.setValue("duration", duration);
        data.setValue("performer", performer);
        data.setValue("title", title);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.sendAudio(chat_id, audio, duration, performer, title, reply_to_message_id, reply_markup);
            data.setReturned(message);
        } catch (IOException e) {
            ioException = e;
            data.setIoException(ioException);
        }
        analyticsWorker.putData(data);
        if (ioException != null) {
            throw ioException;
        }
        return message;
    }

    @Override
    public Message sendDocument(ChatId chat_id, TelegramFile document, long reply_to_message_id, ReplyMarkup reply_markup) throws IOException {
        AnalyticsData data = new AnalyticsData("sendDocument");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("document", document);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.sendDocument(chat_id, document, reply_to_message_id, reply_markup);
            data.setReturned(message);
        } catch (IOException e) {
            ioException = e;
            data.setIoException(ioException);
        }
        analyticsWorker.putData(data);
        if (ioException != null) {
            throw ioException;
        }
        return message;
    }

    @Override
    public Message sendSticker(ChatId chat_id, TelegramFile sticker, long reply_to_message_id, ReplyMarkup reply_markup) throws IOException {
        AnalyticsData data = new AnalyticsData("sendSticker");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("sticker", sticker);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.sendSticker(chat_id, sticker, reply_to_message_id, reply_markup);
            data.setReturned(message);
        } catch (IOException e) {
            ioException = e;
            data.setIoException(ioException);
        }
        analyticsWorker.putData(data);
        if (ioException != null) {
            throw ioException;
        }
        return message;
    }

    @Override
    public Message sendVideo(ChatId chat_id, TelegramFile video, int duration, String caption, long reply_to_message_id, ReplyMarkup reply_markup) throws IOException {
        AnalyticsData data = new AnalyticsData("sendVideo");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("video", video);
        data.setValue("duration", duration);
        data.setValue("caption", caption);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.sendVideo(chat_id, video, duration, caption, reply_to_message_id, reply_markup);
            data.setReturned(message);
        } catch (IOException e) {
            ioException = e;
            data.setIoException(ioException);
        }
        analyticsWorker.putData(data);
        if (ioException != null) {
            throw ioException;
        }
        return message;
    }

    @Override
    public Message sendVoice(ChatId chat_id, TelegramFile voice, int duration, long reply_to_message_id, ReplyMarkup reply_markup) throws IOException {
        AnalyticsData data = new AnalyticsData("sendVoice");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("voice", voice);
        data.setValue("duration", duration);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.sendVoice(chat_id, voice, duration, reply_to_message_id, reply_markup);
            data.setReturned(message);
        } catch (IOException e) {
            ioException = e;
            data.setIoException(ioException);
        }
        analyticsWorker.putData(data);
        if (ioException != null) {
            throw ioException;
        }
        return message;
    }

    @Override
    public Message sendLocation(ChatId chat_id, float latitude, float longitude, long reply_to_message_id, ReplyMarkup reply_markup) throws IOException {
        AnalyticsData data = new AnalyticsData("sendLocation");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("latitude", latitude);
        data.setValue("longitude", longitude);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.sendLocation(chat_id, latitude, longitude, reply_to_message_id, reply_markup);
            data.setReturned(message);
        } catch (IOException e) {
            ioException = e;
            data.setIoException(ioException);
        }
        analyticsWorker.putData(data);
        if (ioException != null) {
            throw ioException;
        }
        return message;
    }

    @Override
    public boolean answerInlineQuery(String inline_query_id, InlineQueryResult[] results, String next_offset, boolean is_personal, int cache_time) throws IOException {
        AnalyticsData data = new AnalyticsData("answerInlineQuery");
        IOException ioException = null;
        boolean returned = false;
        data.setValue("inline_query_id", inline_query_id);
        data.setValue("results", results);
        data.setValue("next_offset", next_offset);
        data.setValue("is_personal", is_personal);
        data.setValue("cache_time", cache_time);
        try {
            returned = bot.answerInlineQuery(inline_query_id, results, next_offset, is_personal, cache_time);
            data.setReturned(returned);
        } catch (IOException e) {
            ioException = e;
            data.setIoException(ioException);
        }
        analyticsWorker.putData(data);
        if (ioException != null) {
            throw ioException;
        }
        return returned;
    }

    @Override
    public boolean sendChatAction(ChatId chat_id, ChatAction action) throws IOException {
        AnalyticsData data = new AnalyticsData("sendChatAction");
        IOException ioException = null;
        boolean returned = false;
        data.setValue("chat_id", chat_id);
        data.setValue("action", action);
        try {
            returned = bot.sendChatAction(chat_id, action);
            data.setReturned(returned);
        } catch (IOException e) {
            ioException = e;
            data.setIoException(ioException);
        }
        analyticsWorker.putData(data);
        if (ioException != null) {
            throw ioException;
        }
        return returned;
    }
}
