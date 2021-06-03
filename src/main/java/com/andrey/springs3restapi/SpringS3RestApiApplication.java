package com.andrey.springs3restapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SpringS3RestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringS3RestApiApplication.class, args);
    }

}
