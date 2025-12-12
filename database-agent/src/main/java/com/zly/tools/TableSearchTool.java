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

import java.util.List;

/**
 * 表名搜索工具
 * 从用户输入中提取关键字，在数据库中搜索匹配的表名
 *
 * @author zhaoliangyu
 * @since 2025/01/XX
 */
@Slf4j
@Component
public class TableSearchTool implements Tool<TableSearchTool.Request, String> {

    @Autowired
    private DatabaseService databaseService;

    @Override
    public ToolCallback toolCallback() {
        return FunctionToolCallback.builder("table_search", this)
                .description("根据关键字在数据库中搜索匹配的表名。从用户输入中提取与表名相关的关键字，然后在数据库中搜索包含该关键字的表名。")
                .inputType(Request.class)
                .build();
    }

    @Override
    public String apply(Request request, ToolContext toolContext) {
        log.info("搜索表名，关键字: {}", request.keyword);
        
        try {
            List<String> tables = databaseService.searchTables(request.keyword);
            
            if (tables.isEmpty()) {
                return String.format("未找到包含关键字 '%s' 的表。请尝试使用其他关键字搜索。", request.keyword);
            }
            
            StringBuilder result = new StringBuilder();
            result.append(String.format("找到 %d 个匹配的表（关键字: %s）:\n\n", tables.size(), request.keyword));
            for (int i = 0; i < tables.size(); i++) {
                result.append(String.format("%d. %s\n", i + 1, tables.get(i)));
            }
            result.append("\n请确认要查询的表名，我将为您获取该表的详细结构。");
            
            return result.toString();
        } catch (Exception e) {
            log.error("搜索表名失败", e);
            return "搜索表名时发生错误: " + e.getMessage();
        }
    }

    @JsonClassDescription("表名搜索请求")
    public record Request(
            @JsonProperty(value = "keyword", required = true)
            @JsonPropertyDescription("从用户输入中提取的与表名相关的关键字，用于在数据库中搜索匹配的表名")
            String keyword
    ) {
    }
}

