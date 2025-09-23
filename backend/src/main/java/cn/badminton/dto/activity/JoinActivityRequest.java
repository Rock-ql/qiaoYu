package cn.badminton.dto.activity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 参加活动请求
 * 作者: xiaolei
 */
@Data
public class JoinActivityRequest {
    @NotBlank
    private String activityId;

    @NotBlank
    private String userId;

    private String remark = "";

    public void setRemark(String remark) { this.remark = remark == null ? "" : remark; }
}
