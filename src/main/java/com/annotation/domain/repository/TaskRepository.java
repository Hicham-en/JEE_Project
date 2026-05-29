package com.annotation.domain.repository;

import com.annotation.domain.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for Task entity.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    boolean existsByDatasetIdAndAnnotatorId(Long datasetId, Long annotatorId);
    
    Optional<Task> findByDatasetIdAndAnnotatorId(Long datasetId, Long annotatorId);
    
    @Query("select distinct t from Task t join fetch t.annotator where t.dataset.id = :datasetId")
    List<Task> findByDatasetId(@Param("datasetId") Long datasetId);
    
    List<Task> findByAnnotatorAndStatusIn(com.annotation.domain.entity.Annotator annotator, List<com.annotation.domain.entity.TaskStatus> statuses);
    
    org.springframework.data.domain.Page<Task> findByAnnotatorIdAndStatusNot(Long annotatorId, com.annotation.domain.entity.TaskStatus status, org.springframework.data.domain.Pageable pageable);
}
