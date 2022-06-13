package telegrambot.commands.todos;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.quartz.SchedulerException;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import telegrambot.commands.CallbackOrientedBotCommand;
import telegrambot.familyassistant.FamilyAssistantBot;
import telegrambot.todos.IReminderEntity;
import telegrambot.todos.IReminderEntityBuilder;
import telegrambot.todos.ITask;
import telegrambot.todos.IncorrectDateTimeException;
import telegrambot.todos.Reminder;
import telegrambot.todos.ReminderEntityBuilder;
import telegrambot.todos.Task;

public class AddTask extends CallbackOrientedBotCommand {
	
	protected final String DEFAULT = "default";
	/**
	 * Набор констант для формирования запросов обратного вызова (CallbackQuery)
	 */
	protected final String NAME = "name";
	protected final String DESCRIPTION = "description";
	protected final String TAGS = "tags";
	protected final String DONE = "done";
	protected final String DONE_SCHEDULING = "done-scheduling";
	protected final String DONE_REMIND_BEFORE = "done-remind-before";	
	protected final String CANCEL = "cancel";
	protected final String EXPECT = "expect";
	protected final String SCHEDULE = "schedule";
	protected final String MAIN_MENU = "main-menu";
	protected final String YES = "yes";	
	protected final String SCHEDULE_AT = "schedule-at";
	protected final String REPEAT_BY = "repeat-by";
	protected final String REPEAT_BY_UNITS = "repeat-by-units";
	protected final String REPEAT_BY_INTERVAL = "repeat-by-interval";
	protected final String REMIND_BEFORE = "remind-before";
	protected final String YEAR = "Years";
	protected final String MONTH = "Months";
	protected final String WEEK = "Weeks";
	protected final String DAY = "Days";
	protected final String HOUR = "Hours";
	protected final String MINUTE = "Minutes";
	protected final String TIMES = "times";
	protected final String INTERVAL = "interval";
	
	protected TelegramLongPollingCommandBot bot;
	protected String prefix, currentUserId, currentChatId, callback;
	/**
	 * Предопределенные inline-кнопки
	 */
	protected InlineKeyboardButton name, description, tags, done, cancel, schedule, yes, scheduleAt, repeatBy, 
	repeatIntervalUnits, remindBefore, remindBeforeMinutes, remindBeforeTimes, remindBeforeInterval, doneScheduling, 
	year, month, day, hour, minute, remindBeforeDefault, doneRemindBefore;
	/**
	 * Предопределенные inline-клавиатуры
	 */
	protected InlineKeyboardMarkup main, scheduling, timeUnits, remindingBefore;
	protected Update currentUpdate;
	protected ITask currentTask;
	protected IReminderEntity currentRemind;
	protected IReminderEntityBuilder remindBuilder;
	protected SendMessage mainMenu, blank;
	protected Reminder reminder;
	
