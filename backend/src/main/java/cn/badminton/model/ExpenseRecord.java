package cn.badminton.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 费用记录实体模型
 * MySQL存储 + Redis缓存
 *
 * 作者: xiaolei
 */
@Entity
@Table(name = "expense_record",
       indexes = {
           @Index(name = "idx_activity_id", columnList = "activity_id"),
           @Index(name = "idx_payer_id", columnList = "payer_id"),
           @Index(name = "idx_type", columnList = "type"),
           @Index(name = "idx_created_at", columnList = "created_at")
       })
public class ExpenseRecord extends BaseEntity {
    
    /**
     * 关联的活动ID
     */
    @Column(name = "activity_id", length = 36, nullable = false)
    @NotBlank(message = "活动ID不能为空")
    private String activityId;

    /**
     * 付款人用户ID
     */
    @Column(name = "payer_id", length = 36, nullable = false)
    @NotBlank(message = "付款人ID不能为空")
    private String payerId;

    /**
     * 费用类型：venue-场地费 food-餐饮费 transport-交通费 other-其他
     */
    @Column(name = "type", length = 20, nullable = false)
    @NotBlank(message = "费用类型不能为空")
    @Pattern(regexp = "^(venue|food|transport|other)$", message = "费用类型无效")
    private String type;

    /**
     * 费用描述
     */
    @Column(name = "description", length = 200, nullable = false)
    @NotBlank(message = "费用描述不能为空")
    @Size(max = 200, message = "费用描述长度不能超过200个字符")
    private String description;

    /**
     * 总金额
     * 最小值0.01，最多2位小数
     */
    @Column(name = "total_amount", nullable = false, precision = 8, scale = 2)
    @NotNull(message = "总金额不能为空")
    @DecimalMin(value = "0.01", message = "总金额必须大于0.01")
    @Digits(integer = 8, fraction = 2, message = "总金额格式不正确")
    private BigDecimal totalAmount;

    /**
     * 分摊方式：equal-平均分摊 custom-自定义分摊
     */
    @Column(name = "split_method", length = 20, nullable = false)
    @NotBlank(message = "分摊方式不能为空")
    @Pattern(regexp = "^(equal|custom)$", message = "分摊方式无效")
    private String splitMethod = "equal";

    public ExpenseRecord() {
        super();
    }

    public ExpenseRecord(String activityId, String payerId, String type, String description, BigDecimal totalAmount) {
        super();
        this.activityId = activityId;
        this.payerId = payerId;
        this.type = type;
        this.description = description;
        this.totalAmount = totalAmount;
    }

    // Getter和Setter方法
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
        updateTimestamp();
    }

    public String getSplitMethod() {
        return splitMethod;
    }

    public void setSplitMethod(String splitMethod) {
        this.splitMethod = splitMethod;
        updateTimestamp();
    }

    /**
     * 检查是否平均分摊
     */
    public boolean isEqualSplit() {
        return "equal".equals(splitMethod);
    }

    /**
     * 检查是否自定义分摊
     */
    public boolean isCustomSplit() {
        return "custom".equals(splitMethod);
    }
    
    /**
     * 检查费用类型
     */
    public boolean isVenueFee() {
        return "venue".equals(type);
    }
    
    public boolean isFoodFee() {
        return "food".equals(type);
    }
    
    public boolean isTransportFee() {
        return "transport".equals(type);
    }
    
    public boolean isOtherFee() {
        return "other".equals(type);
    }
    
    /**
     * 获取费用类型显示名称
     */
    public String getTypeDisplayName() {
        switch (type) {
            case "venue": return "场地费";
            case "food": return "餐饮费";
            case "transport": return "交通费";
            case "other": return "其他费用";
            default: return "未知费用";
        }
    }
    
    /**
     * 获取分摊方式显示名称
     */
    public String getSplitMethodDisplayName() {
        switch (splitMethod) {
            case "equal": return "平均分摊";
            case "custom": return "自定义分摊";
            default: return "未知方式";
        }
    }

    @Override
    public String toString() {
        return "ExpenseRecord{" +
                "id='" + getId() + '\'' +
                ", activityId='" + activityId + '\'' +
                ", payerId='" + payerId + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", totalAmount=" + totalAmount +
                ", splitMethod='" + splitMethod + '\'' +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}