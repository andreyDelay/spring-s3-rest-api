package com.andrey.springs3restapi.dto.account;

import com.andrey.springs3restapi.model.Account;
import com.andrey.springs3restapi.model.Status;
import lombok.Data;

@Data
public class AccountDto {

    private Long id;
    private String login;
    private Status status;
    private String owner;

    public AccountDto(Account account) {
        this.id = account.getId();
        this.login = account.getName();
        this.status = account.getStatus();
        this.owner = account.getUser().getUsername();
    }
}
