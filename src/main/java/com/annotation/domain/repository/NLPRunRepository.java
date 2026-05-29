package com.annotation.domain.repository;

import com.annotation.domain.entity.NLPRun;
import com.annotation.domain.entity.NLPRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository port for persisted NLP script runs.
 */
@Repository
public interface NLPRunRepository extends JpaRepository<NLPRun, Long> {

    /**
     * Finds NLP runs linked to a dataset ordered by most recent start date.
     *
     * @param datasetId dataset ID
     * @return run history
     */
    @Query("select r from NLPRun r join fetch r.dataset where r.dataset.id = :datasetId order by r.startTime desc")
    List<NLPRun> findByDatasetIdOrderByStartTimeDesc(Long datasetId);

    /**
     * Finds runs matching a status.
     *
     * @param status run status
     * @return matching runs
     */
    List<NLPRun> findByStatus(NLPRunStatus status);
}
