package com.andrey.springs3restapi.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.andrey.springs3restapi.model.MyFile;
import com.andrey.springs3restapi.model.Status;
import com.andrey.springs3restapi.model.User;
import com.andrey.springs3restapi.repository.FileRepository;
import com.andrey.springs3restapi.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    private final AmazonS3 s3Client;
    private final FileRepository fileRepository;

    @Autowired
    public FileServiceImpl(AmazonS3 s3Client,
                           FileRepository fileRepository) {
        this.s3Client = s3Client;
        this.fileRepository = fileRepository;
    }

    @Override
    public void uploadFileToS3(MultipartFile multipartFile, User user) {
        String bucketName = getBucketName(user).toLowerCase(Locale.ROOT);
        if (!s3Client.doesBucketExistV2(bucketName)) {
            s3Client.createBucket(new CreateBucketRequest(bucketName));
        }
        String contentType = multipartFile.getContentType();
        MyFile myFile = writeDataToDataBase(user, multipartFile, bucketName);
        if (myFile == null) {
            return;
        }
            upload(bucketName, multipartFile, contentType);
    }

    private MyFile writeDataToDataBase(User user, MultipartFile multipartFile, String bucketName) {
        String originalFileName = multipartFile.getOriginalFilename();
        String filename = originalFileName.substring(0, originalFileName.lastIndexOf("."));
        MyFile myFile;
        if (!isExistInDataBase(filename)) {
            myFile = createAndSetUpMyFile(user, multipartFile, bucketName);
        } else {
            myFile = updateMyFile(user, multipartFile, bucketName, filename);
        }
        fileRepository.save(myFile);
        return myFile;
    }

    private boolean isExistInDataBase(String filename) {
        return fileRepository.findByName(filename) != null;
    }

    private MyFile createAndSetUpMyFile(User user, MultipartFile multipartFile, String bucketName) {
        MyFile myFile = null;
        try {
            String originalFileName = multipartFile.getOriginalFilename();

            myFile = new MyFile();
            myFile.setStatus(Status.ACTIVE);
            myFile.setExtension(originalFileName.substring(originalFileName.lastIndexOf(".") + 1));
            myFile.setName(originalFileName.substring(0,originalFileName.lastIndexOf(".")));
            myFile.setBucketName(bucketName);
            myFile.setSize(multipartFile.getSize());
            myFile.setUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myFile;
    }

    private MyFile updateMyFile(User user, MultipartFile multipartFile, String bucketName, String filename) {
        MyFile oldValue = fileRepository.findByName(filename);
        MyFile newValue = createAndSetUpMyFile(user, multipartFile, bucketName);
        newValue.setId(oldValue.getId());
        return newValue;
    }

    private void upload(String bucketName, MultipartFile multipartFile, String contentType) {
        try {
            if (!s3Client.doesBucketExistV2(bucketName)) {
                s3Client.createBucket(new CreateBucketRequest(bucketName));
            }
            InputStream is = multipartFile.getInputStream();
            String key = multipartFile.getOriginalFilename();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, is, metadata);
            s3Client.putObject(putObjectRequest);
            //TODO exceptions types are implemented according to an official docs
            //https://docs.aws.amazon.com/AmazonS3/latest/userguide/upload-objects.html
        } catch (AmazonServiceException e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ResponseEntity downloadFileFromS3(String filename, User user) {
        String bucketName = getBucketName(user);
        if (!(s3Client.doesBucketExistV2(bucketName))) {
            return new ResponseEntity("Sorry, this user have no any uploaded files.", HttpStatus.NOT_FOUND);
        }
        try {
        S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, filename));
        S3ObjectInputStream is = s3Object.getObjectContent();
        byte[] bytes;

            bytes = IOUtils.toByteArray(is);
            ObjectMetadata objectMetadata = s3Object.getObjectMetadata();
            String contentType = objectMetadata.getContentType();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(bytes);

        } catch (AmazonServiceException e) {
            return new ResponseEntity(e.getErrorMessage(), HttpStatus.EXPECTATION_FAILED);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity("Couldn't download a file.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Override
    public void deleteFileFromS3Bucket(String filename, User user) {
        MyFile file = fileRepository.findByName(filename);
        String bucket = getBucketName(user);
        if (file != null && s3Client.doesBucketExistV2(bucket)) {
            s3Client.deleteObject(bucket, filename);
            file.setStatus(Status.DELETED);
            fileRepository.save(file);
        }
    }

    @Override
    public List<MyFile> getFilesFromS3Bucket(User user) {
        String bucketName = getBucketName(user);
        if (!s3Client.doesBucketExistV2(bucketName)) {
            return Collections.emptyList();
        }

        return
                s3Client.listObjectsV2(new ListObjectsV2Request()
                        .withBucketName(bucketName))
                        .getObjectSummaries()
                        .stream()
                        .map(s3Object -> convertS3ObjectSummaryToFile(s3Object, user))
                        .collect(Collectors.toList());
    }

    private MyFile convertS3ObjectSummaryToFile(S3ObjectSummary s3ObjectSummary, User user) {
        MyFile myFile = new MyFile();
        myFile.setName(s3ObjectSummary.getKey());
        myFile.setExtension(s3ObjectSummary.getETag());
        myFile.setBucketName(s3ObjectSummary.getBucketName());
        myFile.setSize(s3ObjectSummary.getSize());
        myFile.setUser(user);
        return myFile;
    }

    @Override
    public List<MyFile> getFilesFromDb(User user, Status status) {
        return fileRepository.findAllByUser(user)
                .stream()
                .filter(file -> file.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    @Override
    public MyFile getById(Long id) {
        return fileRepository.findById(id).orElse(null);
    }

    private String getBucketName(User user) {
        return (user.getUsername() + "-" + user.getAccount().getName()).toLowerCase();
    }

}
