package com.annotation.application.service;

import com.annotation.domain.entity.Annotation;
import com.annotation.domain.repository.AnnotationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that exports dataset annotations to CSV and JSON.
 */
@Service
public class ExportService {

    private final AnnotationRepository annotationRepository;
    private final ObjectMapper objectMapper;

    public ExportService(AnnotationRepository annotationRepository, ObjectMapper objectMapper) {
        this.annotationRepository = annotationRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Exports annotations for a dataset as CSV.
     *
     * @param datasetId dataset ID
     * @return UTF-8 CSV bytes
     */
    @Transactional(readOnly = true)
    public byte[] exportDatasetCsv(Long datasetId) {
        StringBuilder csv = new StringBuilder("id,text1,text2,chosenClass,annotatorLogin,annotationTime\n");
        for (Annotation annotation : annotationRepository.findByTaskDatasetId(datasetId)) {
            csv.append(annotation.getTextPair().getId()).append(',')
                    .append(escapeCsv(annotation.getTextPair().getText1())).append(',')
                    .append(escapeCsv(annotation.getTextPair().getText2())).append(',')
                    .append(escapeCsv(annotation.getChosenClass())).append(',')
                    .append(escapeCsv(annotation.getAnnotator().getLogin())).append(',')
                    .append(escapeCsv(String.valueOf(annotation.getAnnotationTime())))
                    .append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Exports annotations for a dataset as JSON.
     *
     * @param datasetId dataset ID
     * @return UTF-8 JSON bytes
     */
    @Transactional(readOnly = true)
    public byte[] exportDatasetJson(Long datasetId) {
        List<Map<String, Object>> rows = annotationRepository.findByTaskDatasetId(datasetId).stream()
                .map(annotation -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", annotation.getTextPair().getId());
                    row.put("text1", annotation.getTextPair().getText1());
                    row.put("text2", annotation.getTextPair().getText2());
                    row.put("chosenClass", annotation.getChosenClass());
                    row.put("annotatorLogin", annotation.getAnnotator().getLogin());
                    row.put("annotationTime", annotation.getAnnotationTime());
                    return row;
                })
                .toList();
        try {
            return objectMapper.writeValueAsBytes(rows);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to export dataset JSON", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
