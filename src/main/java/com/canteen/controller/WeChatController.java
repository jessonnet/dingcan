package com.canteen.controller;

import com.canteen.config.WeChatConfig;
import com.canteen.dto.LoginRequest;
import com.canteen.dto.LoginResponse;
import com.canteen.entity.User;
import com.canteen.mapper.UserMapper;
import com.canteen.utils.JwtUtils;
import com.canteen.service.WeChatAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/wechat")
public class WeChatController {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WeChatController.class);
    
    @Autowired
    private WeChatConfig weChatConfig;
    
    @Autowired
    private WeChatAuthService weChatAuthService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @GetMapping("/auth/url")
    public ResponseEntity<Map<String, Object>> getAuthUrl() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("authUrl", weChatAuthService.getAuthUrl());
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> wechatCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state) {
        try {
            log.info("微信授权回调，code: {}, state: {}", code, state);
            
            Map<String, Object> loginResult = weChatAuthService.wechatLogin(code);
            
            return ResponseEntity.ok(loginResult);
            
        } catch (Exception e) {
            log.error("微信授权回调失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "微信授权失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> wechatLogin(@RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");
            
            if (code == null || code.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "授权码不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            Map<String, Object> loginResult = weChatAuthService.wechatLogin(code);
            
            return ResponseEntity.ok(loginResult);
            
        } catch (Exception e) {
            log.error("微信登录失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "微信登录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    @PostMapping("/bind")
    public ResponseEntity<Map<String, Object>> bindUser(@RequestBody Map<String, String> request) {
        try {
            String openid = request.get("openid");
            String username = request.get("username");
            String password = request.get("password");
            
            if (openid == null || openid.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "OpenID不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            if (username == null || username.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "用户名不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            if (password == null || password.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "密码不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            Map<String, Object> bindResult = weChatAuthService.bindUser(openid, username, password);
            
            return ResponseEntity.ok(bindResult);
            
        } catch (Exception e) {
            log.error("绑定微信账号失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "绑定微信账号失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    @GetMapping("/check-browser")
    public ResponseEntity<Map<String, Object>> checkBrowser(@RequestHeader("User-Agent") String userAgent) {
        Map<String, Object> result = new HashMap<>();
        
        String userAgentLower = userAgent.toLowerCase();
        boolean isWeChat = userAgentLower.contains("micromessenger");
        boolean isHarmonyOS = userAgentLower.contains("harmonyos") || 
                              userAgentLower.contains("huaweibrowser") || 
                              userAgentLower.contains("huawei");
        
        result.put("success", true);
        result.put("isWeChat", isWeChat);
        result.put("isHarmonyOS", isHarmonyOS);
        result.put("userAgent", userAgent);
        
        return ResponseEntity.ok(result);
    }
}
