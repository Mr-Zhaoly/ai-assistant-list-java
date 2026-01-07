package com.zly.service;

import com.zly.model.entity.SysUser;
import com.zly.repository.SysUserRepository;
import org.redisson.api.RedissonClient;
import org.redisson.api.RBucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private SysUserRepository repository;

    @Autowired
    private RedissonClient redissonClient;

    @Value("${auth.token-ttl-hours:2}")
    private int tokenTtlHours;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Random random = new SecureRandom();

    public String generateCaptcha(String account) {
        String code = randomCode(4);
        RBucket<String> bucket = redissonClient.getBucket(captchaKey(account));
        bucket.set(code, 5, java.util.concurrent.TimeUnit.MINUTES); // 5分钟有效期
        return code;
    }

    public boolean verifyCaptcha(String account, String code) {
        RBucket<String> bucket = redissonClient.getBucket(captchaKey(account));
        String saved = bucket.get();
        return saved != null && saved.equalsIgnoreCase(code);
    }

    public long register(String username, String email, String rawPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名必填");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("密码至少6位");
        }
        if (repository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (email != null && !email.isBlank() && repository.existsByEmail(email)) {
            throw new IllegalArgumentException("邮箱已存在");
        }
        String hash = passwordEncoder.encode(rawPassword);
        SysUser user = SysUser.builder()
                .username(username)
                .email(email)
                .passwordHash(hash)
                .status("ACTIVE")
                .deleted(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return repository.insert(user);
    }

    public String login(String account, String rawPassword, String captcha) {
        Optional<SysUser> opt = repository.findByUsernameOrEmail(account);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        SysUser user = opt.get();
        if (user.getDeleted() != null && user.getDeleted() == 1) {
            throw new IllegalStateException("用户已注销");
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalStateException("用户已被禁用");
        }
        if (!verifyCaptcha(account, captcha)) {
            throw new IllegalArgumentException("验证码不正确或已过期");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("密码不正确");
        }
        String token = createToken();
        RBucket<Long> bucket = redissonClient.getBucket(tokenKey(token));
        bucket.set(user.getId(), tokenTtlHours, java.util.concurrent.TimeUnit.HOURS);
        return token;
    }

    public void resetPassword(String account) {
        String newHash = passwordEncoder.encode("abc~123456");
        int updated = repository.updatePassword(account, newHash);
        if (updated == 0) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    public void disable(String account) {
        int updated = repository.disableUser(account);
        if (updated == 0) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    public void logicalDelete(String account) {
        int updated = repository.logicalDelete(account);
        if (updated == 0) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    public List<SysUser> list(String name, String email, int page, int size) {
        return repository.pageQuery(name, email, page, size);
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
