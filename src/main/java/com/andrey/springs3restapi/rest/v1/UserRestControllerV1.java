package com.andrey.springs3restapi.rest.v1;

import com.andrey.springs3restapi.aop.Logging;
import com.andrey.springs3restapi.dto.UpdateUserDto;
import com.andrey.springs3restapi.dto.operationresult.OperationResultOk;
import com.andrey.springs3restapi.dto.RepresentationBuilder;
import com.andrey.springs3restapi.model.*;
import com.andrey.springs3restapi.security.jwt.JwtUser;
import com.andrey.springs3restapi.service.EventService;
import com.andrey.springs3restapi.service.FileService;
import com.andrey.springs3restapi.service.UserService;
import com.andrey.springs3restapi.util.ControllerUtils;
import com.andrey.springs3restapi.util.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/")
public class UserRestControllerV1 {

    private final UserService userService;
    private final FileService fileService;
    private final Validator validator;
    private final ErrorResponse errorResponse;
    private final EventService eventService;

    @Autowired
    public UserRestControllerV1(UserService userService,
                                FileService fileService,
                                @Qualifier("updateUserValidator") Validator validator,
                                ErrorResponse errorResponse,
                                EventService eventService) {
        this.userService = userService;
        this.fileService = fileService;
        this.validator = validator;
        this.errorResponse = errorResponse;
        this.eventService = eventService;
    }

    @GetMapping("me")
/*    @Logging(action = "testing getting data for logged in user")*/
    public ResponseEntity getLoggedInUser(@AuthenticationPrincipal JwtUser userDetails) {
        User user = userService.findById(userDetails.getId());
        if (user == null) {
            return new ResponseEntity("User - " + userDetails.getUsername() + " not found.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
        Role role_admin = user.getRoles()
                .stream()
                .filter(r -> r.getName().equals("ROLE_ADMIN"))
                .findAny().orElse(null);

        if (role_admin == null) {
            return new ResponseEntity(RepresentationBuilder.createResponseForUser(user), HttpStatus.OK);
        }
        return new ResponseEntity(RepresentationBuilder.createResponseForAdmin(user), HttpStatus.OK);
    }

    @DeleteMapping("me")
    public ResponseEntity deleteLoggedInUser(@AuthenticationPrincipal JwtUser userDetails) {
        if (ControllerUtils.isAdmin(userDetails)) {
            return new ResponseEntity("Admin cannot be deleted", HttpStatus.SERVICE_UNAVAILABLE);
        }
        User user = userService.findById(userDetails.getId());
        user.setStatus(Status.DELETED);
        user.getAccount().setStatus(Status.DELETED);
        userService.update(user);
        return new ResponseEntity(new OperationResultOk(), HttpStatus.OK);
    }

    @PutMapping("me")
    @Logging(action = "User updates his personal data")
    public ResponseEntity updateLoggedInUser(@RequestBody UpdateUserDto userDto,
                                             BindingResult bindingResult,
                                             @AuthenticationPrincipal JwtUser userDetails) {
        validator.validate(userDto, bindingResult);
        if (bindingResult.hasErrors()) {
            return new ResponseEntity(errorResponse.updateResponse(bindingResult), HttpStatus.BAD_REQUEST);
        }
        User user = userService.findById(userDetails.getId());
        ControllerUtils.updateUser(user, userDto);
        userService.update(user);
        return new ResponseEntity(new OperationResultOk(), HttpStatus.OK);
    }

    @GetMapping("me/events")
    public ResponseEntity getEvents(@AuthenticationPrincipal JwtUser userDetails) {
        User user = userService.findById(userDetails.getId());
        List<Event> events = user.getEvents();
        return new ResponseEntity(events, HttpStatus.OK);
    }

    @GetMapping("me/events/{id}")
    public ResponseEntity getEventById(@PathVariable("id") Long eventId,
                                       @AuthenticationPrincipal JwtUser userDetails) {
        User user = userService.findById(userDetails.getId());
        Event event = eventService.findById(eventId, user);
        if (event == null) {
            return new ResponseEntity("No any event with id - " + eventId + " was found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(event, HttpStatus.OK);
    }

    @GetMapping("me/files")
    public ResponseEntity getFiles(@AuthenticationPrincipal JwtUser userDetails) {
        User user = userService.findById(userDetails.getId());
        List<MyFile> files = fileService.getFilesFromS3Bucket(user);
        return new ResponseEntity(files, HttpStatus.OK);
    }

    @GetMapping("me/files/status/{status}")
    public ResponseEntity getFilesByStatus(@PathVariable("status") String status,
                                           @AuthenticationPrincipal JwtUser userDetails) {
        if (!ControllerUtils.isStatusValid(status)) {
            return new ResponseEntity("Status is not correct.", HttpStatus.BAD_REQUEST);
        }
        User user = userService.findById(userDetails.getId());
        List<MyFile> files = fileService.getFilesFromDb(user, Status.valueOf(status.toUpperCase()));
        return new ResponseEntity(files, HttpStatus.OK);
    }

    @GetMapping("me/files/{id}")
    public ResponseEntity getFileById(@PathVariable("id") Long id) {
        MyFile file = fileService.getById(id);
        if (file == null) {
            return new ResponseEntity("No any file with id - " + id + " was found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(file, HttpStatus.OK);
    }

    @GetMapping("me/files/download/{filename}")
    @Logging(action = "User downloads file")
    public ResponseEntity downloadFile(@PathVariable("filename") String filename,
                                       @AuthenticationPrincipal JwtUser userDetails) {
        User user = userService.findById(userDetails.getId());
        if (user == null) {
            return new ResponseEntity("Something went wrong.", HttpStatus.SERVICE_UNAVAILABLE);
        }
        return fileService.downloadFileFromS3(filename, user);
    }

}
