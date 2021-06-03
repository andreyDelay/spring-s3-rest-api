package com.andrey.springs3restapi.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.persistence.Transient;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Data
public class ErrorResponse {

    private Map<String, String> errorsMap;

    @Transient
    @JsonIgnore
    private final MessageSourceAccessor messageSourceAccessor;

    @Autowired
    public ErrorResponse(MessageSourceAccessor messageSourceAccessor) {
        this.messageSourceAccessor = messageSourceAccessor;
    }

    public ErrorResponse registrationResponse(BindingResult errors) {
        errorsMap = getErrors(errors);
        return this;
    }

    public ErrorResponse updateResponse(BindingResult errors) {
        errorsMap = getErrors(errors);
        return this;
    }

    private Map<String, String> getErrors(BindingResult errors) {
        return errors.getFieldErrors()
                    .stream()
                    .filter(e -> e.getCode().length() >=1)
                    .collect(Collectors.toMap(FieldError::getField,
                    e -> messageSourceAccessor.getMessage(e.getCode())));
    }

}
