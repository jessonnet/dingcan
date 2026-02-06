package com.canteen.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
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
    
    public String getAuthUrl() {
        return authUrl + "?appid=" + appId 
            + "&redirect_uri=" + redirectUri 
            + "&response_type=code" 
            + "&scope=" + scope 
            + "&state=" + state 
            + "#wechat_redirect";
    }
    
    public String getCallbackUrl() {
        return redirectUri;
    }
}
