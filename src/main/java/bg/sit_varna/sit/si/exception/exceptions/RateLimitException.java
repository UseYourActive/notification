package bg.sit_varna.sit.si.exception.exceptions;

import bg.sit_varna.sit.si.constant.ErrorCategory;
import bg.sit_varna.sit.si.constant.NotificationErrorCode;

public class RateLimitException extends NotificationException {

    private final long retryAfterSeconds;

    public RateLimitException(String title, String message, long retryAfterSeconds) {
        super(NotificationErrorCode.RATE_LIMIT_EXCEEDED, ErrorCategory.RATE_LIMIT, title, message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
