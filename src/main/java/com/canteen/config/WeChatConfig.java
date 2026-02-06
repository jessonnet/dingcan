package com.canteen.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wechat")
public class WeChatConfig {
    
    private String appId;
    private String appSecret;
    private String redirectUri;
    private String scope = "snsapi_userinfo";
    private String state = "STATE";
    private String tokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token";
    private String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo";
    private String authUrl = "https://open.weixin.qq.com/connect/oauth2/authorize";
    
    public String getAppId() {
        return appId;
    }
    
    public void setAppId(String appId) {
        this.appId = appId;
    }
    
    public String getAppSecret() {
        return appSecret;
    }
    
    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }
    
    public String getRedirectUri() {
        return redirectUri;
    }
    
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getTokenUrl() {
        return tokenUrl;
    }
    
    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }
    
    public String getUserInfoUrl() {
        return userInfoUrl;
    }
    
    public void setUserInfoUrl(String userInfoUrl) {
        this.userInfoUrl = userInfoUrl;
    }
    
    public String getAuthUrl() {
        return authUrl + "?appid=" + appId 
            + "&redirect_uri=" + redirectUri 
            + "&response_type=code" 
            + "&scope=" + scope 
            + "&state=" + state 
            + "#wechat_redirect";
    }
    
    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }
    
    public String getCallbackUrl() {
        return redirectUri;
    }
}
