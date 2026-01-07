package com.zly.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Service
public class SysUserSchemaService {

    @Autowired
    private DataSource dataSource;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    private Set<String> columns = new HashSet<>();

    public String col(String logical) {
        // map common aliases
        return switch (logical) {
            case "id" -> "id";
            case "username" -> resolve("username", "user_name");
            case "email" -> resolve("email", "mail");
            case "password_hash" -> resolve("password_hash", "password", "pwd");
            case "status" -> resolve("status", "state");
            case "deleted" -> resolve("deleted", "is_deleted", "del_flag");
            case "created_at" -> resolve("created_at", "create_time", "gmt_create");
            case "updated_at" -> resolve("updated_at", "update_time", "gmt_modified");
            default -> logical;
        };
    }

    public String selectColumns() {
        return String.join(", ", col("id"), col("username"), col("email"), col("password_hash"),
                col("status"), col("deleted"), col("created_at"), col("updated_at"));
    }

    public String insertColumns() {
        return String.join(", ",
                col("username"),
                col("email"),
                col("password_hash"),
                col("status"),
                col("deleted"),
                col("created_at"),
                col("updated_at")
        );
    }

    public String insertPlaceholders() {
        return "?, ?, ?, ?, ?, ?, ?";
    }

    private String resolve(String... candidates) {
        for (String c : candidates) {
            if (columns.contains(c)) {
                return c;
            }
        }
        // default to first candidate
        return candidates[0];
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        String db = parseDatabaseName(jdbcUrl);
        String sql = "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'sys_user'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, db);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME"));
                }
            }
        } catch (SQLException e) {
            // ignore, fallback to defaults
        }
    }

    private String parseDatabaseName(String url) {
        // jdbc:mysql://host:port/database?params
        try {
            int idx = url.indexOf("jdbc:mysql://");
            if (idx >= 0) {
                String rest = url.substring(idx + "jdbc:mysql://".length());
                int slash = rest.indexOf('/');
                if (slash >= 0) {
                    String afterSlash = rest.substring(slash + 1);
                    int q = afterSlash.indexOf('?');
                    return q >= 0 ? afterSlash.substring(0, q) : afterSlash;
                }
            }
        } catch (Exception ignored) {}
        return "datax";
    }
}
