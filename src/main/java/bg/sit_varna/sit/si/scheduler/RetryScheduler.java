package bg.sit_varna.sit.si.scheduler;

import bg.sit_varna.sit.si.dto.model.Notification;
import bg.sit_varna.sit.si.service.core.NotificationService;
import bg.sit_varna.sit.si.service.redis.RedisRetryService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class RetryScheduler {

    private static final Logger LOG = Logger.getLogger(RetryScheduler.class);

    @Inject RedisRetryService redisRetryService;
    @Inject NotificationService notificationService;

    /**
     * Runs every 60 seconds to check for messages ready to be retried.
     */
    @Scheduled(every = "60s")
    public void processRetries() {
        List<Notification> dueNotifications = redisRetryService.fetchDueNotifications();

        if (!dueNotifications.isEmpty()) {
            LOG.infof("Resurrecting %d notifications from Redis Cold Queue", dueNotifications.size());

            for (Notification notification : dueNotifications) {
                // We use the dispatch method to put it back into the internal memory queue
                // This triggers the whole @Retry cycle again
                notificationService.dispatchNotification(notification);
            }
        }
    }
}
