package com.andrey.springs3restapi.service;

import com.andrey.springs3restapi.model.Account;

import java.util.List;

public interface AccountService {

    List<Account> getAll();

    Account findByName(String name);

    Account findById(Long id);

    void delete(Long id);
}
