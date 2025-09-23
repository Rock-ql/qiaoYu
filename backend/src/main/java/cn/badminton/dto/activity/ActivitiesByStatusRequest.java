package cn.badminton.dto.activity;

import jakarta.validation.constraints.NotNull;

/**
 * 按状态查询活动请求
 * 作者: xiaolei
 */
public class ActivitiesByStatusRequest {
    @NotNull
    private Integer status;

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}

