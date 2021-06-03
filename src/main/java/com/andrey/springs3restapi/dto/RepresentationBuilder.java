package com.andrey.springs3restapi.dto;

import com.andrey.springs3restapi.dto.account.AccountDto;
import com.andrey.springs3restapi.dto.user.AdminUserDto;
import com.andrey.springs3restapi.dto.user.UserDto;
import com.andrey.springs3restapi.model.Account;
import com.andrey.springs3restapi.model.User;

public final class RepresentationBuilder {

    private RepresentationBuilder() {
    }

    public static UserDto createResponseForUser(User user) {
        return new UserDto(user);
    }

    public static AdminUserDto createResponseForAdmin(User user) {
        return new AdminUserDto(user);
    }

    public static AccountDto createAccountDto(Account account) {
        return new AccountDto(account);
    }

}
