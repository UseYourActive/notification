package bg.sit_varna.sit.si.validation.annotation;

import bg.sit_varna.sit.si.validation.validator.ValidNotificationRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidNotificationRequestValidator.class)
@Documented
public @interface ValidNotificationRequest {

    String message() default "Invalid notification request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
