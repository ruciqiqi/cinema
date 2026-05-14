package com.cinema.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "实名认证请求")
public class RealNameAuthRequest {
    @Schema(description = "真实姓名", required = true)
    private String realName;
    
    @Schema(description = "身份证号", required = true)
    private String idCard;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }
}