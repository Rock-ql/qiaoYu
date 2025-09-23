package cn.badminton.model;

import java.time.LocalDateTime;

/**
 * 参与记录实体模型
 * Redis存储结构: badminton:participation:{participation_id} (Hash)
 * 
 * 作者: xiaolei
 */
public class Participation {
    
    private String id;                  // 参与记录唯一标识
    private String activityId;          // 关联的活动ID
    private String userId;              // 参与者用户ID
    private Integer status;             // 参与状态: 1-已确认 2-已取消 3-已完成
    private String joinRemark;          // 参与备注
    private Double actualFee;           // 实际费用分摊
    private LocalDateTime joinTime;     // 参与时间
    private LocalDateTime cancelTime;   // 取消时间
    private LocalDateTime createdAt;    // 创建时间
    private LocalDateTime updatedAt;    // 更新时间

    // 状态常量
    public static final int STATUS_CONFIRMED = 1;   // 已确认
    public static final int STATUS_CANCELLED = 2;   // 已取消
    public static final int STATUS_COMPLETED = 3;   // 已完成

    // 默认构造函数
    public Participation() {
        this.status = STATUS_CONFIRMED;
        this.actualFee = 0.0;
        this.joinTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 带参构造函数
    public Participation(String activityId, String userId) {
        this();
        this.activityId = activityId;
        this.userId = userId;
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
        this.updatedAt = LocalDateTime.now();
    }

    public String getJoinRemark() {
        return joinRemark;
    }

    public void setJoinRemark(String joinRemark) {
        this.joinRemark = joinRemark;
    }

    public Double getActualFee() {
        return actualFee;
    }

    public void setActualFee(Double actualFee) {
        this.actualFee = actualFee;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }

    public LocalDateTime getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 取消参与
     */
    public void cancel() {
        this.status = STATUS_CANCELLED;
        this.cancelTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 完成参与
     */
    public void complete() {
        this.status = STATUS_COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查是否可以取消
     */
    public boolean canCancel() {
        return status == STATUS_CONFIRMED;
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
     * 检查是否已完成
     */
    public boolean isCompleted() {
        return status == STATUS_COMPLETED;
    }

    /**
     * 验证参与记录数据
     */
    public boolean isValid() {
        return activityId != null && !activityId.isEmpty()
            && userId != null && !userId.isEmpty();
    }

    @Override
    public String toString() {
        return "Participation{" +
                "id='" + id + '\'' +
                ", activityId='" + activityId + '\'' +
                ", userId='" + userId + '\'' +
                ", status=" + status +
                ", actualFee=" + actualFee +
                ", joinTime=" + joinTime +
                '}';
    }
}