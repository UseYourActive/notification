package bg.sit_varna.sit.si.exception.exceptions;

import bg.sit_varna.sit.si.constant.ErrorCategory;
import bg.sit_varna.sit.si.constant.NotificationErrorCode;

public class TelegramSendException extends NotificationException {

    private final String chatId;

    public TelegramSendException(NotificationErrorCode errorCode,
                                 String title,
                                 String detail,
                                 String chatId) {
        super(errorCode, ErrorCategory.NOTIFICATION_SENDING, title, detail);
        this.chatId = chatId;
    }

    public TelegramSendException(NotificationErrorCode errorCode,
                                 String title,
                                 String detail,
                                 String chatId,
                                 Throwable cause) {
        super(errorCode, ErrorCategory.NOTIFICATION_SENDING, title, detail, cause);
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }
}
