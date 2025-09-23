package cn.badminton.dto.activity;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 按时间范围查询活动请求
 * 作者: xiaolei
 */
@Data
public class ActivitiesByTimeRangeRequest {
    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;
}
