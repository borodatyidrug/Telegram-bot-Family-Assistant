package telegrambot.todos;

public class IncorrectDateTimeException extends Exception {

	private static final long serialVersionUID = -4125683436700524153L;
	private String message;
    
    IncorrectDateTimeException(String string) {
        message = string;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
