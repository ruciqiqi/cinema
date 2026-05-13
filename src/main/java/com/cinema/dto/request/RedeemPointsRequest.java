package com.cinema.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "积分兑换请求")
public class RedeemPointsRequest {
    @Schema(description = "积分数量", required = true, example = "100")
    private Integer points;

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}