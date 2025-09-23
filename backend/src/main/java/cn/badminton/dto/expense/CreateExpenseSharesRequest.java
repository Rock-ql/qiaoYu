package cn.badminton.dto.expense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 创建费用分摊请求
 * 作者: xiaolei
 */
public class CreateExpenseSharesRequest {
    @NotBlank
    private String expenseId;

    @NotEmpty
    private List<String> participantIds;

    // 当 shareType=2(自定义) 时使用：userId -> 金额
    private Map<String, BigDecimal> customAmounts;

    public String getExpenseId() { return expenseId; }
    public void setExpenseId(String expenseId) { this.expenseId = expenseId; }
    public List<String> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }
    public Map<String, BigDecimal> getCustomAmounts() { return customAmounts; }
    public void setCustomAmounts(Map<String, BigDecimal> customAmounts) { this.customAmounts = customAmounts; }
}

