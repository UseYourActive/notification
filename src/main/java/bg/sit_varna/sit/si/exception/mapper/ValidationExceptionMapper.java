package bg.sit_varna.sit.si.exception.mapper;

import bg.sit_varna.sit.si.exception.exceptions.ErrorResponse;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    private static final Logger LOG = Logger.getLogger(ValidationExceptionMapper.class);

    @Override
    public Response toResponse(ValidationException exception) {
        LOG.warnf("Validation error: %s", exception.getMessage());

        // Extract the cause message if available
        String message = exception.getCause() != null && exception.getCause().getMessage() != null
                ? exception.getCause().getMessage()
                : exception.getMessage();

        ErrorResponse error = ErrorResponse.of(
                "VALIDATION_ERROR",
                message != null ? message : "Validation failed"
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .build();
    }
}
