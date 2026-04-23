package com.zly.lucas.agent;

/**
 * 垂直域 Agent 统一接口
 * <p>
 * 每个业务域（product/troubleshooting/order/policy/installation/solution）
 * 均实现此接口，由 Supervisor Agent 根据域分类结果调用对应的实现。
 * </p>
 *
 * @author zly
 */
public interface DomainAgent {

    /**
     * 获取该 Agent 负责的业务域标识
     *
     * @return 域标识，如 "product" / "order" 等
     */
    String getDomain();

    /**
     * 处理用户请求
     *
     * @param enrichedQuery 经过 Context Engineering 改写后的有效查询
     * @param sessionId     会话 ID（用于读取域独立记忆）
     * @param userLanguage  用户语言
     * @return 处理结果（可能包含追问话术或最终答案）
     */
    String process(String enrichedQuery, String sessionId, String userLanguage);
}
