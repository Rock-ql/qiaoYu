package cn.badminton.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 参与记录实体模型
 * MySQL存储 + Redis缓存
 *
 * 作者: xiaolei
 */
@Entity
@Table(name = "participation",
       uniqueConstraints = {@UniqueConstraint(name = "uk_activity_user", columnNames = {"activity_id", "user_id"})},
       indexes = {
           @Index(name = "idx_activity_id", columnList = "activity_id"),
           @Index(name = "idx_user_id", columnList = "user_id"),
           @Index(name = "idx_status", columnList = "status"),
           @Index(name = "idx_join_time", columnList = "join_time")
       })
public class Participation extends BaseEntity {
    
    /**
     * 关联的活动ID
     */
    @Column(name = "activity_id", length = 36, nullable = false)
    @NotBlank(message = "活动ID不能为空")
    private String activityId;

    /**
     * 参与者用户ID
     */
    @Column(name = "user_id", length = 36, nullable = false)
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 参与状态：1-已确认 2-已取消
     */
    @Column(name = "status", nullable = false)
    @NotNull(message = "参与状态不能为空")
    @Min(value = 1, message = "参与状态值无效")
    @Max(value = 2, message = "参与状态值无效")
    private Integer status = STATUS_CONFIRMED;

    /**
     * 参与时间
     */
    @Column(name = "join_time", nullable = false)
    @NotNull(message = "参与时间不能为空")
    private LocalDateTime joinTime;

    /**
     * 是否为发起人
     */
    @Column(name = "is_organizer", nullable = false, columnDefinition = "tinyint(1)")
    @NotNull(message = "是否为发起人不能为空")
    private Boolean isOrganizer = false;

    // 状态常量
    public static final int STATUS_CONFIRMED = 1;   // 已确认
    public static final int STATUS_CANCELLED = 2;   // 已取消

    public Participation() {
        super();
        this.joinTime = LocalDateTime.now();
    }

    public Participation(String activityId, String userId) {
        super();
        this.activityId = activityId;
        this.userId = userId;
        this.joinTime = LocalDateTime.now();
    }
    
    public Participation(String activityId, String userId, boolean isOrganizer) {
        super();
        this.activityId = activityId;
        this.userId = userId;
        this.isOrganizer = isOrganizer;
        this.joinTime = LocalDateTime.now();
    }

    // Getter和Setter方法
    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
        updateTimestamp();
    }

    public LocalDateTime getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }
    
    public Boolean getIsOrganizer() {
        return isOrganizer;
    }
    
    public void setIsOrganizer(Boolean isOrganizer) {
        this.isOrganizer = isOrganizer;
    }

    /**
     * 取消参与
     */
    public void cancel() {
        if (canCancel()) {
            this.status = STATUS_CANCELLED;
            updateTimestamp();
        }
    }

    /**
     * 检查是否可以取消
     */
    public boolean canCancel() {
        return status == STATUS_CONFIRMED && !isOrganizer;
    }

    /**
     * 检查是否已确认
     */
    public boolean isConfirmed() {
        return status == STATUS_CONFIRMED;
    }

    /**
     * 检查是否已取消
     */
    public boolean isCancelled() {
        return status == STATUS_CANCELLED;
    }
    
    /**
     * 重新确认参与
     */
    public void reconfirm() {
        if (isCancelled()) {
            this.status = STATUS_CONFIRMED;
            this.joinTime = LocalDateTime.now();
            updateTimestamp();
        }
    }
    
    /**
     * 检查是否为活动发起人
     */
    public boolean isOrganizer() {
        return isOrganizer != null && isOrganizer;
    }

    @Override
    public String toString() {
        return "Participation{" +
                "id='" + getId() + '\'' +
                ", activityId='" + activityId + '\'' +
                ", userId='" + userId + '\'' +
                ", status=" + status +
                ", isOrganizer=" + isOrganizer +
                ", joinTime=" + joinTime +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}