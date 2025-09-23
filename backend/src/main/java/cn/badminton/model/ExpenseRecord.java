package cn.badminton.model;

import java.time.LocalDateTime;

/**
 * 费用记录实体模型
 * Redis存储结构: badminton:expense:{expense_id} (Hash)
 * 
 * 作者: xiaolei
 */
public class ExpenseRecord {
    
    private String id;                  // 费用记录唯一标识
    private String activityId;          // 关联的活动ID
    private String payerId;             // 付款人用户ID
    private String title;               // 费用标题
    private String description;         // 费用描述
    private Double totalAmount;         // 总金额
    private Integer shareType;          // 分摊类型: 1-平均分摊 2-按人分摊 3-自定义分摊
    private Integer status;             // 状态: 1-待分摊 2-已分摊 3-已完成
    private LocalDateTime expenseTime;  // 消费时间
    private LocalDateTime createdAt;    // 创建时间
    private LocalDateTime updatedAt;    // 更新时间

    // 分摊类型常量
    public static final int SHARE_TYPE_AVERAGE = 1;    // 平均分摊
    public static final int SHARE_TYPE_BY_PERSON = 2;  // 按人分摊
    public static final int SHARE_TYPE_CUSTOM = 3;     // 自定义分摊

    // 状态常量
    public static final int STATUS_PENDING = 1;        // 待分摊
    public static final int STATUS_SHARED = 2;         // 已分摊
    public static final int STATUS_COMPLETED = 3;      // 已完成

    // 默认构造函数
    public ExpenseRecord() {
        this.shareType = SHARE_TYPE_AVERAGE;
        this.status = STATUS_PENDING;
        this.totalAmount = 0.0;
        this.expenseTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 带参构造函数
    public ExpenseRecord(String activityId, String payerId, String title, Double totalAmount) {
        this();
        this.activityId = activityId;
        this.payerId = payerId;
        this.title = title;
        this.totalAmount = totalAmount;
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

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getShareType() {
        return shareType;
    }

    public void setShareType(Integer shareType) {
        this.shareType = shareType;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getExpenseTime() {
        return expenseTime;
    }

    public void setExpenseTime(LocalDateTime expenseTime) {
        this.expenseTime = expenseTime;
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
     * 标记为已分摊
     */
    public void markAsShared() {
        this.status = STATUS_SHARED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 标记为已完成
     */
    public void markAsCompleted() {
        this.status = STATUS_COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查是否可以分摊
     */
    public boolean canShare() {
        return status == STATUS_PENDING;
    }

    /**
     * 检查是否已分摊
     */
    public boolean isShared() {
        return status == STATUS_SHARED;
    }

    /**
     * 检查是否已完成
     */
    public boolean isCompleted() {
        return status == STATUS_COMPLETED;
    }

    /**
     * 检查是否平均分摊
     */
    public boolean isAverageShare() {
        return shareType == SHARE_TYPE_AVERAGE;
    }

    /**
     * 检查是否按人分摊
     */
    public boolean isByPersonShare() {
        return shareType == SHARE_TYPE_BY_PERSON;
    }

    /**
     * 检查是否自定义分摊
     */
    public boolean isCustomShare() {
        return shareType == SHARE_TYPE_CUSTOM;
    }

    /**
     * 验证费用记录数据
     */
    public boolean isValid() {
        return activityId != null && !activityId.isEmpty()
            && payerId != null && !payerId.isEmpty()
            && title != null && !title.isEmpty()
            && totalAmount != null && totalAmount > 0;
    }

    @Override
    public String toString() {
        return "ExpenseRecord{" +
                "id='" + id + '\'' +
                ", activityId='" + activityId + '\'' +
                ", payerId='" + payerId + '\'' +
                ", title='" + title + '\'' +
                ", totalAmount=" + totalAmount +
                ", shareType=" + shareType +
                ", status=" + status +
                ", expenseTime=" + expenseTime +
                '}';
    }
}