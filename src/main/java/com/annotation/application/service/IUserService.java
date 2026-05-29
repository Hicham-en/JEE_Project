package com.annotation.application.service;

import com.annotation.application.dto.AnnotatorCreateDTO;
import com.annotation.application.dto.AnnotatorCreatedDTO;
import com.annotation.application.dto.AnnotatorUpdateDTO;
import com.annotation.application.dto.AnnotatorDTO;
import com.annotation.application.dto.PageResponseDTO;
import org.springframework.data.domain.Pageable;

/**
 * Service port for User management.
 */
public interface IUserService {

    AnnotatorCreatedDTO createAnnotator(AnnotatorCreateDTO dto);
    
    AnnotatorDTO updateAnnotator(Long id, AnnotatorUpdateDTO dto);
    
    void softDeleteAnnotator(Long id);
    
    /**
     * Gets all annotators with pagination.
     *
     * @param pageable pagination details
     * @return paginated annotators
     */
    PageResponseDTO<AnnotatorDTO> getAllAnnotators(Pageable pageable);

    /**
     * Retrieves an annotator by ID.
     *
     * @param id the annotator id
     * @return the annotator details
     */
    AnnotatorDTO getAnnotatorById(Long id);
}
