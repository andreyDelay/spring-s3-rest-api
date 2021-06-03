package com.andrey.springs3restapi.dto.auth;

import com.andrey.springs3restapi.model.Account;
import com.andrey.springs3restapi.model.Status;
import com.andrey.springs3restapi.model.User;
import lombok.Data;

@Data
public class UserRegistrationDto {

    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String login;

    public User dtoToUser() {
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(password);

        Account account = new Account();
        account.setName(login);
        account.setStatus(Status.ACTIVE);
        account.setUser(user);

        user.setAccount(account);
        return user;
    }

}
