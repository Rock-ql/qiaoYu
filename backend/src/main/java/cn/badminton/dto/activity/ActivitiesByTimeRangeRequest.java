package cn.badminton.dto.activity;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 按时间范围查询活动请求
 * 作者: xiaolei
 */
public class ActivitiesByTimeRangeRequest {
    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}

