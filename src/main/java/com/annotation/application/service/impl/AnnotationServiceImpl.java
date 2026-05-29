package com.annotation.application.service.impl;

import com.annotation.application.dto.AnnotationCreateDTO;
import com.annotation.application.dto.AnnotationDTO;
import com.annotation.application.dto.PageResponseDTO;
import com.annotation.application.dto.TaskDTO;
import com.annotation.application.exception.EntityNotFoundException;
import com.annotation.application.mapper.TaskMapper;
import com.annotation.application.service.IAnnotationService;
import com.annotation.domain.entity.Annotation;
import com.annotation.domain.entity.Annotator;
import com.annotation.domain.entity.Task;
import com.annotation.domain.entity.TaskStatus;
import com.annotation.domain.entity.TextPair;
import com.annotation.domain.repository.AnnotationRepository;
import com.annotation.domain.repository.TaskRepository;
import com.annotation.domain.repository.TextPairRepository;
import com.annotation.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AnnotationServiceImpl implements IAnnotationService {

    private static final Logger log = LoggerFactory.getLogger(AnnotationServiceImpl.class);

    private final AnnotationRepository annotationRepository;
    private final TaskRepository taskRepository;
    private final TextPairRepository textPairRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public AnnotationServiceImpl(AnnotationRepository annotationRepository,
                                 TaskRepository taskRepository,
                                 TextPairRepository textPairRepository,
                                 UserRepository userRepository,
                                 TaskMapper taskMapper) {
        this.annotationRepository = annotationRepository;
        this.taskRepository = taskRepository;
        this.textPairRepository = textPairRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    public PageResponseDTO<TaskDTO> getTasksForAnnotator(Long annotatorId, Pageable pageable) {
        Page<Task> tasks = taskRepository.findByAnnotatorIdAndStatusNot(annotatorId, TaskStatus.CANCELLED, pageable);
        
        java.util.List<TaskDTO> content = tasks.stream().map(taskMapper::toDto).toList();
        return new PageResponseDTO<>(
            content,
            tasks.getNumber(),
            tasks.getSize(),
            tasks.getTotalElements(),
            tasks.getTotalPages(),
            tasks.isLast()
        );
    }

    @Override
    public AnnotationDTO saveAnnotation(AnnotationCreateDTO dto, Long annotatorId) {
        Task task = taskRepository.findById(dto.taskId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
                
        if (!task.getAnnotator().getId().equals(annotatorId)) {
            throw new IllegalArgumentException("User is not assigned to this task");
        }
        
        TextPair textPair = textPairRepository.findById(dto.textPairId())
                .orElseThrow(() -> new EntityNotFoundException("TextPair not found"));

        // UPSERT LOGIC
        Annotation annotation = annotationRepository
                .findByTaskIdAndTextPairIdAndAnnotatorId(task.getId(), textPair.getId(), annotatorId)
                .orElse(new Annotation());

        if (annotation.getId() == null) {
            annotation.setTask(task);
            annotation.setTextPair(textPair);
            annotation.setAnnotator((Annotator) task.getAnnotator());
            annotation.setSource("WEB");
        }

        annotation.setChosenClass(dto.chosenClass());
        annotation.setAnnotationTime(LocalDateTime.now());
        
        annotation = annotationRepository.save(annotation);
        log.info("Annotation saved taskId={} textPairId={} annotatorId={}", task.getId(), textPair.getId(), annotatorId);

        // Update task status based on count
        long totalPairs = textPairRepository.countByDatasetId(task.getDataset().getId());
        long annotatedCount = annotationRepository.countByTaskId(task.getId());
        
        if (annotatedCount >= totalPairs && task.getStatus() != TaskStatus.COMPLETED) {
            task.setStatus(TaskStatus.COMPLETED);
            taskRepository.save(task);
        } else if (annotatedCount > 0 && task.getStatus() == TaskStatus.PENDING) {
            task.setStatus(TaskStatus.IN_PROGRESS);
            taskRepository.save(task);
        }

        return new AnnotationDTO(
            annotation.getId(),
            task.getId(),
            textPair.getId(),
            annotation.getChosenClass(),
            annotation.getAnnotationTime(),
            annotation.getSource()
        );
    }
}
