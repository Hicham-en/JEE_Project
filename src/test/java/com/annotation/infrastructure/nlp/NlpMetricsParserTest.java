package com.annotation.infrastructure.nlp;

import com.annotation.application.dto.MetricsResultDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NlpMetricsParserTest {

    private final NlpMetricsParser parser = new NlpMetricsParser();

    @Test
    void parseExtractsSimpleMetrics() {
        MetricsResultDTO result = parser.parse("accuracy: 0.91\nf1=0.82\n");

        assertEquals(0.91, result.accuracy());
        assertEquals(0.82, result.f1Score());
    }

    @Test
    void parseExtractsMultilineConfusionMatrix() {
        MetricsResultDTO result = parser.parse("confusion matrix:\n[10, 2]\n[3, 15]\naccuracy: 0.91\n");

        assertNotNull(result.confusionMatrix());
        assertEquals("[10, 2]\n[3, 15]", result.confusionMatrix());
    }
}