package com.annotation.application.service.impl;

import com.annotation.application.exception.AssignmentException;
import com.annotation.domain.entity.Annotator;
import com.annotation.domain.entity.Dataset;
import com.annotation.domain.entity.Task;
import com.annotation.domain.entity.TextPair;
import com.annotation.domain.repository.AnnotatorRepository;
import com.annotation.domain.repository.DatasetRepository;
import com.annotation.domain.repository.TaskRepository;
import com.annotation.domain.repository.TextPairRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceImplTest {

    @Mock private TaskRepository taskRepository;
    @Mock private TextPairRepository textPairRepository;
    @Mock private AnnotatorRepository annotatorRepository;
    @Mock private DatasetRepository datasetRepository;
    @InjectMocks private AssignmentServiceImpl assignmentService;

    @Test
    void testAssignMinimum3Annotators() {
        prepareDataset(10, 3);

        assignmentService.assignAnnotators(1L, List.of(1L, 2L, 3L));

        List<Task> tasks = savedTasks();
        for (TextPair pair : textPairs(10)) {
            long assignments = tasks.stream().filter(task -> task.getTextPairs().contains(pair)).count();
            assertEquals(3, assignments);
        }
    }

    @Test
    void testAssignLessThan3Throws() {
        assertThrows(AssignmentException.class, () -> assignmentService.assignAnnotators(1L, List.of(1L, 2L)));
    }

    @Test
    void testDistributionBalance() {
        prepareDataset(100, 5);

        assignmentService.assignAnnotators(1L, List.of(1L, 2L, 3L, 4L, 5L));

        for (Task task : savedTasks()) {
            assertEquals(60, task.getTextPairs().size(), 6);
        }
    }

    @Test
    void testIdempotentAssignment() {
        prepareDataset(10, 3);
        assignmentService.assignAnnotators(1L, List.of(1L, 2L, 3L));
        List<Task> existing = savedTasks();

        for (Task task : existing) {
            when(taskRepository.findByDatasetIdAndAnnotatorId(1L, task.getAnnotator().getId())).thenReturn(Optional.of(task));
        }

        assignmentService.assignAnnotators(1L, List.of(1L, 2L, 3L));

        for (Task task : existing) {
            assertEquals(10, task.getTextPairs().size());
        }
    }

    private void prepareDataset(int textCount, int annotatorCount) {
        Dataset dataset = new Dataset();
        dataset.setId(1L);
        when(datasetRepository.findById(1L)).thenReturn(Optional.of(dataset));
        when(textPairRepository.findByDatasetIdOrderByIdAsc(1L)).thenReturn(textPairs(textCount));
        when(annotatorRepository.findAllById(any())).thenReturn(annotators(annotatorCount));
    }

    private List<Task> savedTasks() {
        ArgumentCaptor<Iterable<Task>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(taskRepository, org.mockito.Mockito.atLeastOnce()).saveAll(captor.capture());
        List<Task> tasks = new ArrayList<>();
        captor.getValue().forEach(tasks::add);
        return tasks;
    }

    private List<TextPair> textPairs(int count) {
        List<TextPair> pairs = new ArrayList<>();
        for (long i = 1; i <= count; i++) {
            TextPair pair = new TextPair();
            pair.setId(i);
            pair.setText1("Text " + i);
            pairs.add(pair);
        }
        return pairs;
    }

    private List<Annotator> annotators(int count) {
        List<Annotator> annotators = new ArrayList<>();
        for (long i = 1; i <= count; i++) {
            Annotator annotator = new Annotator();
            annotator.setId(i);
            annotator.setNom("Nom" + i);
            annotator.setPrenom("Prenom" + i);
            annotators.add(annotator);
        }
        return annotators;
    }
}
