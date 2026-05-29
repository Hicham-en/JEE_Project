package com.annotation.application.service.impl;

import com.annotation.application.dto.MetricsDTO;
import com.annotation.application.dto.SpammerDTO;
import com.annotation.domain.entity.Annotation;
import com.annotation.domain.entity.Annotator;
import com.annotation.domain.entity.TextPair;
import com.annotation.domain.repository.AnnotationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricsServiceImplTest {

    @Mock private AnnotationRepository annotationRepository;
    @InjectMocks private MetricsServiceImpl metricsService;

    @Test
    void testCohenKappaPerfectAgreement() {
        List<Annotation> annotations = new ArrayList<>();
        for (long i = 1; i <= 10; i++) {
            TextPair pair = textPair(i);
            annotations.add(annotation(pair, annotator(1L), "A", 5L));
            annotations.add(annotation(pair, annotator(2L), "A", 5L));
        }
        when(annotationRepository.findByTaskDatasetId(1L)).thenReturn(annotations);

        assertEquals(1.0, metricsService.computeCohenKappa(1L, 1L, 2L), 0.001);
    }

    @Test
    void testCohenKappaNoAgreement() {
        List<Annotation> annotations = new ArrayList<>();
        for (long i = 1; i <= 10; i++) {
            TextPair pair = textPair(i);
            annotations.add(annotation(pair, annotator(1L), i <= 5 ? "A" : "B", 5L));
            annotations.add(annotation(pair, annotator(2L), i <= 5 ? "B" : "A", 5L));
        }
        when(annotationRepository.findByTaskDatasetId(1L)).thenReturn(annotations);

        assertEquals(-1.0, metricsService.computeCohenKappa(1L, 1L, 2L), 0.001);
    }

    @Test
    void testFleissKappaCalculation() {
        TextPair t1 = textPair(1L);
        TextPair t2 = textPair(2L);
        List<Annotation> annotations = List.of(
                annotation(t1, annotator(1L), "A", 5L),
                annotation(t1, annotator(2L), "A", 5L),
                annotation(t1, annotator(3L), "B", 5L),
                annotation(t2, annotator(1L), "B", 5L),
                annotation(t2, annotator(2L), "B", 5L),
                annotation(t2, annotator(3L), "B", 5L)
        );
        when(annotationRepository.findByTaskDatasetId(1L)).thenReturn(annotations);

        MetricsDTO metrics = metricsService.computeFleissKappa(1L);

        assertEquals(0.25, metrics.fleissKappa(), 0.001);
    }

    @Test
    void testSpammerDetectionSameClassAlways() {
        List<Annotation> annotations = new ArrayList<>();
        Annotator spammer = annotator(1L);
        Annotator peer = annotator(2L);
        for (long i = 1; i <= 20; i++) {
            TextPair pair = textPair(i);
            annotations.add(annotation(pair, spammer, "A", 1L));
            annotations.add(annotation(pair, peer, i % 2 == 0 ? "A" : "B", 5L));
        }
        when(annotationRepository.findByTaskDatasetId(1L)).thenReturn(annotations);

        List<SpammerDTO> spammers = metricsService.detectSpammers(1L);

        assertFalse(spammers.isEmpty());
        assertTrue(spammers.getFirst().suspicionScore() > 70.0);
    }

    private TextPair textPair(Long id) {
        TextPair pair = new TextPair();
        pair.setId(id);
        return pair;
    }

    private Annotator annotator(Long id) {
        Annotator annotator = new Annotator();
        annotator.setId(id);
        annotator.setNom("Nom" + id);
        annotator.setPrenom("Prenom" + id);
        annotator.setLogin("annotator" + id);
        return annotator;
    }

    private Annotation annotation(TextPair pair, Annotator annotator, String chosenClass, Long durationSeconds) {
        Annotation annotation = new Annotation();
        annotation.setTextPair(pair);
        annotation.setAnnotator(annotator);
        annotation.setChosenClass(chosenClass);
        annotation.setDurationSeconds(durationSeconds);
        return annotation;
    }
}
