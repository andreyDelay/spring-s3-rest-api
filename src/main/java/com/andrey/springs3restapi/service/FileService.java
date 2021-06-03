package com.andrey.springs3restapi.service;

import com.andrey.springs3restapi.model.MyFile;
import com.andrey.springs3restapi.model.Status;
import com.andrey.springs3restapi.model.User;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    void uploadFileToS3(MultipartFile file, User user);

    ResponseEntity<Resource> downloadFileFromS3(String filename, User user);

    void deleteFileFromS3Bucket(String filename, User user);

    List<MyFile> getFilesFromS3Bucket(User user);

    List<MyFile> getFilesFromDb(User user, Status status);

    MyFile getById(Long id);

}
