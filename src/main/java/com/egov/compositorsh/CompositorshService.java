package com.egov.compositorsh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class CompositorshService
{

    public static void main(String[] args)
    {
        SpringApplication.run(CompositorshService.class, args);
    }

}
