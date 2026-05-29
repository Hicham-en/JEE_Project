package com.annotation.infrastructure.parser;

import com.annotation.application.dto.TextPairDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * Parser for JSON dataset files.
 * Expects a JSON array of objects with text1 and optional text2 fields.
 */
@Component
public class JsonParser implements IDatasetParser {

    private final ObjectMapper objectMapper;

    public JsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<TextPairDTO> parse(InputStream inputStream) throws Exception {
        return objectMapper.readValue(inputStream, new TypeReference<List<TextPairDTO>>() {});
    }
}
