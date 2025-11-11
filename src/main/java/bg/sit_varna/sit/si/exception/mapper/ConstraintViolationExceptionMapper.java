package bg.sit_varna.sit.si.exception.mapper;

import bg.sit_varna.sit.si.exception.exceptions.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.List;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger LOG = Logger.getLogger(ConstraintViolationExceptionMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        LOG.warnf("Constraint violation: %s", exception.getMessage());

        List<String> errors = exception.getConstraintViolations()
                .stream()
                .map(violation -> {
                    // Extract just the property name (e.g., "channel" from "sendNotification.request.channel")
                    String propertyPath = violation.getPropertyPath().toString();
                    String propertyName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
                    return propertyName + ": " + violation.getMessage();
                })
                .sorted()
                .toList();

        String summary = errors.size() == 1
                ? errors.getFirst()
                : String.format("%d validation errors occurred", errors.size());

        ErrorResponse error = ErrorResponse.of(
                "VALIDATION_FAILED",
                summary,
                errors
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .build();
    }
}
