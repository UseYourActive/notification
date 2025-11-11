package bg.sit_varna.sit.si.service.channel.strategies;

import bg.sit_varna.sit.si.constant.NotificationChannel;
import bg.sit_varna.sit.si.constant.NotificationErrorCode;
import bg.sit_varna.sit.si.dto.model.Notification;
import bg.sit_varna.sit.si.exception.exceptions.ViberSendException;
import bg.sit_varna.sit.si.service.channel.viber.ViberApiSender;
import bg.sit_varna.sit.si.service.core.MessageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public final class ViberNotificationStrategy implements ChannelStrategy {

    private static final Logger LOG = Logger.getLogger(ViberNotificationStrategy.class);

    @Inject
    ViberApiSender viberApiSender;

    @Inject
    MessageService messageService;

    @Override
    public void send(Notification request) {
        LOG.infof("Sending Viber message to: %s", request.getRecipient());

        if (!viberApiSender.isConfigured()) {
            throw new ViberSendException(
                    NotificationErrorCode.VIBER_SEND_FAILED,
                    messageService.getTitle(NotificationErrorCode.VIBER_SEND_FAILED),
                    messageService.getMessage(NotificationErrorCode.VIBER_SEND_FAILED,
                            request.getRecipient(),
                            "Viber is not configured. Please set viber.auth.token and viber.sender.name"),
                    request.getRecipient()
            );
        }

        viberApiSender.send(
                request.getRecipient(),
                request.getProcessedContent(),
                request.getData()  // Pass optional parameters (min_api_version, tracking_data, etc.)
        );

        LOG.infof("Viber message sent successfully to: %s", request.getRecipient());
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.VIBER;
    }
}
