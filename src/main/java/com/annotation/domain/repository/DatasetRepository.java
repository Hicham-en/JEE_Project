package com.annotation.domain.repository;

import com.annotation.domain.entity.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository port for Dataset entity.
 */
@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Long> {
}
