package com.marabagam.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping
public class HealthController {

    @Autowired(required = false)
    private DataSource dataSource;

    @GetMapping({"/health", "/api/v1/health"})
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("service", "marabagam-backend");
        health.put("timestamp", Instant.now().toString());

        Map<String, Object> dbHealth = new LinkedHashMap<>();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData metaData = conn.getMetaData();
                dbHealth.put("connected", true);
                dbHealth.put("databaseName", metaData.getDatabaseProductName());
                dbHealth.put("databaseVersion", metaData.getDatabaseProductVersion());
            } catch (Exception e) {
                dbHealth.put("connected", false);
                dbHealth.put("error", e.getMessage());
            }
        } else {
            dbHealth.put("connected", false);
            dbHealth.put("info", "No DataSource configured");
        }
        health.put("database", dbHealth);

        return ResponseEntity.ok(health);
    }
}
