package com.zly.controller;

import com.zly.common.vo.base.ResultT;
import com.zly.model.dto.QuestionRequestDTO;
import com.zly.service.IMysqlManageService;
import com.zly.service.IMysqlQaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Mysql管理类入口
 *
 * @author zhaoliangyu
 * @since 2025/12/2 10:59
 */
@RestController
@RequestMapping("/mysql/manage")
public class MysqlManageController {

    @Autowired
    private IMysqlManageService mysqlManageService;

    @PostMapping("/init")
    public ResultT<Boolean> init() {
        return ResultT.success(mysqlManageService.init());
    }
}
