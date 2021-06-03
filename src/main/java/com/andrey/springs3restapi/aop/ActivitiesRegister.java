package com.andrey.springs3restapi.aop;

import com.andrey.springs3restapi.model.Event;
import com.andrey.springs3restapi.model.User;
import com.andrey.springs3restapi.security.jwt.JwtUser;
import com.andrey.springs3restapi.service.EventService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

@Aspect
@Component
public class ActivitiesRegister {

    private final EventService eventService;

    @Autowired
    public ActivitiesRegister(EventService eventService) {
        this.eventService = eventService;
    }

    @After("execution(* com.andrey.springs3restapi.rest.v1.*.*(..))" +
            "&& @annotation(com.andrey.springs3restapi.aop.Logging)")
    public void registerMethodActivity(JoinPoint joinPoint) {
        JwtUser principal =(JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Logging annotation = method.getAnnotation(com.andrey.springs3restapi.aop.Logging.class);

        String action = annotation.action();
        String methodName = method.getName();
        Timestamp timestamp = new Timestamp(new Date().getTime());
        if (principal != null) {
                    Event event = new Event();
                    event.setName("Invocation of method - " + methodName);
                    event.setDescription(action);
                    event.setEventDate(timestamp);
                    event.setUser(getUserWithId(principal));
                    eventService.save(event);
        }
    }

    private User getUserWithId(JwtUser jwtUser) {
        User user = new User();
        user.setId(jwtUser.getId());
        return user;
    }
}
