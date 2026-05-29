package com.annotation.infrastructure.nlp;

import com.annotation.application.dto.MetricsResultDTO;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses metrics emitted by Python NLP scripts.
 */
@Component
public class NlpMetricsParser {

    private static final Pattern ACCURACY_PATTERN = Pattern.compile("(?i)accuracy\\s*[:=]\\s*([0-9]+(?:\\.[0-9]+)?)");
    private static final Pattern F1_PATTERN = Pattern.compile("(?i)(?:f1|f1_score|f1-score)\\s*[:=]\\s*([0-9]+(?:\\.[0-9]+)?)");
    private static final Pattern CONFUSION_PATTERN = Pattern.compile("(?ims)confusion(?:_matrix| matrix)?\\s*[:=]\\s*(.+?)(?=\\n\\s*(?:accuracy|f1|f1_score|f1-score)\\s*[:=]|\\z)");

    /**
     * Parses accuracy, F1 and confusion matrix values from raw logs.
     *
     * @param logs process output
     * @return parsed metrics
     */
    public MetricsResultDTO parse(String logs) {
        if (logs == null || logs.isBlank()) {
            return new MetricsResultDTO(null, null, null);
        }
        return new MetricsResultDTO(
                parseDouble(ACCURACY_PATTERN, logs),
                parseDouble(F1_PATTERN, logs),
                parseString(CONFUSION_PATTERN, logs)
        );
    }

    private Double parseDouble(Pattern pattern, String logs) {
        Matcher matcher = pattern.matcher(logs);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : null;
    }

    private String parseString(Pattern pattern, String logs) {
        Matcher matcher = pattern.matcher(logs);
        return matcher.find() ? matcher.group(1).trim() : null;
    }
}
