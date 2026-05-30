package com.annotation.application.service;

import com.annotation.domain.entity.Annotation;
import com.annotation.domain.entity.Dataset;
import com.annotation.domain.entity.Task;
import com.annotation.domain.repository.AnnotationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ExportIntegrationTest {

    @Autowired
    private AnnotationRepository annotationRepository;

    @Test
    @Transactional
    public void testQuery() {
        // Just testing context load
        System.out.println("Annotations found for dataset 1: " + annotationRepository.findByTaskDatasetId(1L).size());
    }
}
