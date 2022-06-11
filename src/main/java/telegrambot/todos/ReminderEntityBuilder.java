package telegrambot.todos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.telegram.telegrambots.meta.api.objects.User;

public class ReminderEntityBuilder implements IReminderEntityBuilder {
    
    private final String TIME_FORMAT_PATTERN = "dd-MM-yyyy-HH-mm";
    private final String DEFAULT_TASK_NAME = "Новая задача";
    private final String DEFAULT_TASK_DESCRIPTION = "Описание задачи";
    private final String DEFAULT_USER_NAME = "Безымянный";
    /**
     * Значение по-умолчанию, которое прибавляется к текущему моменту времени и,
     * таким образом, получается момент времени, на который по-умолчанию будет
     * запланирована задача или событие
     */
    private final long DEFAULT_SCHEDULE_INCREMENT = 1L;
    private final int DEFAULT_REPEAT_INTERVAL = 0;
    private final ChronoUnit DEFAULT_REPEAT_INTERVAL_UNIT = ChronoUnit.DAYS;
    private final int DEFAULT_MINUTES_BEFORE = 0;
    private final int DEFAULT_REMIND_TIMES = 0;
    private final int DEFAULT_REMIND_TIMES_INTERVAL = 0;
    /**
     * Примерное максимальное время жизни человека в днях, отмеренное ему Богом
     */
    private final long LIFETIME = 120 * 365;
    
    protected ITask task;
    protected String scheduledTime;
    protected int repeatInterval;
    protected ChronoUnit repeatIntervalUnit;
    protected int minutesBefore;
    protected int remindTimes;
    protected int remindTimesInterval;
    protected String taskCreationTime;

    public ReminderEntityBuilder() {
        Set<String> tags = new HashSet<>();
        tags.add("задача");
        tags.add("событие");
        task = new Task(
        		new Random().nextLong() + "",
                new User(new Random().nextLong(), DEFAULT_USER_NAME, false), 
                DEFAULT_TASK_NAME, 
                DEFAULT_TASK_DESCRIPTION, 
                tags);
        scheduledTime = LocalDateTime.now().plusDays(DEFAULT_SCHEDULE_INCREMENT)
        		.format(DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN));
        repeatInterval = DEFAULT_REPEAT_INTERVAL;
        repeatIntervalUnit = DEFAULT_REPEAT_INTERVAL_UNIT;
        minutesBefore = DEFAULT_MINUTES_BEFORE;
        remindTimes = DEFAULT_REMIND_TIMES;
        remindTimesInterval = DEFAULT_REMIND_TIMES_INTERVAL;
        taskCreationTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN));
    }

    @Override
    public IReminderEntityBuilder setTask(ITask task) {
        if (task != null) {
            this.task = task;
        } else {
            throw new IllegalArgumentException("Нельзя запланировать пустую задачу!");
        }
        return this;
    }
    
    @Override
    public IReminderEntityBuilder scheduledAt(String dateTime) throws IncorrectDateTimeException, DateTimeParseException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduled = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN));
        long difference = now.until(scheduled, ChronoUnit.DAYS);
        /**
         * Если для запланированной задачи указан момент времени, отличающийся от "сейчас" и - не далее, чем на
         * 120 лет от "сейчас", то - пойдет :)
         */
        if (scheduled.compareTo(now) > 0 && difference <= LIFETIME) {
            scheduledTime = scheduled.format(DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN));
        } else {
            throw new IncorrectDateTimeException("Введено некорректное значение даты-времени. Нельзя напомнить о задаче в прошлом. "
                + "Нет смысла напоминать о настоящем в ту же секунду. Да и больше 120 лет вы точно не проживете, "
                + "т.к. столько отмерил нам Господь.");
        }
        return this;
    }
    
    @Override
	public IReminderEntityBuilder repeatByInterval(int interval) {
    	this.repeatInterval = interval;
    	return this;
	}

	@Override
	public IReminderEntityBuilder repeatByIntervalUnits(ChronoUnit units) {
		this.repeatIntervalUnit = units;
		return this;
	}
    
    @Override
	public IReminderEntityBuilder remindBeforeMinutes(int minutesBefore) {
    	this.minutesBefore = minutesBefore;
		return this;
	}

	@Override
	public IReminderEntityBuilder remindBeforeTimes(int times) {
		this.remindTimes = times;
		return this;
	}

	@Override
	public IReminderEntityBuilder remindBeforeInterval(int interval) throws IncorrectDateTimeException {
		this.remindTimesInterval = interval;
		if (minutesBefore > 0 && remindTimes > 0 && remindTimes * remindTimesInterval >= minutesBefore) {
			throw new IncorrectDateTimeException("Оповещения о приближении запланированного события или задачи "
                    + "не могут быть позднее самого события или задачи");
		}
		return this;
	}

    @Override
    public IReminderEntityBuilder fixTaskCreationTime() {
        this.taskCreationTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN));
        return this;
    }
    
	@Override
    public IReminderEntity build() {
        return new ReminderEntity(
                task, 
                scheduledTime, 
                repeatInterval, 
                repeatIntervalUnit, 
                minutesBefore, 
                remindTimes, 
                remindTimesInterval,  
                taskCreationTime);
    }
}
