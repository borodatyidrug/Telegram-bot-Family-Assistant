package telegrambot.familyassistant;

public enum Emoji {
	
	LOWER_LEFT_PAINTBRUSH(":lower_left_paintbrush:"), DATE(":date:"), WHITE_CHECK_MARK(":white_check_mark:"), 
	NEGATIVE_SQUARED_CROSS_MARK(":negative_squared_cross_mark:"), NO_ENTRY(":no_entry:"), BELL(":bell:"), 
	PUSHPIN(":pushpin:"), MEMO(":memo:"), POINT_RIGHT(":point_right:"), OK_HAND(":ok_hand:");
	
	private String code;
	
	Emoji(String code) {
		this.code = code;
	}
	
	@Override
	public String toString() {
		return code;
	}
}
