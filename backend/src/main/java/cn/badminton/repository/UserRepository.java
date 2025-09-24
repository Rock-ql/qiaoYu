package cn.badminton.repository;

import cn.badminton.config.RedisConfig;
import cn.badminton.model.User;
import cn.badminton.repository.jpa.UserJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户存储库
 * 双写策略：MySQL作为主存储，Redis作为缓存
 *
 * 作者: xiaolei
 */
@Repository
@Slf4j
public class UserRepository {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 保存用户信息（双写策略：先写数据库，后更新缓存）
     */
    @Transactional
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("用户不能为空");
        }

        try {
            // 生成ID（如果是新用户）
            if (user.getId() == null || user.getId().trim().isEmpty()) {
                user.setId(java.util.UUID.randomUUID().toString());
            }
            user.updateTimestamp();

            // 1. 先保存到MySQL数据库（主存储）
            User savedUser = userJpaRepository.save(user);
            log.debug("用户保存到数据库成功，用户ID: {}", savedUser.getId());

            // 2. 更新Redis缓存
            updateUserCache(savedUser);

            return savedUser;

        } catch (Exception e) {
            log.error("保存用户失败，用户ID: {}, 错误信息: {}", user.getId(), e.getMessage(), e);
            // 清理可能不一致的缓存
            if (user.getId() != null) {
                clearUserCache(user.getId());
            }
            throw e;
        }
    }

    /**
     * 根据ID查找用户（缓存优先策略：先查缓存，未命中再查数据库）
     */
    public User findById(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }

        try {
            // 1. 先尝试从Redis缓存获取
            User cachedUser = getUserFromCache(userId);
            if (cachedUser != null) {
                log.debug("从缓存获取用户成功，用户ID: {}", userId);
                return cachedUser;
            }

            // 2. 缓存未命中，从数据库查询
            java.util.Optional<User> userOptional = userJpaRepository.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                log.debug("从数据库获取用户成功，用户ID: {}", userId);

                // 3. 查询成功后更新缓存
                updateUserCache(user);

                return user;
            }

            return null;

        } catch (Exception e) {
            log.error("查询用户失败，用户ID: {}, 错误信息: {}", userId, e.getMessage(), e);
            // 发生异常时，尝试从数据库查询作为备选方案
            try {
                java.util.Optional<User> userOptional = userJpaRepository.findById(userId);
                return userOptional.orElse(null);
            } catch (Exception dbException) {
                log.error("数据库查询用户也失败，用户ID: {}", userId, dbException);
                return null;
            }
        }
    }

    /**
     * 根据手机号查找用户（优先查询数据库，确保数据准确性）
     */
    public User findByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }

        try {
            // 1. 首先尝试从缓存索引查找
            String phoneIndexKey = "badminton:index:phone:" + phone;
            String userId = (String) redisTemplate.opsForValue().get(phoneIndexKey);

            if (userId != null) {
                User cachedUser = getUserFromCache(userId);
                if (cachedUser != null && phone.equals(cachedUser.getPhone())) {
                    log.debug("从缓存通过手机号获取用户成功，phone: {}", phone);
                    return cachedUser;
                }
            }

            // 2. 从数据库查询（保证数据准确性）
            java.util.Optional<User> userOptional = userJpaRepository.findByPhone(phone);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                log.debug("从数据库通过手机号获取用户成功，phone: {}", phone);

                // 3. 更新缓存和索引
                updateUserCache(user);
                updateUserIndexes(user);

                return user;
            }

            return null;

        } catch (Exception e) {
            log.error("根据手机号查询用户失败，phone: {}, 错误信息: {}", phone, e.getMessage(), e);
            // 异常情况下直接查询数据库
            try {
                java.util.Optional<User> userOptional = userJpaRepository.findByPhone(phone);
                return userOptional.orElse(null);
            } catch (Exception dbException) {
                log.error("数据库查询用户也失败，phone: {}", phone, dbException);
                return null;
            }
        }
    }

    /**
     * 根据微信OpenID查找用户
     */
    public User findByWechatOpenId(String openId) {
        if (openId == null || openId.trim().isEmpty()) {
            return null;
        }

        String wechatIndexKey = "badminton:index:wechat:" + openId;
        String userId = (String) redisTemplate.opsForValue().get(wechatIndexKey);
        
        if (userId != null) {
            return findById(userId);
        }

        // 如果索引不存在，扫描所有用户键查找匹配的微信OpenID
        Set<String> keys = redisTemplate.keys(RedisConfig.RedisKeys.USER_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return null;
        }

        for (String key : keys) {
            Object openIdValue = redisTemplate.opsForHash().get(key, "wxOpenId");
            if (openId.equals(openIdValue)) {
                Map<Object, Object> userMap = redisTemplate.opsForHash().entries(key);
                User user = convertMapToUser(userMap);
                // 重建索引
                createUserIndexes(user);
                return user;
            }
        }

        return null;
    }

    /**
     * 兼容别名：findByWxOpenId → findByWechatOpenId
     */
    public User findByWxOpenId(String openId) {
        return findByWechatOpenId(openId);
    }


    /**
     * 检查手机号是否已存在
     */
    public boolean existsByPhone(String phone) {
        return findByPhone(phone) != null;
    }

    /**
     * 删除用户
     */
    public void deleteById(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }

        User user = findById(userId);
        if (user != null) {
            // 删除索引
            deleteUserIndexes(user);
        }

        String userKey = RedisConfig.RedisKeys.userKey(userId);
        String activitiesKey = RedisConfig.RedisKeys.userActivitiesKey(userId);
        
        redisTemplate.delete(userKey);
        redisTemplate.delete(activitiesKey);
    }

    /**
     * 更新用户状态
     */
    public void updateStatus(String userId, Integer newStatus) {
        if (userId == null || userId.trim().isEmpty() || newStatus == null) {
            return;
        }
        String key = RedisConfig.RedisKeys.userKey(userId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForHash().put(key, "status", newStatus.toString());
            redisTemplate.opsForHash().put(key, "updatedAt", LocalDateTime.now().toString());
        }
    }

    /**
     * 添加用户参与的活动
     */
    public void addUserActivity(String userId, String activityId) {
        if (userId == null || activityId == null) {
            return;
        }

        String key = RedisConfig.RedisKeys.userActivitiesKey(userId);
        redisTemplate.opsForSet().add(key, activityId);
        redisTemplate.expire(key, RedisConfig.RedisTTL.ACTIVITY_CACHE, TimeUnit.SECONDS);
    }

    /**
     * 移除用户参与的活动
     */
    public void removeUserActivity(String userId, String activityId) {
        if (userId == null || activityId == null) {
            return;
        }

        String key = RedisConfig.RedisKeys.userActivitiesKey(userId);
        redisTemplate.opsForSet().remove(key, activityId);
    }

    /**
     * 获取用户参与的活动列表
     */
    public Set<String> getUserActivities(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Collections.emptySet();
        }

        String key = RedisConfig.RedisKeys.userActivitiesKey(userId);
        Set<Object> activities = redisTemplate.opsForSet().members(key);
        
        if (activities == null || activities.isEmpty()) {
            return Collections.emptySet();
        }

        return activities.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    /**
     * 获取所有用户
     */
    public List<User> findAll() {
        Set<String> keys = redisTemplate.keys(RedisConfig.RedisKeys.USER_PREFIX + "*");
        List<User> users = new ArrayList<>();
        
        if (keys != null) {
            for (String key : keys) {
                String userId = key.substring(RedisConfig.RedisKeys.USER_PREFIX.length());
                User user = findById(userId);
                if (user != null) {
                    users.add(user);
                }
            }
        }
        
        return users;
    }

    /**
     * 更新用户状态
     */
    public void updateStatus(String userId, int status) {
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }
        
        String key = RedisConfig.RedisKeys.userKey(userId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForHash().put(key, "status", String.valueOf(status));
            redisTemplate.opsForHash().put(key, "updatedAt", LocalDateTime.now().toString());
        }
    }

    /**
     * 统计用户总数
     */
    public long count() {
        Set<String> keys = redisTemplate.keys(RedisConfig.RedisKeys.USER_PREFIX + "*");
        return keys != null ? keys.size() : 0;
    }

    /**
     * 创建用户索引
     */
    private void createUserIndexes(User user) {
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            String phoneIndexKey = "badminton:index:phone:" + user.getPhone();
            redisTemplate.opsForValue().set(phoneIndexKey, user.getId(), 
                RedisConfig.RedisTTL.USER_CACHE, TimeUnit.SECONDS);
        }
        
        if (user.getWxOpenId() != null && !user.getWxOpenId().trim().isEmpty()) {
            String wechatIndexKey = "badminton:index:wechat:" + user.getWxOpenId();
            redisTemplate.opsForValue().set(wechatIndexKey, user.getId(), 
                RedisConfig.RedisTTL.USER_CACHE, TimeUnit.SECONDS);
        }
    }

    /**
     * 删除用户索引
     */
    private void deleteUserIndexes(User user) {
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            String phoneIndexKey = "badminton:index:phone:" + user.getPhone();
            redisTemplate.delete(phoneIndexKey);
        }
        
        if (user.getWxOpenId() != null && !user.getWxOpenId().trim().isEmpty()) {
            String wechatIndexKey = "badminton:index:wechat:" + user.getWxOpenId();
            redisTemplate.delete(wechatIndexKey);
        }
    }

    /**
     * 将User对象转换为Map
     */
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("phone", user.getPhone());
        map.put("nickname", user.getNickname());
        map.put("password", user.getPassword());
        map.put("avatar", user.getAvatar());
        map.put("status", user.getStatus());
        map.put("totalActivities", user.getTotalActivities());
        map.put("totalExpense", user.getTotalExpense().toString());
        map.put("wxOpenId", user.getWxOpenId());
        map.put("wxUnionId", user.getWxUnionId());
        map.put("tenant", user.getTenant());
        map.put("state", user.getState());
        map.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        map.put("updatedAt", user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);
        map.put("deletedAt", user.getDeletedAt() != null ? user.getDeletedAt().toString() : null);
        map.put("organizationId", user.getOrganizationId());
        return map;
    }

    // ==================== 缓存管理方法 ====================

    /**
     * 从缓存获取用户
     */
    private User getUserFromCache(String userId) {
        try {
            String key = RedisConfig.RedisKeys.userKey(userId);
            Map<Object, Object> userMap = redisTemplate.opsForHash().entries(key);

            if (userMap == null || userMap.isEmpty()) {
                return null;
            }

            return convertMapToUser(userMap);
        } catch (Exception e) {
            log.warn("从缓存获取用户失败，用户ID: {}, 错误信息: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 更新用户缓存
     */
    private void updateUserCache(User user) {
        try {
            String key = RedisConfig.RedisKeys.userKey(user.getId());
            Map<String, Object> userMap = convertUserToMap(user);

            redisTemplate.opsForHash().putAll(key, userMap);
            // 缓存7天，用作热点数据加速
            redisTemplate.expire(key, RedisConfig.RedisTTL.USER_SESSION, TimeUnit.SECONDS);

            log.debug("更新用户缓存成功，用户ID: {}", user.getId());
        } catch (Exception e) {
            log.warn("更新用户缓存失败，用户ID: {}, 错误信息: {}", user.getId(), e.getMessage());
        }
    }

    /**
     * 更新用户索引
     */
    private void updateUserIndexes(User user) {
        try {
            // 手机号索引
            if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                String phoneIndexKey = "badminton:index:phone:" + user.getPhone();
                redisTemplate.opsForValue().set(phoneIndexKey, user.getId(),
                    RedisConfig.RedisTTL.USER_SESSION, TimeUnit.SECONDS);
            }

            // 微信OpenID索引
            if (user.getWxOpenId() != null && !user.getWxOpenId().trim().isEmpty()) {
                String wechatIndexKey = "badminton:index:wechat:" + user.getWxOpenId();
                redisTemplate.opsForValue().set(wechatIndexKey, user.getId(),
                    RedisConfig.RedisTTL.USER_SESSION, TimeUnit.SECONDS);
            }

            log.debug("更新用户索引成功，用户ID: {}", user.getId());
        } catch (Exception e) {
            log.warn("更新用户索引失败，用户ID: {}, 错误信息: {}", user.getId(), e.getMessage());
        }
    }

    /**
     * 清理用户缓存
     */
    private void clearUserCache(String userId) {
        try {
            // 先获取用户信息以便清理索引
            User user = getUserFromCache(userId);

            // 清理主缓存
            String userKey = RedisConfig.RedisKeys.userKey(userId);
            redisTemplate.delete(userKey);

            // 清理索引
            if (user != null) {
                if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                    String phoneIndexKey = "badminton:index:phone:" + user.getPhone();
                    redisTemplate.delete(phoneIndexKey);
                }
                if (user.getWxOpenId() != null && !user.getWxOpenId().trim().isEmpty()) {
                    String wechatIndexKey = "badminton:index:wechat:" + user.getWxOpenId();
                    redisTemplate.delete(wechatIndexKey);
                }
            }

            // 清理活动列表缓存
            String activitiesKey = RedisConfig.RedisKeys.userActivitiesKey(userId);
            redisTemplate.delete(activitiesKey);

            log.debug("清理用户缓存成功，用户ID: {}", userId);
        } catch (Exception e) {
            log.warn("清理用户缓存失败，用户ID: {}, 错误信息: {}", userId, e.getMessage());
        }
    }

    /**
     * 将Map转换为User对象
     */
    private User convertMapToUser(Map<Object, Object> map) {
        User user = new User();
        user.setId((String) map.get("id"));
        user.setPhone((String) map.get("phone"));
        user.setNickname((String) map.get("nickname"));
        user.setPassword((String) map.get("password"));
        user.setAvatar((String) map.get("avatar"));
        
        Object status = map.get("status");
        if (status instanceof Integer) {
            user.setStatus((Integer) status);
        } else if (status instanceof String) {
            user.setStatus(Integer.valueOf((String) status));
        }
        
        Object totalActivities = map.get("totalActivities");
        if (totalActivities instanceof Integer) {
            user.setTotalActivities((Integer) totalActivities);
        } else if (totalActivities instanceof String) {
            user.setTotalActivities(Integer.valueOf((String) totalActivities));
        }
        
        Object totalExpenseStr = map.get("totalExpense");
        if (totalExpenseStr != null) {
            user.setTotalExpense(new BigDecimal(totalExpenseStr.toString()));
        }
        
        user.setWxOpenId((String) map.get("wxOpenId"));
        user.setWxUnionId((String) map.get("wxUnionId"));
        
        Object tenant = map.get("tenant");
        if (tenant instanceof Integer) {
            user.setTenant((Integer) tenant);
        } else if (tenant instanceof String) {
            user.setTenant(Integer.valueOf((String) tenant));
        }
        
        Object state = map.get("state");
        if (state instanceof Integer) {
            user.setState((Integer) state);
        } else if (state instanceof String) {
            user.setState(Integer.valueOf((String) state));
        }
        
        // 处理时间字段的反序列化
        Object createdAtObj = map.get("createdAt");
        if (createdAtObj instanceof String && !((String) createdAtObj).isEmpty()) {
            user.setCreatedAt(LocalDateTime.parse((String) createdAtObj));
        } else if (createdAtObj instanceof LocalDateTime) {
            user.setCreatedAt((LocalDateTime) createdAtObj);
        }
        
        Object updatedAtObj = map.get("updatedAt");
        if (updatedAtObj instanceof String && !((String) updatedAtObj).isEmpty()) {
            user.setUpdatedAt(LocalDateTime.parse((String) updatedAtObj));
        } else if (updatedAtObj instanceof LocalDateTime) {
            user.setUpdatedAt((LocalDateTime) updatedAtObj);
        }
        
        Object deletedAtObj = map.get("deletedAt");
        if (deletedAtObj instanceof String && !((String) deletedAtObj).isEmpty()) {
            user.setDeletedAt(LocalDateTime.parse((String) deletedAtObj));
        } else if (deletedAtObj instanceof LocalDateTime) {
            user.setDeletedAt((LocalDateTime) deletedAtObj);
        }
        
        Object organizationId = map.get("organizationId");
        if (organizationId instanceof Integer) {
            user.setOrganizationId((Integer) organizationId);
        } else if (organizationId instanceof String && !((String) organizationId).isEmpty()) {
            user.setOrganizationId(Integer.valueOf((String) organizationId));
        }
        
        return user;
    }
}
