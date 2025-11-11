package bg.sit_varna.sit.si.mapper;

import bg.sit_varna.sit.si.dto.model.Notification;
import bg.sit_varna.sit.si.dto.request.SendNotificationRequest;

import java.util.Locale;

public interface NotificationMapper {

    Notification toNotification(SendNotificationRequest request, Locale locale);
}
