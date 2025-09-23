package cn.badminton.repository;

import cn.badminton.model.ExpenseRecord;
import cn.badminton.model.ExpenseShare;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 费用Redis存储库
 * 存储结构: 
 * - badminton:expense:{expense_id} (Hash)
 * - badminton:share:{share_id} (Hash)
 * 
 * 作者: xiaolei
 */
@Repository
public class ExpenseRepository {

    private static final String EXPENSE_KEY_PREFIX = "badminton:expense:";
    private static final String SHARE_KEY_PREFIX = "badminton:share:";
    private static final String ACTIVITY_EXPENSE_INDEX = "badminton:expense:activity:";
    private static final String EXPENSE_SHARE_INDEX = "badminton:share:expense:";
    private static final String USER_SHARE_INDEX = "badminton:share:user:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private HashOperations<String, String, Object> hashOps;

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOps = redisTemplate.opsForHash();
    }

    // ==================== 费用记录相关 ====================

    /**
     * 保存费用记录
     */
    public ExpenseRecord saveExpense(ExpenseRecord expense) {
        if (expense.getId() == null || expense.getId().isEmpty()) {
            expense.setId(UUID.randomUUID().toString());
        }
        expense.setUpdatedAt(LocalDateTime.now());

        String key = EXPENSE_KEY_PREFIX + expense.getId();
        
        // 保存费用记录数据
        hashOps.put(key, "id", expense.getId());
        hashOps.put(key, "activityId", expense.getActivityId());
        hashOps.put(key, "payerId", expense.getPayerId());
        hashOps.put(key, "title", expense.getTitle());
        hashOps.put(key, "description", expense.getDescription() != null ? expense.getDescription() : "");
        hashOps.put(key, "totalAmount", expense.getTotalAmount().toString());
        hashOps.put(key, "shareType", expense.getShareType().toString());
        hashOps.put(key, "status", expense.getStatus().toString());
        hashOps.put(key, "expenseTime", expense.getExpenseTime().toString());
        hashOps.put(key, "createdAt", expense.getCreatedAt().toString());
        hashOps.put(key, "updatedAt", expense.getUpdatedAt().toString());

        // 创建活动索引
        redisTemplate.opsForSet().add(ACTIVITY_EXPENSE_INDEX + expense.getActivityId(), expense.getId());

        return expense;
    }

    /**
     * 根据ID查找费用记录
     */
    public ExpenseRecord findExpenseById(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        String key = EXPENSE_KEY_PREFIX + id;
        Map<String, Object> expenseMap = hashOps.entries(key);
        
        if (expenseMap.isEmpty()) {
            return null;
        }

        return mapToExpense(expenseMap);
    }

    /**
     * 根据活动ID查找费用记录
     */
    public List<ExpenseRecord> findExpensesByActivityId(String activityId) {
        if (activityId == null || activityId.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Object> expenseIds = redisTemplate.opsForSet().members(ACTIVITY_EXPENSE_INDEX + activityId);
        List<ExpenseRecord> expenses = new ArrayList<>();
        
        if (expenseIds != null) {
            for (Object expenseId : expenseIds) {
                ExpenseRecord expense = findExpenseById((String) expenseId);
                if (expense != null) {
                    expenses.add(expense);
                }
            }
        }
        
        return expenses;
    }

    /**
     * 删除费用记录
     */
    public void deleteExpenseById(String id) {
        if (id == null || id.isEmpty()) {
            return;
        }

        ExpenseRecord expense = findExpenseById(id);
        if (expense != null) {
            // 删除活动索引
            redisTemplate.opsForSet().remove(ACTIVITY_EXPENSE_INDEX + expense.getActivityId(), id);
            
            // 删除相关的分摊记录
            List<ExpenseShare> shares = findSharesByExpenseId(id);
            for (ExpenseShare share : shares) {
                deleteShareById(share.getId());
            }
        }

        // 删除费用记录数据
        redisTemplate.delete(EXPENSE_KEY_PREFIX + id);
    }

    // ==================== 费用分摊相关 ====================

    /**
     * 保存费用分摊
     */
    public ExpenseShare saveShare(ExpenseShare share) {
        if (share.getId() == null || share.getId().isEmpty()) {
            share.setId(UUID.randomUUID().toString());
        }
        share.setUpdatedAt(LocalDateTime.now());

        String key = SHARE_KEY_PREFIX + share.getId();
        
        // 保存分摊数据
        hashOps.put(key, "id", share.getId());
        hashOps.put(key, "expenseId", share.getExpenseId());
        hashOps.put(key, "userId", share.getUserId());
        hashOps.put(key, "shareAmount", share.getShareAmount().toString());
        hashOps.put(key, "status", share.getStatus().toString());
        hashOps.put(key, "remark", share.getRemark() != null ? share.getRemark() : "");
        hashOps.put(key, "confirmedAt", share.getConfirmedAt() != null ? share.getConfirmedAt().toString() : "");
        hashOps.put(key, "paidAt", share.getPaidAt() != null ? share.getPaidAt().toString() : "");
        hashOps.put(key, "createdAt", share.getCreatedAt().toString());
        hashOps.put(key, "updatedAt", share.getUpdatedAt().toString());

        // 创建索引
        redisTemplate.opsForSet().add(EXPENSE_SHARE_INDEX + share.getExpenseId(), share.getId());
        redisTemplate.opsForSet().add(USER_SHARE_INDEX + share.getUserId(), share.getId());

        return share;
    }

    /**
     * 根据ID查找费用分摊
     */
    public ExpenseShare findShareById(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        String key = SHARE_KEY_PREFIX + id;
        Map<String, Object> shareMap = hashOps.entries(key);
        
        if (shareMap.isEmpty()) {
            return null;
        }

        return mapToShare(shareMap);
    }

    /**
     * 根据费用记录ID查找分摊记录
     */
    public List<ExpenseShare> findSharesByExpenseId(String expenseId) {
        if (expenseId == null || expenseId.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Object> shareIds = redisTemplate.opsForSet().members(EXPENSE_SHARE_INDEX + expenseId);
        List<ExpenseShare> shares = new ArrayList<>();
        
        if (shareIds != null) {
            for (Object shareId : shareIds) {
                ExpenseShare share = findShareById((String) shareId);
                if (share != null) {
                    shares.add(share);
                }
            }
        }
        
        return shares;
    }

    /**
     * 根据用户ID查找分摊记录
     */
    public List<ExpenseShare> findSharesByUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Object> shareIds = redisTemplate.opsForSet().members(USER_SHARE_INDEX + userId);
        List<ExpenseShare> shares = new ArrayList<>();
        
        if (shareIds != null) {
            for (Object shareId : shareIds) {
                ExpenseShare share = findShareById((String) shareId);
                if (share != null) {
                    shares.add(share);
                }
            }
        }
        
        return shares;
    }

    /**
     * 删除费用分摊
     */
    public void deleteShareById(String id) {
        if (id == null || id.isEmpty()) {
            return;
        }

        ExpenseShare share = findShareById(id);
        if (share != null) {
            // 删除索引
            redisTemplate.opsForSet().remove(EXPENSE_SHARE_INDEX + share.getExpenseId(), id);
            redisTemplate.opsForSet().remove(USER_SHARE_INDEX + share.getUserId(), id);
        }

        // 删除分摊数据
        redisTemplate.delete(SHARE_KEY_PREFIX + id);
    }

    /**
     * 更新费用状态
     */
    public void updateExpenseStatus(String id, Integer status) {
        if (id == null || id.isEmpty()) {
            return;
        }
        
        String key = EXPENSE_KEY_PREFIX + id;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            hashOps.put(key, "status", status.toString());
            hashOps.put(key, "updatedAt", LocalDateTime.now().toString());
        }
    }

    /**
     * 更新分摊状态
     */
    public void updateShareStatus(String id, Integer status) {
        if (id == null || id.isEmpty()) {
            return;
        }
        
        String key = SHARE_KEY_PREFIX + id;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            hashOps.put(key, "status", status.toString());
            hashOps.put(key, "updatedAt", LocalDateTime.now().toString());
            
            if (status == ExpenseShare.STATUS_CONFIRMED) {
                hashOps.put(key, "confirmedAt", LocalDateTime.now().toString());
            } else if (status == ExpenseShare.STATUS_PAID) {
                hashOps.put(key, "paidAt", LocalDateTime.now().toString());
            }
        }
    }

    /**
     * 转换Map为ExpenseRecord对象
     */
    private ExpenseRecord mapToExpense(Map<String, Object> expenseMap) {
        ExpenseRecord expense = new ExpenseRecord();
        
        expense.setId((String) expenseMap.get("id"));
        expense.setActivityId((String) expenseMap.get("activityId"));
        expense.setPayerId((String) expenseMap.get("payerId"));
        expense.setTitle((String) expenseMap.get("title"));
        expense.setDescription((String) expenseMap.get("description"));
        expense.setTotalAmount(Double.valueOf((String) expenseMap.get("totalAmount")));
        expense.setShareType(Integer.valueOf((String) expenseMap.get("shareType")));
        expense.setStatus(Integer.valueOf((String) expenseMap.get("status")));
        expense.setExpenseTime(LocalDateTime.parse((String) expenseMap.get("expenseTime")));
        expense.setCreatedAt(LocalDateTime.parse((String) expenseMap.get("createdAt")));
        expense.setUpdatedAt(LocalDateTime.parse((String) expenseMap.get("updatedAt")));
        
        return expense;
    }

    /**
     * 转换Map为ExpenseShare对象
     */
    private ExpenseShare mapToShare(Map<String, Object> shareMap) {
        ExpenseShare share = new ExpenseShare();
        
        share.setId((String) shareMap.get("id"));
        share.setExpenseId((String) shareMap.get("expenseId"));
        share.setUserId((String) shareMap.get("userId"));
        share.setShareAmount(Double.valueOf((String) shareMap.get("shareAmount")));
        share.setStatus(Integer.valueOf((String) shareMap.get("status")));
        share.setRemark((String) shareMap.get("remark"));
        share.setCreatedAt(LocalDateTime.parse((String) shareMap.get("createdAt")));
        share.setUpdatedAt(LocalDateTime.parse((String) shareMap.get("updatedAt")));
        
        String confirmedAt = (String) shareMap.get("confirmedAt");
        if (confirmedAt != null && !confirmedAt.isEmpty()) {
            share.setConfirmedAt(LocalDateTime.parse(confirmedAt));
        }
        
        String paidAt = (String) shareMap.get("paidAt");
        if (paidAt != null && !paidAt.isEmpty()) {
            share.setPaidAt(LocalDateTime.parse(paidAt));
        }
        
        return share;
    }
}