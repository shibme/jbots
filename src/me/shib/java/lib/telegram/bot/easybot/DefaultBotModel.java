package me.shib.java.lib.telegram.bot.easybot;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.imageio.ImageIO;

import me.shib.java.lib.telegram.bot.service.TelegramBot;
import me.shib.java.lib.telegram.bot.service.TelegramBot.ChatAction;
import me.shib.java.lib.telegram.bot.types.ChatId;
import me.shib.java.lib.telegram.bot.types.Message;
import me.shib.java.lib.telegram.bot.types.ParseMode;
import me.shib.java.lib.telegram.bot.types.TelegramFile;
import me.shib.java.lib.telegram.bot.types.User;

public class DefaultBotModel implements TBotModel {

	private static final Date startTime = new Date();

	private TBotConfig tBotConfig;
	private UpdateReceiver updRecvr;
	private TBotModel appModel;

	protected DefaultBotModel(TBotConfig tBotConfig, TBotModel appModel) {
		this.appModel = appModel;
		this.tBotConfig = tBotConfig;
		this.updRecvr = UpdateReceiver.getDefaultInstance(tBotConfig.getBotApiToken());
	}

	public Message onReceivingMessage(TelegramBot tBotService, Message message) {
		if(appModel != null) {
			Message appModelReponseMessage = appModel.onReceivingMessage(tBotService, message);
			if(appModelReponseMessage != null) {
				return appModelReponseMessage;
			}
		}
		try {
			long[] admins = tBotConfig.getAdminIdList();
			if ((admins != null) && (admins.length > 0)) {
				for (long admin : admins) {
					try {
						tBotService.forwardMessage(new ChatId(admin), new ChatId(message.getFrom().getId()),
								message.getMessage_id());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return tBotService.sendMessage(new ChatId(message.getChat().getId()),
						"Hey " + message.getFrom().getFirst_name()
								+ ",Your request was taken. I'll get back to you ASAP.",
						ParseMode.None, false, message.getMessage_id());
			} else {
				return tBotService.sendMessage(new ChatId(message.getChat().getId()),
						"The support team is unavailable. Please try later.", ParseMode.None, false,
						message.getMessage_id());
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Message onMessageFromAdmin(TelegramBot tBotService, Message message) {
		if(appModel != null) {
			Message appModelReponseMessage = appModel.onMessageFromAdmin(tBotService, message);
			if(appModelReponseMessage != null) {
				return appModelReponseMessage;
			}
		}
		try {
			long replyToUser = message.getReply_to_message().getForward_from().getId();
			if (replyToUser > 0) {
				if (message.getText() != null) {
					return tBotService.sendMessage(new ChatId(replyToUser), message.getText());
				} else if (message.getDocument() != null) {
					return tBotService.sendDocument(new ChatId(replyToUser),
							new TelegramFile(message.getDocument().getFile_id()));
				} else if (message.getVideo() != null) {
					return tBotService.sendVideo(new ChatId(replyToUser),
							new TelegramFile(message.getVideo().getFile_id()));
				} else if (message.getPhoto() != null) {
					return tBotService.sendPhoto(new ChatId(replyToUser),
							new TelegramFile(message.getPhoto()[message.getPhoto().length - 1].getFile_id()));
				} else if (message.getAudio() != null) {
					return tBotService.sendDocument(new ChatId(replyToUser),
							new TelegramFile(message.getDocument().getFile_id()));
				} else if (message.getVoice() != null) {
					return tBotService.sendDocument(new ChatId(replyToUser),
							new TelegramFile(message.getDocument().getFile_id()));
				} else if (message.getSticker() != null) {
					return tBotService.sendDocument(new ChatId(replyToUser),
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
		User myself = updRecvr.whoAmI();
		return tBotService.sendMessage(new ChatId(message.getChat().getId()),
				"Hi " + message.getFrom().getFirst_name() + ". My name is *" + myself.getFirst_name()
						+ "* (@" + myself.getUsername() + "). I'll try to serve you the best"
								+ " with all my efforts. Welcome!", ParseMode.Markdown);
	}
	
	public Message onCommand(TelegramBot tBotService, Message message) {
		if(appModel != null) {
			Message appModelReponseMessage = appModel.onCommand(tBotService, message);
			if(appModelReponseMessage != null) {
				return appModelReponseMessage;
			}
		}
		switch (message.getText()) {
		case "/start":
			if (tBotConfig.isValidCommand("/start")) {
				try {
					return onStartAndHelp(tBotService, message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;
		case "/help":
			if (tBotConfig.isValidCommand("/help")) {
				try {
					return onStartAndHelp(tBotService, message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;
		case "/scr":
			if (tBotConfig.isValidCommand("/scr") && tBotConfig.isAdmin(message.getChat().getId())) {
				try {
					tBotService.sendChatAction(new ChatId(message.getChat().getId()), ChatAction.upload_document);
					File screenShotFile = getCurrentScreenShotFile();
					if (screenShotFile != null) {
						Message returMessage = tBotService.sendDocument(new ChatId(message.getChat().getId()),
								new TelegramFile(screenShotFile));
						screenShotFile.delete();
						return returMessage;
					}
					return tBotService.sendMessage(new ChatId(message.getChat().getId()),
							"I couldn't take a screenshot right now. I'm sorry.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;
		case "/status":
			if (tBotConfig.isValidCommand("/status") && tBotConfig.isAdmin(message.getChat().getId())) {
				try {
					tBotService.sendChatAction(new ChatId(message.getChat().getId()), ChatAction.typing);
					return sendStatusMessage(tBotService, message.getChat().getId());
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
	public Message sendStatusMessage(TelegramBot tBotService, long chatId) {
		if(appModel != null) {
			Message appModelReponseMessage = appModel.sendStatusMessage(tBotService, chatId);
			if(appModelReponseMessage != null) {
				return appModelReponseMessage;
			}
		}
		try {
			return tBotService.sendMessage(new ChatId(chatId),
					"Reporting status:\nHost: " + getHostInfo() + "\nUp Time: " + getUpTime());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
