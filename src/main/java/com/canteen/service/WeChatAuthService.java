package com.canteen.service;

import com.canteen.config.WeChatConfig;
import com.canteen.dto.WeChatAuthResponse;
import com.canteen.dto.WeChatUserInfo;
import com.canteen.entity.WeChatUser;
import com.canteen.mapper.WeChatUserMapper;
import com.canteen.mapper.UserMapper;
import com.canteen.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WeChatAuthService {
    
    @Autowired
    private WeChatConfig weChatConfig;
    
    @Autowired
    private WeChatUserMapper weChatUserMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public String getAuthUrl() {
        if (!systemConfigService.isWeChatLoginEnabled()) {
            log.warn("微信登录功能已禁用");
            throw new RuntimeException("微信登录功能已禁用");
        }
        return weChatConfig.getAuthUrl();
    }
    
    private WeChatAuthResponse getAccessToken(String code) {
        try {
            String url = weChatConfig.getTokenUrl() + 
                "?appid=" + weChatConfig.getAppId() +
                "&secret=" + weChatConfig.getAppSecret() +
                "&code=" + code +
                "&grant_type=authorization_code";
            
            log.info("请求微信access_token: {}", url);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String body = response.getBody();
            
            log.info("微信access_token响应: {}", body);
            
            WeChatAuthResponse authResponse = objectMapper.readValue(body, WeChatAuthResponse.class);
            
            if (authResponse.getErrcode() != null && !authResponse.getErrcode().equals("0")) {
                log.error("获取微信access_token失败: {}", authResponse.getErrmsg());
                throw new RuntimeException("获取微信access_token失败: " + authResponse.getErrmsg());
            }
            
            return authResponse;
        } catch (Exception e) {
            log.error("获取微信access_token异常", e);
            throw new RuntimeException("获取微信access_token失败: " + e.getMessage());
        }
    }
    
    private WeChatUserInfo getUserInfo(String accessToken, String openid) {
        try {
            String url = weChatConfig.getUserInfoUrl() + 
                "?access_token=" + accessToken +
                "&openid=" + openid +
                "&lang=zh_CN";
            
            log.info("请求微信用户信息: {}", url);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String body = response.getBody();
            
            log.info("微信用户信息响应: {}", body);
            
            WeChatUserInfo userInfo = objectMapper.readValue(body, WeChatUserInfo.class);
            
            if (userInfo.getErrcode() != null && !userInfo.getErrcode().equals("0")) {
                log.error("获取微信用户信息失败: {}", userInfo.getErrmsg());
                throw new RuntimeException("获取微信用户信息失败: " + userInfo.getErrmsg());
            }
            
            return userInfo;
        } catch (Exception e) {
            log.error("获取微信用户信息异常", e);
            throw new RuntimeException("获取微信用户信息失败: " + e.getMessage());
        }
    }
    
    public Map<String, Object> wechatLogin(String code) {
        try {
            if (!systemConfigService.isWeChatLoginEnabled()) {
                log.warn("微信登录功能已禁用");
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "微信登录功能已禁用，请使用账号密码登录");
                result.put("fallbackToPassword", true);
                return result;
            }
            
            log.info("开始微信登录流程，code: {}", code);
            
            WeChatAuthResponse authResponse = getAccessToken(code);
            String openid = authResponse.getOpenid();
            
            WeChatUserInfo userInfo = getUserInfo(authResponse.getAccess_token(), openid);
            
            WeChatUser weChatUser = weChatUserMapper.findByOpenid(openid);
            
            if (weChatUser != null && weChatUser.getUserId() != null) {
                log.info("微信用户已绑定，openid: {}, userId: {}", openid, weChatUser.getUserId());
                
                User user = userMapper.findById(weChatUser.getUserId());
                
                if (user != null) {
                    weChatUser.setNickname(userInfo.getNickname());
                    weChatUser.setAvatar(userInfo.getHeadimgurl());
                    weChatUser.setGender(userInfo.getSex());
                    weChatUser.setCountry(userInfo.getCountry());
                    weChatUser.setProvince(userInfo.getProvince());
                    weChatUser.setCity(userInfo.getCity());
                    weChatUser.setLastLoginTime(LocalDateTime.now());
                    weChatUserMapper.updateByOpenid(weChatUser);
                    
                    log.info("微信登录成功，用户: {}", user.getUsername());
                    
                    String token = jwtService.generateToken(user.getUsername());
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("token", token);
                    result.put("user", user);
                    result.put("needBind", false);
                    
                    return result;
                }
            }
            
            log.info("微信用户未绑定，返回绑定信息，openid: {}", openid);
            
            if (weChatUser == null) {
                weChatUser = new WeChatUser();
                weChatUser.setOpenid(openid);
                weChatUser.setUnionid(userInfo.getUnionid());
                weChatUser.setNickname(userInfo.getNickname());
                weChatUser.setAvatar(userInfo.getHeadimgurl());
                weChatUser.setGender(userInfo.getSex());
                weChatUser.setCountry(userInfo.getCountry());
                weChatUser.setProvince(userInfo.getProvince());
                weChatUser.setCity(userInfo.getCity());
                weChatUser.setLanguage("zh_CN");
                weChatUser.setSubscribeStatus(1);
                weChatUser.setLastLoginTime(LocalDateTime.now());
                
                weChatUserMapper.insert(weChatUser);
                
                log.info("微信用户信息保存成功，ID: {}", weChatUser.getId());
            } else {
                weChatUser.setNickname(userInfo.getNickname());
                weChatUser.setAvatar(userInfo.getHeadimgurl());
                weChatUser.setGender(userInfo.getSex());
                weChatUser.setCountry(userInfo.getCountry());
                weChatUser.setProvince(userInfo.getProvince());
                weChatUser.setCity(userInfo.getCity());
                weChatUser.setLastLoginTime(LocalDateTime.now());
                weChatUserMapper.updateByOpenid(weChatUser);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("needBind", true);
            result.put("openid", openid);
            result.put("nickname", userInfo.getNickname());
            result.put("avatar", userInfo.getHeadimgurl());
            
            return result;
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("微信登录功能已禁用")) {
                throw e;
            }
            log.error("微信登录失败", e);
            throw new RuntimeException("微信登录失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("微信登录失败", e);
            throw new RuntimeException("微信登录失败: " + e.getMessage());
        }
    }
    
    public Map<String, Object> bindUser(String openid, String username, String password) {
        try {
            log.info("绑定微信账号，openid: {}, username: {}", openid, username);
            
            User user = userMapper.findByUsername(username);
            
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            if (!user.getPassword().equals(password)) {
                throw new RuntimeException("密码错误");
            }
            
            WeChatUser weChatUser = weChatUserMapper.findByOpenid(openid);
            
            if (weChatUser == null) {
                throw new RuntimeException("微信用户不存在");
            }
            
            if (weChatUser.getUserId() != null) {
                throw new RuntimeException("该微信账号已绑定其他账号");
            }
            
            weChatUserMapper.bindUser(openid, user.getId());
            
            log.info("微信账号绑定成功，openid: {}, userId: {}", openid, user.getId());
            
            String token = jwtService.generateToken(user.getUsername());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("token", token);
            result.put("user", user);
            
            return result;
            
        } catch (Exception e) {
            log.error("绑定微信账号失败", e);
            throw new RuntimeException("绑定微信账号失败: " + e.getMessage());
        }
    }
}
