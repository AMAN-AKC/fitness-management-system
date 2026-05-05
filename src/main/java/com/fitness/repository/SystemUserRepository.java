package com.fitness.repository;

import com.fitness.entity.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SystemUserRepository extends JpaRepository<SystemUser, Long> {
	Optional<SystemUser> findByUsername(String username);

	Optional<SystemUser> findByEmail(String email);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);
}
