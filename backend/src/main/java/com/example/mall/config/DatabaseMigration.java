package com.example.mall.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class DatabaseMigration implements ApplicationListener<ApplicationStartedEvent> {

    private final JdbcTemplate jdbcTemplate;
    private static volatile boolean migrated = false;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        if (migrated) {
            return;
        }
        synchronized (DatabaseMigration.class) {
            if (migrated) {
                return;
            }
            try {
                addDeletedColumn("sys_user");
                addDeletedColumn("sys_role");
                addDeletedColumn("sys_permission");
                log.info("Database migration completed successfully");
                migrated = true;
            } catch (Exception e) {
                log.warn("Database migration warning: {}", e.getMessage());
            }
        }
    }

    private void addDeletedColumn(String tableName) {
        try {
            String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = 'deleted'";

            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, tableName);

            if (count == null || count == 0) {
                String alterSql = "ALTER TABLE " + tableName + " ADD COLUMN deleted INT DEFAULT 0";
                jdbcTemplate.update(alterSql);
                log.info("Added 'deleted' column to table '{}'", tableName);
            } else {
                log.debug("'deleted' column already exists in table '{}'", tableName);
            }
        } catch (Exception e) {
            log.warn("Failed to add 'deleted' column to table '{}': {}", tableName, e.getMessage());
        }
    }
}
