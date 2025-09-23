package cn.badminton.dto.expense;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 创建费用请求
 * 作者: xiaolei
 */
public class CreateExpenseRequest {
    @NotBlank
    private String activityId;

    @NotBlank
    private String payerId;

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal totalAmount;

    private String description = "";

    // 1 平均分摊；2 自定义
    private Integer shareType = 1;

    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }
    public String getPayerId() { return payerId; }
    public void setPayerId(String payerId) { this.payerId = payerId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description == null ? "" : description; }
    public Integer getShareType() { return shareType; }
    public void setShareType(Integer shareType) { this.shareType = shareType; }
}

