package com.canteen.dto;

import lombok.Data;

@Data
public class WeChatUserInfo {
    private String openid;
    private String nickname;
    private Integer sex;
    private String province;
    private String city;
    private String country;
    private String headimgurl;
    private String privilege;
    private String unionid;
    private String errcode;
    private String errmsg;
}
