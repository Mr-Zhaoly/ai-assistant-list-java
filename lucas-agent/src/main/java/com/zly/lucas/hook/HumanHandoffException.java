package com.zly.lucas.hook;

/**
 * 转人工异常
 * <p>
 * 当情绪识别结果为 angry 或 human 时，由 {@link GlobalPreProcessHook} 抛出，
 * 由 {@link com.zly.lucas.controller.LucasController} 捕获后返回转人工响应。
 * </p>
 *
 * @author zly
 */
public class HumanHandoffException extends RuntimeException {

    /** 触发转人工的情绪类型（angry / human） */
    private final String emotion;

    /** 用户语言，用于生成对应语言的转人工话术 */
    private final String userLanguage;

    public HumanHandoffException(String emotion, String userLanguage) {
        super("Human handoff triggered: emotion=" + emotion);
        this.emotion = emotion;
        this.userLanguage = userLanguage;
    }

    public String getEmotion() { return emotion; }

    public String getUserLanguage() { return userLanguage; }

    /**
     * 根据用户语言生成转人工提示语
     * <p>对应 Dify 中 "语言识别转人工" 代码节点的逻辑</p>
     */
    public String getHandoffMessage() {
        return switch (userLanguage) {
            case "zh" -> "正在为您转接人工客服，请稍候...";
            case "ja" -> "人工オペレーターに転送しています。お待ちください...";
            case "es" -> "Transfiriéndole a un agente humano, por favor espere...";
            case "fr" -> "Transfert vers un agent humain, veuillez patienter...";
            case "de" -> "Verbindung zu einem menschlichen Agenten wird hergestellt, bitte warten...";
            default  -> "Transferring you to a human agent, please wait...";
        };
    }
}
