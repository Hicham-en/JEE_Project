package com.annotation.domain.repository;

import com.annotation.domain.entity.TextPair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository port for TextPair entity.
 */
@Repository
public interface TextPairRepository extends JpaRepository<TextPair, Long> {
    /**
     * Count text pairs in a dataset.
     * @param datasetId The ID of the dataset
     * @return Number of text pairs
     */
    long countByDatasetId(Long datasetId);

    Page<TextPair> findByDatasetId(Long datasetId, Pageable pageable);

    List<TextPair> findByDatasetIdOrderByIdAsc(Long datasetId);
}
