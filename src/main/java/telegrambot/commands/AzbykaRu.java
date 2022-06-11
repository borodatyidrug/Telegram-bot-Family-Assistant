package telegrambot.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import telegrambot.familyassistant.FamilyAssistantBot;
import telegrambot.webparsers.azbykaru.AzbykaParser;

/**
 * Расширяет BotCommand. Объект класса представляет собой "команду" для бота, которая регистрируется кодом бота и может исполняться при
 * ее вызове в чате по ее идентификатору
 * @author borodatyidrug
 *
 */
public class AzbykaRu extends BotCommand {
    
    private final String START = "start";
    private final String STOP = "stop";
    private final String VIEW = "view";
    private final String CONFIG_SUFFIX = "config";
    /**
     * Идентификатор аргумента для разового запуска команды
     */
    private final String ONE_TIME = "one-time";
    /**
     * Имена ключей и значений для записи и извлечения из мапы с конфигом
     */
    private final String TYPE = "type";
    private final String REPEAT = "repeat";
    private final String WHEN = "when";
    private final String PERIOD = "period";
    /**
     * Корневой каталог для хранения конфига расписаний отправки сообщений в чаты
     */
    private final String rootPath;
    /**
     * В данной мапе хранятся настройки планировщика задач для отсылки запланированных сообщений для каждого чата
     * Корневая мапа: ключ - chatId, значение - мапа с конфигом для данного чата
     * Вложенная мапа: ключ - имя параметра, значение - временной промежуток
     */
    private Map<String, Map<String, String>> configMap;
    private ObjectMapper mapper;
    
    /**
     * Хранит переданный через конструктор экземпляр планировщика задач
     */
    private final ScheduledExecutorService scheduler;
    /**
     * Хранит объект, отправляющий сообщения в чаты
     */
    private final AbsSender as;
    private LocalDateTime when;
    private Runnable task;
    /**
     * Мапа с запланированными планировщиком задачами. Задачи можно отменить, получая их по chatId (ключ)
     */
    private Map<String, ScheduledFuture<?>> futureTasks;
    private File file, rootDir;

    /**
     * Создает объект "команды"
     * @param identifier Идентификатор команды (суть - имя)
     * @param description Описание команды
     * @param as Объект, который может посылать сообщения в чаты. Обычно - сам бот
     */
    public AzbykaRu(String identifier, String description, AbsSender as) {
        super(identifier, description);
        this.as = as;
        // Имя конечной папки для хранения расписания берем из "хранилища конфигураций" в классе бота статическим методом
        rootPath = Paths.get("").toAbsolutePath().toString() + FamilyAssistantBot.getConfigStorage().getValue("azbykaRuPath");
        mapper = new ObjectMapper();
        // Планировщик достаем статическим методом из объекта бота
        this.scheduler = FamilyAssistantBot.getScheduler();
        futureTasks = new HashMap<>();
        // Создаем корневой каталог с конфигурацией в рабочем каталоге программы, если он несуществует
        rootDir = new File(rootPath);
        file = new File(rootDir, "/" + CONFIG_SUFFIX);
        if (!rootDir.exists()) {
        	rootDir.mkdir();
        }
        try {
			file.createNewFile();
		} catch (IOException e1) {
			System.out.println(e1.getMessage());
		}
        // Если файл - пуст, то мапа с конфигом создается заново
        try (FileInputStream fis = new FileInputStream(file)) {
        	configMap = mapper.readValue(fis, new TypeReference<Map<String, Map<String, String>>>() {});
			rescheduleTasks();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			configMap = new HashMap<>();
		}
    }
    
    /**
     * Вложенный класс, который используется для создания
     * задания типа Runnable, которое можно запланировать, передав объекту типа
     * ScheduledExecutorService. Задание отсылает в чат заранее подготовленное сообщение с распарсенными инф. блоками с Azbyka.ru
     */
    protected class AzbykaTask implements Runnable {

        private final String chatId;
        private final AbsSender as;
        
        public AzbykaTask(String chatId, AbsSender as) {
            this.chatId = chatId;
            this.as = as;
        }
        
