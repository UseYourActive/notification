package bg.sit_varna.sit.si.exception.exceptions;

import bg.sit_varna.sit.si.constant.ErrorCategory;
import bg.sit_varna.sit.si.constant.NotificationErrorCode;

public class TemplateScanException extends NotificationException {

    public TemplateScanException(NotificationErrorCode errorCode,
                                 String title,
                                 String detail) {
        super(errorCode, ErrorCategory.TEMPLATE_PROCESSING, title, detail);
    }

    public TemplateScanException(NotificationErrorCode errorCode,
                                 String title,
                                 String detail,
                                 Throwable cause) {
        super(errorCode, ErrorCategory.TEMPLATE_PROCESSING, title, detail, cause);
    }
}
