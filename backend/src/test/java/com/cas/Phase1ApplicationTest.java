package com.cas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Phase1ApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Test
    void should_load_application_context() {
        assertNotNull(applicationContext, "Application context should load successfully");
    }

    @Test
    void should_have_datasource_configured() {
        assertNotNull(dataSource, "DataSource should be configured");
    }

    @Test
    void should_have_all_7_tables_created_by_flyway() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES")) {

            // Collect all table names
            java.util.Set<String> tables = new java.util.HashSet<>();
            while (rs.next()) {
                tables.add(rs.getString(1).toLowerCase());
            }

            // Verify all 7 required tables exist
            assertTrue(tables.contains("sys_user"), "sys_user table should exist");
            assertTrue(tables.contains("semester"), "semester table should exist");
            assertTrue(tables.contains("course"), "course table should exist");
            assertTrue(tables.contains("course_offering"), "course_offering table should exist");
            assertTrue(tables.contains("selection_campaign"), "selection_campaign table should exist");
            assertTrue(tables.contains("enrollment"), "enrollment table should exist");
            assertTrue(tables.contains("notification"), "notification table should exist");

            // 7 business tables + flyway_schema_history = 8
            assertTrue(tables.size() >= 7, "At least 7 business tables should exist");
            assertTrue(tables.contains("flyway_schema_history"), "flyway_schema_history should exist");
        }
    }

    @Test
    void should_have_admin_user_seeded_by_flyway() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) FROM sys_user WHERE username = 'admin'")) {

            assertTrue(rs.next(), "Should have result");
            int count = rs.getInt(1);
            assertEquals(1, count, "Admin user should be seeded");
        }
    }
}