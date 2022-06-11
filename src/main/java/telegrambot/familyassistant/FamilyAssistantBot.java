package telegrambot.familyassistant;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.quartz.SchedulerException;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import telegrambot.commands.ICallbackHandlerCommand;
import telegrambot.commands.AzbykaRu;
import telegrambot.commands.todos.AddTask;
import telegrambot.commands.todos.ListTasks;
import telegrambot.istorage.FileConfigStorage;
import telegrambot.istorage.IConfigStorage;
import telegrambot.todos.Reminder;

/**
 * Telegram-бот на основе технологии "длительного опроса" (дословно). Выполняет команды. Умеет отправлять в чат пользователя по запросу
 * некоторые материалы с портала Azbyka.ru, что можно запланировать на регулярной основе. Умеет добавлять в базу данных задачи и напоминания,
 * а так же восстанавливает их после перезагрузки, перезапуска, аварийного завершения работы и - т.д. Позволяет просмотреть список задач,
 * завершить или отменить задачу или напоминание. Конфигурационный файл для бота находится в корне проекта, либо - в том же каталоге, что
 * архив с приложением. В качестве упражнения и тренировки навыков работы с файловым вводом-выводом, хранение расписания запуска задач по 
 * рассылке сообщений бот-команды AzbykaRu организовано посредством фалового хранилища и json-сериализации соответствующих объектов.
 * Менеджер задач и напоминаний реализован посредством фреймворка Quartz, хранение задач - в БД PostgreSQL. На данном этапе приложение 
 * реализовано как одиночное, самостоятельное, независимое (standalone). В дальнейшем планируется интегрировать его с сервером приложений
 * Tomcat. Приложение - рабочее, выполняет свои функции. В разработке новые функции.
 * @author borodatyidrug
 *
 */
public class FamilyAssistantBot extends TelegramLongPollingCommandBot {
    
	public static final String DATE_TIME_FORMAT = "dd-MM-yyyy-HH-mm";
	private final String CONFIG = "/config";
    private final String botToken;
    /**
     * Размер пула потоков планировщика задач
     */
    private static final int SCHEDULER_POOL_SIZE = 2;
    protected static IConfigStorage configStorage;
    protected static Reminder reminder;
    protected static ScheduledExecutorService scheduler;
    // Получаем рабочий каталог, из которого было запущено приложение
    protected final static String workingDir = Paths.get("").toAbsolutePath().toString();
    protected final AzbykaRu azbykaRu;
    /**
     * Хранит список объектов типа ICallbackHandlerCommand - бот-комманд, которые перехватывают управление, если обновление с сервера
     * telegram содержит запрос обратного вызова (CallbackQuery), совпадающий с одним из запросов набора запросов бот-команды, которые
     * бот-команда может обработать
     */
    protected List<ICallbackHandlerCommand> handleableCommandList;
    
    public FamilyAssistantBot() {
        super();
        // Bot-token берем из соответствующей переменной окружения, которую нужно предварительно создать и присвоить ей значение
        this.botToken = (System.getenv("BD_FAMILY_ASSISTANT_BOT_TOKEN") == null 
                ? "5233864273:AAFxZ3JdeVnJpssGwvlt5AeLezJDRQlqRzw" : System.getenv("BD_FAMILY_ASSISTANT_BOT_TOKEN"));
        try {
			getConfigStorage().readConfig(workingDir + CONFIG);
		} catch (IOException e) {
			System.out.println("Файл конфигурации не найден по этому пути: " + workingDir);
			System.exit(1);
		}
        // Создаем и добавляем в список объекты комманд типа ICallbackHandlerCommand (команды с методом-перехватчиком)
        handleableCommandList = new ArrayList<>();
        handleableCommandList.add(new AddTask("newtask", "Создать новую задачу в списке ваших задач", this));
        handleableCommandList.add(new ListTasks("listtask", "Посмотреть список ваших задач"));
        // Объекты команд без метода-перехватчика
        azbykaRu = new AzbykaRu("azbyka", "Цитата дня на православном сайте Azbyka.ru", this);
        // Регистрируем команды
        register(azbykaRu);
        for (ICallbackHandlerCommand c : handleableCommandList) {
        	register((IBotCommand) c);
        }
    }
    /**
     * Возвращает рабочий каталог приложения
     * @return Рабочий каталог
     */
    public static String getWorkingDir() {
    	return workingDir;
    }
    /**
     * Возвращает планировщик задач для команды AzbykaRu, один общий для всего приложения в целом
     * @return Планировщик-синглтон
     */
    public static ScheduledExecutorService getScheduler( ) {
    	if (scheduler == null) {
    		scheduler = Executors.newScheduledThreadPool(SCHEDULER_POOL_SIZE);
    	}
    	return scheduler;
    }
    /**
     * Возвращает планировщик задач для остальных команд, один общий объект-обертка (фасад) над библиотекой Quartz для всего приложения
     * @return Планировщик-синглтон
     * @throws SchedulerException
     */
    public static Reminder getReminder() throws SchedulerException {
        if (reminder == null) {
            reminder = new Reminder((AbsSender) FamilyAssistant.bot);
        }
        return reminder;
    }
    /**
     * Возвращает объект-хранилище конфигурации приложения. Естественно, хранилище конфигурации - одно на все приложение. Соответственно,
     * объект, его представляющий, есть синглтон
     * @return Объект-хранилище конфига
     */
    public static IConfigStorage getConfigStorage() {
    	if (configStorage == null) {
    		configStorage = new FileConfigStorage();
    	}
    	return configStorage;
    }
    
    @Override
    public String getBotToken() {
        return botToken;
    }
       
    @Override
    public String getBotUsername() {
        return "@bd_FamilyAssistantBot";
    }
    
    @Override
    public void processNonCommandUpdate(Update update) {
		try {
			/**
			 * Основная работа приложения происходит через этот метод. Метод позволяет организовать диалог с пользователем посредством
			 * использования объектов команд с методом-перехватчиком "некомандных" обновлений, т.е. - обновлений, которые не представляют
			 * собой вызов команды бота типа "/command" или "/command@botname". Такой диалог начинается после вызова команды, когда
			 * отдельный управляющий объект, которому команда делегирует обработку запросов, или сам объект бот-команды обрабатывают
			 * запросы обратного вызова (CallbackQuery)
			 */
			for (ICallbackHandlerCommand c : handleableCommandList) {
				if (c.catched(update)) { // если объект бот-команды сообщает через метод-перехватчик, что данное обновление содержит 
					// запрос обратного вызова, который он ожидает и готов обработать, то управление передается методу бот-команды,
					// который это обновление обрабатывает (самостоятельно, или - делегируя) и возвращает результат, содержащий ответное
					// сообщение
					SendMessage answer = c.getAnswer(update);
					if (!answer.getText().isBlank()) { // если сообщение вернулось непустое, то бот выполняет соответстующий метод (SendMessage)
						// (согласно терминологии, принятой разработчиком данной библиотеки для telegram bot API)
						execute(answer);
					}
				}
			}
		} catch (TelegramApiException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
    }
    
}
