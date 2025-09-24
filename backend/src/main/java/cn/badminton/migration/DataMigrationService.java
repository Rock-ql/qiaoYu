package cn.badminton.migration;

import cn.badminton.config.RedisConfig;
import cn.badminton.model.*;
import cn.badminton.repository.jpa.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * 数据迁移服务
 * 将Redis中的数据迁移到MySQL数据库
 *
 * 作者: xiaolei
 */
@Service
@Slf4j
public class DataMigrationService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ActivityJpaRepository activityJpaRepository;

    @Autowired
    private ParticipationJpaRepository participationJpaRepository;

    @Autowired
    private ExpenseJpaRepository expenseJpaRepository;

    @Autowired
    private ExpenseShareJpaRepository expenseShareJpaRepository;

    /**
     * 执行完整的数据迁移
     */
    @Transactional
    public void migrateAllData() {
        log.info("开始执行数据迁移...");

        try {
            // 1. 迁移用户数据
            int userCount = migrateUsers();
            log.info("用户数据迁移完成，共迁移: {} 条记录", userCount);

            // 2. 迁移活动数据
            int activityCount = migrateActivities();
            log.info("活动数据迁移完成，共迁移: {} 条记录", activityCount);

            // 3. 迁移费用数据
            int expenseCount = migrateExpenses();
            log.info("费用数据迁移完成，共迁移: {} 条记录", expenseCount);

            // 4. 迁移费用分摊数据
            int shareCount = migrateExpenseShares();
            log.info("费用分摊数据迁移完成，共迁移: {} 条记录", shareCount);

            log.info("数据迁移全部完成！用户: {}, 活动: {}, 费用: {}, 分摊: {}",
                    userCount, activityCount, expenseCount, shareCount);

        } catch (Exception e) {
            log.error("数据迁移失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 迁移用户数据
     */
    private int migrateUsers() {
        log.info("开始迁移用户数据...");
        int count = 0;

        try {
            Set<String> userKeys = redisTemplate.keys(RedisConfig.RedisKeys.USER_PREFIX + "*");
            if (userKeys == null || userKeys.isEmpty()) {
                log.info("未发现用户数据，跳过迁移");
                return 0;
            }

            for (String key : userKeys) {
                try {
                    Map<Object, Object> userMap = redisTemplate.opsForHash().entries(key);
                    if (userMap.isEmpty()) {
                        continue;
                    }

                    User user = convertMapToUser(userMap);
                    if (user.getId() != null) {
                        // 检查是否已存在
                        if (!userJpaRepository.existsById(user.getId())) {
                            userJpaRepository.save(user);
                            count++;
                            log.debug("迁移用户成功: {}", user.getId());
                        } else {
                            log.debug("用户已存在，跳过: {}", user.getId());
                        }
                    }
                } catch (Exception e) {
                    log.warn("迁移单个用户失败，键: {}, 错误: {}", key, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("迁移用户数据失败: {}", e.getMessage(), e);
            throw e;
        }

        return count;
    }

    /**
     * 迁移活动数据
     */
    private int migrateActivities() {
        log.info("开始迁移活动数据...");
        int count = 0;

        try {
            Set<String> activityKeys = redisTemplate.keys(RedisConfig.RedisKeys.ACTIVITY_PREFIX + "*");
            if (activityKeys == null || activityKeys.isEmpty()) {
                log.info("未发现活动数据，跳过迁移");
                return 0;
            }

            for (String key : activityKeys) {
                try {
                    Map<Object, Object> activityMap = redisTemplate.opsForHash().entries(key);
                    if (activityMap.isEmpty()) {
                        continue;
                    }

                    BookingActivity activity = convertMapToActivity(activityMap);
                    if (activity.getId() != null) {
                        // 检查是否已存在
                        if (!activityJpaRepository.existsById(activity.getId())) {
                            activityJpaRepository.save(activity);
                            count++;
                            log.debug("迁移活动成功: {}", activity.getId());
                        } else {
                            log.debug("活动已存在，跳过: {}", activity.getId());
                        }
                    }
                } catch (Exception e) {
                    log.warn("迁移单个活动失败，键: {}, 错误: {}", key, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("迁移活动数据失败: {}", e.getMessage(), e);
            throw e;
        }

        return count;
    }

    /**
     * 迁移费用数据
     */
    private int migrateExpenses() {
        log.info("开始迁移费用数据...");
        int count = 0;

        try {
            Set<String> expenseKeys = redisTemplate.keys(RedisConfig.RedisKeys.EXPENSE_PREFIX + "*");
            if (expenseKeys == null || expenseKeys.isEmpty()) {
                log.info("未发现费用数据，跳过迁移");
                return 0;
            }

            for (String key : expenseKeys) {
                try {
                    Map<Object, Object> expenseMap = redisTemplate.opsForHash().entries(key);
                    if (expenseMap.isEmpty()) {
                        continue;
                    }

                    ExpenseRecord expense = convertMapToExpenseRecord(expenseMap);
                    if (expense.getId() != null) {
                        // 检查是否已存在
                        if (!expenseJpaRepository.existsById(expense.getId())) {
                            expenseJpaRepository.save(expense);
                            count++;
                            log.debug("迁移费用成功: {}", expense.getId());
                        } else {
                            log.debug("费用已存在，跳过: {}", expense.getId());
                        }
                    }
                } catch (Exception e) {
                    log.warn("迁移单个费用失败，键: {}, 错误: {}", key, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("迁移费用数据失败: {}", e.getMessage(), e);
            throw e;
        }

        return count;
    }

    /**
     * 迁移费用分摊数据
     */
    private int migrateExpenseShares() {
        log.info("开始迁移费用分摊数据...");
        int count = 0;

        try {
            Set<String> shareKeys = redisTemplate.keys(RedisConfig.RedisKeys.SHARE_PREFIX + "*");
            if (shareKeys == null || shareKeys.isEmpty()) {
                log.info("未发现费用分摊数据，跳过迁移");
                return 0;
            }

            for (String key : shareKeys) {
                try {
                    Map<Object, Object> shareMap = redisTemplate.opsForHash().entries(key);
                    if (shareMap.isEmpty()) {
                        continue;
                    }

                    ExpenseShare share = convertMapToExpenseShare(shareMap);
                    if (share.getId() != null) {
                        // 检查是否已存在
                        if (!expenseShareJpaRepository.existsById(share.getId())) {
                            expenseShareJpaRepository.save(share);
                            count++;
                            log.debug("迁移费用分摊成功: {}", share.getId());
                        } else {
                            log.debug("费用分摊已存在，跳过: {}", share.getId());
                        }
                    }
                } catch (Exception e) {
                    log.warn("迁移单个费用分摊失败，键: {}, 错误: {}", key, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("迁移费用分摊数据失败: {}", e.getMessage(), e);
            throw e;
        }

        return count;
    }

    // ==================== 数据转换方法 ====================

    private User convertMapToUser(Map<Object, Object> map) {
        User user = new User();
        user.setId(getString(map, "id"));
        user.setPhone(getString(map, "phone"));
        user.setNickname(getString(map, "nickname"));
        user.setPassword(getString(map, "password"));
        user.setAvatar(getString(map, "avatar"));
        user.setStatus(getInteger(map, "status", 1));
        user.setTotalActivities(getInteger(map, "totalActivities", 0));
        user.setTotalExpense(getBigDecimal(map, "totalExpense", BigDecimal.ZERO));
        user.setWxOpenId(getString(map, "wxOpenId"));
        user.setWxUnionId(getString(map, "wxUnionId"));
        user.setTenant(getInteger(map, "tenant", 1));
        user.setState(getInteger(map, "state", 1));
        user.setCreatedAt(getLocalDateTime(map, "createdAt"));
        user.setUpdatedAt(getLocalDateTime(map, "updatedAt"));
        user.setDeletedAt(getLocalDateTime(map, "deletedAt"));
        user.setOrganizationId(getInteger(map, "organizationId", 0));
        return user;
    }

    private BookingActivity convertMapToActivity(Map<Object, Object> map) {
        BookingActivity activity = new BookingActivity();
        activity.setId(getString(map, "id"));
        activity.setTitle(getString(map, "title"));
        activity.setOrganizer(getString(map, "organizer"));
        activity.setVenue(getString(map, "venue"));
        activity.setAddress(getString(map, "address"));
        activity.setStartTime(getLocalDateTime(map, "startTime"));
        activity.setEndTime(getLocalDateTime(map, "endTime"));
        activity.setMaxPlayers(getInteger(map, "maxPlayers", 2));
        activity.setCurrentPlayers(getInteger(map, "currentPlayers", 1));
        activity.setFee(getBigDecimal(map, "fee", BigDecimal.ZERO));
        activity.setDescription(getString(map, "description"));
        activity.setStatus(getInteger(map, "status", 1));
        activity.setTenant(getInteger(map, "tenant", 1));
        activity.setState(getInteger(map, "state", 1));
        activity.setCreatedAt(getLocalDateTime(map, "createdAt"));
        activity.setUpdatedAt(getLocalDateTime(map, "updatedAt"));
        activity.setDeletedAt(getLocalDateTime(map, "deletedAt"));
        activity.setOrganizationId(getInteger(map, "organizationId", 0));
        return activity;
    }

    private ExpenseRecord convertMapToExpenseRecord(Map<Object, Object> map) {
        ExpenseRecord expense = new ExpenseRecord();
        expense.setId(getString(map, "id"));
        expense.setActivityId(getString(map, "activityId"));
        expense.setPayerId(getString(map, "payerId"));
        expense.setType(getString(map, "type"));
        expense.setDescription(getString(map, "description"));
        expense.setTotalAmount(getBigDecimal(map, "totalAmount", BigDecimal.ZERO));
        expense.setSplitMethod(getString(map, "splitMethod", "equal"));
        expense.setTenant(getInteger(map, "tenant", 1));
        expense.setState(getInteger(map, "state", 1));
        expense.setCreatedAt(getLocalDateTime(map, "createdAt"));
        expense.setUpdatedAt(getLocalDateTime(map, "updatedAt"));
        expense.setDeletedAt(getLocalDateTime(map, "deletedAt"));
        expense.setOrganizationId(getInteger(map, "organizationId", 0));
        return expense;
    }

    private ExpenseShare convertMapToExpenseShare(Map<Object, Object> map) {
        ExpenseShare share = new ExpenseShare();
        share.setId(getString(map, "id"));
        share.setExpenseId(getString(map, "expenseId"));
        share.setUserId(getString(map, "userId"));
        share.setAmount(getBigDecimal(map, "amount", BigDecimal.ZERO));
        share.setStatus(getInteger(map, "status", 1));
        share.setSettledAt(getLocalDateTime(map, "settledAt"));
        share.setTenant(getInteger(map, "tenant", 1));
        share.setState(getInteger(map, "state", 1));
        share.setCreatedAt(getLocalDateTime(map, "createdAt"));
        share.setUpdatedAt(getLocalDateTime(map, "updatedAt"));
        share.setDeletedAt(getLocalDateTime(map, "deletedAt"));
        share.setOrganizationId(getInteger(map, "organizationId", 0));
        return share;
    }

    // ==================== 工具方法 ====================

    private String getString(Map<Object, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private String getString(Map<Object, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null && !value.toString().isEmpty() ? value.toString() : defaultValue;
    }

    private Integer getInteger(Map<Object, Object> map, String key, Integer defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;

        try {
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                return Integer.valueOf((String) value);
            }
        } catch (NumberFormatException e) {
            log.warn("转换整数失败，键: {}, 值: {}, 使用默认值: {}", key, value, defaultValue);
        }
        return defaultValue;
    }

    private BigDecimal getBigDecimal(Map<Object, Object> map, String key, BigDecimal defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;

        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            log.warn("转换BigDecimal失败，键: {}, 值: {}, 使用默认值: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    private LocalDateTime getLocalDateTime(Map<Object, Object> map, String key) {
        Object value = map.get(key);
        if (value == null || value.toString().isEmpty()) return null;

        try {
            if (value instanceof LocalDateTime) {
                return (LocalDateTime) value;
            } else if (value instanceof String) {
                return LocalDateTime.parse((String) value);
            }
        } catch (DateTimeParseException e) {
            log.warn("转换LocalDateTime失败，键: {}, 值: {}", key, value);
        }
        return null;
    }
}