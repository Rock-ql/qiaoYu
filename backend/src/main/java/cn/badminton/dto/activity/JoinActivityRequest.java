package cn.badminton.dto.activity;

import jakarta.validation.constraints.NotBlank;

/**
 * 参加活动请求
 * 作者: xiaolei
 */
public class JoinActivityRequest {
    @NotBlank
    private String activityId;

    @NotBlank
    private String userId;

    private String remark = "";

    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark == null ? "" : remark; }
}

