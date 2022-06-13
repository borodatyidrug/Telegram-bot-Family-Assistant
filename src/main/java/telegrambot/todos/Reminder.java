package telegrambot.todos;

import static org.quartz.JobBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.utils.Key;
import org.telegram.telegrambots.meta.bots.AbsSender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdurmont.emoji.EmojiParser;

import telegrambot.familyassistant.Emoji;
import telegrambot.familyassistant.FamilyAssistantBot;
/**
 * Класс-обертка (фасад), "прослойка" между бот-командами и классами фреймворка Quartz. Фасад - потому, что весь мощный, гибкий и
 * разнообразный функционал скрывается за ограниченным набором методов класса. Позволяет планировать задачи и напоминания, удалять их,
 * получать списки актуальных задач и напоминаний
 * @author borodatyidrug
 *
 */
public class Reminder implements IReminder {

	/**
	 * Набор строковых констант для формирования данных JobDetail и JobDetailMap планировщика задач
	 */
	protected static final String TAGS = "tags";
	protected static final String TASK_CHAT_ID = "chatId";
	protected static final String SEND_MESSAGE = "sendmessage";
	protected static final String REMIND_PREFIX = "remindBefore";
	protected static final String TRIGGER_PREFIX = "trigger";
	protected static final String TASK_JSON = "taskjson";
	protected static final String REMIND_JSON = "remindjson";
	
	protected static final String QUARTZ_PROPERTIES_PATH = "/src/main/java/telegrambot/todos/quartz.properties";
	
