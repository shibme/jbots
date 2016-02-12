package me.shib.java.lib.jbots;

import me.shib.java.lib.jbotstats.AnalyticsData;
import me.shib.java.lib.jbotstats.AnalyticsWorker;
import me.shib.java.lib.jbotstats.JBotStats;
import me.shib.java.lib.jtelebot.service.TelegramBot;
import me.shib.java.lib.jtelebot.types.*;
import me.shib.java.lib.rest.client.HTTPFileDownloader;

import java.io.File;
import java.io.IOException;

public class AnalyticsBot extends TelegramBot {

    private TelegramBot bot;
    private AnalyticsWorker analyticsWorker;
    private long lastKnownUpdateId;

    protected AnalyticsBot(JBotStats jBotStats, TelegramBot bot) {
        this.bot = bot;
        this.analyticsWorker = new AnalyticsWorker(jBotStats);
        this.analyticsWorker.start();
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
        User me = bot.getMe();
        AnalyticsData data = new AnalyticsData("getMe", me);
        analyticsWorker.putData(data);
        return me;
    }

    @Override
    public UserProfilePhotos getUserProfilePhotos(long user_id, int offset, int limit) throws IOException {
        UserProfilePhotos photos = bot.getUserProfilePhotos(user_id, offset, limit);
        AnalyticsData data = new AnalyticsData("getUserProfilePhotos", photos);
        data.setValue("user_id", user_id);
        data.setValue("offset", offset);
        data.setValue("limit", limit);
        analyticsWorker.putData(data);
        return photos;
    }

    @Override
    public TelegramFile getFile(String file_id) throws IOException {
        TelegramFile telegramFile = bot.getFile(file_id);
        AnalyticsData data = new AnalyticsData("getFile", telegramFile);
        data.setValue("file_id", file_id);
        analyticsWorker.putData(data);
        return telegramFile;
    }

    @Override
    public HTTPFileDownloader.DownloadProgress downloadToFile(String file_id, File downloadToFile, boolean waitForCompletion) throws IOException {
        HTTPFileDownloader.DownloadProgress progress = bot.downloadToFile(file_id, downloadToFile, waitForCompletion);
        AnalyticsData data = new AnalyticsData("downloadToFile", progress);
        data.setValue("file_id", file_id);
        data.setValue("downloadToFile", downloadToFile);
        data.setValue("waitForCompletion", waitForCompletion);
        analyticsWorker.putData(data);
        return progress;
    }

    @Override
    public File downloadFile(String file_id, File downloadToFile) throws IOException {
        File file = bot.downloadFile(file_id, downloadToFile);
        AnalyticsData data = new AnalyticsData("downloadFile", file);
        data.setValue("file_id", file_id);
        data.setValue("downloadToFile", downloadToFile);
        analyticsWorker.putData(data);
        return file;
    }

    @Override
    public Update[] getUpdates(int timeout, int limit, long offset) throws IOException {
        Update[] updates = bot.getUpdates(timeout, limit, offset);
        if (updates != null) {
            for (Update update : updates) {
                if (update.getUpdate_id() > lastKnownUpdateId) {
                    lastKnownUpdateId = update.getUpdate_id();
                    AnalyticsData data = new AnalyticsData("getUpdates", update);
                    data.setValue("timeout", timeout);
                    data.setValue("limit", limit);
                    data.setValue("offset", offset);
                    analyticsWorker.putData(data);
                }
            }
        }
        return updates;
    }

    @Override
    public Update[] getUpdates(int timeout, int limit) throws IOException {
        Update[] updates = bot.getUpdates(timeout, limit);
        if (updates != null) {
            for (Update update : updates) {
                if (update.getUpdate_id() > lastKnownUpdateId) {
                    lastKnownUpdateId = update.getUpdate_id();
                    AnalyticsData data = new AnalyticsData("getUpdates", update);
                    data.setValue("timeout", timeout);
                    data.setValue("limit", limit);
                    analyticsWorker.putData(data);
                }
            }
        }
        return updates;
    }

    @Override
    public Message sendMessage(ChatId chat_id, String text, ParseMode parse_mode, boolean disable_web_page_preview, long reply_to_message_id, ReplyMarkup reply_markup) throws IOException {
        Message message = bot.sendMessage(chat_id, text, parse_mode, disable_web_page_preview, reply_to_message_id, reply_markup);
        AnalyticsData data = new AnalyticsData("sendMessage", message);
        data.setValue("chat_id", chat_id);
        data.setValue("text", text);
        data.setValue("parse_mode", parse_mode);
        data.setValue("disable_web_page_preview", disable_web_page_preview);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        analyticsWorker.putData(data);
        return message;
    }

    @Override
    public Message forwardMessage(ChatId chat_id, ChatId from_chat_id, long message_id) throws IOException {
        Message message = bot.forwardMessage(chat_id, from_chat_id, message_id);
        AnalyticsData data = new AnalyticsData("forwardMessage", message);
        data.setValue("chat_id", chat_id);
        data.setValue("from_chat_id", from_chat_id);
        data.setValue("message_id", message_id);
        analyticsWorker.putData(data);
        return message;
    }

