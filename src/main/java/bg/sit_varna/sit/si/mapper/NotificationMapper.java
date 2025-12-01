package bg.sit_varna.sit.si.mapper;

import bg.sit_varna.sit.si.dto.model.Notification;
import bg.sit_varna.sit.si.dto.request.SendNotificationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Locale;
import java.util.UUID;

@Mapper(componentModel = "jakarta", imports = UUID.class)
public interface NotificationMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "locale", expression = "java(locale.toLanguageTag())")
    @Mapping(target = "processedContent", ignore = true)
    Notification toDomain(SendNotificationRequest request, Locale locale);
}
