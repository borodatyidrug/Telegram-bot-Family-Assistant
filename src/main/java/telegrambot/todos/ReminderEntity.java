/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package telegrambot.todos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Класс для хранения параметров напоминаний
 * @author aurumbeats
 */
public class ReminderEntity implements IReminderEntity, Comparable<IReminderEntity> {
    
    private final String TIME_FORMAT_PATTERN = "dd-MM-yyyy-HH-mm";
    
    protected ITask task;
    protected String scheduledTime;
    protected int repeatInterval;
    protected ChronoUnit repeatIntervalUnit;
    protected int minutesBefore;
    protected int remindTimes;
    protected int remindTimesInterval;
    protected String taskCreationTime;

    public ReminderEntity(
    		@JsonProperty("task") ITask task, 
    		@JsonProperty("scheduledTime") String scheduledTime,
    		@JsonProperty("repeatInterval") int repeatInterval, 
    		@JsonProperty("repeatIntervalUnit") ChronoUnit repeatIntervalUnit, 
    		@JsonProperty("minutesBefore") int minutesBefore, 
    		@JsonProperty("remindTimes") int remindTimes, 
    		@JsonProperty("remindTimesInterval") int remindTimesInterval,  
    		@JsonProperty("taskCreationTime") String taskCreationTime) {
        this.task = task;
        this.scheduledTime = scheduledTime;
        this.repeatInterval = repeatInterval;
        this.repeatIntervalUnit = repeatIntervalUnit;
        this.minutesBefore = minutesBefore;
        this.remindTimes = remindTimes;
        this.remindTimesInterval = remindTimesInterval;
        this.taskCreationTime = taskCreationTime;
    }

    @JsonProperty("task")
    @Override
    public ITask getTask() {
		return task;
	}

    @JsonProperty("task")
    @Override
	public void setTask(ITask task) {
		this.task = task;
	}

    @JsonProperty("scheduledTime")
    @Override
	public String getScheduledTime() {
		return scheduledTime;
	}

    @JsonProperty("scheduledTime")
    @Override
	public void setScheduledTime(String scheduledTime) {
		this.scheduledTime = scheduledTime;
	}

    @JsonProperty("repeatInterval")
    @Override
	public int getRepeatInterval() {
		return repeatInterval;
	}

    @JsonProperty("repeatInterval")
    @Override
	public void setRepeatInterval(int repeatInterval) {
		this.repeatInterval = repeatInterval;
	}

    @JsonProperty("repeatIntervalUnit")
    @Override
	public ChronoUnit getRepeatIntervalUnit() {
		return repeatIntervalUnit;
	}

    @JsonProperty("repeatIntervalUnit")
    @Override
	public void setRepeatIntervalUnit(ChronoUnit repeatIntervalUnit) {
		this.repeatIntervalUnit = repeatIntervalUnit;
	}

    @JsonProperty("minutesBefore")
    @Override
	public int getMinutesBefore() {
		return minutesBefore;
	}

    @JsonProperty("minutesBefore")
    @Override
	public void setMinutesBefore(int minutesBefore) {
		this.minutesBefore = minutesBefore;
	}

    @JsonProperty("remindTimes")
    @Override
	public int getRemindTimes() {
		return remindTimes;
	}

    @JsonProperty("remindTimes")
    @Override
	public void setRemindTimes(int remindTimes) {
		this.remindTimes = remindTimes;
	}

    @JsonProperty("remindTimesInterval")
    @Override
	public int getRemindTimesInterval() {
		return remindTimesInterval;
	}

    @JsonProperty("remindTimesInterval")
    @Override
	public void setRemindTimesInterval(int remindTimesInterval) {
		this.remindTimesInterval = remindTimesInterval;
	}

    @JsonProperty("taskCreationTime")
    @Override
	public String getTaskCreationTime() {
		return taskCreationTime;
	}

    @JsonProperty("taskCreationTime")
    @Override
	public void setTaskCreationTime(String taskCreationTime) {
		this.taskCreationTime = taskCreationTime;
	}

	@Override
    public long complete(ChronoUnit units) {
        return LocalDateTime.parse(taskCreationTime, DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN))
        		.until(LocalDateTime.now(), ChronoUnit.valueOf(units.toString()));
    }

	@Override
	public int hashCode() {
		return Objects.hash(TIME_FORMAT_PATTERN, minutesBefore,
				remindTimes, remindTimesInterval, repeatInterval, repeatIntervalUnit, scheduledTime,
				task, taskCreationTime);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReminderEntity other = (ReminderEntity) obj;
		return Objects.equals(TIME_FORMAT_PATTERN, other.TIME_FORMAT_PATTERN)
				&& minutesBefore == other.minutesBefore
				&& remindTimes == other.remindTimes && remindTimesInterval == other.remindTimesInterval
				&& repeatInterval == other.repeatInterval
				&& repeatIntervalUnit == other.repeatIntervalUnit && Objects.equals(scheduledTime, other.scheduledTime)
				&& Objects.equals(task, other.task)
				&& Objects.equals(taskCreationTime, other.taskCreationTime);
	}

	@Override
	public int compareTo(IReminderEntity o) {
		var other = o;
		LocalDateTime otherDate = LocalDateTime.parse(other.getScheduledTime(), DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN));
		LocalDateTime thisDate = LocalDateTime.parse(this.getScheduledTime(), DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN));
		if (thisDate.isAfter(otherDate)) {
			return 1;
		} else if (thisDate.isBefore(otherDate)) {
			return -1;
		} else {
			return 0;
		}
	}
	
	
}
