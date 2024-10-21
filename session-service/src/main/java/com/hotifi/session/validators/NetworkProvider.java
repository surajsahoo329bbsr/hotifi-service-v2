package com.hotifi.session.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = NetworkProviderValidator.class)
public @interface NetworkProvider {

    String message() default "Value must be one in {WIFI, JIO, AIRTEL, BSNL, VODAFONE, OTHERS}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
