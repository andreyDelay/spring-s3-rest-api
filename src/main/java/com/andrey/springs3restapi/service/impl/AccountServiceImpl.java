package com.andrey.springs3restapi.service.impl;

import com.andrey.springs3restapi.model.Account;
import com.andrey.springs3restapi.repository.AccountRepository;
import com.andrey.springs3restapi.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public List<Account> getAll() {
        return accountRepository.findAll();
    }

    @Override
    public Account findByName(String name) {
        return accountRepository.findByName(name);
    }

    @Override
    public Account findById(Long id) {
        return accountRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        Account account = accountRepository.findById(id).orElse(null);
        if (account == null) {
            //TODO not the best solution to throw an exception
            throw new RuntimeException("Id - " + id + " not found.");
        }
        accountRepository.delete(account);
    }
}
