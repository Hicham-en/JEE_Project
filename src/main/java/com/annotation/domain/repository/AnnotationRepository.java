package com.annotation.domain.repository;

import com.annotation.domain.entity.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for Annotation entity.
 */
@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    @Query("SELECT a FROM Annotation a JOIN FETCH a.textPair tp JOIN FETCH a.annotator an JOIN a.task t WHERE t.dataset.id = :datasetId")
    List<Annotation> findByTaskDatasetId(@Param("datasetId") Long datasetId);

    @Query("SELECT COUNT(a) FROM Annotation a WHERE a.task.dataset.id = :datasetId")
    long countByTaskDatasetId(@Param("datasetId") Long datasetId);

    Optional<Annotation> findByTaskIdAndTextPairIdAndAnnotatorId(Long taskId, Long textPairId, Long annotatorId);

    long countByTaskId(Long taskId);
}
