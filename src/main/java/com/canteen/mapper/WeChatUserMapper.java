package com.canteen.mapper;

import com.canteen.entity.WeChatUser;
import org.apache.ibatis.annotations.*;

@Mapper
public interface WeChatUserMapper {
    
    @Select("SELECT * FROM wechat_user WHERE openid = #{openid}")
    WeChatUser findByOpenid(String openid);
    
    @Select("SELECT * FROM wechat_user WHERE user_id = #{userId}")
    WeChatUser findByUserId(Long userId);
    
    @Insert("INSERT INTO wechat_user (openid, unionid, nickname, avatar, gender, country, province, city, language, phone, subscribe_status, last_login_time) " +
            "VALUES (#{openid}, #{unionid}, #{nickname}, #{avatar}, #{gender}, #{country}, #{province}, #{city}, #{language}, #{phone}, #{subscribeStatus}, #{lastLoginTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WeChatUser weChatUser);
    
    @Update("UPDATE wechat_user SET user_id = #{userId}, nickname = #{nickname}, avatar = #{avatar}, gender = #{gender}, " +
            "country = #{country}, province = #{province}, city = #{city}, language = #{language}, " +
            "phone = #{phone}, last_login_time = #{lastLoginTime} WHERE openid = #{openid}")
    int updateByOpenid(WeChatUser weChatUser);
    
    @Update("UPDATE wechat_user SET user_id = #{userId} WHERE openid = #{openid}")
    int bindUser(String openid, Long userId);
}
