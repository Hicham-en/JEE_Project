package com.annotation.infrastructure.parser;

import com.annotation.application.dto.TextPairDTO;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for CSV dataset files.
 * Expects format: optionally id, text1, text2 (nullable).
 */
@Component
public class CsvParser implements IDatasetParser {

    @Override
    public List<TextPairDTO> parse(InputStream inputStream) throws Exception {
        List<TextPairDTO> results = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String[] line;
            boolean firstLine = true;
            while ((line = csvReader.readNext()) != null) {
                if (firstLine) {
                    firstLine = false;
                    if (isHeaderRow(line)) {
                        continue;
                    }
                }
                
                if (line.length >= 1 && !line[0].trim().isEmpty()) {
                    String text1 = line[0].trim();
                    String text2 = line.length >= 2 ? line[1].trim() : null;
                    if (text2 != null && text2.isEmpty()) {
                        text2 = null;
                    }
                    
                    results.add(new TextPairDTO(null, text1, text2, null));
                }
            }
        }
        return results;
    }

    private boolean isHeaderRow(String[] line) {
        if (line.length == 0) {
            return false;
        }

        String firstColumn = line[0].trim().toLowerCase();
        return "id".equals(firstColumn)
                || "text".equals(firstColumn)
                || "text1".equals(firstColumn)
                || "texte".equals(firstColumn)
                || "texte1".equals(firstColumn)
                || "sentence".equals(firstColumn);
    }
}
