package com.annotation.domain.repository;

import com.annotation.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository port for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Finds a User by their exact login.
     *
     * @param login the login username
     * @return an Optional containing the User if found
     */
    Optional<User> findByLogin(String login);
}
