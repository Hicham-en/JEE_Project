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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
        prepareDataset(10, 2);
        when(taskRepository.findByDatasetId(1L)).thenReturn(List.of());

        assertThrows(AssignmentException.class, () -> assignmentService.assignAnnotators(1L, List.of(1L, 2L)));
    }

    @Test
    void testCanAddAnnotatorWhenDatasetAlreadyHasThree() {
        prepareDataset(10, 1);
        when(taskRepository.findByDatasetId(1L)).thenReturn(existingTasks(3));
        when(annotatorRepository.findAllById(any())).thenReturn(List.of(annotator(4L)));

        assertDoesNotThrow(() -> assignmentService.assignAnnotators(1L, List.of(4L)));
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
        lenient().when(datasetRepository.findById(1L)).thenReturn(Optional.of(dataset));
        lenient().when(textPairRepository.findByDatasetIdOrderByIdAsc(1L)).thenReturn(textPairs(textCount));
        lenient().when(annotatorRepository.findAllById(any())).thenReturn(annotators(annotatorCount));
        lenient().when(taskRepository.findByDatasetId(1L)).thenReturn(List.of());
    }

    private List<Task> savedTasks() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Task>> captor = ArgumentCaptor.forClass(List.class);
        verify(taskRepository, org.mockito.Mockito.atLeastOnce()).saveAll(captor.capture());
        return new ArrayList<>(captor.getValue());
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
            annotators.add(annotator(i));
        }
        return annotators;
    }

    private Annotator annotator(long id) {
        Annotator annotator = new Annotator();
        annotator.setId(id);
        annotator.setNom("Nom" + id);
        annotator.setPrenom("Prenom" + id);
        return annotator;
    }

    private List<Task> existingTasks(int count) {
        List<Task> tasks = new ArrayList<>();
        for (long i = 1; i <= count; i++) {
            Task task = new Task();
            task.setId(i);
            Annotator annotator = new Annotator();
            annotator.setId(i);
            annotator.setNom("Existing" + i);
            annotator.setPrenom("Annotator" + i);
            task.setAnnotator(annotator);
            task.setStatus(com.annotation.domain.entity.TaskStatus.PENDING);
            tasks.add(task);
        }
        return tasks;
    }
}
