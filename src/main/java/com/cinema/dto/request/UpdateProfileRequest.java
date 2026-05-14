package com.cinema.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "更新用户信息请求")
public class UpdateProfileRequest {
    @Schema(description = "用户昵称")
    private String nickname;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "头像URL")
    private String avatar;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}