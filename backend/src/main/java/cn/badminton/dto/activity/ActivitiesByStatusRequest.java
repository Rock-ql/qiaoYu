package cn.badminton.dto.activity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 按状态查询活动请求
 * 作者: xiaolei
 */
@Data
public class ActivitiesByStatusRequest {
    @NotNull
    private Integer status;
}
