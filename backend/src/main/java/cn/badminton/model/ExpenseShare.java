package cn.badminton.model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 费用分摊实体模型
 * Redis存储结构: badminton:share:{share_id} (Hash)
 * 
 * 作者: xiaolei
 */
public class ExpenseShare extends BaseEntity {
    
    /**
     * 关联的费用记录ID
     */
    @NotBlank(message = "费用记录ID不能为空")
    private String expenseId;
    
    /**
     * 分摊用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    
    /**
     * 分摊金额
     * 最小值0.01，最多2位小数
     */
    @NotNull(message = "分摊金额不能为空")
    @DecimalMin(value = "0.01", message = "分摊金额必须大于0")
    @Digits(integer = 8, fraction = 2, message = "分摊金额格式不正确")
    private BigDecimal amount;
    
    /**
     * 分摊状态：1-待结算 2-已结算
     */
    @NotNull(message = "分摊状态不能为空")
    @Min(value = 1, message = "分摊状态值无效")
    @Max(value = 2, message = "分摊状态值无效")
    private Integer status = STATUS_PENDING;
    
    /**
     * 结算时间
     */
    private LocalDateTime settledAt;

    // 状态常量
    public static final int STATUS_PENDING = 1;    // 待结算
    public static final int STATUS_SETTLED = 2;    // 已结算

    public ExpenseShare() {
        super();
    }

    public ExpenseShare(String expenseId, String userId, BigDecimal amount) {
        super();
        this.expenseId = expenseId;
        this.userId = userId;
        this.amount = amount;
    }

    // Getter和Setter方法
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
        updateTimestamp();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
        updateTimestamp();
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(LocalDateTime settledAt) {
        this.settledAt = settledAt;
    }

    /**
     * 结算分摊
     */
    public void settle() {
        if (canSettle()) {
            this.status = STATUS_SETTLED;
            this.settledAt = LocalDateTime.now();
            updateTimestamp();
        }
    }

    /**
     * 检查是否待结算
     */
    public boolean isPending() {
        return status == STATUS_PENDING;
    }

    /**
     * 检查是否已结算
     */
    public boolean isSettled() {
        return status == STATUS_SETTLED;
    }

    /**
     * 检查是否可以结算
     */
    public boolean canSettle() {
        return status == STATUS_PENDING;
    }
    
    /**
     * 重新设为待结算状态
     */
    public void resetToPending() {
        if (isSettled()) {
            this.status = STATUS_PENDING;
            this.settledAt = null;
            updateTimestamp();
        }
    }
    
    /**
     * 获取状态显示名称
     */
    public String getStatusDisplayName() {
        switch (status) {
            case STATUS_PENDING: return "待结算";
            case STATUS_SETTLED: return "已结算";
            default: return "未知状态";
        }
    }

    @Override
    public String toString() {
        return "ExpenseShare{" +
                "id='" + getId() + '\'' +
                ", expenseId='" + expenseId + '\'' +
                ", userId='" + userId + '\'' +
                ", amount=" + amount +
                ", status=" + status +
                ", settledAt=" + settledAt +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}