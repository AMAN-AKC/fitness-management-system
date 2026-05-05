package com.fitness.repository;

import com.fitness.entity.AuditLog;
import com.fitness.entity.AuditLog.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
	List<AuditLog> findByPerformedByUserIdOrderByCreatedAtDesc(Long userId);

	List<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId);

	List<AuditLog> findByAction(Action action);

	List<AuditLog> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}