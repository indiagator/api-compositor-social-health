package com.egov.socialservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class SocialServiceApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(SocialServiceApplication.class, args);
    }

}
