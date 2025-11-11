package bg.sit_varna.sit.si.mapper;

import bg.sit_varna.sit.si.dto.model.Notification;
import bg.sit_varna.sit.si.dto.request.SendNotificationRequest;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.Locale;

@ApplicationScoped
public class NotificationMapperImpl implements NotificationMapper {

    private static final Logger LOG = Logger.getLogger(NotificationMapperImpl.class);

    @Override
    public Notification toNotification(SendNotificationRequest request, Locale locale) {
        LOG.debugf("Mapping SendNotificationRequest to Notification - Channel: %s, Recipient: %s",
                request.channel(), request.recipient());

        return Notification.builder()
                .channel(request.channel())
                .recipient(request.recipient())
                .templateName(request.templateName())
                .locale(locale.toLanguageTag())
                .message(request.message())
                .data(request.data())
                .build();
    }
}
