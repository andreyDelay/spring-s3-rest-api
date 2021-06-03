package com.andrey.springs3restapi.repository;

import com.andrey.springs3restapi.model.MyFile;
import com.andrey.springs3restapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<MyFile, Long> {

    List<MyFile> findAllByUser(User user);

    MyFile findByName(String filename);


}
