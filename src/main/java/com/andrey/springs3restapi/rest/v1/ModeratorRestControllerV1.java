package com.andrey.springs3restapi.rest.v1;

import com.andrey.springs3restapi.aop.Logging;
import com.andrey.springs3restapi.dto.operationresult.OperationResultOk;
import com.andrey.springs3restapi.model.User;
import com.andrey.springs3restapi.security.jwt.JwtUser;
import com.andrey.springs3restapi.service.FileService;
import com.andrey.springs3restapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = {"/api/v1/moderator/"})
public class ModeratorRestControllerV1 {

    private final UserService userService;
    private final FileService fileService;

    @Autowired
    public ModeratorRestControllerV1(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService = fileService;
    }

    @GetMapping("users/{id}/files/download/{filename}")
    @Logging(action = "downloading file")
    public ResponseEntity downloadFile(@PathVariable("id") Long userId, @PathVariable("filename") String filename) {
        User user = userService.findById(userId);
        if (user == null) {
            return new ResponseEntity("Looks like user with such id doesn't exist.", HttpStatus.NOT_FOUND);
        }
        return fileService.downloadFileFromS3(filename, user);
    }

    @PostMapping(value ="users/{id}/files/upload")
    @Logging(action = "Uploading file for specified User by invocation of method - uploadFile, in class ModeratorRestControllerV1.")
    public ResponseEntity uploadFile(@PathVariable("id") Long userId, @RequestParam("file") MultipartFile file) {
        User user = userService.findById(userId);
        if (file.isEmpty()) {
            return new ResponseEntity("File is empty.",HttpStatus.NOT_ACCEPTABLE);
        }

        if (user == null) {
            return new ResponseEntity("User with such id doesn't exist.", HttpStatus.NOT_FOUND);
        }

        fileService.uploadFileToS3(file, user);
        return new ResponseEntity(new OperationResultOk(), HttpStatus.OK);
    }

    @PutMapping(value = "users/{id}/files/update")
    @Logging(action = "Uploading file for specified User by invocation of method - uploadFile, in class ModeratorRestControllerV1.")
    public ResponseEntity updateFile(@PathVariable("id") Long userId, @RequestParam("file") MultipartFile file) {
        User user = userService.findById(userId);
        if (file.isEmpty()) {
            return new ResponseEntity("File is empty.",HttpStatus.NOT_ACCEPTABLE);
        }

        if (user == null) {
            return new ResponseEntity("User with such id doesn't exist.", HttpStatus.NOT_FOUND);
        }

        fileService.uploadFileToS3(file, user);
        return new ResponseEntity(new OperationResultOk(), HttpStatus.OK);
    }

    @DeleteMapping("users/{id}/files/{filename}")
    @Logging(action = "Deleting file for specified User by invocation of method - deleteFile, in class ModeratorRestControllerV1.")
    public ResponseEntity deleteFile(@PathVariable("id") Long userId, @PathVariable("filename") String filename) {
        User user = userService.findById(userId);
        if (user == null) {
            return new ResponseEntity("User with such id doesn't exist.", HttpStatus.NOT_FOUND);
        }
        fileService.deleteFileFromS3Bucket(filename, user);
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping("users/me/files/{filename}")
    @Logging(action = "Deleting file in method - deleteFileForLoggedInUser, " +
            "class ModeratorRestControllerV1.")
    public ResponseEntity deleteFileForLoggedInUser(@AuthenticationPrincipal JwtUser userDetails,
                                                    @PathVariable("filename") String filename) {
        User user = userService.findById(userDetails.getId());
        if (user == null) {
            return new ResponseEntity("Something went wrong.", HttpStatus.SERVICE_UNAVAILABLE);
        }
        fileService.deleteFileFromS3Bucket(filename, user);
        return new ResponseEntity(HttpStatus.OK);
    }

}
