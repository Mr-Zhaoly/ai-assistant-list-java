package com.zly.controller;

import com.zly.common.vo.base.ResultT;
import com.zly.model.dto.*;
import com.zly.model.entity.SysUser;
import com.zly.model.vo.CaptchaVO;
import com.zly.model.vo.LoginTokenVO;
import com.zly.model.vo.SysUserVO;
import com.zly.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/business/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/captcha")
    public ResultT<CaptchaVO> captcha(@RequestBody Map<String, String> body) {
        String account = body.getOrDefault("account", "");
        if (account.isBlank()) {
            return ResultT.error("account必填");
        }
        String code = userService.generateCaptcha(account);
        return ResultT.success(CaptchaVO.builder().account(account).code(code).ttlSeconds(300).build());
    }

    @PostMapping("/register")
    public ResultT<SysUserVO> register(@RequestBody UserRegisterDTO request) {
        try {
            long id = userService.register(request.getUsername(), request.getEmail(), request.getPassword());
            SysUserVO vo = SysUserVO.builder()
                    .id(id)
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .status("ACTIVE")
                    .deleted(0)
                    .build();
            return ResultT.success(vo);
        } catch (Exception e) {
            return ResultT.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResultT<LoginTokenVO> login(@RequestBody UserLoginDTO request) {
        try {
            String token = userService.login(request.getAccount(), request.getPassword(), request.getCaptcha());
            return ResultT.success(LoginTokenVO.builder().token(token).expiresInSeconds(7200L).build());
        } catch (Exception e) {
            return ResultT.error(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResultT<Map<String, String>> resetPassword(@RequestBody UserResetPasswordDTO request) {
        try {
            userService.resetPassword(request.getAccount());
            return ResultT.success(Map.of("msg", "密码已重置为 abc~123456"));
        } catch (Exception e) {
            return ResultT.error(e.getMessage());
        }
    }

    @PostMapping("/disable")
    public ResultT<Map<String, String>> disable(@RequestBody UserDisableDTO request) {
        try {
            userService.disable(request.getAccount());
            return ResultT.success(Map.of("msg", "用户已禁用"));
        } catch (Exception e) {
            return ResultT.error(e.getMessage());
        }
    }

    @PostMapping("/delete")
    public ResultT<Map<String, String>> delete(@RequestBody UserDeleteDTO request) {
        try {
            userService.logicalDelete(request.getAccount());
            return ResultT.success(Map.of("msg", "用户已注销（逻辑删除）"));
        } catch (Exception e) {
            return ResultT.error(e.getMessage());
        }
    }

    @PostMapping("/list")
    public ResultT<List<SysUserVO>> list(@RequestBody UserQueryDTO request) {
        try {
            List<SysUser> users = userService.list(request.getName(), request.getEmail(),
                    request.getPage() == null ? 1 : request.getPage(),
                    request.getSize() == null ? 10 : request.getSize());
            List<SysUserVO> vos = users.stream().map(u -> SysUserVO.builder()
                    .id(u.getId())
                    .username(u.getUsername())
                    .email(u.getEmail())
                    .status(u.getStatus())
                    .deleted(u.getDeleted())
                    .createdAt(u.getCreatedAt())
                    .updatedAt(u.getUpdatedAt())
                    .build()).toList();
            return ResultT.success(vos);
        } catch (Exception e) {
            return ResultT.error(e.getMessage());
        }
    }
}
