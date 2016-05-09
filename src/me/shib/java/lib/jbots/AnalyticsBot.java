package me.shib.java.lib.jbots;

import me.shib.java.lib.jbotstats.AnalyticsData;
import me.shib.java.lib.jbotstats.AnalyticsWorker;
import me.shib.java.lib.jbotstats.JBotStats;
import me.shib.java.lib.jtelebot.models.inline.InlineKeyboardMarkup;
import me.shib.java.lib.jtelebot.models.inline.InlineQueryResult;
import me.shib.java.lib.jtelebot.models.types.*;
import me.shib.java.lib.jtelebot.models.updates.Message;
import me.shib.java.lib.jtelebot.models.updates.Update;
import me.shib.java.lib.jtelebot.service.TelegramBot;
import me.shib.java.lib.restiny.HTTPFileDownloader;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

final class AnalyticsBot extends TelegramBot {

    private TelegramBot bot;
    private JBotStats jBotStats;
    private AnalyticsWorker analyticsWorker;
    private long lastKnownUpdateId;
    private Map<Long, Map<String, String>> analyticsUserURLMap;

    AnalyticsBot(TelegramBot bot, JBotStats jBotStats) {
        this.bot = bot;
        this.jBotStats = jBotStats;
        this.analyticsWorker = new AnalyticsWorker(jBotStats);
        this.lastKnownUpdateId = 0;
        this.analyticsUserURLMap = new HashMap<>();
    }

