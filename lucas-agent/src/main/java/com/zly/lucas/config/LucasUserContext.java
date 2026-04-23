package com.zly.lucas.config;

/**
 * Lucas 用户上下文持有器
 * <p>
 * 基于 ThreadLocal 存储请求级别的用户信息，
 * 包含用户语言、情绪状态、改写后的查询（enriched_query）等全局前置处理结果。
 * </p>
 *
 * @author zly
 */
public class LucasUserContext {

    private static final ThreadLocal<LucasUserContext> CONTEXT = new ThreadLocal<>();

    /** 用户 ID */
    private Long userId;

    /** 会话 ID */
    private String sessionId;

    /** 用户输入的语言（zh/en/ja/es/fr/de 等） */
    private String userLanguage = "en";

    /** 情绪识别结果（normal / negative / angry / human） */
    private String emotion = "normal";

    /** 经过 Context Engineering 改写后的有效查询文本 */
    private String enrichedQuery;

    /** 当前识别到的业务域（product/troubleshooting/order/policy/installation/solution/general） */
    private String currentDomain;

    // ─── 静态工厂方法 ───────────────────────────────────────────────────────────

    public static void set(LucasUserContext ctx) {
        CONTEXT.set(ctx);
    }

    public static LucasUserContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    // ─── Getter / Setter ────────────────────────────────────────────────────────

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserLanguage() { return userLanguage; }
    public void setUserLanguage(String userLanguage) { this.userLanguage = userLanguage; }

    public String getEmotion() { return emotion; }
    public void setEmotion(String emotion) { this.emotion = emotion; }

    public String getEnrichedQuery() { return enrichedQuery; }
    public void setEnrichedQuery(String enrichedQuery) { this.enrichedQuery = enrichedQuery; }

    public String getCurrentDomain() { return currentDomain; }
    public void setCurrentDomain(String currentDomain) { this.currentDomain = currentDomain; }
}
