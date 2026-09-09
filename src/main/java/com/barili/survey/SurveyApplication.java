package com.barili.survey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class SurveyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SurveyApplication.class, args);
    }
}
