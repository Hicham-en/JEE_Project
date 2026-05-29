package com.annotation.application.service;

import com.annotation.application.dto.AnnotationCreateDTO;
import com.annotation.application.dto.AnnotationDTO;
import com.annotation.application.dto.PageResponseDTO;
import com.annotation.application.dto.TaskDTO;
import org.springframework.data.domain.Pageable;

/**
 * Service port for handling annotations during tasks.
 */
public interface IAnnotationService {

    /**
     * Retrieves assigned tasks for an annotator.
     *
     * @param annotatorId the annotator
     * @param pageable pagination
     * @return Page of tasks
     */
    PageResponseDTO<TaskDTO> getTasksForAnnotator(Long annotatorId, Pageable pageable);

    /**
     * Saves a user's annotation.
     *
     * @param dto the annotation payload
     * @param annotatorId the annotator's ID
     * @return the saved annotation
     */
    AnnotationDTO saveAnnotation(AnnotationCreateDTO dto, Long annotatorId);
}
