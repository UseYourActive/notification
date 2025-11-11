package bg.sit_varna.sit.si.exception.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Error response structure")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(

        @Schema(description = "Error code (machine-readable identifier)",
                example = "VALIDATION_FAILED",
                required = true)
        String code,

        @Schema(description = "Error title (human-readable category)",
                example = "Validation Failed")
        String title,

        @Schema(description = "Error message (detailed description)",
                example = "The request contains invalid data",
                required = true)
        String message,

        @Schema(description = "Error category (VALIDATION, TEMPLATE, NOTIFICATION, SYSTEM)",
                example = "VALIDATION")
        String category,

        @Schema(description = "Timestamp when the error occurred",
                example = "2025-10-26T15:30:00",
                required = true)
        LocalDateTime timestamp,

        @Schema(description = "Detailed error information (optional)",
                example = "[\"recipient: must not be blank\", \"channel: must not be null\"]")
        List<String> details
) {
    public ErrorResponse(String code, String message) {
        this(code,
                null,
                message,
                null,
                LocalDateTime.now(),
                null);
    }

    public ErrorResponse(String code, String message, List<String> details) {
        this(code, null, message, null, LocalDateTime.now(), details);
    }

    public ErrorResponse(String code, String title, String message, String category) {
        this(code, title, message, category, LocalDateTime.now(), null);
    }

    public ErrorResponse {
        if (details != null) {
            details = List.copyOf(details);
        }
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, null, message, null, LocalDateTime.now(), null);
    }

    public static ErrorResponse of(String code, String message, List<String> details) {
        return new ErrorResponse(code, null, message, null, LocalDateTime.now(), details);
    }

    public static ErrorResponse of(String code, String title, String message, String category) {
        return new ErrorResponse(code, title, message, category, LocalDateTime.now(), null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String code;
        private String title;
        private String message;
        private String category;
        private LocalDateTime timestamp = LocalDateTime.now();
        private List<String> details;

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder details(List<String> details) {
            this.details = details;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(code, title, message, category, timestamp, details);
        }
    }
}
