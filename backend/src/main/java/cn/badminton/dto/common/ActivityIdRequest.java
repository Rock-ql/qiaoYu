package cn.badminton.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 活动ID请求
 * 作者: xiaolei
 */
@Data
public class ActivityIdRequest {
    @NotBlank
    private String activityId;
}
