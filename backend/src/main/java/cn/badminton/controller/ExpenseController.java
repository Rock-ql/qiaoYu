package cn.badminton.controller;

import cn.badminton.common.Result;
import cn.badminton.dto.common.ActivityIdRequest;
import cn.badminton.dto.common.ExpenseIdRequest;
import cn.badminton.dto.common.UserIdRequest;
import cn.badminton.dto.expense.ConfirmShareRequest;
import cn.badminton.dto.expense.CreateExpenseRequest;
import cn.badminton.dto.expense.CreateExpenseSharesRequest;
import cn.badminton.dto.expense.DeleteExpenseRequest;
import cn.badminton.dto.expense.MarkPaidRequest;
import cn.badminton.model.ExpenseRecord;
import cn.badminton.model.ExpenseShare;
import cn.badminton.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 费用控制器
 * 作者: xiaolei
 */
@RestController
@Slf4j
@RequestMapping("/api/expense")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping("/create")
    public Result<ExpenseRecord> create(@Valid @RequestBody CreateExpenseRequest req) {
        log.info("[Expense] 创建入参: activityId={}, payerId={}, title={}, amount={}", req.getActivityId(), req.getPayerId(), req.getTitle(), req.getTotalAmount());
        ExpenseRecord er = expenseService.createExpense(req.getActivityId(), req.getPayerId(), req.getTitle(), req.getTotalAmount(), req.getDescription(), req.getShareType());
        return Result.ok(er);
    }

    @PostMapping("/shares")
    public Result<List<ExpenseShare>> shares(@Valid @RequestBody CreateExpenseSharesRequest req) {
        log.info("[Expense] 分摊入参: expenseId={}, participants={}", req.getExpenseId(), req.getParticipantIds().size());
        List<ExpenseShare> list = expenseService.createExpenseShares(req.getExpenseId(), req.getParticipantIds(), req.getCustomAmounts());
        return Result.ok(list);
    }

    @PostMapping("/confirm")
    public Result<Void> confirm(@Valid @RequestBody ConfirmShareRequest req) {
        log.info("[Expense] 确认分摊: shareId={}, userId={}", req.getShareId(), req.getUserId());
        expenseService.confirmShare(req.getShareId(), req.getUserId());
        return Result.ok();
    }

    @PostMapping("/paid")
    public Result<Void> paid(@Valid @RequestBody MarkPaidRequest req) {
        log.info("[Expense] 标记已支付: shareId={}, userId={}", req.getShareId(), req.getUserId());
        expenseService.markAsPaid(req.getShareId(), req.getUserId());
        return Result.ok();
    }

    @PostMapping("/activityExpenses")
    public Result<List<ExpenseRecord>> activityExpenses(@Valid @RequestBody ActivityIdRequest req) {
        return Result.ok(expenseService.getActivityExpenses(req.getActivityId()));
    }

    @PostMapping("/expenseShares")
    public Result<List<ExpenseShare>> expenseShares(@Valid @RequestBody ExpenseIdRequest req) {
        return Result.ok(expenseService.getExpenseShares(req.getExpenseId()));
    }

    @PostMapping("/userShares")
    public Result<List<ExpenseShare>> userShares(@Valid @RequestBody UserIdRequest req) {
        return Result.ok(expenseService.getUserShares(req.getUserId()));
    }

    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody DeleteExpenseRequest req) {
        log.info("[Expense] 删除费用: expenseId={}, userId={}", req.getExpenseId(), req.getUserId());
        expenseService.deleteExpense(req.getExpenseId(), req.getUserId());
        return Result.ok();
    }
}
