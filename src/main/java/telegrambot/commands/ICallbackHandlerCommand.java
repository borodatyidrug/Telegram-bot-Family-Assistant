package telegrambot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
/**
 * Интерфейс определяет для класса бот-команды два метода: метод-перехватчик управления и метод, возвращающий результат обработки
 * перехваченного обновления
 * @author borodatyidrug
 *
 */
public interface ICallbackHandlerCommand {
	/**
	 * Возвращает true, если имплементатор может перехватить и обработать данное обновление
	 * @param update Обновление, полученное ботом
	 * @return
	 */
	boolean catched(Update update);
	/**
	 * Возвращает сгенерированное сообщение-ответ на перехваченное обновление
	 * @param update Обновление, полученное ботом
	 * @return
	 */
	SendMessage getAnswer(Update update);
}
