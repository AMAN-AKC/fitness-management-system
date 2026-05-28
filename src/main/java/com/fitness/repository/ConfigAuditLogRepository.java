package com.fitness.repository;

import com.fitness.entity.ConfigAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigAuditLogRepository extends JpaRepository<ConfigAuditLog, Long> {
	List<ConfigAuditLog> findTop50ByOrderByTimestampDesc();
}
