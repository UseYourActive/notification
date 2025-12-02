package bg.sit_varna.sit.si.service.channel.strategies;

import bg.sit_varna.sit.si.constant.NotificationChannel;
import bg.sit_varna.sit.si.dto.model.Notification;

public sealed interface ChannelStrategy permits
        EmailNotificationStrategy,
        SmsNotificationStrategy,
        TelegramNotificationStrategy {

    void send(Notification request);

    NotificationChannel getChannel();
}
