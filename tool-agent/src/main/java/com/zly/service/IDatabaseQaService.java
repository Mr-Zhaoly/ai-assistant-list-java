package com.zly.service;

import com.zly.model.dto.QuestionRequestDTO;

/**
 * TODO 写明类的作用
 *
 * @author zhaoliangyu
 * @since 2025/12/12 10:15
 */
public interface IDatabaseQaService {

    String getAnswer(QuestionRequestDTO request);
}
