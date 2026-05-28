package com.fitness.repository;

import com.fitness.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
	Optional<NotificationPreference> findByUserUserId(Long userId);
}
