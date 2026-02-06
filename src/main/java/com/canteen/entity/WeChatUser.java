package com.canteen.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WeChatUser {
    private Long id;
    private Long userId;
    private String openid;
    private String unionid;
    private String nickname;
    private String avatar;
    private Integer gender;
    private String country;
    private String province;
    private String city;
    private String language;
    private String phone;
    private Integer subscribeStatus;
    private LocalDateTime subscribeTime;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