    @Override
    public Message sendPhoto(ChatId chat_id, TelegramFile photo, String caption, long reply_to_message_id, ReplyMarkup reply_markup) throws IOException {
        Message message = bot.sendPhoto(chat_id, photo, caption, reply_to_message_id, reply_markup);
        AnalyticsData data = new AnalyticsData("sendPhoto", message);
        data.setValue("chat_id", chat_id);
        data.setValue("photo", photo);
        data.setValue("caption", caption);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        analyticsWorker.putData(data);
        return message;
    }

    @Override
    public Message sendAudio(ChatId chat_id, TelegramFile audio, int duration, String performer, String title, long reply_to_message_id, ReplyMarkup reply_markup) throws IOException {
        Message message = bot.sendAudio(chat_id, audio, duration, performer, title, reply_to_message_id, reply_markup);
        AnalyticsData data = new AnalyticsData("sendAudio", message);
        data.setValue("chat_id", chat_id);
        data.setValue("audio", audio);
        data.setValue("duration", duration);
        data.setValue("performer", performer);
        data.setValue("title", title);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        analyticsWorker.putData(data);
        return message;
    }

    @Override
    public Message sendDocument(ChatId chat_id, TelegramFile document, long reply_to_message_id, ReplyMarkup reply_markup) throws IOException {
        Message message = bot.sendDocument(chat_id, document, reply_to_message_id, reply_markup);
        AnalyticsData data = new AnalyticsData("sendDocument", message);
        data.setValue("chat_id", chat_id);
        data.setValue("document", document);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        analyticsWorker.putData(data);
        return message;
    }

    @Override
    public Message sendSticker(ChatId chat_id, TelegramFile sticker, long reply_to_message_id, ReplyMarkup reply_markup) throws IOException {
        Message message = bot.sendSticker(chat_id, sticker, reply_to_message_id, reply_markup);
        AnalyticsData data = new AnalyticsData("sendSticker", message);
        data.setValue("chat_id", chat_id);
        data.setValue("sticker", sticker);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        analyticsWorker.putData(data);
        return message;
    }

    @Override
    public Message sendVideo(ChatId chat_id, TelegramFile video, int duration, String caption, long reply_to_message_id, ReplyMarkup reply_markup) throws IOException {
        Message message = bot.sendVideo(chat_id, video, duration, caption, reply_to_message_id, reply_markup);
        AnalyticsData data = new AnalyticsData("sendVideo", message);
        data.setValue("chat_id", chat_id);
        data.setValue("video", video);
        data.setValue("duration", duration);
        data.setValue("caption", caption);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        analyticsWorker.putData(data);
        return message;
    }

    @Override
    public Message sendVoice(ChatId chat_id, TelegramFile voice, int duration, long reply_to_message_id, ReplyMarkup reply_markup) throws IOException {
        Message message = bot.sendVoice(chat_id, voice, duration, reply_to_message_id, reply_markup);
        AnalyticsData data = new AnalyticsData("sendVoice", message);
        data.setValue("chat_id", chat_id);
        data.setValue("voice", voice);
        data.setValue("duration", duration);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        analyticsWorker.putData(data);
        return message;
    }

    @Override
    public Message sendLocation(ChatId chat_id, float latitude, float longitude, long reply_to_message_id, ReplyMarkup reply_markup) throws IOException {
        Message message = bot.sendLocation(chat_id, latitude, longitude, reply_to_message_id, reply_markup);
        AnalyticsData data = new AnalyticsData("sendLocation", message);
        data.setValue("chat_id", chat_id);
        data.setValue("latitude", latitude);
        data.setValue("longitude", longitude);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        analyticsWorker.putData(data);
        return message;
    }

    @Override
    public boolean answerInlineQuery(String inline_query_id, InlineQueryResult[] results, String next_offset, boolean is_personal, int cache_time) throws IOException {
        boolean returned = bot.answerInlineQuery(inline_query_id, results, next_offset, is_personal, cache_time);
        AnalyticsData data = new AnalyticsData("answerInlineQuery", returned);
        data.setValue("inline_query_id", inline_query_id);
        data.setValue("results", results);
        data.setValue("next_offset", next_offset);
        data.setValue("is_personal", is_personal);
        data.setValue("cache_time", cache_time);
        analyticsWorker.putData(data);
        return returned;
    }

    @Override
    public boolean sendChatAction(ChatId chat_id, ChatAction action) throws IOException {
        boolean returned = bot.sendChatAction(chat_id, action);
        AnalyticsData data = new AnalyticsData("sendChatAction", returned);
        data.setValue("chat_id", chat_id);
        data.setValue("action", action);
        analyticsWorker.putData(data);
        return returned;
    }
}
