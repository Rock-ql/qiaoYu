package cn.badminton.service;

import cn.badminton.model.BookingActivity;
import cn.badminton.model.ExpenseRecord;
import cn.badminton.model.ExpenseShare;
import cn.badminton.repository.ExpenseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 费用管理服务
 * 负责活动费用记录、分摊计算等核心业务逻辑
 * 
 * 作者: xiaolei
 */
@Service
public class ExpenseService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private ActivityService activityService;
    
    @Autowired
    private UserService userService;

    /**
     * 创建费用记录
     */
    public ExpenseRecord createExpense(String activityId, String payerId, String title, 
                                     BigDecimal totalAmount, String description, Integer shareType) {
        logger.info("创建费用记录，活动ID: {}, 付款人: {}, 标题: {}, 金额: {}", activityId, payerId, title, totalAmount);
        
        try {
            // 参数验证
            if (activityId == null || activityId.trim().isEmpty()) {
                throw new IllegalArgumentException("活动ID不能为空");
            }
            
            if (payerId == null || payerId.trim().isEmpty()) {
                throw new IllegalArgumentException("付款人不能为空");
            }
            
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("费用标题不能为空");
            }
            
            if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("费用金额必须大于0");
            }
            
            // 验证活动是否存在
            BookingActivity activity = activityService.getActivityById(activityId);
            if (activity == null) {
                throw new IllegalArgumentException("活动不存在");
            }
            
            // 验证付款人是否存在
            if (userService.findById(payerId) == null) {
                throw new IllegalArgumentException("付款人不存在");
            }
            
            // 创建费用记录
            ExpenseRecord expense = new ExpenseRecord(activityId, payerId, "other", description != null ? description.trim() : title.trim(), totalAmount);
            if (shareType != null && shareType == 1) {
                expense.setSplitMethod("equal");
            } else if (shareType != null && shareType == 2) {
                expense.setSplitMethod("custom");
            }
            
            expense = expenseRepository.saveExpense(expense);
            
            logger.info("创建费用记录成功，费用ID: {}", expense.getId());
            return expense;
            
        } catch (Exception e) {
            logger.error("创建费用记录失败，活动ID: {}, 错误信息: {}", activityId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 计算并创建费用分摊
     */
    public List<ExpenseShare> createExpenseShares(String expenseId, List<String> participantIds, 
                                                Map<String, BigDecimal> customAmounts) {
        logger.info("创建费用分摊，费用ID: {}, 参与人数: {}", expenseId, participantIds.size());
        
        try {
            // 获取费用记录
            ExpenseRecord expense = expenseRepository.findExpenseById(expenseId);
            if (expense == null) {
                throw new IllegalArgumentException("费用记录不存在");
            }
            
            if (expense.getState() != 1) { // state=1表示上架/可用状态
                throw new IllegalArgumentException("费用记录状态不正确，不能分摊");
            }
            
            if (participantIds == null || participantIds.isEmpty()) {
                throw new IllegalArgumentException("参与分摊的用户不能为空");
            }
            
            // 验证所有参与者是否存在
            for (String userId : participantIds) {
                if (userService.findById(userId) == null) {
                    throw new IllegalArgumentException("用户不存在: " + userId);
                }
            }
            
            List<ExpenseShare> shares = new ArrayList<>();
            BigDecimal totalAmount = expense.getTotalAmount();
            
            if (expense.isEqualSplit()) {
                // 平均分摊
                shares = createAverageShares(expenseId, participantIds, totalAmount);
                
            } else if (expense.isCustomSplit()) {
                // 自定义分摊
                shares = createCustomShares(expenseId, participantIds, customAmounts, totalAmount);
            } else {
                // 默认平均分摊
                shares = createAverageShares(expenseId, participantIds, totalAmount);
            }
            
            // 保存分摊记录
            for (ExpenseShare share : shares) {
                expenseRepository.saveShare(share);
            }
            
            // 费用记录已经保存，无需更新状态
            
            logger.info("创建费用分摊成功，费用ID: {}, 分摊记录数: {}", expenseId, shares.size());
            return shares;
            
        } catch (Exception e) {
            logger.error("创建费用分摊失败，费用ID: {}, 错误信息: {}", expenseId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 确认分摊
     */
    public void confirmShare(String shareId, String userId) {
        logger.info("确认分摊，分摊ID: {}, 用户ID: {}", shareId, userId);
        
        try {
            ExpenseShare share = expenseRepository.findShareById(shareId);
            if (share == null) {
                throw new IllegalArgumentException("分摊记录不存在");
            }
            
            // 验证权限
            if (!share.getUserId().equals(userId)) {
                throw new IllegalArgumentException("只能确认自己的分摊记录");
            }
            
            if (!share.canSettle()) {
                throw new IllegalArgumentException("分摊记录不可结算");
            }
            
            share.settle();
            expenseRepository.saveShare(share);
            
            logger.info("确认分摊成功，分摊ID: {}", shareId);
            
        } catch (Exception e) {
            logger.error("确认分摊失败，分摊ID: {}, 用户ID: {}, 错误信息: {}", shareId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 标记为已支付
     */
    public void markAsPaid(String shareId, String userId) {
        logger.info("标记为已支付，分摊ID: {}, 用户ID: {}", shareId, userId);
        
        try {
            ExpenseShare share = expenseRepository.findShareById(shareId);
            if (share == null) {
                throw new IllegalArgumentException("分摊记录不存在");
            }
            
            // 验证权限（分摊者或付款人都可以标记）
            ExpenseRecord expense = expenseRepository.findExpenseById(share.getExpenseId());
            if (!share.getUserId().equals(userId) && !expense.getPayerId().equals(userId)) {
                throw new IllegalArgumentException("只有分摊者或付款人可以标记为已支付");
            }
            
            if (!share.canSettle()) {
                throw new IllegalArgumentException("分摊记录不可结算");
            }
            
            share.settle();
            expenseRepository.saveShare(share);
            
            // 增加用户消费金额
            userService.addUserExpense(share.getUserId(), share.getAmount());
            
            logger.info("标记为已支付成功，分摊ID: {}", shareId);
            
        } catch (Exception e) {
            logger.error("标记为已支付失败，分摊ID: {}, 用户ID: {}, 错误信息: {}", shareId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取活动的费用记录
     */
    public List<ExpenseRecord> getActivityExpenses(String activityId) {
        logger.debug("获取活动的费用记录，活动ID: {}", activityId);
        
        try {
            List<ExpenseRecord> expenses = expenseRepository.findExpensesByActivityId(activityId);
            logger.debug("获取活动费用记录成功，记录数: {}", expenses.size());
            return expenses;
            
        } catch (Exception e) {
            logger.error("获取活动费用记录失败，活动ID: {}, 错误信息: {}", activityId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取费用的分摊记录
     */
    public List<ExpenseShare> getExpenseShares(String expenseId) {
        logger.debug("获取费用的分摊记录，费用ID: {}", expenseId);
        
        try {
            List<ExpenseShare> shares = expenseRepository.findSharesByExpenseId(expenseId);
            logger.debug("获取费用分摊记录成功，记录数: {}", shares.size());
            return shares;
            
        } catch (Exception e) {
            logger.error("获取费用分摊记录失败，费用ID: {}, 错误信息: {}", expenseId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取用户的分摊记录
     */
    public List<ExpenseShare> getUserShares(String userId) {
        logger.debug("获取用户的分摊记录，用户ID: {}", userId);
        
        try {
            List<ExpenseShare> shares = expenseRepository.findSharesByUserId(userId);
            logger.debug("获取用户分摊记录成功，记录数: {}", shares.size());
            return shares;
            
        } catch (Exception e) {
            logger.error("获取用户分摊记录失败，用户ID: {}, 错误信息: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 删除费用记录
     */
    public void deleteExpense(String expenseId, String userId) {
        logger.info("删除费用记录，费用ID: {}, 用户ID: {}", expenseId, userId);
        
        try {
            ExpenseRecord expense = expenseRepository.findExpenseById(expenseId);
            if (expense == null) {
                throw new IllegalArgumentException("费用记录不存在");
            }
            
            // 验证权限（只有付款人可以删除）
            if (!expense.getPayerId().equals(userId)) {
                throw new IllegalArgumentException("只有付款人可以删除费用记录");
            }
            
            // 检查状态（只有正常状态的记录可以删除）
            if (expense.getState() != 1) {
                throw new IllegalArgumentException("费用记录状态不正确，不能删除");
            }
            
            expenseRepository.deleteExpenseById(expenseId);
            
            logger.info("删除费用记录成功，费用ID: {}", expenseId);
            
        } catch (Exception e) {
            logger.error("删除费用记录失败，费用ID: {}, 用户ID: {}, 错误信息: {}", expenseId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 创建平均分摊
     */
    private List<ExpenseShare> createAverageShares(String expenseId, List<String> participantIds, BigDecimal totalAmount) {
        List<ExpenseShare> shares = new ArrayList<>();
        int participantCount = participantIds.size();
        
        // 计算平均金额（保留2位小数）
        BigDecimal averageAmount = totalAmount.divide(BigDecimal.valueOf(participantCount), 2, RoundingMode.DOWN);
        
        // 计算余额
        BigDecimal remainder = totalAmount.subtract(averageAmount.multiply(BigDecimal.valueOf(participantCount)));
        
        for (int i = 0; i < participantIds.size(); i++) {
            String userId = participantIds.get(i);
            BigDecimal shareAmount = averageAmount;
            
            // 将余额分配给前几个参与者
            if (i < remainder.multiply(BigDecimal.valueOf(100)).intValue()) {
                shareAmount = shareAmount.add(BigDecimal.valueOf(0.01));
            }
            
            ExpenseShare share = new ExpenseShare(expenseId, userId, shareAmount);
            shares.add(share);
        }
        
        return shares;
    }

    /**
     * 创建自定义分摊
     */
    private List<ExpenseShare> createCustomShares(String expenseId, List<String> participantIds, 
                                                Map<String, BigDecimal> customAmounts, BigDecimal totalAmount) {
        if (customAmounts == null || customAmounts.isEmpty()) {
            throw new IllegalArgumentException("自定义分摊金额不能为空");
        }
        
        List<ExpenseShare> shares = new ArrayList<>();
        BigDecimal customTotal = BigDecimal.ZERO;
        
        // 计算自定义金额总和
        for (String userId : participantIds) {
            BigDecimal amount = customAmounts.get(userId);
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("用户 " + userId + " 的分摊金额无效");
            }
            customTotal = customTotal.add(amount);
        }
        
        // 验证总金额是否匹配
        if (customTotal.compareTo(totalAmount) != 0) {
            throw new IllegalArgumentException("自定义分摊金额总和与费用总额不匹配");
        }
        
        // 创建分摊记录
        for (String userId : participantIds) {
            BigDecimal amount = customAmounts.get(userId);
            ExpenseShare share = new ExpenseShare(expenseId, userId, amount);
            shares.add(share);
        }
        
        return shares;
    }

    /**
     * 根据ID获取费用记录
     */
    public ExpenseRecord getExpenseById(String expenseId) {
        logger.debug("根据ID获取费用记录: {}", expenseId);
        
        try {
            ExpenseRecord expense = expenseRepository.findExpenseById(expenseId);
            logger.debug("获取费用记录结果: {}", expense != null ? "找到" : "未找到");
            return expense;
            
        } catch (Exception e) {
            logger.error("根据ID获取费用记录失败，费用ID: {}, 错误信息: {}", expenseId, e.getMessage(), e);
            return null;
        }
    }
}