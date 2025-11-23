package bg.sit_varna.sit.si.controller.resource;

import bg.sit_varna.sit.si.config.app.LocaleResolver;
import bg.sit_varna.sit.si.constant.NotificationStatus;
import bg.sit_varna.sit.si.controller.api.NotificationApi;
import bg.sit_varna.sit.si.controller.base.BaseResource;
import bg.sit_varna.sit.si.dto.model.Notification;
import bg.sit_varna.sit.si.dto.request.SendNotificationRequest;
import bg.sit_varna.sit.si.dto.response.SendNotificationResponse;
import bg.sit_varna.sit.si.mapper.NotificationMapper;
import bg.sit_varna.sit.si.service.core.NotificationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.Locale;
import java.util.UUID;

@ApplicationScoped
public class NotificationResource extends BaseResource implements NotificationApi {

    private static final Logger LOG = Logger.getLogger(NotificationResource.class);

    private NotificationService notificationService;
    private NotificationMapper notificationMapper;

    @Inject
    public NotificationResource(LocaleResolver localeResolver,
                                NotificationService notificationService,
                                NotificationMapper notificationMapper) {
        super(localeResolver);
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
    }

    protected NotificationResource() {
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * POST /api/v1/notifications/send
     */
    @Override
    public Response sendNotification(@Valid SendNotificationRequest request) {
        Locale resolvedLocale = resolveLocale();

        String notificationId = UUID.randomUUID().toString();

        Notification notification = notificationMapper.toNotification(request, resolvedLocale);

        // notification.setId(notificationId);

        notificationService.dispatchNotification(notification);

        SendNotificationResponse response = SendNotificationResponse.of(
                notificationId,
                NotificationStatus.QUEUED,
                "Notification queued for delivery",
                request.recipient(),
                request.channel().toString()
        );

        return Response.status(Response.Status.ACCEPTED).entity(response).build();
    }
}
