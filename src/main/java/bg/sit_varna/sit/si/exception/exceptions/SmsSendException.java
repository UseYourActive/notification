package bg.sit_varna.sit.si.exception.exceptions;

import bg.sit_varna.sit.si.constant.ErrorCategory;
import bg.sit_varna.sit.si.constant.NotificationErrorCode;

public class SmsSendException extends NotificationException {

    private final String recipient;
    private final String provider;

    public SmsSendException(NotificationErrorCode errorCode,
                            String title,
                            String detail,
                            String recipient,
                            String provider) {
        super(errorCode, ErrorCategory.NOTIFICATION_SENDING, title, detail);
        this.recipient = recipient;
        this.provider = provider;
    }

    public SmsSendException(NotificationErrorCode errorCode,
                            String title,
                            String detail,
                            String recipient,
                            String provider,
                            Throwable cause) {
        super(errorCode, ErrorCategory.NOTIFICATION_SENDING, title, detail, cause);
        this.recipient = recipient;
        this.provider = provider;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getProvider() {
        return provider;
    }
}
