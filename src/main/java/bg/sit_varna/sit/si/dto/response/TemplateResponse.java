package bg.sit_varna.sit.si.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record TemplateResponse(
        UUID id,
        String templateName,
        String locale,
        String content,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
