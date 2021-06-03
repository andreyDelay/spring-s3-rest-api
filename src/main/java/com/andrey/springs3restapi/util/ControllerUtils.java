package com.andrey.springs3restapi.util;

import com.andrey.springs3restapi.dto.UpdateUserDto;
import com.andrey.springs3restapi.model.Status;
import com.andrey.springs3restapi.model.User;
import org.springframework.security.core.userdetails.UserDetails;

public class ControllerUtils {

    public static boolean isStatusValid(String status) {
        return status.equalsIgnoreCase("ACTIVE") || status.equalsIgnoreCase("DELETED");
    }

    public static User updateUser(User user, UpdateUserDto userDto) {
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setStatus(Status.valueOf(userDto.getStatus()));
        user.getAccount().setStatus(Status.valueOf(userDto.getStatus()));
        return user;
    }

    public static boolean isAdmin(UserDetails userDetails) {
        System.out.println(userDetails.getAuthorities());
        return userDetails.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER"));
    }
}
