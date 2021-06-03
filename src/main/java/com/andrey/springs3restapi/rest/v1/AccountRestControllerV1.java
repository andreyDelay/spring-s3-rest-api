package com.andrey.springs3restapi.rest.v1;

import com.andrey.springs3restapi.aop.Logging;
import com.andrey.springs3restapi.dto.account.AccountDto;
import com.andrey.springs3restapi.model.Status;
import com.andrey.springs3restapi.model.User;
import com.andrey.springs3restapi.security.jwt.JwtUser;
import com.andrey.springs3restapi.service.UserService;
import com.andrey.springs3restapi.util.ControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts/")
public class AccountRestControllerV1 {

    private final UserService userService;

    @Autowired
    public AccountRestControllerV1(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("me")
    public ResponseEntity getLoggedInAccount(@AuthenticationPrincipal JwtUser userDetails) {
        User user = userService.findByUserName(userDetails.getUsername());
        if (user == null) {
            return new ResponseEntity("Couldn't define user current user", HttpStatus.SERVICE_UNAVAILABLE);
        }
        AccountDto accountDto = new AccountDto(user.getAccount());
        return new ResponseEntity(accountDto, HttpStatus.OK);
    }

    @DeleteMapping("me")
    @Logging(action = "Deleting account by invocation of method - deleteLoggedInAccount, in class AccountRestControllerV1.")
    public ResponseEntity deleteLoggedInAccount(@AuthenticationPrincipal JwtUser userDetails) {
        if (ControllerUtils.isAdmin(userDetails)) {
            return new ResponseEntity("Admin cannot be deleted", HttpStatus.SERVICE_UNAVAILABLE);
        }

        User user = userService.findByUserName(userDetails.getUsername());
        if (user == null) {
            return new ResponseEntity("Couldn't define user current user", HttpStatus.SERVICE_UNAVAILABLE);
        }

        user.setStatus(Status.DELETED);
        user.getAccount().setStatus(Status.DELETED);
        userService.updateStatus(user);
        AccountDto accountDto = new AccountDto(user.getAccount());
        return new ResponseEntity(accountDto, HttpStatus.OK);
    }
}
