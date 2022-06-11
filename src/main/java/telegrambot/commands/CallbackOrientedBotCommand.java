package telegrambot.commands;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;

/**
 * Расширяет класс BotCommand, добавляя поле, в которое записывается префикс, соответствующий
 * классу-наследнику. Поле используется наследниками для маркировки callbackData, отправляемых
 * пользователями по нажатию на inline-кнопки, и - для фильтрации обновлений обработчиками этих
 * классов-наследников, если обновление содержит текст callbackData с этим префиксом
 * @author borodatyidrug
 */
public abstract class CallbackOrientedBotCommand extends BotCommand implements ICallbackHandlerCommand {
    /**
     * Префикс класса-наследника, добавляемый в callbackData inline-кнопок
     */
    protected final String callbackDataPrefix;
    protected boolean waitingUpdates;

    public CallbackOrientedBotCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
        this.callbackDataPrefix = "Command[" + commandIdentifier + "]"; // Так формируется префикс команды
        this.waitingUpdates = false; // По-умолчанию обновления для перехвата и обработки НЕ ожидаются до явного вызова команды
    }
    
    
    
    /**
     * Возвращает сгенерированный конструктором префикс для callbackData
     * @return Префикс для callbackData
     */
	public String getCallbackDataPrefix() {
	    return callbackDataPrefix;
	}
    
}
