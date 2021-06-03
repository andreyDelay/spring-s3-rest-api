package com.andrey.springs3restapi.validator;

import com.andrey.springs3restapi.dto.UpdateUserDto;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component(value = "updateUserValidator")
@Scope(value = "prototype")
@PropertySource("classpath:validation.properties")
@Data
public class UpdateUserValidator implements Validator {

    @Value("${first.name.min.length}")
    private int FIRST_NAME_MIN_LENGTH;
    @Value("${first.name.max.length}")
    private int FIRST_NAME_MAX_LENGTH;
    @Value("${last.name.min.length}")
    private int LAST_NAME_MIN_LENGTH;
    @Value("${last.name.max.length}")
    private int LAST_NAME_MAX_LENGTH;
    @Value("${password.min.length}")
    private int PASSWORD_MIN_LENGTH;
    @Value("${password.max.length}")
    private int PASSWORD_MAX_LENGTH;

    @Override
    public boolean supports(Class<?> aClass) {
        return UpdateUserDto.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        UpdateUserDto user = (UpdateUserDto) o;
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String password = user.getPassword();
        String status = user.getStatus();

        if (isLengthNotValid(firstName, FIRST_NAME_MIN_LENGTH, FIRST_NAME_MAX_LENGTH) || isOnlyDigits(firstName)) {
            errors.rejectValue("firstName", "firstName.invalid");
        }

        if (isLengthNotValid(lastName, LAST_NAME_MIN_LENGTH, LAST_NAME_MAX_LENGTH) || isOnlyDigits(lastName)) {
            errors.rejectValue("lastName", "lastName.invalid");
        }

        if (isLengthNotValid(password, PASSWORD_MIN_LENGTH, PASSWORD_MAX_LENGTH)) {
            errors.rejectValue("password", "password.invalid");
        }

        if (!(status.equals("ACTIVE") || status.equals("DELETED"))) {
            errors.rejectValue("status", "status.invalid");
        }

    }

    private boolean isOnlyDigits(String field) {
        return field.replaceAll("[^a-zA-Z]", "").length() == 0;
    }

    private boolean isLengthNotValid(String field, int min, int max) {
        return field.length() < min || field.length() > max;
    }
}
