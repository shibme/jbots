# JBots
A java framework for creating Telegram Bots with less efforts.

### Build Status
[![Build Status](https://travis-ci.org/shibme/jbots.svg)](https://travis-ci.org/shibme/jbots)

### Maven Dependency for Consumers
Add to your `pom.xml`
```xml
<dependency>
	<groupId>me.shib.java.lib</groupId>
	<artifactId>jbots</artifactId>
	<version>0.3.2</version>
</dependency>
```
Also, you'll have to add the main class as `me.shib.java.lib.jbots.JBotLauncher` when you use maven assembly plugin to create a runnable binary JAR.

### Example
Extend `me.shib.java.lib.jbots.JBotModel` abstract model class and create a new Model for your bot
```java
public class YourModelClassName extends JBotModel {

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
    public Message sendStatusMessage(long chatId) {
        try {
            return getBot().sendMessage(new ChatId(chatId), getBot().getIdentity().getUsername() + " is running fine!");
        } catch (IOException e) {
            return null;
        }
    }
}
```

#####Sample Config file:
Create a file named `jbots-config.json` and add the following
```json
[
	{
		"botApiToken": "YourBotApiToken1",
		"botModelClassName": "com.example.YourModelClassName1",
		"commandList": ["/start","/help","/status","/scr"],
		"threadCount": 4,
		"adminIdList": [1111111111111, 2222222222222],
		"reportIntervalInSeconds": 604800,
		"analyticsEnabled": true,
		"botanProxyToken": "BotanToken1",
		"constants": {
			"channelName": "@ExampleChannel2"
		}
	},
	{
		"botApiToken": "YourBotApiToken2",
		"botModelClassName": "com.example.YourModelClassName2",
		"commandList": ["/start","/help","/status","/scr"],
		"threadCount": 2,
		"adminIdList": [1111111111111, 2222222222222],
		"reportIntervalInSeconds": 86400,
		"constants": {
			"channelName": "@ExampleChannel1"
		}
	}
]
```
* `botApiToken` - The API token that you receive when you create a bot with [@BotFather](https://telegram.me/BotFather).
* `botModelClassName` - The fully qualified class name of the bot's Model extended from JBotModel.
* `commandList` - The list of supported commands you may have.
* `threadCount` - The number of threads the bot should have.
* `adminIdList` - Use [@GO_Robot](https://telegram.me/GO_Robot) to find your telegram ID and add it to admin list.
* `reportIntervalInSeconds` - The intervals at which the Bot reports the Admins the status (More often to know if it is up and running). 
* `analyticsEnabled` - If set to true will enable analytics. Must have the right analytics/proxy dependency included.
* `botanProxyToken` - If you're using botan analytics, all you need to do is mention the token, if not the framework will use `jbotstats` analytics when enabled.