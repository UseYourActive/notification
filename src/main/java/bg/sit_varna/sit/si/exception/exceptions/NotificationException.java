package bg.sit_varna.sit.si.exception.exceptions;

import bg.sit_varna.sit.si.constant.ErrorCategory;
import bg.sit_varna.sit.si.constant.NotificationErrorCode;

public class NotificationException extends RuntimeException {

    private final NotificationErrorCode errorCode;
    private final ErrorCategory errorCategory;
    private final String title;
    private final String detail;

    public NotificationException(
            NotificationErrorCode errorCode,
            ErrorCategory errorCategory,
            String title,
            String detail) {
        super(detail);
        this.errorCode = errorCode;
        this.errorCategory = errorCategory;
        this.title = title;
        this.detail = detail;
    }

    public NotificationException(
            NotificationErrorCode errorCode,
            ErrorCategory errorCategory,
            String title,
            String detail,
            Throwable cause) {
        super(detail, cause);
        this.errorCode = errorCode;
        this.errorCategory = errorCategory;
        this.title = title;
        this.detail = detail;
    }

    public NotificationErrorCode getErrorCode() {
        return errorCode;
    }

    public ErrorCategory getErrorCategory() {
        return errorCategory;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public String getCode() {
        return errorCode.getCode();
    }

    public int getStatusCode() {
        return errorCode.getStatusCode();
    }
}
