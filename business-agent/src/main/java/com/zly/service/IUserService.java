package com.zly.service;

import com.zly.model.entity.SysUser;
import java.util.List;

public interface IUserService {
    String generateCaptcha(String account);
    boolean verifyCaptcha(String account, String code);
    long register(String username, String email, String rawPassword);
    String login(String account, String rawPassword, String captcha);
    void resetPassword(String account);
    void disable(String account);
    void logicalDelete(String account);
    List<SysUser> list(String name, String email, int page, int size);
}