	protected SchedulerFactory schedulerFactory;
	protected Scheduler scheduler;
	protected final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FamilyAssistantBot.DATE_TIME_FORMAT);
	protected GroupMatcher<Key<?>> groupMatcher;
	protected ObjectMapper mapper;
	
	public Reminder(AbsSender as) {
		mapper = new ObjectMapper();
		try {
			// для настройки параметров планировщика фабрика читает файл с настройками
			schedulerFactory = new StdSchedulerFactory(FamilyAssistantBot.getWorkingDir() + QUARTZ_PROPERTIES_PATH);
			scheduler = schedulerFactory.getScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
		System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	/**
	 * Формирует и возвращает текст сообщения напоминания о приближении дедлайна запланированной задачи
	 * @param remindEntity Напоминание или задача с обозначенным дедлайном
	 * @return Сообщения напоминания о приближении дедлайна запланированной задачи
	 */
	protected String buildRemindMessageBeforeDeadline(IReminderEntity remindEntity) {
		return EmojiParser.parseToUnicode(
				Emoji.BELL + " " + "Напоминаю вам о приближении дедлайна вашей задачи:\n"
				+ Emoji.PUSHPIN + " " + remindEntity.getTask().getName().toUpperCase() + "\n"
				+ Emoji.MEMO + " " + remindEntity.getTask().getDescription() + "\n" + "Теги: "
				+ remindEntity.getTask().getTags().toString() + "\n"
				+ Emoji.DATE + " " + "Дедлайн задачи: "
				+ remindEntity.getScheduledTime());
	}
	/**
	 * Формирует и возвращает текст сообщения о наступившем дедлайне запланированной задачи
	 * @param remindEntity Напоминание или задача с обозначенным дедлайном
	 * @return Сообщение о наступившем дедлайне запланированной задачи
	 */
	protected String buildDeadlineMessage(IReminderEntity remindEntity) {
		return EmojiParser.parseToUnicode(
				Emoji.BELL + " " + "Наступил дедлайн для вашей задачи:\n"
				+ Emoji.PUSHPIN + " " + remindEntity.getTask().getName().toUpperCase() + "\n"
				+ Emoji.MEMO + " " + remindEntity.getTask().getDescription() + "\n" + "Теги: "
				+ remindEntity.getTask().getTags().toString() + "\n"
				+ Emoji.DATE + " " + "Дедлайн задачи: "
				+ remindEntity.getScheduledTime());
	}

	@Override
	public void addTask(ITask task) throws SchedulerException, JsonProcessingException {
		var userId = task.getOwner().getId().toString();
		var chatId = task.getChatId();
		var taskName = task.getName();
		var tags = task.getTags();
		var jobBuilder = newJob(ScheduledJob.class)
				.withIdentity(taskName, userId)
				.storeDurably()
				.usingJobData(TASK_CHAT_ID, chatId)
				.usingJobData(SEND_MESSAGE, buildTaskMessage(task))
				.usingJobData(TASK_JSON, mapper.writeValueAsString(task));
		if (tags != null && !tags.isEmpty()) {
			jobBuilder.usingJobData(TAGS, tags.toString());
		}
		scheduler.addJob(jobBuilder.build(), true);
	}
	
	/**
	 * Формирует и возвращает текст сообщения с описанием простой задачи
	 * @param task Простая задача
	 * @return Сообщение с описанием простой задачи
	 */
	protected String buildTaskMessage(ITask task) {
		return EmojiParser.parseToUnicode(Emoji.PUSHPIN + " " + task.getName().toUpperCase() + "\n"
				+ Emoji.MEMO + " " + task.getDescription() + "\n" + "Теги: "
				+ task.getTags().toString());
	}

	@Override
	public void scheduleRemind(IReminderEntity reminderEntity) throws SchedulerException, JsonProcessingException {
		
		TriggerBuilder<Trigger> triggerBuilder;
		JobBuilder jobBuilder = newJob();
		String cronWhen;
		JobDetail job, remind;
		Trigger trigger;
		
		var userId = reminderEntity.getTask().getOwner().getId().toString();
		var chatId = reminderEntity.getTask().getChatId();
		var taskName = reminderEntity.getTask().getName();
		var tags = reminderEntity.getTask().getTags();
		var when = LocalDateTime.parse(reminderEntity.getScheduledTime(), formatter);
		var whenToDate = DateBuilder.dateOf(when.getHour(), when.getMinute(), 0, when.getDayOfMonth(), when.getMonthValue(), when.getYear());
		var period = reminderEntity.getRepeatInterval();
		var periodUnits = reminderEntity.getRepeatIntervalUnit();
		var minutesBefore = reminderEntity.getMinutesBefore();
		var minutesBeforeTimes = reminderEntity.getRemindTimes();
		var minutesBeforeTimesInterval = reminderEntity.getRemindTimesInterval();
		
		jobBuilder = newJob(ScheduledJob.class)
				.withIdentity(taskName, userId)
				.requestRecovery(true)
				.usingJobData(TASK_CHAT_ID, chatId)
				.usingJobData(SEND_MESSAGE, buildDeadlineMessage(reminderEntity))
				.usingJobData(TASK_JSON, mapper.writeValueAsString(reminderEntity.getTask()))
				.usingJobData(REMIND_JSON, mapper.writeValueAsString(reminderEntity));
		
		if (tags != null && !tags.isEmpty()) {
			jobBuilder.usingJobData(TAGS, tags.toString());
		}
		
		triggerBuilder = newTrigger()
				.withIdentity(TRIGGER_PREFIX + "-" + taskName, userId)
				.startAt(whenToDate);
		// Если задача - разовая, непериодическая
		if (period == 0) {
			job = jobBuilder.build();
			trigger = triggerBuilder.build();
			scheduler.scheduleJob(job, trigger);
			// Если задача - периодическая
		} else if (period > 0) {
			// Если период задан в годах
			if (periodUnits == ChronoUnit.YEARS) {
				cronWhen = "0 " + when.getMinute() + " " + when.getHour() + " " + when.getDayOfMonth() + " " + when.getMonthValue()
				+ " ? " + when.getYear() + "/" + period;
				trigger = triggerBuilder.withSchedule(cronSchedule(cronWhen)
						.withMisfireHandlingInstructionFireAndProceed())
						.build();
				job = jobBuilder.build();
				scheduler.scheduleJob(job, trigger);
				// Если - в остальных единицах
			} else {
				period = (int) TimeUnit.of(periodUnits).toMinutes(period);
				job = jobBuilder.build();
				trigger = triggerBuilder
						.withSchedule(simpleSchedule()
								.withIntervalInMinutes(period)
								.withMisfireHandlingInstructionFireNow()
						.repeatForever())
						.build();
				scheduler.scheduleJob(job, trigger);
			}
		}
		// Если задано напоминание несколько раз с заданным интервалом напоминаний за заданное кол-во минут до наступления дедлайна
		if (minutesBefore > 0) {
			var before = when.minusMinutes(minutesBefore);
			var beforeToDate = DateBuilder.dateOf(before.getHour(), before.getMinute(), 0, before.getDayOfMonth(), before.getMonthValue(), before.getYear());
			jobBuilder = newJob(ScheduledJob.class)
					.withIdentity(REMIND_PREFIX + "-" + taskName, userId)
					.requestRecovery()
					.usingJobData(TASK_CHAT_ID, chatId)
					.usingJobData(SEND_MESSAGE, buildRemindMessageBeforeDeadline(reminderEntity));
			if (tags != null && !tags.isEmpty()) {
				jobBuilder.usingJobData(TAGS, tags.toString());
			}
			remind = jobBuilder.build();
			if ( minutesBeforeTimes == 1) {
				scheduler.scheduleJob(remind, newTrigger()
						.withIdentity(TRIGGER_PREFIX + "-" + REMIND_PREFIX + "-" + taskName, userId)
						.startAt(beforeToDate)
						.build());
			} else if (minutesBeforeTimes > 1 && minutesBeforeTimesInterval > 0) {
				scheduler.scheduleJob(remind, newTrigger()
						.withIdentity(TRIGGER_PREFIX + "-" + REMIND_PREFIX + "-" + taskName, userId)
						.startAt(beforeToDate)
						.withSchedule(simpleSchedule()
								.withIntervalInMinutes(minutesBeforeTimesInterval)
								.withRepeatCount(minutesBeforeTimes))
						.build());
			}
		}
	}

	@Override
	public List<ITask> getTaskList(String userId) throws SchedulerException, JsonMappingException, JsonProcessingException {
		List<ITask> taskList = new ArrayList<>();
		for (var k : scheduler.getJobKeys(GroupMatcher.groupEquals(userId))) {
			JobDetail jobDetail = scheduler.getJobDetail(k);
			if (!jobDetail.getKey().getName().contains(REMIND_PREFIX)) {
				JobDataMap jobDataMap = jobDetail.getJobDataMap();
				if (jobDataMap.containsKey(TASK_JSON)) {
					taskList.add(mapper.readValue(jobDataMap.getString(TASK_JSON), new TypeReference<ITask>() {}));
				}
			}
		}
		return taskList;
	}
	
	@Override
	public List<IReminderEntity> getRemindsList(String userId) throws SchedulerException, JsonMappingException, JsonProcessingException {
		List<IReminderEntity> remindList = new ArrayList<>();
		for (var k : scheduler.getJobKeys(GroupMatcher.groupEquals(userId))) {
			JobDetail jobDetail = scheduler.getJobDetail(k);
			if (!jobDetail.getKey().getName().contains(REMIND_PREFIX)) {
				JobDataMap jobDataMap = jobDetail.getJobDataMap();
				if (jobDataMap.containsKey(REMIND_JSON)) {
					remindList.add(mapper.readValue(jobDataMap.getString(REMIND_JSON), new TypeReference<IReminderEntity>() {}));
				}
			}
		}
		return remindList;
	}
	
	@Override
	public ITask completeTask(ITask task) throws SchedulerException {
		JobKey key = new JobKey(task.getName(), task.getOwner().getId().toString());
		if (scheduler.deleteJob(key)) {
			return task;
		} else {
			throw new NoSuchElementException("В списке задач такой задачи не обнаружено");
		}
	}
	
	@Override
	public IReminderEntity completeTask(IReminderEntity reminderEntity, ChronoUnit unit) throws SchedulerException {
		ITask task = reminderEntity.getTask();
		JobKey key = new JobKey(task.getName(), task.getOwner().getId().toString());
		if (scheduler.deleteJob(key)) {
			reminderEntity.complete(unit);
			return reminderEntity;
		} else {
			throw new NoSuchElementException("В списке задач такой задачи не обнаружено");
		}
	}
}
