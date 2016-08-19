# JBots
[![Build Status](https://travis-ci.org/shibme/jbots.svg)](https://travis-ci.org/shibme/jbots)
[![Dependency Status](https://www.versioneye.com/user/projects/56adffd47e03c700377e0046/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56adffd47e03c700377e0046)
[![Download](https://api.bintray.com/packages/shibme/maven/jbots/images/download.svg)](https://bintray.com/shibme/maven/jbots/_latestVersion)
[![Percentage of issues still open](http://isitmaintained.com/badge/open/shibme/jbots.svg)](http://isitmaintained.com/project/shibme/jbots "Percentage of issues still open")

A java framework for creating Telegram Bots

### Maven Dependency for Consumers
Add to your `pom.xml`
```xml
<dependency>
	<groupId>me.shib.java.lib</groupId>
	<artifactId>jbots</artifactId>
	<version>0.8.0</version>
</dependency>
```
Also, you'll have to add the main class as `me.shib.java.lib.jbots.JBotLauncher` when you use maven assembly plugin to create a runnable binary JAR.

### Example
Extend `me.shib.java.lib.jbots.JBotModel` abstract model class and create a new Model for your bot
```java
public final class YourModelClassName extends JBotModel {

    public YourModelClassName(JBotConfig config) {
        super(config);
    }

    @Override
    public Message onMessageFromAdmin(Message message) {
        try {
            return getBot().sendMessage(new ChatId(message.getChat().getId()), "Got a message from admin!");
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Message onCommand(Message message) {
        try {
            return getBot().sendMessage(new ChatId(message.getChat().getId()), "Got the command - " + message.getText());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Message onReceivingMessage(Message message) {
        try {
            return getBot().sendMessage(new ChatId(message.getChat().getId()), "Got a message from user!");
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean onInlineQuery(InlineQuery query) {
        try {
            return getBot().answerInlineQuery(query.getId(), new InlineQueryResult[]{new InlineQueryResultArticle("1", "Test Title", "Test Text")});
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean onChosenInlineResult(ChosenInlineResult chosenInlineResult) {
        System.out.println(chosenInlineResult);
        return true;
    }

    @Override
    public boolean onCallbackQuery(CallbackQuery callbackQuery) {
        System.out.println(callbackQuery);
        return true;
    }
}
```

##### Sample Config class
Create a class extending `me.shib.java.lib.jbots.JBotConfig` and override the methods that might be necessary
```java
public final class SampleConfig extends JBotConfig {

    private static final String botApiToken = "xxxxxxxxxxxxxxxxxxx";
    private static final Class<? extends JBot> botModelClass = XYZ.class;
    private static final int threadCount = 4;
    private static final int reportInterval = 43200;
    private static final int minimumAllowedRating = 5;
    private static final boolean handleMissedChats = true;
    private static final boolean defaultWorker = true;
    private static final long[] admins = {1111111111, 2222222222};

    @Override
    public int threadCount() {
        return threadCount;
    }

    @Override
    public int reportInterval() {
        return reportInterval;
    }

    @Override
    public int minimumAllowedRating() {
        return minimumAllowedRating;
    }

    @Override
    public boolean handleMissedChats() {
        return handleMissedChats;
    }

    @Override
    public boolean defaultWorker() {
        return defaultWorker;
    }

    @Override
    protected long[] admins() {
        return admins;
    }

    @Override
    public String botApiToken() {
        return botApiToken;
    }

    @Override
    public Class<? extends JBot> botModelClass() {
        return botModelClass;
    }
}
```