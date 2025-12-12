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
 * 表结构查询工具
 * 获取用户确认的表名的完整表结构信息
 *
 * @author zhaoliangyu
 * @since 2025/01/XX
 */
@Slf4j
@Component
public class TableStructureTool implements Tool<TableStructureTool.Request, String> {

    @Autowired
    private DatabaseService databaseService;

    @Override
    public ToolCallback toolCallback() {
        return FunctionToolCallback.builder("table_structure", this)
                .description("获取指定表的完整结构信息，包括字段名、数据类型、是否可空、默认值、主键信息和字段注释。在使用此工具前，必须确保用户已经确认了要查询的表名。")
                .inputType(Request.class)
                .build();
    }

    @Override
    public String apply(Request request, ToolContext toolContext) {
        log.info("获取表结构，表名: {}", request.tableName);
        
        try {
            String structure = databaseService.getTableStructure(request.tableName);
            return structure;
        } catch (Exception e) {
            log.error("获取表结构失败", e);
            return "获取表结构时发生错误: " + e.getMessage();
        }
    }

    @JsonClassDescription("表结构查询请求")
    public record Request(
            @JsonProperty(value = "table_name", required = true)
            @JsonPropertyDescription("用户确认的表名，用于获取该表的完整结构信息")
            String tableName
    ) {
    }
}

