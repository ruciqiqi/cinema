package com.cinema.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "更新用户请求（管理员）")
public class UpdateUserRequest {
    @Schema(description = "用户角色")
    private String role;
    
    @Schema(description = "会员等级")
    private Integer memberLevel;
    
    @Schema(description = "积分")
    private Integer points;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getMemberLevel() {
        return memberLevel;
    }

    public void setMemberLevel(Integer memberLevel) {
        this.memberLevel = memberLevel;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}