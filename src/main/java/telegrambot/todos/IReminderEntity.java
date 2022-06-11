package telegrambot.todos;

import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
/**
 * Интерфейс определяет набор методов для построителя объектов-напоминаний, в которые бот в диалоговом режиме с пользователем
 * записывает необходимые параметры формирования расписания запуска планировщиком напоминаний
 * @author borodatyidrug
 *
 */
@JsonSerialize(as = ReminderEntity.class)
@JsonDeserialize(as = ReminderEntity.class)
public interface IReminderEntity {
	
	/**
	 * Установить задачу
	 * @param task Задача
	 */
	@JsonProperty("task")
	void setTask(ITask task);
	/**
	 * Установить срок исполнения (дедлайн) для задачи
	 * @param scheduledTime
	 */
	@JsonProperty("scheduledTime")
	void setScheduledTime(String scheduledTime);
	/**
	 * Установить интервал повторений, если задача - периодическая
	 * @param repeatInterval Интервал повторений
	 */
	@JsonProperty("repeatInterval")
	void setRepeatInterval(int repeatInterval);
	/**
	 * Установить единицу измерения интервала повторений для периодической задачи
	 * @param repeatIntervalUnit Единица измерения
	 */
	@JsonProperty("repeatIntervalUnit")
	void setRepeatIntervalUnit(ChronoUnit repeatIntervalUnit);
	/**
	 * Установить, за сколько минут до дедлайна напомнить о задаче
	 * @param minutesBefore Количество минут
	 */
	@JsonProperty("minutesBefore")
	void setMinutesBefore(int minutesBefore);
	/**
	 * Установить количество напоминаний до наступления дедлайна
	 * @param remindTimes Количество
	 */
	@JsonProperty("remindTimes")
	void setRemindTimes(int remindTimes);
	/**
	 * Установить интервал между напоминаниями о наступлении дедлайна. Интервал должен быть выбран таким образом, чтобы его произведение
	 * на количество напоминаний до наступления дедлайна не превышало заданное до наступления дедлайна количество минут
	 * @param remindTimesInterval Интервал
	 */
	@JsonProperty("remindTimesInterval")
	void setRemindTimesInterval(int remindTimesInterval);
	/**
	 * Устанавливает время создания напоминания
	 * @param taskCreationTime
	 */
	@JsonProperty("taskCreationTime")
	void setTaskCreationTime(String taskCreationTime);
	/**
	 * Возвращает задачу
	 * @return Задача
	 */
	@JsonProperty("task")
	ITask getTask();
	/**
	 * Возвращает время, на которое запланирован дедлайн задачи
	 * @return Строковое представление времени фиксированного формата
	 */
	@JsonProperty("scheduledTime")
	String getScheduledTime();
	/**
	 * Возвращает интервал повторений, если задача - периодическая
	 * @return Интервал повторений
	 */
	@JsonProperty("repeatInterval")
	int getRepeatInterval();
	/**
	 * Возвращает единицу измерения, в которой задан интервал повторений
	 * @return Единица измерения
	 */
	@JsonProperty("repeatIntervalUnit")
	ChronoUnit getRepeatIntervalUnit();
	/**
	 * Возвращает, за сколько минут до дедлайна напомнить о задаче
	 * @return Количество
	 */
	@JsonProperty("minutesBefore")
	int getMinutesBefore();
	/**
	 * Возвращает количество напоминаний до наступления дедлайна
	 * @return Количество
	 */
	int getRemindTimes();
	/**
	 * Возвращает интервал между напоминаниями о наступлении дедлайна
	 * @return Интервал
	 */
	@JsonProperty("remindTimes")
	int getRemindTimesInterval();
	/**
	 * Возвращает момент времени, в который была создана и запланирована задача
	 * @return Время
	 */
	@JsonProperty("taskCreationTime")
	String getTaskCreationTime();
	/**
	 * Завершает задачу, связанную с напоминанием, вычисляет в заданных единицах времени время, затраченное на выполнение задачи, и возвращает
	 * это время.
	 * @param units Единица измерения времени
	 * @return Время, затраченное на выполнение задачи
	 */
	long complete(ChronoUnit units);
}
