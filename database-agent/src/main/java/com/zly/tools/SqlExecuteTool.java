package com.zly.tools;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.zly.service.DatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * SQL执行工具
 * 根据表结构和用户问题，执行SQL查询并返回结果
 *
 * @author zhaoliangyu
 * @since 2025/01/XX
 */
@Slf4j
@Component
public class SqlExecuteTool implements Tool<SqlExecuteTool.Request, String> {

    @Autowired
    private DatabaseService databaseService;

    @Override
    public ToolCallback toolCallback() {
        return FunctionToolCallback.builder("sql_execute", this)
                .description("执行SQL查询语句并返回结果。只能执行SELECT查询语句，不支持INSERT、UPDATE、DELETE等修改操作。根据表结构和用户的问题生成相应的SQL语句并执行。")
                .inputType(Request.class)
                .build();
    }

    @Override
    public String apply(Request request, ToolContext toolContext) {
        log.info("执行SQL查询: {}", request.sql);
        
        try {
            return databaseService.executeQuery(request.sql);
        } catch (IllegalArgumentException e) {
            log.warn("SQL安全检查失败: {}", e.getMessage());
            return "错误: " + e.getMessage();
        } catch (Exception e) {
            log.error("执行SQL查询失败", e);
            return "执行SQL查询时发生错误: " + e.getMessage();
        }
    }

    @JsonClassDescription("SQL执行请求")
    public record Request(
            @JsonProperty(value = "sql", required = true)
            @JsonPropertyDescription("要执行的SQL查询语句，必须是SELECT语句。根据表结构和用户的问题生成相应的SQL语句。")
            String sql
    ) {
    }
}

