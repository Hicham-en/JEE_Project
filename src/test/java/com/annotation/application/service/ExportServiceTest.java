package com.annotation.application.service;

import com.annotation.domain.entity.Annotation;
import com.annotation.domain.entity.Annotator;
import com.annotation.domain.entity.TextPair;
import com.annotation.domain.repository.AnnotationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExportServiceTest {

    private final AnnotationRepository annotationRepository = mock(AnnotationRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final ExportService exportService = new ExportService(annotationRepository, objectMapper);

    @Test
    void testCsvExportEscaping() {
        when(annotationRepository.findByTaskDatasetId(1L)).thenReturn(List.of(annotation("Bonjour, \"monde\"", "Classe,A")));

        String csv = new String(exportService.exportDatasetCsv(1L), StandardCharsets.UTF_8);

        assertTrue(csv.contains("\"Bonjour, \"\"monde\"\"\""));
        assertTrue(csv.contains("\"Classe,A\""));
    }

    @Test
    void testJsonExportStructure() throws Exception {
        when(annotationRepository.findByTaskDatasetId(1L)).thenReturn(List.of(annotation("Texte", "Positive")));

        JsonNode root = objectMapper.readTree(exportService.exportDatasetJson(1L));

        assertTrue(root.isArray());
        assertEquals("Texte", root.get(0).get("text1").asText());
        assertEquals("Positive", root.get(0).get("chosenClass").asText());
    }

    private Annotation annotation(String text, String chosenClass) {
        TextPair textPair = new TextPair();
        textPair.setId(10L);
        textPair.setText1(text);
        textPair.setText2(null);
        Annotator annotator = new Annotator();
        annotator.setLogin("ann");
        Annotation annotation = new Annotation();
        annotation.setTextPair(textPair);
        annotation.setAnnotator(annotator);
        annotation.setChosenClass(chosenClass);
        annotation.setAnnotationTime(LocalDateTime.parse("2026-05-28T10:15:30"));
        return annotation;
    }
}