    String getAnalyticsRedirectedURL(long user_id, String url) {
        Map<String, String> urlMap = analyticsUserURLMap.get(user_id);
        if (urlMap == null) {
            urlMap = new HashMap<>();
            analyticsUserURLMap.put(user_id, urlMap);
        }
        String analyticsURL = urlMap.get(url);
        if (analyticsURL == null) {
            analyticsURL = this.jBotStats.getAnalyticsRedirectedURL(user_id, url);
            if (analyticsURL != null) {
                urlMap.put(url, analyticsURL);
            }
        }
        return analyticsURL;
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
    public TFile getFile(String file_id) throws IOException {
        AnalyticsData data = new AnalyticsData("getFile");
        IOException ioException = null;
        TFile telegramFile = null;
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
    public Message sendMessage(ChatId chat_id, String text, ParseMode parse_mode, boolean disable_web_page_preview, long reply_to_message_id, ReplyMarkup reply_markup, boolean disable_notification) throws IOException {
        AnalyticsData data = new AnalyticsData("sendMessage");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("text", text);
        data.setValue("disable_notification", disable_notification);
        data.setValue("parse_mode", parse_mode);
        data.setValue("disable_web_page_preview", disable_web_page_preview);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.sendMessage(chat_id, text, parse_mode, disable_web_page_preview, reply_to_message_id, reply_markup, disable_notification);
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
    public Message forwardMessage(ChatId chat_id, ChatId from_chat_id, long message_id, boolean disable_notification) throws IOException {
        AnalyticsData data = new AnalyticsData("forwardMessage");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("from_chat_id", from_chat_id);
        data.setValue("message_id", message_id);
        data.setValue("disable_notification", disable_notification);
        try {
            message = bot.forwardMessage(chat_id, from_chat_id, message_id, disable_notification);
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
    public Message sendPhoto(ChatId chat_id, InputFile photo, String caption, long reply_to_message_id, ReplyMarkup reply_markup, boolean disable_notification) throws IOException {
        AnalyticsData data = new AnalyticsData("sendPhoto");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("photo", photo);
        data.setValue("disable_notification", disable_notification);
        data.setValue("caption", caption);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.sendPhoto(chat_id, photo, caption, reply_to_message_id, reply_markup, disable_notification);
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
    public Message sendAudio(ChatId chat_id, InputFile audio, int duration, String performer, String title, long reply_to_message_id, ReplyMarkup reply_markup, boolean disable_notification) throws IOException {
        AnalyticsData data = new AnalyticsData("sendAudio");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("audio", audio);
        data.setValue("disable_notification", disable_notification);
        data.setValue("duration", duration);
        data.setValue("performer", performer);
        data.setValue("title", title);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.sendAudio(chat_id, audio, duration, performer, title, reply_to_message_id, reply_markup, disable_notification);
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
    public Message sendDocument(ChatId chat_id, InputFile document, String caption, long reply_to_message_id, ReplyMarkup reply_markup, boolean disable_notification) throws IOException {
        AnalyticsData data = new AnalyticsData("sendDocument");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("document", document);
        data.setValue("caption", caption);
        data.setValue("disable_notification", disable_notification);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.sendDocument(chat_id, document, caption, reply_to_message_id, reply_markup, disable_notification);
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
    public Message sendSticker(ChatId chat_id, InputFile sticker, long reply_to_message_id, ReplyMarkup reply_markup, boolean disable_notification) throws IOException {
        AnalyticsData data = new AnalyticsData("sendSticker");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("sticker", sticker);
        data.setValue("disable_notification", disable_notification);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.sendSticker(chat_id, sticker, reply_to_message_id, reply_markup, disable_notification);
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
    public Message sendVideo(ChatId chat_id, InputFile video, int duration, String caption, long reply_to_message_id, ReplyMarkup reply_markup, int width, int height, boolean disable_notification) throws IOException {
        AnalyticsData data = new AnalyticsData("sendVideo");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("video", video);
        data.setValue("disable_notification", disable_notification);
        data.setValue("duration", duration);
        data.setValue("caption", caption);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("width", width);
        data.setValue("height", height);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.sendVideo(chat_id, video, duration, caption, reply_to_message_id, reply_markup, width, height, disable_notification);
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
    public Message sendVoice(ChatId chat_id, InputFile voice, int duration, long reply_to_message_id, ReplyMarkup reply_markup, boolean disable_notification) throws IOException {
        AnalyticsData data = new AnalyticsData("sendVoice");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("voice", voice);
        data.setValue("disable_notification", disable_notification);
        data.setValue("duration", duration);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.sendVoice(chat_id, voice, duration, reply_to_message_id, reply_markup, disable_notification);
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

    private Message sendLocationOrVenue(String methodName, ChatId chat_id, float latitude, float longitude, String title, String address, String foursquare_id, long reply_to_message_id, ReplyMarkup reply_markup, boolean disable_notification) throws IOException {
        AnalyticsData data = new AnalyticsData(methodName);
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("latitude", latitude);
        data.setValue("longitude", longitude);
        data.setValue("disable_notification", disable_notification);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        try {
            if (methodName.equalsIgnoreCase("sendVenue")) {
                data.setValue("title", title);
                data.setValue("address", address);
                data.setValue("foursquare_id", foursquare_id);
                message = bot.sendVenue(chat_id, latitude, longitude, title, address, foursquare_id, reply_to_message_id, reply_markup, disable_notification);
            } else {
                message = bot.sendLocation(chat_id, latitude, longitude, reply_to_message_id, reply_markup, disable_notification);
            }
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
    public Message sendLocation(ChatId chat_id, float latitude, float longitude, long reply_to_message_id, ReplyMarkup reply_markup, boolean disable_notification) throws IOException {
        return sendLocationOrVenue("sendLocation", chat_id, latitude, longitude, null, null, null, reply_to_message_id, reply_markup, disable_notification);
    }

    @Override
    public Message sendVenue(ChatId chat_id, float latitude, float longitude, String title, String address, String foursquare_id, long reply_to_message_id, ReplyMarkup reply_markup, boolean disable_notification) throws IOException {
        return sendLocationOrVenue("sendVenue", chat_id, latitude, longitude, title, address, foursquare_id, reply_to_message_id, reply_markup, disable_notification);
    }

    @Override
    public Message sendContact(ChatId chat_id, String phone_number, String first_name, String last_name, long reply_to_message_id, ReplyMarkup reply_markup, boolean disable_notification) throws IOException {
        AnalyticsData data = new AnalyticsData("sendContact");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("phone_number", phone_number);
        data.setValue("first_name", first_name);
        data.setValue("last_name", last_name);
        data.setValue("reply_to_message_id", reply_to_message_id);
        data.setValue("reply_markup", reply_markup);
        data.setValue("disable_notification", disable_notification);
        try {
            message = bot.sendContact(chat_id, phone_number, first_name, last_name, reply_to_message_id, reply_markup, disable_notification);
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
    public boolean answerInlineQuery(String inline_query_id, InlineQueryResult[] results, String next_offset, boolean is_personal, int cache_time, String switch_pm_text, String switch_pm_parameter) throws IOException {
        AnalyticsData data = new AnalyticsData("answerInlineQuery");
        IOException ioException = null;
        boolean returned = false;
        data.setValue("inline_query_id", inline_query_id);
        data.setValue("results", results);
        data.setValue("next_offset", next_offset);
        data.setValue("is_personal", is_personal);
        data.setValue("cache_time", cache_time);
        data.setValue("switch_pm_text", switch_pm_text);
        data.setValue("switch_pm_parameter", switch_pm_parameter);
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
    public boolean kickChatMember(ChatId chat_id, long user_id) throws IOException {
        AnalyticsData data = new AnalyticsData("kickChatMember");
        IOException ioException = null;
        boolean returned = false;
        data.setValue("chat_id", chat_id);
        data.setValue("user_id", user_id);
        try {
            returned = bot.kickChatMember(chat_id, user_id);
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
    public boolean unbanChatMember(ChatId chat_id, long user_id) throws IOException {
        AnalyticsData data = new AnalyticsData("unbanChatMember");
        IOException ioException = null;
        boolean returned = false;
        data.setValue("chat_id", chat_id);
        data.setValue("user_id", user_id);
        try {
            returned = bot.unbanChatMember(chat_id, user_id);
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
    public boolean answerCallbackQuery(String callback_query_id, String text, boolean show_alert) throws IOException {
        AnalyticsData data = new AnalyticsData("answerCallbackQuery");
        IOException ioException = null;
        boolean returned = false;
        data.setValue("callback_query_id", callback_query_id);
        data.setValue("text", text);
        data.setValue("show_alert", show_alert);
        try {
            returned = bot.answerCallbackQuery(callback_query_id, text, show_alert);
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
    public Message editMessageText(ChatId chat_id, long message_id, String text, ParseMode parse_mode, boolean disable_web_page_preview, InlineKeyboardMarkup reply_markup) throws IOException {
        AnalyticsData data = new AnalyticsData("editMessageText");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("message_id", message_id);
        data.setValue("text", text);
        data.setValue("parse_mode", parse_mode);
        data.setValue("disable_web_page_preview", disable_web_page_preview);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.editMessageText(chat_id, message_id, text, parse_mode, disable_web_page_preview, reply_markup);
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
    public boolean editMessageText(String inline_message_id, String text, ParseMode parse_mode, boolean disable_web_page_preview, InlineKeyboardMarkup reply_markup) throws IOException {
        AnalyticsData data = new AnalyticsData("editMessageText");
        IOException ioException = null;
        boolean returned = false;
        data.setValue("inline_message_id", inline_message_id);
        data.setValue("text", text);
        data.setValue("parse_mode", parse_mode);
        data.setValue("disable_web_page_preview", disable_web_page_preview);
        data.setValue("reply_markup", reply_markup);
        try {
            returned = bot.editMessageText(inline_message_id, text, parse_mode, disable_web_page_preview, reply_markup);
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
    public Message editMessageCaption(ChatId chat_id, long message_id, String caption, InlineKeyboardMarkup reply_markup) throws IOException {
        AnalyticsData data = new AnalyticsData("editMessageCaption");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("message_id", message_id);
        data.setValue("caption", caption);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.editMessageCaption(chat_id, message_id, caption, reply_markup);
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
    public boolean editMessageCaption(String inline_message_id, String caption, InlineKeyboardMarkup reply_markup) throws IOException {
        AnalyticsData data = new AnalyticsData("editMessageCaption");
        IOException ioException = null;
        boolean returned = false;
        data.setValue("inline_message_id", inline_message_id);
        data.setValue("caption", caption);
        data.setValue("reply_markup", reply_markup);
        try {
            returned = bot.editMessageCaption(inline_message_id, caption, reply_markup);
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
    public Message editMessageReplyMarkup(ChatId chat_id, long message_id, InlineKeyboardMarkup reply_markup) throws IOException {
        AnalyticsData data = new AnalyticsData("editMessageReplyMarkup");
        IOException ioException = null;
        Message message = null;
        data.setValue("chat_id", chat_id);
        data.setValue("message_id", message_id);
        data.setValue("reply_markup", reply_markup);
        try {
            message = bot.editMessageReplyMarkup(chat_id, message_id, reply_markup);
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
    public boolean editMessageReplyMarkup(String inline_message_id, InlineKeyboardMarkup reply_markup) throws IOException {
        AnalyticsData data = new AnalyticsData("editMessageReplyMarkup");
        IOException ioException = null;
        boolean returned = false;
        data.setValue("inline_message_id", inline_message_id);
        data.setValue("reply_markup", reply_markup);
        try {
            returned = bot.editMessageReplyMarkup(inline_message_id, reply_markup);
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