	public AddTask(String commandIdentifier, String commandDescription, TelegramLongPollingCommandBot bot) {
		
		super(commandIdentifier, commandDescription);
		this.bot = bot;
		try {
			reminder = FamilyAssistantBot.getReminder();
		} catch (SchedulerException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		remindBuilder = new ReminderEntityBuilder();
		
		prefix = getCallbackDataPrefix();
		callback = "";
		
		// Все кнопки
		name = new InlineKeyboardButton("Имя задачи");
		description = new InlineKeyboardButton("Описание");
		tags = new InlineKeyboardButton("Теги");
		done = new InlineKeyboardButton("Готово");
		cancel = new InlineKeyboardButton("Отмена");
		schedule = new InlineKeyboardButton("Запланировать");
		yes = new InlineKeyboardButton("Да");
		
		scheduleAt = new InlineKeyboardButton("Срок (дата и время) исполнения");
		repeatBy = new InlineKeyboardButton("Задать период повторения");
		remindBefore = new InlineKeyboardButton("Напомнить о приближении срока");
		doneScheduling = new InlineKeyboardButton("Готово");
		year = new InlineKeyboardButton("Год");
		month = new InlineKeyboardButton("Месяц");
		day = new InlineKeyboardButton("День");
		hour = new InlineKeyboardButton("Час");
		minute = new InlineKeyboardButton("Минута");
		remindBeforeMinutes = new InlineKeyboardButton("За сколько минут до?");
		remindBeforeTimes = new InlineKeyboardButton("Сколько раз?");
		remindBeforeInterval = new InlineKeyboardButton("С каким интервалом?");
		remindBeforeDefault = new InlineKeyboardButton("Настроить по умолчанию");
		doneRemindBefore = new InlineKeyboardButton("Готово");
		
		// задание запросов обратного вызова для каждой кнопки
		name.setCallbackData(prefix + NAME);
		description.setCallbackData(prefix + DESCRIPTION);
		tags.setCallbackData(prefix + TAGS);
		done.setCallbackData(prefix + DONE);
		cancel.setCallbackData(prefix + CANCEL);
		schedule.setCallbackData(prefix + SCHEDULE);
		
		scheduleAt.setCallbackData(prefix + SCHEDULE_AT);
		repeatBy.setCallbackData(prefix + REPEAT_BY);
		remindBefore.setCallbackData(prefix + REMIND_BEFORE);
		doneScheduling.setCallbackData(prefix + DONE_SCHEDULING);
		year.setCallbackData(prefix + REPEAT_BY_UNITS + "-" + EXPECT + "-" + YEAR);
		month.setCallbackData(prefix + REPEAT_BY_UNITS + "-" + EXPECT + "-" + MONTH);
		day.setCallbackData(prefix + REPEAT_BY_UNITS + "-" + EXPECT + "-" + DAY);
		hour.setCallbackData(prefix + REPEAT_BY_UNITS + "-" + EXPECT + "-" + HOUR);
		minute.setCallbackData(prefix + REPEAT_BY_UNITS + "-" + EXPECT + "-" + MINUTE);
		remindBeforeMinutes.setCallbackData(prefix + REMIND_BEFORE + "-" + MINUTE);
		remindBeforeInterval.setCallbackData(prefix + REMIND_BEFORE + "-" + INTERVAL);
		remindBeforeTimes.setCallbackData(prefix + REMIND_BEFORE + "-" + TIMES);
		remindBeforeDefault.setCallbackData(prefix + REMIND_BEFORE + "-" + DEFAULT);
		doneRemindBefore.setCallbackData(prefix + DONE_REMIND_BEFORE);
		yes.setCallbackData(prefix + "-" + YES);
		
		// конструируем объекты inline-клавиатур
		main = InlineKeyboardMarkup.builder()
				.keyboardRow(List.of(name))
				.keyboardRow(List.of(description))
				.keyboardRow(List.of(tags))
				.keyboardRow(List.of(done))
				.keyboardRow(List.of(cancel))
				.keyboardRow(List.of(schedule))
				.build();
		
		scheduling = InlineKeyboardMarkup.builder()
				.keyboardRow(List.of(scheduleAt))
				.keyboardRow(List.of(repeatBy))
				.keyboardRow(List.of(remindBefore))
				.keyboardRow(List.of(doneScheduling))
				.keyboardRow(List.of(cancel))
				.build();
		
		timeUnits = InlineKeyboardMarkup.builder()
				.keyboardRow(List.of(year))
				.keyboardRow(List.of(month))
				.keyboardRow(List.of(day))
				.keyboardRow(List.of(hour))
				.keyboardRow(List.of(minute))
				.keyboardRow(List.of(cancel))
				.build();
		
		remindingBefore = InlineKeyboardMarkup.builder()
				.keyboardRow(List.of(remindBeforeMinutes))
				.keyboardRow(List.of(remindBeforeTimes))
				.keyboardRow(List.of(remindBeforeInterval))
				.keyboardRow(List.of(remindBeforeDefault))
				.keyboardRow(List.of(doneRemindBefore))
				.keyboardRow(List.of(cancel))
				.build();
	}
	
	/**
	 * Переводит строковое представление единицы измерения периода времени в ChronoUnit
	 * @param unit Конвертируемая единица времени
	 * @return Единица измерения периода времени
	 */
	protected ChronoUnit toChronoUnit(String unit) {
		switch (unit) {
			case YEAR:
				return ChronoUnit.YEARS;
			case MONTH:
				return ChronoUnit.MONTHS;
			case WEEK:
				return ChronoUnit.WEEKS;
			case DAY:
				return ChronoUnit.DAYS;
			case HOUR:
				return ChronoUnit.HOURS;
			case MINUTE:
				return ChronoUnit.MINUTES;
			default:
				return ChronoUnit.DAYS;
		}
	}
	
	protected SendMessage mainMenu(String currentChatId) {
		return SendMessage.builder()
				.text("Выберите поля, которые хотите заполнить для вашей новой задачи. Поле \"Имя задачи\" - обязательное, "
						+ "остальные поля - на ваше усмотрение. Нажмите \"Готово\", когда закончите заполнять необходимые вам поля. "
						+ "Нажмите \"Отмена\", если вы передумали создавать задачу. Если хотите запланировать напоминание на основе "
						+ "текущей задачи или задать срок исполнения для нее, нажмите \"Запланировать\"")
				.chatId(currentChatId)
				.replyMarkup(main)
				.build();
	}
	
	protected SendMessage schedulingMenu(String currentChatId) {
		return SendMessage.builder()
				.text("Выберите желаемые действия и отправьте мне сообщения с данными. Я подскажу вам, в каком формате их отправить. "
						+ "Для того, чтобы запланировать задачу, обязательно нужно указать срок исполнения задачи (дату и время). "
						+ "Остальные настройки указывать необязательно, но - на ваше усмотрение.")
				.chatId(currentChatId)
				.replyMarkup(scheduling)
				.build();
	}
	
	protected SendMessage timeUnitsMenu(String currentChatId) {
		return SendMessage.builder()
				.text("В каких единицах будете указывать интервал повторения задачи?")
				.chatId(currentChatId)
				.replyMarkup(timeUnits)
				.build();
	}
	
	protected SendMessage enterValue(String currentChatId) {
		return SendMessage.builder()
				.text("Введите необходимое значение")
				.chatId(currentChatId)
				.build();
	}
	
	protected SendMessage blank(String currentChatId) {
		return SendMessage.builder().text("").chatId(currentChatId).build();
	}
	
	protected SendMessage remindBeforeMenu(String currentChatId) {
		return SendMessage.builder()
				.text("Задайте параметры напоминания: 1) За сколько минут до наступления срока выполенения задачи напомнить? "
						+ "2) Сколько раз напомнить? 3) С каким интервалом напоминать? Нажмите \"Настроить по-умолчанию\" для того, "
						+ "чтобы напомнить о задаче за 15 минут до наступления срока исполнения один раз.")
				.chatId(currentChatId)
				.replyMarkup(remindingBefore)
				.build();
	}
	
	@Override
	public boolean catched(Update update) {
		return ((update.hasCallbackQuery() && update.getCallbackQuery().getData().contains(prefix)
				&& update.getCallbackQuery().getFrom().getId().toString().equals(currentUserId))
				|| (update.hasMessage() && update.getMessage().getChatId().toString().equals(currentChatId))) && waitingUpdates;
	}
	
	/**
	 * Обрабатывает вызов после формирования простой задачи и нажатия пользователем кнопки "Готово" и возвращает результат
	 * @return Сообщение с результатом
	 */
	protected SendMessage done() {
		if (currentTask.getName() == null || currentTask.getName().isBlank()) {
			try {
				bot.execute(SendMessage.builder()
						.text("\"Имя\" - обязательное поле для любой задачи. Нельзя создать задачу без имени. "
								+ "Заполните, пожалуйста, это поле")
						.chatId(currentChatId)
						.build());
			} catch (TelegramApiException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			return mainMenu(currentChatId);
		}
		waitingUpdates = false; // обновления больше не ожидаются
		callback = ""; // запрос обратного вызова обнуляем
		try {
			reminder.addTask(currentTask);
		} catch (IOException | SchedulerException e) {
			e.printStackTrace();
			return SendMessage.builder()
					.text("Готово! Ваша задача создана и добавлена в ваш список! Однако, что-то пошло не так, и я не могу "
							+ "сохранить ее в базу данных. После моей перезагрузки я ничего не буду помнить... :(")
					.chatId(currentChatId)
					.build();
		}
		return SendMessage.builder()
				.text("Готово! Ваша задача создана и добавлена в ваш список!")
				.chatId(currentChatId)
				.build();
	}
	/**
	 * Обрабатывает вызов после формирования напоминания или задачи с обозначенным дедлайном и нажатия пользователем кнопки "Готово"
	 * и возвращает результат
	 * @return Сообщение с результатом
	 */
	protected SendMessage doneScheduling() {
		currentRemind = remindBuilder.fixTaskCreationTime().build();
		waitingUpdates = false; // обновления больше не ожидаются
		callback = ""; // запрост обратного вызова обнуляем
		try {
			reminder.scheduleRemind(currentRemind);
		} catch (IOException | SchedulerException e) {
			e.printStackTrace();
			return SendMessage.builder()
					.text("Готово! Ваша задача создана, запланирована и добавлена в ваш список! Однако, что-то пошло не так, и я не могу "
							+ "сохранить ее в базу данных. После моей перезагрузки я ничего не буду помнить... :(")
					.chatId(currentChatId)
					.build();
		}
		return SendMessage.builder()
				.text("Готово! Ваша задача создана, запланирована и добавлена в ваш список!")
				.chatId(currentChatId)
				.build();
	}
	
	@Override
	public SendMessage getAnswer(Update update) {
		/**
		 * Здесь происходит основной диалог с пользователем посредством обработки запросов обратного вызова, которые получаем или из
		 * кнопок inline-клавиатур, или принудительно устанавливая необходимый запрос прямо в коде этого метода
		 */
		if (update.hasCallbackQuery()) {
			callback = update.getCallbackQuery().getData().substring(prefix.length());
		}
		switch (callback) {
		/**
		 * В начале - обработка обратных вызовов для создания простых задач.
		 */
			case CANCEL:
				waitingUpdates = false;
				callback = "";
				return SendMessage.builder()
						.text("Ок, тогда - в следующий раз!")
						.chatId(currentChatId)
						.build();
				
			case DONE:
				return done();
				
			case NAME: 
				// если диалог с пользователем необходимо продолжить, но, при этом, не предполагается на данном шаге отправка в чат
				// сооббщения с inline-клавиатурой с кнопками, которым можно было бы задать callback, то просто задаем его значение 
				// принудительно
				callback = NAME + "-" + EXPECT;
				return SendMessage.builder()
						.text("Отправьте мне имя вашей задачи")
						.chatId(currentChatId)
						.build();
				
			case NAME + "-" + EXPECT:
				currentTask.setName(update.getMessage().getText());
				return mainMenu(currentChatId);
				
			case DESCRIPTION:
				callback = DESCRIPTION + "-" + EXPECT;
				return SendMessage.builder()
						.text("Отправьте мне описание вашей задачи")
						.chatId(currentChatId)
						.build();
				
			case DESCRIPTION + "-" + EXPECT:
				currentTask.setDescription(update.getMessage().getText());
				return mainMenu(currentChatId);
				
			case TAGS:
				callback = TAGS + "-" + EXPECT;
				return SendMessage.builder()
						.text("Отправьте мне теги через пробел для вашей задачи")
						.chatId(currentChatId)
						.build();
				
			case TAGS + "-" + EXPECT:
				Set<String> tags = new HashSet<>();
				String[] input = update.getMessage().getText().split(" ");
				for (String t : input) {
					tags.add(t);
				}
				currentTask.setTags(tags);
				return mainMenu(currentChatId);
				
			case MAIN_MENU:
				return mainMenu(currentChatId);
			/**
			 * Здесь начинается обработка обратных вызовов для формирования напоминаний на основе текущей задачи
			 */
			case SCHEDULE:
				if (currentTask.getName() == null || currentTask.getName().isBlank()) {
					try {
						bot.execute(SendMessage.builder()
								.text("\"Имя\" - обязательное поле для любой задачи. Нельзя создать задачу без имени. "
										+ "Заполните, пожалуйста, это поле")
								.chatId(currentChatId)
								.build());
					} catch (TelegramApiException e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
					return mainMenu(currentChatId);
				}
				remindBuilder.setTask(currentTask);
				return schedulingMenu(currentChatId);
				
			case SCHEDULE_AT:
				callback = SCHEDULE_AT + "-" + EXPECT;
				return SendMessage.builder()
						.text("Отправьте мне дату и время, на которые вы планируете срок исполнения задачи, в формате "
								+ "\"дд-ММ-гггг-чч-мм\" (день, месяц, год, час, минута) без кавычек")
						.chatId(currentChatId)
						.build();
				
			case SCHEDULE_AT + "-" + EXPECT:
				try {
					remindBuilder.scheduledAt(update.getMessage().getText());
				} catch (DateTimeParseException e) {
					return SendMessage.builder()
							.text("Неверный формат даты. Отправьте еще раз. Напоминаю формат: \"дд-ММ-гггг-чч-мм\" "
									+ "(день, месяц, год, час, минута) без кавычек. Например: 14-02-2013-09-00")
							.chatId(currentChatId)
							.build();
				} catch (IncorrectDateTimeException e) {
					return SendMessage.builder()
							.text(e.getMessage())
							.chatId(currentChatId)
							.build();
				}
				return schedulingMenu(currentChatId);
				
			case REPEAT_BY:
				return timeUnitsMenu(currentChatId);
				
			case REPEAT_BY_UNITS + "-" + EXPECT + "-" + YEAR:
			case REPEAT_BY_UNITS + "-" + EXPECT + "-" + MONTH:
			case REPEAT_BY_UNITS + "-" + EXPECT + "-" + DAY:
			case REPEAT_BY_UNITS + "-" + EXPECT + "-" + HOUR:
			case REPEAT_BY_UNITS + "-" + EXPECT + "-" + MINUTE:
				var offset = REPEAT_BY_UNITS.length() + EXPECT.length() + 2;
				var extractedChronoUnit = callback.substring(offset);
				remindBuilder.repeatByIntervalUnits(toChronoUnit(extractedChronoUnit));
				callback = REPEAT_BY_INTERVAL;
				return enterValue(currentChatId);
				
			case REPEAT_BY_INTERVAL:
				try {
					var inputValue = Integer.parseInt(update.getMessage().getText());
					if (inputValue <= 0) throw new IllegalArgumentException();
					remindBuilder.repeatByInterval(inputValue);
					return schedulingMenu(currentChatId);
				} catch (IllegalArgumentException e) {
					try {
						bot.execute(SendMessage.builder()
								.text("Неверный формат числа. Введите целое положительное число!")
								.chatId(currentChatId)
								.build());
					} catch (TelegramApiException e1) {
						System.out.println(e1.getMessage());
						e1.printStackTrace();
					}
				}
				
			case REMIND_BEFORE:
				return remindBeforeMenu(currentChatId);
				
			case REMIND_BEFORE + "-" + MINUTE:
				callback = REMIND_BEFORE + "-" + EXPECT + "-" + MINUTE;
				return enterValue(currentChatId);
				
			case REMIND_BEFORE + "-" + EXPECT + "-" + MINUTE:
				try {
					int inputValue = Integer.parseInt(update.getMessage().getText());
					if (inputValue <= 0) throw new IllegalArgumentException();
					remindBuilder.remindBeforeMinutes(inputValue);
					return remindBeforeMenu(currentChatId);
				} catch (IllegalArgumentException e) {
					try {
						bot.execute(SendMessage.builder()
								.text("Неверный формат числа. Введите целое положительное число!")
								.chatId(currentChatId)
								.build());
					} catch (TelegramApiException e1) {
						System.out.println(e1.getMessage());
						e1.printStackTrace();
					}
				}
			case REMIND_BEFORE + "-" + TIMES:
				callback = REMIND_BEFORE + "-" + EXPECT + "-" + TIMES;
				return enterValue(currentChatId);
				
			case REMIND_BEFORE + "-" + EXPECT + "-" + TIMES:
				try {
					int inputValue = Integer.parseInt(update.getMessage().getText());
					if (inputValue <= 0) throw new IllegalArgumentException();
					remindBuilder.remindBeforeTimes(inputValue);
					return remindBeforeMenu(currentChatId);
				} catch (IllegalArgumentException e) {
					try {
						bot.execute(SendMessage.builder()
								.text("Неверный формат числа. Введите целое положительное число!")
								.chatId(currentChatId)
								.build());
					} catch (TelegramApiException e1) {
						System.out.println(e1.getMessage());
						e1.printStackTrace();
					}
				}
			
			case REMIND_BEFORE + "-" + INTERVAL:
				callback = REMIND_BEFORE + "-" + EXPECT + "-" + INTERVAL;
				return enterValue(currentChatId);
				
			case REMIND_BEFORE + "-" + EXPECT + "-" + INTERVAL:
				try {
					int inputValue = Integer.parseInt(update.getMessage().getText());
					if (inputValue <= 0) throw new IllegalArgumentException();
					remindBuilder.remindBeforeInterval(inputValue);
					return remindBeforeMenu(currentChatId);
				} catch (IllegalArgumentException e) {
					try {
						bot.execute(SendMessage.builder()
								.text("Неверный формат числа. Введите целое положительное число!")
								.chatId(currentChatId)
								.build());
					} catch (TelegramApiException e1) {
						System.out.println(e1.getMessage());
						e1.printStackTrace();
					}
				} catch (IncorrectDateTimeException e) {
					try {
						bot.execute(SendMessage.builder()
								.text(e.getMessage())
								.chatId(currentChatId)
								.build());
					} catch (TelegramApiException e1) {
						System.out.println(e1.getMessage());
						e1.printStackTrace();
					}
				}
				
			case REMIND_BEFORE + "-" + DEFAULT:
				remindBuilder.remindBeforeMinutes(15).remindBeforeTimes(1);
				try {
					bot.execute(SendMessage.builder()
							.text("Напоминание настроено по-умолчанию!")
							.chatId(currentChatId)
							.build());
				} catch (TelegramApiException e1) {
					System.out.println(e1.getMessage());
					e1.printStackTrace();
				}
				return schedulingMenu(currentChatId);
				
			case DONE_REMIND_BEFORE:
				try {
					bot.execute(SendMessage.builder()
							.text("Напоминание настроено!")
							.chatId(currentChatId)
							.build());
				} catch (TelegramApiException e1) {
					System.out.println(e1.getMessage());
					e1.printStackTrace();
				}
				return schedulingMenu(currentChatId);
				
			case DONE_SCHEDULING:
				return doneScheduling();
				
			default:
				return blank(currentChatId);
		}
	}
	
	@Override
	public void execute(AbsSender as, User user, Chat chat, String[] strings) {
		waitingUpdates = true;
		currentUserId = user.getId().toString();
		currentChatId = chat.getId().toString();
		currentTask = new Task();
		currentTask.setChatId(currentChatId);
		currentTask.setOwner(user);
		try {
			as.execute(mainMenu(currentChatId));
		} catch (TelegramApiException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

}
