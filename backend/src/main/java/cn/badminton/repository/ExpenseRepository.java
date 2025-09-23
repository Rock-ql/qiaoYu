package cn.badminton.repository;

import cn.badminton.config.RedisConfig;
import cn.badminton.model.ExpenseRecord;
import cn.badminton.model.ExpenseShare;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        expense.updateTimestamp();

        String key = RedisConfig.RedisKeys.expenseKey(expense.getId());
        
        // 保存费用记录数据到Hash
        Map<String, Object> expenseMap = convertExpenseToMap(expense);
        redisTemplate.opsForHash().putAll(key, expenseMap);
        
        // 设置过期时间
        redisTemplate.expire(key, RedisConfig.RedisTTL.ACTIVITY_CACHE, TimeUnit.SECONDS);

        // 创建活动索引
        createExpenseIndexes(expense);

        return expense;
    }

    /**
     * 根据ID查找费用记录
     */
    public ExpenseRecord findExpenseById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }

        String key = RedisConfig.RedisKeys.expenseKey(id);
        Map<Object, Object> expenseMap = redisTemplate.opsForHash().entries(key);
        
        if (expenseMap.isEmpty()) {
            return null;
        }

        return convertMapToExpense(expenseMap);
    }

    /**
     * 根据活动ID查找费用记录
     */
    public List<ExpenseRecord> findExpensesByActivityId(String activityId) {
        if (activityId == null || activityId.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String activityExpenseIndexKey = "badminton:index:activity_expense:" + activityId;
        Set<Object> expenseIds = redisTemplate.opsForSet().members(activityExpenseIndexKey);
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
        if (id == null || id.trim().isEmpty()) {
            return;
        }

        ExpenseRecord expense = findExpenseById(id);
        if (expense != null) {
            // 删除索引
            deleteExpenseIndexes(expense);
            
            // 删除相关的分摊记录
            List<ExpenseShare> shares = findSharesByExpenseId(id);
            for (ExpenseShare share : shares) {
                deleteShareById(share.getId());
            }
        }

        // 删除费用记录数据
        String expenseKey = RedisConfig.RedisKeys.expenseKey(id);
        String expenseSharesKey = RedisConfig.RedisKeys.EXPENSE_SHARES_PREFIX + id;
        
        redisTemplate.delete(expenseKey);
        redisTemplate.delete(expenseSharesKey);
    }

    // ==================== 费用分摊相关 ====================

    /**
     * 保存费用分摊
     */
    public ExpenseShare saveShare(ExpenseShare share) {
        if (share.getId() == null || share.getId().isEmpty()) {
            share.setId(UUID.randomUUID().toString());
        }
        share.updateTimestamp();

        String key = RedisConfig.RedisKeys.shareKey(share.getId());
        
        // 保存分摊数据到Hash
        Map<String, Object> shareMap = convertShareToMap(share);
        redisTemplate.opsForHash().putAll(key, shareMap);
        
        // 设置过期时间
        redisTemplate.expire(key, RedisConfig.RedisTTL.ACTIVITY_CACHE, TimeUnit.SECONDS);

        // 创建索引
        createShareIndexes(share);

        return share;
    }

    /**
     * 根据ID查找费用分摊
     */
    public ExpenseShare findShareById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }

        String key = RedisConfig.RedisKeys.shareKey(id);
        Map<Object, Object> shareMap = redisTemplate.opsForHash().entries(key);
        
        if (shareMap.isEmpty()) {
            return null;
        }

        return convertMapToShare(shareMap);
    }

    /**
     * 根据费用记录ID查找分摊记录
     */
    public List<ExpenseShare> findSharesByExpenseId(String expenseId) {
        if (expenseId == null || expenseId.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String expenseShareIndexKey = "badminton:index:expense_share:" + expenseId;
        Set<Object> shareIds = redisTemplate.opsForSet().members(expenseShareIndexKey);
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
        if (userId == null || userId.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String userShareIndexKey = "badminton:index:user_share:" + userId;
        Set<Object> shareIds = redisTemplate.opsForSet().members(userShareIndexKey);
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
        if (id == null || id.trim().isEmpty()) {
            return;
        }

        ExpenseShare share = findShareById(id);
        if (share != null) {
            // 删除索引
            deleteShareIndexes(share);
        }

        // 删除分摊数据
        redisTemplate.delete(RedisConfig.RedisKeys.shareKey(id));
    }

    /**
     * 更新分摊状态
     */
    public void updateShareStatus(String id, Integer status) {
        if (id == null || id.trim().isEmpty()) {
            return;
        }
        
        String key = RedisConfig.RedisKeys.shareKey(id);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            hashOps.put(key, "status", status.toString());
            hashOps.put(key, "updatedAt", LocalDateTime.now().toString());
            
            if (status == ExpenseShare.STATUS_SETTLED) {
                hashOps.put(key, "settledAt", LocalDateTime.now().toString());
            }
        }
    }

    /**
     * 统计用户总费用
     */
    public BigDecimal calculateUserTotalExpense(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<ExpenseShare> shares = findSharesByUserId(userId);
        return shares.stream()
                .filter(share -> share.getStatus() == ExpenseShare.STATUS_SETTLED)
                .map(ExpenseShare::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 创建费用索引
     */
    private void createExpenseIndexes(ExpenseRecord expense) {
        if (expense.getActivityId() != null && !expense.getActivityId().trim().isEmpty()) {
            String activityExpenseIndexKey = "badminton:index:activity_expense:" + expense.getActivityId();
            redisTemplate.opsForSet().add(activityExpenseIndexKey, expense.getId());
            redisTemplate.expire(activityExpenseIndexKey, RedisConfig.RedisTTL.ACTIVITY_CACHE, TimeUnit.SECONDS);
        }
    }

    /**
     * 删除费用索引
     */
    private void deleteExpenseIndexes(ExpenseRecord expense) {
        if (expense.getActivityId() != null && !expense.getActivityId().trim().isEmpty()) {
            String activityExpenseIndexKey = "badminton:index:activity_expense:" + expense.getActivityId();
            redisTemplate.opsForSet().remove(activityExpenseIndexKey, expense.getId());
        }
    }

    /**
     * 创建分摊索引
     */
    private void createShareIndexes(ExpenseShare share) {
        if (share.getExpenseId() != null && !share.getExpenseId().trim().isEmpty()) {
            String expenseShareIndexKey = "badminton:index:expense_share:" + share.getExpenseId();
            redisTemplate.opsForSet().add(expenseShareIndexKey, share.getId());
            redisTemplate.expire(expenseShareIndexKey, RedisConfig.RedisTTL.ACTIVITY_CACHE, TimeUnit.SECONDS);
        }
        
        if (share.getUserId() != null && !share.getUserId().trim().isEmpty()) {
            String userShareIndexKey = "badminton:index:user_share:" + share.getUserId();
            redisTemplate.opsForSet().add(userShareIndexKey, share.getId());
            redisTemplate.expire(userShareIndexKey, RedisConfig.RedisTTL.ACTIVITY_CACHE, TimeUnit.SECONDS);
        }
    }

    /**
     * 删除分摊索引
     */
    private void deleteShareIndexes(ExpenseShare share) {
        if (share.getExpenseId() != null && !share.getExpenseId().trim().isEmpty()) {
            String expenseShareIndexKey = "badminton:index:expense_share:" + share.getExpenseId();
            redisTemplate.opsForSet().remove(expenseShareIndexKey, share.getId());
        }
        
        if (share.getUserId() != null && !share.getUserId().trim().isEmpty()) {
            String userShareIndexKey = "badminton:index:user_share:" + share.getUserId();
            redisTemplate.opsForSet().remove(userShareIndexKey, share.getId());
        }
    }

    /**
     * 将ExpenseRecord对象转换为Map
     */
    private Map<String, Object> convertExpenseToMap(ExpenseRecord expense) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", expense.getId());
        map.put("activityId", expense.getActivityId());
        map.put("payerId", expense.getPayerId());
        map.put("type", expense.getType());
        map.put("description", expense.getDescription());
        map.put("totalAmount", expense.getTotalAmount().toString());
        map.put("splitMethod", expense.getSplitMethod());
        map.put("tenant", expense.getTenant());
        map.put("state", expense.getState());
        map.put("createdAt", expense.getCreatedAt().toString());
        map.put("updatedAt", expense.getUpdatedAt().toString());
        map.put("deletedAt", expense.getDeletedAt() != null ? expense.getDeletedAt().toString() : null);
        map.put("organizationId", expense.getOrganizationId());
        return map;
    }

    /**
     * 将Map转换为ExpenseRecord对象
     */
    private ExpenseRecord convertMapToExpense(Map<Object, Object> map) {
        ExpenseRecord expense = new ExpenseRecord();
        
        expense.setId((String) map.get("id"));
        expense.setActivityId((String) map.get("activityId"));
        expense.setPayerId((String) map.get("payerId"));
        expense.setType((String) map.get("type"));
        expense.setDescription((String) map.get("description"));
        
        Object totalAmountStr = map.get("totalAmount");
        if (totalAmountStr != null) {
            expense.setTotalAmount(new BigDecimal(totalAmountStr.toString()));
        }
        
        expense.setSplitMethod((String) map.get("splitMethod"));
        
        Object tenant = map.get("tenant");
        if (tenant instanceof Integer) {
            expense.setTenant((Integer) tenant);
        } else if (tenant instanceof String) {
            expense.setTenant(Integer.valueOf((String) tenant));
        }
        
        Object state = map.get("state");
        if (state instanceof Integer) {
            expense.setState((Integer) state);
        } else if (state instanceof String) {
            expense.setState(Integer.valueOf((String) state));
        }
        
        Object createdAtStr = map.get("createdAt");
        if (createdAtStr != null) {
            expense.setCreatedAt(LocalDateTime.parse(createdAtStr.toString()));
        }
        
        Object updatedAtStr = map.get("updatedAt");
        if (updatedAtStr != null) {
            expense.setUpdatedAt(LocalDateTime.parse(updatedAtStr.toString()));
        }
        
        Object deletedAtStr = map.get("deletedAt");
        if (deletedAtStr != null && !deletedAtStr.toString().isEmpty()) {
            expense.setDeletedAt(LocalDateTime.parse(deletedAtStr.toString()));
        }
        
        Object organizationId = map.get("organizationId");
        if (organizationId instanceof Integer) {
            expense.setOrganizationId((Integer) organizationId);
        } else if (organizationId instanceof String && !((String) organizationId).isEmpty()) {
            expense.setOrganizationId(Integer.valueOf((String) organizationId));
        }
        
        return expense;
    }

    /**
     * 将ExpenseShare对象转换为Map
     */
    private Map<String, Object> convertShareToMap(ExpenseShare share) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", share.getId());
        map.put("expenseId", share.getExpenseId());
        map.put("userId", share.getUserId());
        map.put("amount", share.getAmount().toString());
        map.put("status", share.getStatus().toString());
        map.put("settledAt", share.getSettledAt() != null ? share.getSettledAt().toString() : null);
        map.put("tenant", share.getTenant());
        map.put("state", share.getState());
        map.put("createdAt", share.getCreatedAt().toString());
        map.put("updatedAt", share.getUpdatedAt().toString());
        map.put("deletedAt", share.getDeletedAt() != null ? share.getDeletedAt().toString() : null);
        map.put("organizationId", share.getOrganizationId());
        return map;
    }

    /**
     * 将Map转换为ExpenseShare对象
     */
    private ExpenseShare convertMapToShare(Map<Object, Object> map) {
        ExpenseShare share = new ExpenseShare();
        
        share.setId((String) map.get("id"));
        share.setExpenseId((String) map.get("expenseId"));
        share.setUserId((String) map.get("userId"));
        
        Object amountStr = map.get("amount");
        if (amountStr != null) {
            share.setAmount(new BigDecimal(amountStr.toString()));
        }
        
        Object status = map.get("status");
        if (status instanceof Integer) {
            share.setStatus((Integer) status);
        } else if (status instanceof String) {
            share.setStatus(Integer.valueOf((String) status));
        }
        
        Object settledAtStr = map.get("settledAt");
        if (settledAtStr != null && !settledAtStr.toString().isEmpty()) {
            share.setSettledAt(LocalDateTime.parse(settledAtStr.toString()));
        }
        
        Object tenant = map.get("tenant");
        if (tenant instanceof Integer) {
            share.setTenant((Integer) tenant);
        } else if (tenant instanceof String) {
            share.setTenant(Integer.valueOf((String) tenant));
        }
        
        Object state = map.get("state");
        if (state instanceof Integer) {
            share.setState((Integer) state);
        } else if (state instanceof String) {
            share.setState(Integer.valueOf((String) state));
        }
        
        Object createdAtStr = map.get("createdAt");
        if (createdAtStr != null) {
            share.setCreatedAt(LocalDateTime.parse(createdAtStr.toString()));
        }
        
        Object updatedAtStr = map.get("updatedAt");
        if (updatedAtStr != null) {
            share.setUpdatedAt(LocalDateTime.parse(updatedAtStr.toString()));
        }
        
        Object deletedAtStr = map.get("deletedAt");
        if (deletedAtStr != null && !deletedAtStr.toString().isEmpty()) {
            share.setDeletedAt(LocalDateTime.parse(deletedAtStr.toString()));
        }
        
        Object organizationId = map.get("organizationId");
        if (organizationId instanceof Integer) {
            share.setOrganizationId((Integer) organizationId);
        } else if (organizationId instanceof String && !((String) organizationId).isEmpty()) {
            share.setOrganizationId(Integer.valueOf((String) organizationId));
        }
        
        return share;
    }
}