package cn.badminton.model;

import java.time.LocalDateTime;

/**
 * 费用分摊实体模型
 * Redis存储结构: badminton:share:{share_id} (Hash)
 * 
 * 作者: xiaolei
 */
public class ExpenseShare {
    
    private String id;                  // 分摊记录唯一标识
    private String expenseId;           // 关联的费用记录ID
    private String userId;              // 分摊用户ID
    private Double shareAmount;         // 分摊金额
    private Integer status;             // 分摊状态: 1-待确认 2-已确认 3-已支付
    private String remark;              // 分摊备注
    private LocalDateTime confirmedAt;  // 确认时间
    private LocalDateTime paidAt;       // 支付时间
    private LocalDateTime createdAt;    // 创建时间
    private LocalDateTime updatedAt;    // 更新时间

    // 状态常量
    public static final int STATUS_PENDING = 1;        // 待确认
    public static final int STATUS_CONFIRMED = 2;      // 已确认
    public static final int STATUS_PAID = 3;           // 已支付

    // 默认构造函数
    public ExpenseShare() {
        this.status = STATUS_PENDING;
        this.shareAmount = 0.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 带参构造函数
    public ExpenseShare(String expenseId, String userId, Double shareAmount) {
        this();
        this.expenseId = expenseId;
        this.userId = userId;
        this.shareAmount = shareAmount;
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(String expenseId) {
        this.expenseId = expenseId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getShareAmount() {
        return shareAmount;
    }

    public void setShareAmount(Double shareAmount) {
        this.shareAmount = shareAmount;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
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
     * 确认分摊
     */
    public void confirm() {
        this.status = STATUS_CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 标记为已支付
     */
    public void markAsPaid() {
        this.status = STATUS_PAID;
        this.paidAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查是否待确认
     */
    public boolean isPending() {
        return status == STATUS_PENDING;
    }

    /**
     * 检查是否已确认
     */
    public boolean isConfirmed() {
        return status == STATUS_CONFIRMED;
    }

    /**
     * 检查是否已支付
     */
    public boolean isPaid() {
        return status == STATUS_PAID;
    }

    /**
     * 检查是否可以确认
     */
    public boolean canConfirm() {
        return status == STATUS_PENDING;
    }

    /**
     * 检查是否可以支付
     */
    public boolean canPay() {
        return status == STATUS_CONFIRMED;
    }

    /**
     * 验证分摊记录数据
     */
    public boolean isValid() {
        return expenseId != null && !expenseId.isEmpty()
            && userId != null && !userId.isEmpty()
            && shareAmount != null && shareAmount > 0;
    }

    @Override
    public String toString() {
        return "ExpenseShare{" +
                "id='" + id + '\'' +
                ", expenseId='" + expenseId + '\'' +
                ", userId='" + userId + '\'' +
                ", shareAmount=" + shareAmount +
                ", status=" + status +
                ", confirmedAt=" + confirmedAt +
                ", paidAt=" + paidAt +
                '}';
    }
}