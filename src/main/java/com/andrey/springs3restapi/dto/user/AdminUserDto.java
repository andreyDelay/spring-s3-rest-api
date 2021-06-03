package com.andrey.springs3restapi.dto.user;

import com.andrey.springs3restapi.model.Role;
import com.andrey.springs3restapi.model.User;
import lombok.Data;

import java.util.List;

@Data
public class AdminUserDto {

    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private List<Role> roles;

    public AdminUserDto(User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.password = user.getPassword();
        this.roles = user.getRoles();
    }
}
