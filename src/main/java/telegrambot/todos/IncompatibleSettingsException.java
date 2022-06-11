package telegrambot.todos;

public class IncompatibleSettingsException extends Exception {

	private static final long serialVersionUID = -3528138474533185455L;
	private static String defaultMessage = "Обнаружены несовместимые друг с другом значения полей. Значения должны быть взаимоисключающими.";
	
	public IncompatibleSettingsException() {
		super(defaultMessage);
	}
	
	public IncompatibleSettingsException(String message) {
		super(message);
	}
	
}
