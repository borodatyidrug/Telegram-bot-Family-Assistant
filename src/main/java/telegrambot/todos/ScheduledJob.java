package telegrambot.todos;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import telegrambot.familyassistant.FamilyAssistant;
/**
 * Класс-задача ("Работа"), единственное предназначение которой - отправить в нужный телеграм-чат сообщение, которое строится
 * объектом класса при вызове метода execute(). Сообщение собирается на основе данных из JobDataMap, получаемый из контекста.
 * В свою очередь, JobDataMap заполняется на этапе построения объектов JobDetail в иных очевидных местах
 * @author borodatyidrug
 *
 */
public class ScheduledJob implements Job {

	public ScheduledJob() {
	}

	@Override
	public void execute(JobExecutionContext context) {
		// ключи
		String taskChatId = "chatId";
		String sendMessage = "sendmessage";
		// ссылка на экземпляр бота
		AbsSender as = (AbsSender) FamilyAssistant.bot;
		
		JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
		try {
			System.out.println(jobDataMap.getString(sendMessage));
			as.execute(SendMessage.builder()
					.text(jobDataMap.getString(sendMessage))
					.chatId(jobDataMap.getString(taskChatId))
					.build());
		} catch (TelegramApiException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

}
