package com.annotation.domain.repository;

import com.annotation.domain.entity.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository port for Administrator entity.
 */
@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
}
