package com.project.pastebinlite.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/healthz")
    public ResponseEntity<Map<String, Boolean>> healthCheck() {
        try (Connection connection = dataSource.getConnection()) {
			boolean isValid = connection.isValid(2); // 2 second timeout
            return ResponseEntity.ok(Map.of("ok", isValid));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("ok", false));
        }
    }
}
