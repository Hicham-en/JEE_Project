package com.annotation.application.service.impl;

import com.annotation.application.dto.AnnotationCreateDTO;
import com.annotation.application.mapper.TaskMapper;
import com.annotation.domain.entity.Annotation;
import com.annotation.domain.entity.Annotator;
import com.annotation.domain.entity.Dataset;
import com.annotation.domain.entity.Task;
import com.annotation.domain.entity.TaskStatus;
import com.annotation.domain.entity.TextPair;
import com.annotation.domain.repository.AnnotationRepository;
import com.annotation.domain.repository.TaskRepository;
import com.annotation.domain.repository.TextPairRepository;
import com.annotation.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnotationServiceImplTest {

    @Mock private AnnotationRepository annotationRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private TextPairRepository textPairRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskMapper taskMapper;
    @InjectMocks private AnnotationServiceImpl annotationService;

    @Test
    void testUpsertAnnotation() {
        Task task = task(1L);
        task.getTextPairs().add(textPair(10L));
        task.getTextPairs().add(textPair(11L));
        TextPair pair = textPair(10L);
        Annotation existing = new Annotation();
        existing.setId(99L);
        existing.setTask(task);
        existing.setTextPair(pair);
        existing.setAnnotator(task.getAnnotator());
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(textPairRepository.findById(10L)).thenReturn(Optional.of(pair));
        when(annotationRepository.findByTaskIdAndTextPairIdAndAnnotatorId(1L, 10L, 7L)).thenReturn(Optional.of(existing));
        when(annotationRepository.save(any(Annotation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(annotationRepository.countByTaskId(1L)).thenReturn(1L);

        annotationService.saveAnnotation(new AnnotationCreateDTO(1L, 10L, "A"), 7L);

        verify(annotationRepository, times(1)).save(existing);
        assertEquals("A", existing.getChosenClass());
    }

    @Test
    void testTaskCompletionOnLastAnnotation() {
        Task task = task(1L);
        task.getTextPairs().add(textPair(10L));
        TextPair pair = textPair(10L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(textPairRepository.findById(10L)).thenReturn(Optional.of(pair));
        when(annotationRepository.findByTaskIdAndTextPairIdAndAnnotatorId(1L, 10L, 7L)).thenReturn(Optional.empty());
        when(annotationRepository.save(any(Annotation.class))).thenAnswer(invocation -> {
            Annotation annotation = invocation.getArgument(0);
            annotation.setId(100L);
            return annotation;
        });
        when(annotationRepository.countByTaskId(1L)).thenReturn(1L);

        annotationService.saveAnnotation(new AnnotationCreateDTO(1L, 10L, "B"), 7L);

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }

    private Task task(Long id) {
        Dataset dataset = new Dataset();
        dataset.setId(1L);
        Annotator annotator = new Annotator();
        annotator.setId(7L);
        Task task = new Task();
        task.setId(id);
        task.setDataset(dataset);
        task.setAnnotator(annotator);
        task.setStatus(TaskStatus.PENDING);
        return task;
    }

    private TextPair textPair(Long id) {
        TextPair pair = new TextPair();
        pair.setId(id);
        return pair;
    }
}
