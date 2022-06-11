package telegrambot.todos;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import org.quartz.SchedulerException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
/**
 * Интерфейс определяет простой набор методов для управления задачами и напоминаниями планировщика задач Quartz
 * @author borodatyidrug
 *
 */
public interface IReminder {
	/**
	 * Добавляет простую задачу в планировщик задач. В терминологии Quartz такая задача (JobDetail) является Durable ("длительная", 
	 * "продолжительная"), и не связана с каким-либо временнЫм триггером.
	 * @param task Задача
	 * @throws SchedulerException
	 * @throws JsonProcessingException
	 */
	void addTask(ITask task) throws SchedulerException, JsonProcessingException;
	/**
	 * Добавляет в планировщик задачу-напоминание, формируемое из объекта IReminderEntity, собираемого на этапе диалога с пользователем.
	 * @param reminderEntity Объект-хранилище, сконструированный при опросе пользователя о параметрах расписания выполения задачи-напоминания
	 * @throws SchedulerException
	 * @throws JsonProcessingException
	 */
	void scheduleRemind(IReminderEntity reminderEntity) throws SchedulerException, JsonProcessingException;
	/**
	 * Возвращает актуальный список простых задач из планировщика Quartz
	 * @param userId ID пользователя, для которого возвращается список его задач
	 * @return Список задач
	 * @throws SchedulerException
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
	List<ITask> getTaskList(String userId) throws SchedulerException, JsonMappingException, JsonProcessingException;
	/**
	 * Возвращает актуальный список напоминаний и задач с обозначенным дедлайном
	 * @param userId ID пользователя, для которого возвращается список его напоминаний и задач с обозначенным дедлайном
	 * @return Список напоминаний и задач с обозначенным дедлайном
	 * @throws SchedulerException
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
	List<IReminderEntity> getRemindsList(String userId) throws SchedulerException, JsonMappingException, JsonProcessingException;
	/**
	 * Удалаяет простую задачу из планировщика
	 * @param task Удаляемая задача
	 * @return Удаленная задача
	 * @throws SchedulerException
	 */
	ITask completeTask(ITask task) throws SchedulerException;
	/**
	 * Удаляет напоминание или задачу с обозначенным дедлайном из планировщика
	 * @param reminderEntity Удаляемое напоминание или задача с обозначенным дедлайном
	 * @param unit Единица измерения для рассчета времени, затраченного на выполнение задачи
	 * @return Удаленное напоминание или задача с обозначенным дедлайном
	 * @throws SchedulerException
	 */
	IReminderEntity completeTask(IReminderEntity reminderEntity, ChronoUnit unit) throws SchedulerException;
}
