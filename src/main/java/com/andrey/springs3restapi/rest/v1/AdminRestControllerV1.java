package com.andrey.springs3restapi.rest.v1;

import com.andrey.springs3restapi.aop.Logging;
import com.andrey.springs3restapi.dto.RepresentationBuilder;
import com.andrey.springs3restapi.dto.UpdateUserDto;
import com.andrey.springs3restapi.dto.account.AccountDto;
import com.andrey.springs3restapi.dto.auth.UserRegistrationDto;
import com.andrey.springs3restapi.dto.operationresult.OperationResultOk;
import com.andrey.springs3restapi.model.Account;
import com.andrey.springs3restapi.model.Event;
import com.andrey.springs3restapi.model.Status;
import com.andrey.springs3restapi.model.User;
import com.andrey.springs3restapi.service.AccountService;
import com.andrey.springs3restapi.service.EventService;
import com.andrey.springs3restapi.service.UserService;
import com.andrey.springs3restapi.util.ControllerUtils;
import com.andrey.springs3restapi.util.ErrorResponse;
import com.andrey.springs3restapi.validator.UpdateUserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/")
public class AdminRestControllerV1 {

    private final Validator validator;
    private final ErrorResponse errorResponse;
    private final UserService userService;
    private final AccountService accountService;
    private final EventService eventService;

    @Autowired
    public AdminRestControllerV1(@Qualifier("registrationValidator") Validator validator,
                                 ErrorResponse errorResponse,
                                 UserService userService,
                                 AccountService accountService,
                                 EventService eventService) {
        this.validator = validator;
        this.errorResponse = errorResponse;
        this.userService = userService;
        this.accountService = accountService;
        this.eventService = eventService;
    }

    @GetMapping(value = "users")
    public ResponseEntity getAllUsers() {
        List<User> allUsers = userService.getAll();
        return new ResponseEntity(allUsers, HttpStatus.OK);
    }

    @GetMapping(value = "users/{id}")
    public ResponseEntity getUserById(@PathVariable(name = "id") Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return new ResponseEntity("User with id - " + id + " not found.",HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(user, HttpStatus.OK);
    }

    @GetMapping(value = "users/status/{status}")
    public ResponseEntity getUsersByStatus(@PathVariable(name = "status") String status) {
        if (!ControllerUtils.isStatusValid(status)) {
            return new ResponseEntity("Application doesn't supply status - " + status, HttpStatus.BAD_REQUEST);
        }
        Status requiredStatus = Status.valueOf(status.toUpperCase());
        List<User> usersWithConcreteStatus = userService.getAll()
                .stream()
                .filter(user -> user.getStatus().equals(requiredStatus))
                .collect(Collectors.toList());
        return new ResponseEntity(usersWithConcreteStatus, HttpStatus.OK);
    }

    @GetMapping(value = "accounts")
    public ResponseEntity getAllAccounts() {
        List<AccountDto> accounts = accountService.getAll()
                                    .stream()
                                    .map(AccountDto::new)
                                    .collect(Collectors.toList());
        return new ResponseEntity(accounts, HttpStatus.OK);
    }

    @GetMapping(value = "accounts/{login}")
    public ResponseEntity getAccountByLogin(@PathVariable(name = "login") String login) {
        Account account = accountService.findByName(login);
        if (account == null) {
            return new ResponseEntity("Account with login - " + login + " not found.",HttpStatus.NOT_FOUND);
        }
        AccountDto accountDto = new AccountDto(account);
        return new ResponseEntity(accountDto, HttpStatus.OK);
    }

    @GetMapping(value = "accounts/status/{status}")
    public ResponseEntity getAccountsByStatus(@PathVariable(name = "status") String status) {
        if (!ControllerUtils.isStatusValid(status)) {
            return new ResponseEntity("Application doesn't supply status - " + status, HttpStatus.BAD_REQUEST);
        }

        Status requiredStatus = Status.valueOf(status.toUpperCase());
        List<AccountDto> accountsWithConcreteStatus = accountService.getAll()
                .stream()
                .filter(user -> user.getStatus().equals(requiredStatus))
                .map(AccountDto::new)
                .collect(Collectors.toList());
        return new ResponseEntity(accountsWithConcreteStatus, HttpStatus.OK);
    }

    @PostMapping ("users/add")
    @Logging(action = "Admin adds user by invocation of method - addUser, in class AdminRestControllerV1.")
    public ResponseEntity addUser(@RequestBody UserRegistrationDto userRegistrationDto, BindingResult bindingResult) {
        validator.validate(userRegistrationDto, bindingResult);
        if (bindingResult.hasErrors()) {
            return new ResponseEntity(errorResponse.registrationResponse(bindingResult), HttpStatus.BAD_REQUEST);
        }
        User user = userRegistrationDto.dtoToUser();
        userService.register(user);
        return new ResponseEntity(RepresentationBuilder.createResponseForAdmin(user), HttpStatus.OK);
    }

    @PutMapping("users/{id}")
    @Logging(action = "Admin updates user by invocation of method - updateUser, in class AdminRestControllerV1.")
    public ResponseEntity updateUser(@RequestBody UpdateUserDto userDto, BindingResult bindingResult,
                                     @PathVariable("id") Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return new ResponseEntity("Not any user was found with such id", HttpStatus.NOT_FOUND);
        }
        //TODO У меня тут не инициализируются поля из проперти файла в классе UpdateUserValidator
        //так как у меня тут используются 2 бина одновременно, и второй я создаю вручную.
        UpdateUserValidator updateUserValidator = new UpdateUserValidator();
        updateUserValidator.validate(userDto, bindingResult);
        if (bindingResult.hasErrors()) {
            return new ResponseEntity(errorResponse.updateResponse(bindingResult), HttpStatus.BAD_REQUEST);
        }
        ControllerUtils.updateUser(user, userDto);
        userService.update(user);
        return new ResponseEntity(new OperationResultOk(), HttpStatus.OK);
    }

    @DeleteMapping("users/{id}")
    @Logging(action = "Admin deletes user by id, by invocation of method - deleteUser, in class AdminRestControllerV1.")
    public ResponseEntity deleteUser(@PathVariable("id") Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return new ResponseEntity("Not any user was found with such id", HttpStatus.NOT_FOUND);
        }
        user.setStatus(Status.DELETED);
        user.getAccount().setStatus(Status.DELETED);
        userService.updateStatus(user);
        return new ResponseEntity(new OperationResultOk(), HttpStatus.OK);
    }

    @GetMapping(value = "users/{user_id}/events/{id}")
    public ResponseEntity getEventByUserId(@PathVariable("user_id") Long userId, @PathVariable("id") Long eventId) {
        User user = userService.findById(userId);
        if (user == null) {
            return new ResponseEntity("User with id - " + userId + " not found.",HttpStatus.NOT_FOUND);
        }

        Event event = eventService.findById(eventId, user);
        if (event == null) {
            return new ResponseEntity("No any events was found.",HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(event, HttpStatus.OK);
    }

    @GetMapping(value = "users/{id}/events")
    public ResponseEntity getEventsByUserId(@PathVariable("id") Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            return new ResponseEntity("User with id - " + userId + " not found.",HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(user.getEvents(), HttpStatus.OK);
    }

}
