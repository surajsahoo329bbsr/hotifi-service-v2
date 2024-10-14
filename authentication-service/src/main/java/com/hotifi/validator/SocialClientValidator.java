package com.hotifi.validator;

import com.hotifi.constants.codes.SocialCodes;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SocialClientValidator implements ConstraintValidator<SocialClient, String> {

    List<String> socialClients;

    @Override
    public void initialize(SocialClient constraintAnnotation) {
        socialClients = Stream.of(SocialCodes.values()).map(SocialCodes::name).collect(Collectors.toList());
    }

    @Override
    public boolean isValid(String socialClient, ConstraintValidatorContext constraintValidatorContext) {
        return socialClient == null || socialClients.contains(socialClient);
    }
}