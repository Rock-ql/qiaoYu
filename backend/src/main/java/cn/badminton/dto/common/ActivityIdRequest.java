package cn.badminton.dto.common;

import jakarta.validation.constraints.NotBlank;

/**
 * 活动ID请求
 * 作者: xiaolei
 */
public class ActivityIdRequest {
    @NotBlank
    private String activityId;

    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }
}

