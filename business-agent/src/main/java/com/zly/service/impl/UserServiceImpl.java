package com.zly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zly.mapper.SysUserMapper;
import com.zly.model.entity.SysUser;
import com.zly.service.IUserService;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements IUserService {

    @Autowired
    private RedissonClient redissonClient;

    @Value("${auth.token-ttl-hours:2}")
    private int tokenTtlHours;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Random random = new SecureRandom();

    @Override
    public String generateCaptcha(String account) {
        String code = randomCode(4);
        RBucket<String> bucket = redissonClient.getBucket(captchaKey(account));
        bucket.set(code, 5, TimeUnit.MINUTES); // 5分钟有效期
        return code;
    }

    @Override
    public boolean verifyCaptcha(String account, String code) {
        RBucket<String> bucket = redissonClient.getBucket(captchaKey(account));
        String saved = bucket.get();
        return saved != null && saved.equalsIgnoreCase(code);
    }

    @Override
    public long register(String username, String email, String rawPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名必填");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("密码至少6位");
        }
        
        boolean existsUsername = lambdaQuery().eq(SysUser::getUsername, username).exists();
        if (existsUsername) {
            throw new IllegalArgumentException("用户名已存在");
        }
        
        if (email != null && !email.isBlank()) {
            boolean existsEmail = lambdaQuery().eq(SysUser::getEmail, email).exists();
            if (existsEmail) {
                throw new IllegalArgumentException("邮箱已存在");
            }
        }
        
        String hash = passwordEncoder.encode(rawPassword);
        SysUser user = SysUser.builder()
                .userNo("U" + System.currentTimeMillis())
                .username(username)
                .email(email)
                .password(hash)
                .status(1)
                .gender(0)
                .build();
        user.setIsDelete(0);
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());
        
        save(user);
        return user.getId();
    }

    @Override
    public String login(String account, String rawPassword, String captcha) {
        SysUser user = lambdaQuery()
                .and(w -> w.eq(SysUser::getUsername, account).or().eq(SysUser::getEmail, account))
                .one();
                
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        if (user.getIsDelete() != null && user.getIsDelete() == 1) {
            throw new IllegalStateException("用户已注销");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalStateException("用户已被禁用");
        }
        if (!verifyCaptcha(account, captcha)) {
            throw new IllegalArgumentException("验证码不正确或已过期");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("密码不正确");
        }
        String token = createToken();
        RBucket<String> bucket = redissonClient.getBucket(tokenKey(token), StringCodec.INSTANCE);
        bucket.set(String.valueOf(user.getId()), tokenTtlHours, TimeUnit.HOURS);
        
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);
        
        return token;
    }

    @Override
    public void resetPassword(String account) {
        String newHash = passwordEncoder.encode("abc~123456");
        boolean updated = lambdaUpdate()
                .set(SysUser::getPassword, newHash)
                .set(SysUser::getUpdatedTime, LocalDateTime.now())
                .and(w -> w.eq(SysUser::getUsername, account).or().eq(SysUser::getEmail, account))
                .update();
                
        if (!updated) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    @Override
    public void disable(String account) {
        boolean updated = lambdaUpdate()
                .set(SysUser::getStatus, 0)
                .set(SysUser::getUpdatedTime, LocalDateTime.now())
                .and(w -> w.eq(SysUser::getUsername, account).or().eq(SysUser::getEmail, account))
                .update();
                
        if (!updated) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    @Override
    public void logicalDelete(String account) {
        boolean updated = lambdaUpdate()
                .set(SysUser::getIsDelete, 1)
                .set(SysUser::getUpdatedTime, LocalDateTime.now())
                .and(w -> w.eq(SysUser::getUsername, account).or().eq(SysUser::getEmail, account))
                .update();
                
        if (!updated) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    @Override
    public List<SysUser> list(String name, String email, int page, int size) {
        Page<SysUser> p = new Page<>(page, size);
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
        query.eq(SysUser::getIsDelete, 0);
        
        if (StringUtils.hasText(name)) {
            query.like(SysUser::getUsername, name);
        }
        if (StringUtils.hasText(email)) {
            query.like(SysUser::getEmail, email);
        }
        query.orderByDesc(SysUser::getId);
        
        Page<SysUser> result = page(p, query);
        return result.getRecords();
    }

    private String captchaKey(String account) {
        return "business:captcha:" + account;
    }

    private String tokenKey(String token) {
        return "business:token:" + token;
    }

    private String randomCode(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String createToken() {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
