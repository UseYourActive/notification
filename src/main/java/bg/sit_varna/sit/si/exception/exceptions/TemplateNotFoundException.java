package bg.sit_varna.sit.si.exception.exceptions;

import bg.sit_varna.sit.si.constant.ErrorCategory;
import bg.sit_varna.sit.si.constant.NotificationErrorCode;

public class TemplateNotFoundException extends NotificationException {

    private final String templateName;
    private final String locale;

    public TemplateNotFoundException(NotificationErrorCode errorCode,
                                     String title,
                                     String detail,
                                     String templateName,
                                     String locale) {
        super(errorCode, ErrorCategory.TEMPLATE_PROCESSING, title, detail);
        this.templateName = templateName;
        this.locale = locale;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getLocale() {
        return locale;
    }
}
