package com.canteen.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoginSecurityService {
    
    private static final Logger log = LoggerFactory.getLogger(LoginSecurityService.class);
    
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;
    
    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lockedAccounts = new ConcurrentHashMap<>();
    
    public boolean isAccountLocked(String username) {
        LocalDateTime lockTime = lockedAccounts.get(username);
        
        if (lockTime == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutesPassed = ChronoUnit.MINUTES.between(lockTime, now);
        
        if (minutesPassed >= LOCK_DURATION_MINUTES) {
            unlockAccount(username);
            return false;
        }
        
        long remainingMinutes = LOCK_DURATION_MINUTES - minutesPassed;
        log.warn("账户已锁定，用户名: {}, 剩余锁定时间: {} 分钟", username, remainingMinutes);
        return true;
    }
    
    public void recordLoginAttempt(String username, boolean success) {
        if (success) {
            loginAttempts.remove(username);
            log.info("登录成功，清除失败记录，用户名: {}", username);
        } else {
            Integer attempts = loginAttempts.getOrDefault(username, 0) + 1;
            loginAttempts.put(username, attempts);
            
            log.warn("登录失败，用户名: {}, 失败次数: {}", username, attempts);
            
            if (attempts >= MAX_ATTEMPTS) {
                lockAccount(username);
            }
        }
    }
    
    private void lockAccount(String username) {
        lockedAccounts.put(username, LocalDateTime.now());
        
        log.error("账户已锁定，用户名: {}, 锁定时长: {} 分钟", username, LOCK_DURATION_MINUTES);
    }
    
    public void unlockAccount(String username) {
        lockedAccounts.remove(username);
        loginAttempts.remove(username);
        
        log.info("账户已解锁，用户名: {}", username);
    }
    
    public int getRemainingAttempts(String username) {
        Integer attempts = loginAttempts.getOrDefault(username, 0);
        return MAX_ATTEMPTS - attempts;
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
