package bg.sit_varna.sit.si.exception.mapper;

import bg.sit_varna.sit.si.exception.exceptions.ErrorResponse;
import bg.sit_varna.sit.si.exception.exceptions.NotificationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class NotificationExceptionHandler implements ExceptionMapper<NotificationException> {

    private static final Logger LOG = Logger.getLogger(NotificationExceptionHandler.class);

    @Override
    public Response toResponse(NotificationException exception) {
        LOG.errorf("Notification error [%s]: %s - %s",
                exception.getCode(),
                exception.getTitle(),
                exception.getDetail());

        if (exception.getCause() != null) {
            LOG.error("Caused by: ", exception.getCause());
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(exception.getCode())
                .title(exception.getTitle())
                .message(exception.getDetail())
                .category(exception.getErrorCategory().getValue())
                .build();

        return Response
                .status(exception.getStatusCode())
                .entity(errorResponse)
                .build();
    }
}
