package telegrambot.commands.todos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.quartz.SchedulerException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup.InlineKeyboardMarkupBuilder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.vdurmont.emoji.EmojiParser;

import telegrambot.commands.CallbackOrientedBotCommand;
import telegrambot.familyassistant.Emoji;
import telegrambot.familyassistant.FamilyAssistantBot;
import telegrambot.todos.IReminderEntity;
import telegrambot.todos.ITask;
import telegrambot.todos.Reminder;

public class ListTasks extends CallbackOrientedBotCommand {
	// Константы для построения запросов обратного вызова (CallbackQuery) с inline-кнопок
	protected final String TIME_FORMAT_PATTERN = "dd-MM-yyyy-HH-mm";
	protected final String TASK = "task";
	protected final String REMIND = "remind";
	protected final String CHECKED = "checked";
	protected final String COMPLETE = "complete";
	protected final String CANCEL = "cancel";
	protected final String OK = "accepted";
	
	protected AbsSender as;
	protected String prefix, currentUserId, currentChatId, callback;
	protected Reminder reminder;
	protected List<ITask> taskList;
	protected List<IReminderEntity> remindList;
	protected InlineKeyboardButton complete, cancel, ok;

	public ListTasks(String commandIdentifier, String description) {
		super(commandIdentifier, description);
		prefix = getCallbackDataPrefix();
		callback = "";
		try {
			reminder = FamilyAssistantBot.getReminder();
		} catch (SchedulerException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		complete = new InlineKeyboardButton("Завершить");
		cancel = new InlineKeyboardButton("Отменить");
		ok = new InlineKeyboardButton("Ок, спасибо!");
	}
	
	protected SendMessage makeTaskListKeyboard(List<ITask> taskList) {
		
		InlineKeyboardMarkupBuilder taskListKeyboardBuilder = InlineKeyboardMarkup.builder();
		
		if (taskList.isEmpty()) {
			return SendMessage.builder()
					.text("Список ваших задач - пуст, т.к. вы еще не создали ни одной задачи")
					.chatId(currentChatId)
					.build();
		} else {
			/**
			 * Поскольку Telegram Bot API декларирует максимально возможный размер запроса обратного вызова - 64 байта,
			 * то для создания запросов обратного вызова для кнопок, ассоциированных с каждой задачей пользователя, будем
			 * использовать нумерацию кнопок в запросах вместо их имен.
			 */
			for (int i = 0; i < taskList.size(); i++) {
				InlineKeyboardButton taskButton = new InlineKeyboardButton(taskList.get(i).getName());
				taskButton.setCallbackData(prefix + "-" + TASK + "-" + CHECKED + "-" + i);
				taskListKeyboardBuilder.keyboardRow(List.of(taskButton));
			}
			return SendMessage.builder()
					.text("Cписок ваших текущих задач. Нажмите на любую из них, чтобы посмотреть описание задачи, завершить "
							+ "задачу или отменить задачу")
					.chatId(currentChatId)
					.replyMarkup(taskListKeyboardBuilder.build())
					.build();
		}
	}
	
	protected SendMessage makeRemindListKeyboard(List<IReminderEntity> remindList) {
		
		InlineKeyboardMarkupBuilder remindListKeyboardBuilder = InlineKeyboardMarkup.builder();
		
		if (remindList.isEmpty()) {
			return SendMessage.builder()
					.text("Список ваших напоминаний - пуст, т.к. вы еще не запланировали ни одного напоминания")
					.chatId(currentChatId)
					.build();
		} else {
			for (int i = 0; i < remindList.size(); i++) {
				InlineKeyboardButton remindButton = new InlineKeyboardButton(remindList.get(i).getTask().getName());
				remindButton.setCallbackData(prefix + "-" + REMIND + "-" + CHECKED + "-" + i);
				remindListKeyboardBuilder.keyboardRow(List.of(remindButton));
			}
			return SendMessage.builder()
					.text("Список ваших запланированных напоминаний. Нажмите на любое из них, штобы посмотреть описание напоминания, завершить "
							+ "задачу, связанную с напоминанием, или отменить задачу и напоминание")
					.chatId(currentChatId)
					.replyMarkup(remindListKeyboardBuilder.build())
					.build();
		}
	}

	protected String toHumanReadableDate(String dateString) {
		return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN)).format(DateTimeFormatter.ISO_DATE_TIME);
	}
	
	protected String toHumanReadableChronoUnits(ChronoUnit unit) {
		switch(unit) {
		case SECONDS:
			return "секунд";
		case MINUTES:
			return "минут";
		case HOURS:
			return "часов";
		case DAYS:
			return "дней";
		case MONTHS:
			return "месяцев";
		case YEARS:
			return "лет";
		default:
			return "дней";
		}
	}
	
	protected SendMessage taskActionMenu(int taskNumber) {
		
		var task = taskList.get(taskNumber);
		complete.setCallbackData(prefix + "-" + TASK + "-" + COMPLETE + "-" + taskNumber);
		cancel.setCallbackData(prefix + "-" + TASK + "-" + CANCEL + "-" + taskNumber);
		ok.setCallbackData(prefix + "-" + TASK + "-" + OK);
		
		String taskString = EmojiParser.parseToUnicode(
				Emoji.PUSHPIN + " " + task.getName().toUpperCase() + "\n"
				+ Emoji.MEMO + " " + task.getDescription() + "\n"
						+ "ТЕГИ: \n\t" + task.getTags());
		
		return SendMessage.builder()
				.text(taskString)
				.chatId(currentChatId)
				.replyMarkup(InlineKeyboardMarkup.builder()
						.keyboardRow(List.of(complete, cancel))
						.keyboardRow(List.of(ok))
						.build())
				.build();
	}
	
	protected SendMessage remindActionMenu(int remindNumber) {
		
		var remind = remindList.get(remindNumber);
		complete.setCallbackData(prefix + "-" + REMIND + "-" + COMPLETE + "-" + remindNumber);
		cancel.setCallbackData(prefix + "-" + REMIND + "-" + CANCEL + "-" + remindNumber);
		ok.setCallbackData(prefix + "-" + REMIND + "-" + OK);
		
		String remindString = EmojiParser.parseToUnicode(
				Emoji.PUSHPIN + " " + remind.getTask().getName().toUpperCase() + "\n"
				+ Emoji.MEMO + " " + remind.getTask().getDescription() + "\n"
				+ "ТЕГИ: \n\t" + remind.getTask().getTags() + "\n"
				+ Emoji.DATE + " " + "СРОК ИСПОЛНЕНИЯ: " + toHumanReadableDate(remind.getScheduledTime()) + "\n"
				+ (remind.getRemindTimes() == 0 ? "" : Emoji.POINT_RIGHT + " " + "НАПОМНИТЬ за " + remind.getMinutesBefore() + " минут до дедлайна \n" 
				+ (remind.getRemindTimes() == 1 ? "" : remind.getRemindTimes() + " раз с интервалом в " + remind.getRemindTimesInterval()
				+ " минут")) + "\n"
				+ (remind.getRepeatInterval() > 0 ? Emoji.POINT_RIGHT + " " + "ПОВТОРЯТЬ задачу с интервалом " + remind.getRepeatInterval() + " "
				+ toHumanReadableChronoUnits(remind.getRepeatIntervalUnit()) : ""));
		
		return SendMessage.builder()
				.text(remindString)
				.chatId(currentChatId)
				.replyMarkup(InlineKeyboardMarkup.builder()
						.keyboardRow(List.of(complete, cancel))
						.keyboardRow(List.of(ok))
						.build())
				.build();
	}
	
	@Override
	public boolean catched(Update update) {
		return ((update.hasCallbackQuery() && update.getCallbackQuery().getData().contains(prefix)
				&& update.getCallbackQuery().getFrom().getId().toString().equals(currentUserId))
				|| (update.hasMessage() && update.getMessage().getChatId().toString().equals(currentChatId))) && waitingUpdates;
	}

	@Override
	public SendMessage getAnswer(Update update) {
		String[] split = null;
		int numberOfTaskOrRemind = 0;
		if (update.hasCallbackQuery()) {
			callback = update.getCallbackQuery().getData().substring(prefix.length());
			split = callback.split("-");
			try {
				numberOfTaskOrRemind = Integer.parseInt(split[split.length - 1]);
			} catch (NumberFormatException e) {
				System.out.println(e.getMessage());
				System.out.println("Значит, это здесь и - не нужно :)");
			}
		}
		try {
			if (callback.contains(TASK + "-" + CHECKED)) {
				
				return taskActionMenu(numberOfTaskOrRemind);
				
			} else if (callback.contains(REMIND + "-" + CHECKED)) {
				
				return remindActionMenu(numberOfTaskOrRemind);
				
			} else if (callback.contains(TASK + "-" + COMPLETE)) {
				
				var task = taskList.get(numberOfTaskOrRemind);
				waitingUpdates = false;
				taskList.remove(task);
				return SendMessage.builder()
						.text(EmojiParser.parseToUnicode(
								Emoji.WHITE_CHECK_MARK + "Задача \"" + reminder.completeTask(task).getName()
								+ "\" успешно завершена!"))
						.chatId(currentChatId)
						.build();
			} else if (callback.contains(TASK + "-" + CANCEL)) {
				
				var task = taskList.get(numberOfTaskOrRemind);
				waitingUpdates = false;
				taskList.remove(task);
				return SendMessage.builder()
						.text(EmojiParser.parseToUnicode(
								Emoji.NEGATIVE_SQUARED_CROSS_MARK + " Задача \"" + reminder.completeTask(task).getName()
								+ "\" успешно отменена!"))
						.chatId(currentChatId)
						.build();
			} else if (callback.contains(REMIND + "-" + COMPLETE)) {
				
				var remind = remindList.get(numberOfTaskOrRemind);
				waitingUpdates = false;
				remindList.remove(remind);
				return SendMessage.builder()
						.text(EmojiParser.parseToUnicode(
								Emoji.WHITE_CHECK_MARK + " Задача \"" + reminder.completeTask(remind.getTask()).getName()
								+ "\" успешно завершена!\n" + "Вы потратили " + remind.complete(ChronoUnit.HOURS)
								+ " на ее выполнение"))
						.chatId(currentChatId)
						.build();
			} else if (callback.contains(REMIND + "-" + CANCEL)) {
				
				var remind = remindList.get(numberOfTaskOrRemind);
				waitingUpdates = false;
				remindList.remove(remind);
				return SendMessage.builder()
						.text(EmojiParser.parseToUnicode(Emoji.NEGATIVE_SQUARED_CROSS_MARK + " Задача \"" + reminder.completeTask(remind.getTask()).getName()
								+ "\" успешно отменена!"))
						.chatId(currentChatId)
						.build();
			} else if (callback.contains(TASK + "-" + OK) || callback.contains(REMIND + "-" + OK)) {
				
				waitingUpdates = false;
				return SendMessage.builder()
						.text(EmojiParser.parseToUnicode(Emoji.OK_HAND + " Отлично! Всегда рад помочь!"))
						.chatId(currentChatId)
						.build();
			}
		} catch (IllegalArgumentException | SchedulerException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return SendMessage.builder()
				.text("")
				.chatId(currentChatId)
				.build();
	}

	@Override
	public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
		
		as = absSender;
		waitingUpdates = true;
		currentUserId = user.getId().toString();
		currentChatId = chat.getId().toString();
		
		try {
			taskList = reminder.getTaskList(currentUserId);
			remindList = reminder.getRemindsList(currentUserId);
			as.execute(makeTaskListKeyboard(taskList));
			as.execute(makeRemindListKeyboard(remindList));
		} catch (JsonMappingException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (SchedulerException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (TelegramApiException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

}
