package bg.sit_varna.sit.si.exception.exceptions;

import bg.sit_varna.sit.si.constant.ErrorCategory;
import bg.sit_varna.sit.si.constant.NotificationErrorCode;

public class TemplateRenderException extends NotificationException {

    private final String templateName;

    public TemplateRenderException(NotificationErrorCode errorCode,
                                   String title,
                                   String detail,
                                   String templateName) {
        super(errorCode, ErrorCategory.TEMPLATE_PROCESSING, title, detail);
        this.templateName = templateName;
    }

    public TemplateRenderException(NotificationErrorCode errorCode,
                                   String title,
                                   String detail,
                                   String templateName,
                                   Throwable cause) {
        super(errorCode, ErrorCategory.TEMPLATE_PROCESSING, title, detail, cause);
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }
}
