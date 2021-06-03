package com.andrey.springs3restapi.service.impl;

import com.andrey.springs3restapi.model.Role;
import com.andrey.springs3restapi.model.Status;
import com.andrey.springs3restapi.model.User;
import com.andrey.springs3restapi.repository.RoleRepository;
import com.andrey.springs3restapi.repository.UserRepository;
import com.andrey.springs3restapi.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           BCryptPasswordEncoder  passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User register(User user) {
        Role roleUser = roleRepository.findByName("ROLE_USER");
        List<Role> userRoles = new ArrayList<>();
        userRoles.add(roleUser);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(userRoles);
        user.setStatus(Status.ACTIVE);

        User registeredUser = userRepository.save(user);
        return registeredUser;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User update(User user) {
        User updatableUser = userRepository.findById(user.getId()).orElse(null);
        if (updatableUser == null) {
            return null;
        }
        updatableUser.setFirstName(user.getFirstName());
        updatableUser.setLastName(user.getLastName());
        updatableUser.setPassword(passwordEncoder.encode(user.getPassword()));
        updatableUser.setStatus(user.getStatus());
        updatableUser.setAccount(user.getAccount());
        return userRepository.save(updatableUser);
    }

    @Override
    public User updateStatus(User user) {
        User updatableUser = userRepository.findById(user.getId()).orElse(null);
        if (updatableUser == null) {
            return null;
        }
        updatableUser.setStatus(user.getStatus());
        updatableUser.setAccount(user.getAccount());
        return userRepository.save(updatableUser);
    }

    @Override
    public User findByUserName(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            log.warn("IN UserService findById() no user found with id: {}", id);
        }
        return user;
    }

}
