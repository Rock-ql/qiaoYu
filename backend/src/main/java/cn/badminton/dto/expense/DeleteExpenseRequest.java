package cn.badminton.dto.expense;

import jakarta.validation.constraints.NotBlank;

/**
 * 删除费用请求
 * 作者: xiaolei
 */
public class DeleteExpenseRequest {
    @NotBlank
    private String expenseId;

    @NotBlank
    private String userId;

    public String getExpenseId() { return expenseId; }
    public void setExpenseId(String expenseId) { this.expenseId = expenseId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}

