package com.annotation.domain.repository;

import com.annotation.domain.entity.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for Annotation entity.
 */
@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    List<Annotation> findByTaskDatasetId(Long datasetId);

    long countByTaskDatasetId(Long datasetId);

    Optional<Annotation> findByTaskIdAndTextPairIdAndAnnotatorId(Long taskId, Long textPairId, Long annotatorId);

    long countByTaskId(Long taskId);
}
