package com.canteen.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LoginSecurityService {
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String LOGIN_ATTEMPT_PREFIX = "login_attempt:";
    private static final String LOGIN_LOCK_PREFIX = "login_lock:";
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;
    
    public boolean isAccountLocked(String username) {
        if (redisTemplate == null) {
            return false;
        }
        
        String key = LOGIN_LOCK_PREFIX + username;
        Boolean isLocked = redisTemplate.hasKey(key);
        
        if (Boolean.TRUE.equals(isLocked)) {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.MINUTES);
            log.warn("账户已锁定，用户名: {}, 剩余锁定时间: {} 分钟", username, ttl);
            return true;
        }
        
        return false;
    }
    
    public void recordLoginAttempt(String username, boolean success) {
        if (redisTemplate == null) {
            return;
        }
        
        if (success) {
            String attemptKey = LOGIN_ATTEMPT_PREFIX + username;
            redisTemplate.delete(attemptKey);
            log.info("登录成功，清除失败记录，用户名: {}", username);
        } else {
            String attemptKey = LOGIN_ATTEMPT_PREFIX + username;
            Long attempts = redisTemplate.opsForValue().increment(attemptKey);
            
            if (attempts == 1) {
                redisTemplate.expire(attemptKey, Duration.ofMinutes(30));
            }
            
            log.warn("登录失败，用户名: {}, 失败次数: {}", username, attempts);
            
            if (attempts >= MAX_ATTEMPTS) {
                lockAccount(username);
            }
        }
    }
    
    private void lockAccount(String username) {
        String lockKey = LOGIN_LOCK_PREFIX + username;
        redisTemplate.opsForValue().set(lockKey, "locked", Duration.ofMinutes(LOCK_DURATION_MINUTES));
        
        log.error("账户已锁定，用户名: {}, 锁定时长: {} 分钟", username, LOCK_DURATION_MINUTES);
    }
    
    public void unlockAccount(String username) {
        if (redisTemplate == null) {
            return;
        }
        
        String lockKey = LOGIN_LOCK_PREFIX + username;
        String attemptKey = LOGIN_ATTEMPT_PREFIX + username;
        
        redisTemplate.delete(lockKey);
        redisTemplate.delete(attemptKey);
        
        log.info("账户已解锁，用户名: {}", username);
    }
    
    public int getRemainingAttempts(String username) {
        if (redisTemplate == null) {
            return MAX_ATTEMPTS;
        }
        
        String attemptKey = LOGIN_ATTEMPT_PREFIX + username;
        Object attempts = redisTemplate.opsForValue().get(attemptKey);
        
        if (attempts == null) {
            return MAX_ATTEMPTS;
        }
        
        return MAX_ATTEMPTS - Integer.parseInt(attempts.toString());
    }
    
    public boolean validatePasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        boolean hasLetter = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        
        return hasLetter && hasDigit;
    }
}
