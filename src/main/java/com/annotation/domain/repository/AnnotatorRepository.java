package com.annotation.domain.repository;

import com.annotation.domain.entity.Annotator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository port for Annotator entity.
 */
@Repository
public interface AnnotatorRepository extends JpaRepository<Annotator, Long> {
    List<Annotator> findByActiveTrue();
}
