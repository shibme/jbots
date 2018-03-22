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
	<version>0.8.1</version>
</dependency>
```
Also, you'll have to add the main class as `me.shib.java.lib.jbots.JBotLauncher` when you use maven assembly plugin to create a runnable binary JAR.

### Example
Extend `me.shib.java.lib.jbots.JBot` abstract model class and create a new Model for your bot
```java
public final class SampleJBot extends JBot {
    public SampleJBot(JBotConfig config) {
        super(config);
    }

    @Override
    public MessageHandler onMessage(Message message) {
        return new MessageHandler(message) {
            @Override
            public boolean onCommandFromAdmin(String command, String argument) {
                try {
                    bot().sendMessage(new ChatId(message.getChat().getId()), "Got a command \"" + command + "\" with the following argument from admin:\n" + argument);
                    return true;
                } catch (IOException e) {
                    return true;
                }
            }

            @Override
            public boolean onCommandFromUser(String command, String argument) {
                try {
                    bot().sendMessage(new ChatId(message.getChat().getId()), "Got a command \"" + command + "\" with the following argument from user:\n" + argument);
                    return true;
                } catch (IOException e) {
                    return true;
                }
            }

            @Override
            public boolean onMessageFromAdmin() {
                try {
                    bot().sendMessage(new ChatId(message.getChat().getId()), "Got a message from admin!");
                    return true;
                } catch (IOException e) {
                    return true;
                }
            }

            @Override
            public boolean onMessageFromUser() {
                try {
                    bot().sendMessage(new ChatId(message.getChat().getId()), "Got a message from user!");
                    return true;
                } catch (IOException e) {
                    return true;
                }
            }
        };
    }

    @Override
    public void onInlineQuery(InlineQuery query) {
        System.out.println(query);
    }

    @Override
    public void onChosenInlineResult(ChosenInlineResult chosenInlineResult) {
        System.out.println(chosenInlineResult);
    }

    @Override
    public void onCallbackQuery(CallbackQuery callbackQuery) {
        System.out.println(callbackQuery);
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