        @Override
        public void run() {
            try {
                as.execute(sendMessageBuild(chatId));
            } catch (TelegramApiException ex) {
                System.out.println(ex.getMessage());
            }
        }
        
    }
    /**
     * Возвращает сконфигурированный SendMessage с результатами парсинга необходимых информационных блоков с заданной страницы
     * портала Azbyka.ru
     * @param chatId Иденитификатор чата, в который будет обправлен собранный объект сообщения
     * @return Объект сообщения
     */
    protected SendMessage sendMessageBuild(String chatId) {
    	return SendMessage.builder()
                .chatId(chatId)
                .disableWebPagePreview(Boolean.TRUE)
                .text(new AzbykaParser()
                        .getQuoteOfDay("https://azbyka.ru/days/")
                        .getParableOfDay("https://azbyka.ru/days/")
                        .getFundamentals("https://azbyka.ru/days/")
                        .getText())
                .build();
    }
    /**
     * Восстанавливает ранее запланированные задания, если таковые были, считывая расписание из конфигурационной мапы
     */
    protected void rescheduleTasks() {
    	if (configMap != null && !configMap.isEmpty()) {
    		LocalDateTime when, now; // Моменты времени целевой и текущий ("сейчас")
    		long remains; // Время, оставшееся до момента запуска задания планировщиком
    		var keySet = configMap.keySet(); // Множество с chatId всех чатов, для которых была запланирована отправка сообщения
    		for (var k : keySet) {
    			var currentChatConfig = configMap.get(k); // Мапа с параметрами расписания для текущего чата
    			var chatId = k; // ID текущего чата
    			if (currentChatConfig.get(TYPE).equals(ONE_TIME)) { // Если тип запланированной задачи - одиночная
    				when = LocalDateTime.parse(currentChatConfig.get(WHEN));
    				now = LocalDateTime.now();
    				if (when.isAfter(now)) { // Если запланированные дата-время еще не наступили, то создаем объект задачи и передаем ему сообщение
    					var task = new AzbykaTask(chatId, as);
    					remains = now.until(when, ChronoUnit.MINUTES); // считаем задержку в заданных единицах
    					// помещаем в мапу с запланированными задачами, вызывая планировщик и передавая ему объект задачи. Планировщик
    					// возвращает объект ScheduledFuture, у которого можно получить информацию о текущем состоянии запланированной задачи
                        futureTasks.put(chatId, scheduler.schedule(task, remains, TimeUnit.MINUTES));
    				}
    				// Если тип запланированной задачи - периодическая
    			} else if (currentChatConfig.get(TYPE).equals(REPEAT)) {
    				when = LocalDateTime.parse(currentChatConfig.get(WHEN));
    				now = LocalDateTime.now();
    				var period = Long.parseLong(currentChatConfig.get(PERIOD)); // то получаем период повторений в часах
    				if (when.isBefore(now)) { // если задание по расписанию запланированно на уже ранее наступившую дату
    					while(when.isBefore(now)) {
    						when = when.plusHours(period); // то увеличиваем целевую дату на период до тех пор, пока она не станет предстоящей
    					}
    				}
					var task = new AzbykaTask(chatId, as);
					remains = now.until(when, ChronoUnit.MINUTES); // считаем задержку до целевой даты от текущего момента времени
					futureTasks.put( // планируем задачу
							chatId, 
							scheduler.scheduleAtFixedRate(
									task, 
									remains, 
									TimeUnit.HOURS.toMinutes(period), 
									TimeUnit.MINUTES));
    			}
    		}
    	}
    }
    
