package bg.sit_varna.sit.si.exception.exceptions;

import bg.sit_varna.sit.si.constant.ErrorCategory;
import bg.sit_varna.sit.si.constant.NotificationErrorCode;

public class ViberSendException extends NotificationException {

    private final String recipient;

    public ViberSendException(NotificationErrorCode errorCode,
                              String title,
                              String detail,
                              String recipient) {
        super(errorCode, ErrorCategory.NOTIFICATION_SENDING, title, detail);
        this.recipient = recipient;
    }

    public ViberSendException(NotificationErrorCode errorCode,
                              String title,
                              String detail,
                              String recipient,
                              Throwable cause) {
        super(errorCode, ErrorCategory.NOTIFICATION_SENDING, title, detail, cause);
        this.recipient = recipient;
    }

    public String getRecipient() {
        return recipient;
    }
}
