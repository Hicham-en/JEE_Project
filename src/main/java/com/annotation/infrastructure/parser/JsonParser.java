package com.annotation.infrastructure.parser;

import com.annotation.application.dto.TextPairDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parser for JSON dataset files.
 * Expects a JSON array of objects with text1 and optional text2 fields.
 * Supports both English ("text1", "text2") and French ("texte", "texte1", "texte2") field names.
 */
@Component
public class JsonParser implements IDatasetParser {

    private static final String[] TEXT1_KEYS = {"text1", "texte", "texte1", "text"};
    private static final String[] TEXT2_KEYS = {"text2", "texte2"};

    private final ObjectMapper objectMapper;

    public JsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<TextPairDTO> parse(InputStream inputStream) throws Exception {
        List<Map<String, Object>> rawList = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
        List<TextPairDTO> results = new ArrayList<>();

        for (Map<String, Object> item : rawList) {
            Long id = null;
            if (item.containsKey("id")) {
                Object idVal = item.get("id");
                if (idVal instanceof Number num) {
                    id = num.longValue();
                }
            }

            String text1 = extractFirstValue(item, TEXT1_KEYS);
            String text2 = extractFirstValue(item, TEXT2_KEYS);
            String metadata = item.containsKey("metadata") ? String.valueOf(item.get("metadata")) : null;

            if (text1 != null && !text1.isBlank()) {
                results.add(new TextPairDTO(id, text1, text2, metadata));
            }
        }

        return results;
    }

    private String extractFirstValue(Map<String, Object> map, String[] keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return null;
    }
}
