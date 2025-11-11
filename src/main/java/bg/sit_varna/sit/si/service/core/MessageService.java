package bg.sit_varna.sit.si.service.core;

import bg.sit_varna.sit.si.constant.NotificationErrorCode;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;
import java.util.ResourceBundle;


@ApplicationScoped
public class MessageService {

    private static final String BUNDLE_NAME = "messages/messages";

    public String getMessage(NotificationErrorCode errorCode, Locale locale, Object... args) {
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        String pattern = bundle.getString(errorCode.getMessageKey());

        if (args != null && args.length > 0) {
            return String.format(pattern, args);
        }
        return pattern;
    }

    public String getTitle(NotificationErrorCode errorCode, Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        return bundle.getString(errorCode.getTitleKey());
    }

    public String getMessage(NotificationErrorCode errorCode, Object... args) {
        return getMessage(errorCode, Locale.ENGLISH, args);
    }

    public String getTitle(NotificationErrorCode errorCode) {
        return getTitle(errorCode, Locale.ENGLISH);
    }
}