    @Override
    public void execute(AbsSender as, User user, Chat chat, String[] args) {
    	var chatId = chat.getId().toString();
    	// Создаем сообщение со результатом парсинга целевых html-блоков указанных ранее страниц
        task = new AzbykaTask(chat.getId().toString(), as);
        try (FileOutputStream fos = new FileOutputStream(file, false)) {
            // Если никаких аргументов при вызове не указано, то задание с отправкой сообщения выполняется сразу
            if (args == null || args.length == 0) {
                task.run();
            } else {
            	// Иначе если два аргумента - ONE_TIME и время, то - пытаемся запланировать разовое выполнение задачи
                if (args.length == 2 && args[0].toLowerCase().equals(ONE_TIME)) {
                	// Если мапа с конфигом непуста и содержит внутреннюю мапу с конфигом для данного chatId, то
                	if (!configMap.isEmpty() && configMap.containsKey(chatId)) {
                		as.execute(SendMessage.builder()
                				.text("Сначала отмените ранее запланированное задание с помощью команды \"" + STOP + "\"")
                				.chatId(chatId)
                				.build());
                		// Если мапа с конфигом не содержит внутреннюю мапу с конфигом для данного chatId, то
                	} else if (!configMap.containsKey(chatId)) {
                        when = LocalDateTime.parse(args[1].toLowerCase(), DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm"));
                        LocalDateTime now = LocalDateTime.now();
                        if (when.isAfter(now)) {
                        	long remains = now.until(when, ChronoUnit.MINUTES);
                            // даем планировщику задачу и полученный ScheduledFuture кладем в мапу запланированных ScheduledFuture'ов по ключу chatId
                            futureTasks.put(chatId, scheduler.schedule(task, remains, TimeUnit.MINUTES));
                            // и создаем внутреннюю мапу-конфиг для этой задачи
                            Map<String, String> currChatEntry = new HashMap<>();
                            currChatEntry.put(TYPE, ONE_TIME);
                            currChatEntry.put(WHEN, when.toString());
                            // которую помещаем в основную мапу-конфиг по ключу chatId
                            configMap.put(chatId, currChatEntry);
                            
                            as.execute(SendMessage.builder()
                            		.text("Задача запланирована разово на " + when.toString())
                            		.chatId(chatId)
                            		.build());
                        } else {
                        	as.execute(SendMessage.builder()
                        			.text("Джордж Оруэлл имел в виду совсем не то, когда говорил: \"кто управляет прошлым, тот управляет будущим\"...")
                        			.chatId(chatId)
                        			.build());
                        }
                	}
                	// Иначе если три аргумента - START, dd-MM-yyyy-HH-mm и PERIOD, то
                } else if (args.length == 3 && args[0].toLowerCase().equals(START)) {
                	// по аналогии с предыдущим случаем
                	if (!configMap.isEmpty() && configMap.containsKey(chatId)) {
                		as.execute(SendMessage.builder()
                				.text("Сначала отмените ранее запланированное задание с помощью команды \"" + STOP + "\"")
                				.chatId(chatId)
                				.build());
                	} else if (!configMap.containsKey(chatId)) {
                        when = LocalDateTime.parse(args[1].toLowerCase(), DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm"));
                        LocalDateTime now = LocalDateTime.now();
                        if (when.isAfter(now)) {
                        	long remains = now.until(when, ChronoUnit.MINUTES);
                            long period = Long.parseLong(args[2].toLowerCase());
                            futureTasks.put(
                            		chatId, 
                            		scheduler.scheduleAtFixedRate(
                            				task,
                            				remains, 
                            				TimeUnit.HOURS.toMinutes(period), 
                            				TimeUnit.MINUTES));
                            
                            Map<String, String> currChatEntry = new HashMap<>();
                            currChatEntry.put(TYPE, REPEAT);
                            currChatEntry.put(WHEN, when.toString());
                            currChatEntry.put(PERIOD, period + "");
                            configMap.put(chatId, currChatEntry);
                            
                            as.execute(SendMessage.builder()
                            		.text("Задача запланирована на " + when.toString() + " c периодом повторения " + period + " ч.")
                            		.chatId(chatId)
                            		.build());
                        } else {
                        	as.execute(SendMessage.builder()
                        			.text("Джордж Оруэлл имел в виду совсем не то, когда говорил: \"кто управляет прошлым, тот управляет будущим\"...")
                        			.chatId(chatId)
                        			.build());
                        }
                	}
                } else if (args.length == 1 && args[0].toLowerCase().equals(STOP)) {
                    if (futureTasks.containsKey(chatId)) {
                    	futureTasks.remove(chatId).cancel(true);
                    	configMap.remove(chatId);
                    	as.execute(SendMessage.builder()
                    			.text("Задача отменена")
                    			.chatId(chatId)
                    			.build());
                    } else {
                    	as.execute(SendMessage.builder()
                    			.text("Вы еще не запланировали задачу. Отменять - нечего")
                    			.chatId(chatId)
                    			.build());
                    }
                } else if (args.length == 1 && args[0].toLowerCase().equals(VIEW)) {
                	if (!configMap.isEmpty() && configMap.containsKey(chatId)) {
                		as.execute(SendMessage.builder()
                				.text(configMap.get(chatId).toString())
                				.chatId(chatId)
                				.build());
                	} else {
                		as.execute(SendMessage.builder()
                				.text("Задача не запланирована")
                				.chatId(chatId)
                				.build());
                	}
                }
            }
            mapper.writeValue(fos, configMap);
        } catch (IndexOutOfBoundsException | DateTimeParseException | TelegramApiException | IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
}
