package telegrambot.todos;

import java.time.temporal.ChronoUnit;

public interface IReminderEntityBuilder {
    IReminderEntityBuilder setTask(ITask task);
    /**
     * Время, на которое запланировано задание. Время должно отличаться от текущего момента времени
     * и быть не далее, чем 120 лет от текущего момента времени
     * @param dateTime Время и дата в формате "dd-MM-yyyy-HH-mm"
     * @return IReminderEntityBuilder
     * @throws telegrambot.todos.IncorrectDateTimeException
     */
    IReminderEntityBuilder scheduledAt(String dateTime) throws IncorrectDateTimeException;
//    IReminderEntityBuilder repeatBy(int interval, TimeUnit unit);
    /**
     * Установить интервал времени для повторения задачи
     * @param interval Интервал времени повторения в заданых единицах измерения
     * @return 
     */
    IReminderEntityBuilder repeatByInterval (int interval);
    /**
     * Установить единицу измерения интервала времени для повторения задачи
     * @param units Единица измерения для интервала времени
     * @return
     */
    IReminderEntityBuilder repeatByIntervalUnits(ChronoUnit units);
    /**
     * Установить напоминание до требуемого момента выполнения задачи
     * @param minutesBefore За сколько минут до момента выполнения задачи напомнить
     * @return
     */
    IReminderEntityBuilder remindBeforeMinutes(int minutesBefore);
    /**
     * Установить напоминание до требуемого момента выполнения задачи
     * @param times Количество напоминаний до наступления задачи
     * @return
     */
    IReminderEntityBuilder remindBeforeTimes(int times);
    /**
     * Установить напоминание до требуемого момента выполнения задачи
     * @param interval Интервал напоминаний
     * @return
     * @throws IncorrectDateTimeException 
     */
    IReminderEntityBuilder remindBeforeInterval(int interval) throws IncorrectDateTimeException;
    /**
     * Зафиксировать время создания задачи
     * @return IReminderEntityBuilder
     */
    IReminderEntityBuilder fixTaskCreationTime();
    IReminderEntity build();
}
