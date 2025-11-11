package bg.sit_varna.sit.si.service.core;

import bg.sit_varna.sit.si.config.app.ApplicationConfig;
import bg.sit_varna.sit.si.constant.ErrorCategory;
import bg.sit_varna.sit.si.constant.NotificationChannel;
import bg.sit_varna.sit.si.constant.NotificationErrorCode;
import bg.sit_varna.sit.si.constant.NotificationStatus;
import bg.sit_varna.sit.si.dto.model.Notification;
import bg.sit_varna.sit.si.exception.exceptions.NotificationException;
import bg.sit_varna.sit.si.exception.exceptions.RateLimitException;
import bg.sit_varna.sit.si.service.channel.strategies.ChannelStrategy;
import bg.sit_varna.sit.si.service.redis.DeduplicationService;
import bg.sit_varna.sit.si.service.redis.MetricsService;
import bg.sit_varna.sit.si.service.redis.RateLimitService;
import bg.sit_varna.sit.si.template.core.TemplateService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class NotificationService {

    private static final Logger LOG = Logger.getLogger(NotificationService.class);

    @Inject
    ApplicationConfig applicationConfig;

    @Inject
    TemplateService templateService;

    @Inject
    Instance<ChannelStrategy> strategies;

    @Inject
    MessageService messageService;

    Map<NotificationChannel, ChannelStrategy> strategyMap;

    @Inject
    MetricsService metricsService;

    @Inject
    DeduplicationService deduplicationService;

    @Inject
    RateLimitService rateLimitService;

    @PostConstruct
    void initializeStrategyMap() {
        strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        ChannelStrategy::getChannel,
                        strategy -> strategy,
                        (existing, replacement) -> {
                            LOG.warnf("Duplicate strategy found for channel %s. Using first one.",
                                    existing.getChannel());
                            return existing;
                        }
                ));

        LOG.infof("Initialized NotificationService with %d channel strategies: %s",
                strategyMap.size(),
                strategyMap.keySet());
    }

    /**
     * Send a notification through the appropriate channel.
     *
     * This method processes the request by:
     * 1. Rendering the template (if templateName is provided) or using the plain message
     * 2. Setting the processedContent in the request
     * 3. Passing the complete request to the channel strategy
     *
     * @param request the notification request
     */
    public void sendNotification(Notification request) {
        try {
            Locale locale = Locale.forLanguageTag(request.getLocale());

            // 1. Rate limiting check FIRST
            if (!rateLimitService.isAllowed(request.getRecipient(), request.getChannel())) {
                long resetTime = rateLimitService.getResetTime(request.getRecipient(), request.getChannel());
                throw new RateLimitException(
                        messageService.getTitle(NotificationErrorCode.RATE_LIMIT_EXCEEDED, locale),
                        messageService.getMessage(NotificationErrorCode.RATE_LIMIT_EXCEEDED,
                                locale,
                                request.getChannel().toString(),
                                request.getRecipient(),
                                resetTime),
                        resetTime
                );
            }

            // 2. Check for duplicates
            String content = request.usesTemplate() ? request.getTemplateName() : request.getMessage();
            if (deduplicationService.isDuplicate(request.getRecipient(), request.getChannel(), content)) {
                LOG.warnf("Skipping duplicate notification for %s via %s",
                        request.getRecipient(), request.getChannel());
                return;
            }

            // 3. Process the content (template rendering or plain message)
            String processedContent = processContent(request);

            // 4. Set the processed content
            request.setProcessedContent(processedContent);

            // 5. Get the strategy and send
            ChannelStrategy strategy = getStrategy(request.getChannel(), locale);
            strategy.send(request);

            // 6. Record success metric
            metricsService.recordNotification(request.getChannel(), NotificationStatus.SENT);

        } catch (Exception e) {
            if (!(e instanceof RateLimitException)) {
                metricsService.recordNotification(request.getChannel(), NotificationStatus.FAILED);
            }
            throw e;
        }
    }

    private String processContent(Notification request) {
        // Check if using template or plain message
        if (request.getTemplateName() != null && !request.getTemplateName().isBlank()) {
            // Use template
            LOG.debugf("Using template: %s with locale: %s",
                    request.getTemplateName(), request.getLocale());
            return templateService.renderTemplate(
                    request.getTemplateName(),
                    request.getLocale() != null ? request.getLocale() : applicationConfig.defaultLocale(),
                    request.getData()
            );
        } else {
            // Use plain message
            LOG.debugf("Using plain message (no template)");
            return request.getMessage();
        }
    }

    private ChannelStrategy getStrategy(NotificationChannel channel, Locale locale) {
        ChannelStrategy strategy = strategyMap.get(channel);

        if (strategy == null) {
            String supportedChannels = strategyMap.keySet()
                    .stream()
                    .map(NotificationChannel::toString)
                    .collect(Collectors.joining(", "));

            throw new NotificationException(
                    NotificationErrorCode.CHANNEL_NOT_SUPPORTED,
                    ErrorCategory.VALIDATION,
                    messageService.getTitle(NotificationErrorCode.CHANNEL_NOT_SUPPORTED, locale),
                    messageService.getMessage(NotificationErrorCode.CHANNEL_NOT_SUPPORTED,
                            locale,
                            channel.toString(),
                            supportedChannels)
            );
        }
        return strategy;
    }
}
