package com.annotation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main entry point for the NLP Annotation Platform application.
 * Bootstraps the embedded web server and Spring application context.
 */
@SpringBootApplication
@EnableAsync
public class AnnotationPlatformApplication {

    /**
     * Main method.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AnnotationPlatformApplication.class, args);
    }
}
