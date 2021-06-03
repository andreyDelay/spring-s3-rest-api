package com.andrey.springs3restapi.validator;

import com.andrey.springs3restapi.dto.auth.UserRegistrationDto;
import com.andrey.springs3restapi.service.AccountService;
import com.andrey.springs3restapi.service.UserService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(value = "registrationValidator")
@PropertySource("classpath:validation.properties")
@Data
public class RegistrationValidator implements Validator {

    private final UserService userService;
    private final AccountService accountService;

    @Value("${spring.validator.email.regex}")
    private String emailRegex;

    @Value("${username.min.length}")
    private int USERNAME_MIN_LENGTH;
    @Value("${username.max.length}")
    private int USERNAME_MAX_LENGTH;
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

    @Autowired
    public RegistrationValidator(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return UserRegistrationDto.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        UserRegistrationDto user = (UserRegistrationDto) o;
        String username = user.getUsername();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String email = user.getEmail();
        String password = user.getPassword();
        String login = user.getLogin();

        if (isLengthNotValid(username, USERNAME_MIN_LENGTH, USERNAME_MAX_LENGTH) || isOnlyDigits(username)) {
            errors.rejectValue("username", "username.invalid");
        }

        if (isLengthNotValid(firstName, FIRST_NAME_MIN_LENGTH, FIRST_NAME_MAX_LENGTH) || isOnlyDigits(firstName)) {
            errors.rejectValue("firstName", "firstName.invalid");
        }

        if (isLengthNotValid(lastName, LAST_NAME_MIN_LENGTH, LAST_NAME_MAX_LENGTH) || isOnlyDigits(lastName)) {
            errors.rejectValue("lastName", "lastName.invalid");
        }

        if (isEmailNotValid(email)) {
            errors.rejectValue("email", "email.invalid");
        }

        if (isLengthNotValid(password, PASSWORD_MIN_LENGTH, PASSWORD_MAX_LENGTH)) {
            errors.rejectValue("password", "password.invalid");
        }

        if (isLengthNotValid(login, USERNAME_MIN_LENGTH, USERNAME_MAX_LENGTH) || isOnlyDigits(login)) {
            errors.rejectValue("login", "login.invalid");
        }

        if (userService.findByEmail(email) != null) {
            errors.rejectValue("email", "duplicate.email");
        }

        if (accountService.findByName(login) != null) {
            errors.rejectValue("login", "duplicate.login");
        }
    }

    private boolean isEmailNotValid(String email) {
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return !matcher.matches();
    }

    private boolean isOnlyDigits(String field) {
        return field.replaceAll("[^a-zA-Z]", "").length() == 0;
    }

    private boolean isLengthNotValid(String field, int min, int max) {
        return field.length() < min || field.length() > max;
    }

}
