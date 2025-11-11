package bg.sit_varna.sit.si.exception.mapper;

import bg.sit_varna.sit.si.exception.exceptions.ErrorResponse;
import bg.sit_varna.sit.si.exception.exceptions.RateLimitException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.List;

@Provider
public class RateLimitExceptionMapper implements ExceptionMapper<RateLimitException> {

    private static final Logger LOG = Logger.getLogger(RateLimitExceptionMapper.class);

    @Override
    public Response toResponse(RateLimitException exception) {
        LOG.warnf("Rate limit exceeded: %s", exception.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(exception.getErrorCode().getCode())
                .title(exception.getTitle())
                .message(exception.getMessage())
                .category(exception.getErrorCategory().name())
                .details(List.of("retryAfter: " + exception.getRetryAfterSeconds() + " seconds"))
                .build();

        return Response
                .status(Response.Status.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(exception.getRetryAfterSeconds()))
                .entity(errorResponse)
                .build();
    }
}
