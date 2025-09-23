package cn.badminton.dto.common;

import jakarta.validation.constraints.NotBlank;

/**
 * 费用ID请求
 * 作者: xiaolei
 */
public class ExpenseIdRequest {
    @NotBlank
    private String expenseId;

    public String getExpenseId() { return expenseId; }
    public void setExpenseId(String expenseId) { this.expenseId = expenseId; }
}

