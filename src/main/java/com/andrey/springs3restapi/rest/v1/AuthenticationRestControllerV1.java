package com.andrey.springs3restapi.rest.v1;

import com.amazonaws.services.directory.model.AuthenticationFailedException;
import com.andrey.springs3restapi.aop.NoLogging;
import com.andrey.springs3restapi.dto.auth.AuthenticationRequestDto;
import com.andrey.springs3restapi.dto.auth.UserRegistrationDto;
import com.andrey.springs3restapi.model.Status;
import com.andrey.springs3restapi.model.User;
import com.andrey.springs3restapi.security.jwt.JwtTokenProvider;
import com.andrey.springs3restapi.service.UserService;
import com.andrey.springs3restapi.util.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/auth/")
public class AuthenticationRestControllerV1 {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final Validator validator;
    private final ErrorResponse errorResponse;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationRestControllerV1(AuthenticationManager authenticationManager,
                                          JwtTokenProvider jwtTokenProvider,
                                          UserService userService,
                                          @Qualifier("registrationValidator") Validator validator,
                                          ErrorResponse errorResponse,
                                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.validator = validator;
        this.errorResponse = errorResponse;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("singin")
    @NoLogging
    public ResponseEntity login(@RequestBody AuthenticationRequestDto requestDto) {
        try {
            String username = requestDto.getUsername();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, requestDto.getPassword()));
            User user = userService.findByUserName(username);
            if (user == null) {
                return new ResponseEntity("User with username : " + username + " not found", HttpStatus.NOT_FOUND);
            }

            return ResponseEntity.ok(jwtTokenProvider.getResponseWithToken(user));
        } catch (AuthenticationException e) {
            return new ResponseEntity("Invalid username or password", HttpStatus.NOT_ACCEPTABLE);
            /*throw new BadCredentialsException("Invalid username or password");*/
        }
    }

    @PostMapping(value = "singup")
    @NoLogging
    public ResponseEntity registerUser(@RequestBody UserRegistrationDto userRegistrationDto, BindingResult bindingResult) {
        validator.validate(userRegistrationDto, bindingResult);
        if (bindingResult.hasErrors()) {
            return new ResponseEntity(errorResponse.registrationResponse(bindingResult), HttpStatus.BAD_REQUEST);
        }
        User user = userRegistrationDto.dtoToUser();
        userService.register(user);
        return new ResponseEntity(jwtTokenProvider.getResponseWithToken(user), HttpStatus.OK);
    }

    @PostMapping(value = "restore")
    @NoLogging
    public ResponseEntity restoreAccount(@RequestBody AuthenticationRequestDto requestDto) {
        try {
            String username = requestDto.getUsername();
            User user = userService.findByUserName(username);
            if (user == null) {
                return new ResponseEntity("User with username : " + username + " not found", HttpStatus.NOT_FOUND);
                /*throw new UsernameNotFoundException("User with username : " + username + " not found");*/
            }

/*            if (user.getStatus().equals(Status.ACTIVE)) {
                return new ResponseEntity("No need to restore the account. Account already active.",
                        HttpStatus.METHOD_NOT_ALLOWED);
            }

            String providedPassword = passwordEncoder.encode(requestDto.getPassword());
            if (!user.getPassword().equals(providedPassword)) {
                return new ResponseEntity("Invalid username or password", HttpStatus.NOT_ACCEPTABLE);
            }*/
            user.getAccount().setStatus(Status.ACTIVE);
            user.setStatus(Status.ACTIVE);
            user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
            userService.updateStatus(user);
            return ResponseEntity.ok(jwtTokenProvider.getResponseWithToken(user));
        } catch (AuthenticationException e) {
            return new ResponseEntity("Invalid username or password", HttpStatus.NOT_ACCEPTABLE);
            /*throw new BadCredentialsException("Invalid username or password");*/
        }
    }
}
