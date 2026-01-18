package com.zly.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库服务类，用于执行数据库查询操作
 * 使用 Druid 数据源连接池
 *
 * @author zhaoliangyu
 * @since 2025/01/XX
 */
@Service
@Slf4j
public class DatabaseService {

    @Autowired
    private DataSource dataSource;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    /**
     * 从 JDBC URL 中提取数据库名
     */
    private String getDatabaseName() {
        // 从 jdbc:mysql://host:port/database 中提取数据库名
        try {
            String[] parts = jdbcUrl.split("/");
            if (parts.length > 3) {
                String dbPart = parts[3].split("\\?")[0];
                return dbPart;
            }
        } catch (Exception e) {
            log.warn("无法从JDBC URL提取数据库名", e);
        }
        return "datax"; // 默认值
    }

    /**
     * 表信息类，包含表名和注释
     */
    public static class TableInfo {
        private final String tableName;
        private final String tableComment;

        public TableInfo(String tableName, String tableComment) {
            this.tableName = tableName;
            this.tableComment = tableComment != null ? tableComment : "";
        }

        public String getTableName() {
            return tableName;
        }

        public String getTableComment() {
            return tableComment;
        }
    }

    /**
     * 根据关键字搜索表名
     * 支持通过表名和表注释（中文描述）进行搜索
     *
     * @param keyword 搜索关键字（可以是表名或中文描述）
     * @return 匹配的表信息列表（包含表名和注释）
     */
    public List<TableInfo> searchTables(String keyword) {
        List<TableInfo> tables = new ArrayList<>();
        String database = getDatabaseName();
        
        try (Connection conn = dataSource.getConnection()) {
            // 同时搜索表名和表注释，支持用户使用中文描述搜索英文表名
            String sql = "SELECT DISTINCT TABLE_NAME, TABLE_COMMENT " +
                    "FROM information_schema.TABLES " +
                    "WHERE TABLE_SCHEMA = ? " +
                    "AND (TABLE_NAME LIKE ? OR TABLE_COMMENT LIKE ?) " +
                    "ORDER BY TABLE_NAME";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                String searchPattern = "%" + keyword + "%";
                stmt.setString(1, database);
                stmt.setString(2, searchPattern);
                stmt.setString(3, searchPattern);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        String tableComment = rs.getString("TABLE_COMMENT");
                        
                        // 避免重复添加
                        boolean exists = tables.stream()
                                .anyMatch(t -> t.getTableName().equals(tableName));
                        if (!exists) {
                            tables.add(new TableInfo(tableName, tableComment));
                            log.debug("找到匹配表: {} (注释: {})", tableName, tableComment);
                        }
                    }
                }
            }
            log.info("搜索关键字 '{}' 找到 {} 个表", keyword, tables.size());
        } catch (SQLException e) {
            log.error("搜索表名失败", e);
            throw new RuntimeException("搜索表名失败: " + e.getMessage(), e);
        }
        return tables;
    }

    /**
     * 获取表结构
     *
     * @param tableName 表名
     * @return 表结构信息（字段名、类型、是否为空、默认值、注释等）
     */
    public String getTableStructure(String tableName) {
        StringBuilder structure = new StringBuilder();
        String database = getDatabaseName();
        
        try (Connection conn = dataSource.getConnection()) {
            // 获取字段信息
            String sql = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT, COLUMN_KEY " +
                    "FROM information_schema.COLUMNS " +
                    "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
                    "ORDER BY ORDINAL_POSITION";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, database);
                stmt.setString(2, tableName);
                try (ResultSet rs = stmt.executeQuery()) {
                    structure.append("表名: ").append(tableName).append("\n");
                    structure.append("字段信息:\n");
                    structure.append(String.format("%-20s %-20s %-10s %-15s %-10s %s\n", 
                            "字段名", "数据类型", "可空", "默认值", "键", "注释"));
                    structure.append("-".repeat(100)).append("\n");
                    
                    while (rs.next()) {
                        String columnName = rs.getString("COLUMN_NAME");
                        String dataType = rs.getString("DATA_TYPE");
                        String isNullable = rs.getString("IS_NULLABLE");
                        String defaultValue = rs.getString("COLUMN_DEFAULT");
                        String columnKey = rs.getString("COLUMN_KEY");
                        String comment = rs.getString("COLUMN_COMMENT");
                        
                        if (defaultValue == null) {
                            defaultValue = "";
                        }
                        if (columnKey == null) {
                            columnKey = "";
                        }
                        if (comment == null) {
                            comment = "";
                        }
                        
                        structure.append(String.format("%-20s %-20s %-10s %-15s %-10s %s\n",
                                columnName, dataType, isNullable, defaultValue, columnKey, comment));
                    }
                }
            }
            
            log.info("获取表 {} 的结构成功", tableName);
        } catch (SQLException e) {
            log.error("获取表结构失败: {}", tableName, e);
            throw new RuntimeException("获取表结构失败: " + e.getMessage(), e);
        }
        return structure.toString();
    }

    /**
     * 执行SQL查询（仅支持SELECT语句）
     *
     * @param sql SQL语句
     * @return 查询结果（格式化的字符串）
     */
    public String executeQuery(String sql) {
        // 安全检查：只允许SELECT语句
        String trimmedSql = sql.trim().toUpperCase();
        if (!trimmedSql.startsWith("SELECT")) {
            throw new IllegalArgumentException("只允许执行SELECT查询语句");
        }
        
        StringBuilder result = new StringBuilder();
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    
                    // 打印表头
                    result.append("查询结果:\n");
                    for (int i = 1; i <= columnCount; i++) {
                        result.append(String.format("%-20s", metaData.getColumnName(i)));
                    }
                    result.append("\n");
                    result.append("-".repeat(columnCount * 20)).append("\n");
                    
                    // 打印数据
                    int rowCount = 0;
                    while (rs.next() && rowCount < 100) { // 限制最多返回100行
                        for (int i = 1; i <= columnCount; i++) {
                            Object value = rs.getObject(i);
                            String valueStr = value != null ? value.toString() : "NULL";
                            result.append(String.format("%-20s", valueStr));
                        }
                        result.append("\n");
                        rowCount++;
                    }
                    
                    if (rowCount >= 100) {
                        result.append("\n(结果已截断，最多显示100行)\n");
                    }
                    result.append(String.format("\n共查询到 %d 行数据\n", rowCount));
                }
            }
            log.info("执行SQL查询成功");
        } catch (SQLException e) {
            log.error("执行SQL查询失败", e);
            throw new RuntimeException("执行SQL查询失败: " + e.getMessage(), e);
        }
        return result.toString();
    }
}

