package com.fitness.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@ConditionalOnProperty(name = "app.db.schemaPatch.enabled", havingValue = "true", matchIfMissing = true)
public class DbSchemaPatchRunner {

	private static final Logger log = LoggerFactory.getLogger(DbSchemaPatchRunner.class);

	@Bean
	public CommandLineRunner dbSchemaPatch(JdbcTemplate jdbcTemplate) {
		return args -> {
			ensureTableExists(jdbcTemplate, "system_config",
					"CREATE TABLE IF NOT EXISTS system_config (" +
							"config_key VARCHAR(255) NOT NULL, " +
							"config_value VARCHAR(255) NOT NULL, " +
							"version INT NOT NULL DEFAULT 1, " +
							"updated_by VARCHAR(255) DEFAULT NULL, " +
							"updated_at DATETIME(6) DEFAULT NULL, " +
							"PRIMARY KEY (config_key)" +
						") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci");

			ensureColumnExists(jdbcTemplate, "branch", "branch_code",
					"ALTER TABLE branch ADD COLUMN branch_code VARCHAR(20) NULL");
			ensureColumnExists(jdbcTemplate, "member", "my_referral_code",
					"ALTER TABLE member ADD COLUMN my_referral_code VARCHAR(30) NULL");
			ensureColumnExists(jdbcTemplate, "member", "wallet_balance",
					"ALTER TABLE member ADD COLUMN wallet_balance DECIMAL(10,2) NOT NULL DEFAULT 0.00");
			ensureColumnExists(jdbcTemplate, "notification", "deep_link",
					"ALTER TABLE notification ADD COLUMN deep_link VARCHAR(500) NULL AFTER body");
			ensureColumnExists(jdbcTemplate, "invoice", "wallet_credit_applied",
					"ALTER TABLE invoice ADD COLUMN wallet_credit_applied DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER discount");
			ensureColumnDefinition(jdbcTemplate, "classes", "weekdays", "varchar(100)",
					"ALTER TABLE classes MODIFY COLUMN weekdays VARCHAR(100) NOT NULL");
			ensureTableExists(jdbcTemplate, "system_user_branches",
					"CREATE TABLE IF NOT EXISTS system_user_branches (" +
							"user_id BIGINT NOT NULL, " +
							"branch_id BIGINT NOT NULL, " +
							"PRIMARY KEY (user_id, branch_id), " +
							"KEY idx_system_user_branches_branch (branch_id), " +
							"CONSTRAINT fk_system_user_branches_user FOREIGN KEY (user_id) REFERENCES system_user (user_id), " +
							"CONSTRAINT fk_system_user_branches_branch FOREIGN KEY (branch_id) REFERENCES branch (branch_id)" +
						") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci");
			jdbcTemplate.execute(
					"INSERT IGNORE INTO system_user_branches (user_id, branch_id) " +
							"SELECT user_id, branch_id FROM system_user WHERE branch_id IS NOT NULL");
		};
	}

	private void ensureTableExists(JdbcTemplate jdbcTemplate, String tableName, String createTableSql) {
		Integer existingCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.TABLES " +
						"WHERE TABLE_SCHEMA = DATABASE() " +
						"AND LOWER(TABLE_NAME) = LOWER(?)",
				Integer.class,
				tableName
		);

		if (existingCount != null && existingCount > 0) {
			return;
		}

		log.warn("DB schema patch: creating missing table {}", tableName);
		jdbcTemplate.execute(createTableSql);
	}

	private void ensureColumnExists(JdbcTemplate jdbcTemplate, String tableName, String columnName, String alterSql) {
		Integer existingCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.COLUMNS " +
						"WHERE TABLE_SCHEMA = DATABASE() " +
						"AND LOWER(TABLE_NAME) = LOWER(?) " +
						"AND LOWER(COLUMN_NAME) = LOWER(?)",
				Integer.class,
				tableName,
				columnName
		);

		if (existingCount != null && existingCount > 0) {
			return;
		}

		log.warn("DB schema patch: adding missing column {}.{}", tableName, columnName);
		jdbcTemplate.execute(alterSql);
	}

	private void ensureColumnDefinition(JdbcTemplate jdbcTemplate, String tableName, String columnName,
			String expectedColumnType, String alterSql) {
		String currentColumnType = jdbcTemplate.queryForObject(
				"SELECT LOWER(COLUMN_TYPE) FROM information_schema.COLUMNS " +
						"WHERE TABLE_SCHEMA = DATABASE() " +
						"AND LOWER(TABLE_NAME) = LOWER(?) " +
						"AND LOWER(COLUMN_NAME) = LOWER(?)",
				String.class,
				tableName,
				columnName
		);

		if (expectedColumnType.equals(currentColumnType)) {
			return;
		}

		log.warn("DB schema patch: modifying column {}.{} to {}", tableName, columnName, expectedColumnType);
		jdbcTemplate.execute(alterSql);
	}
}
