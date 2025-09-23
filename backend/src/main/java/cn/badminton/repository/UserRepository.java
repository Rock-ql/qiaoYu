package cn.badminton.repository;

import cn.badminton.config.RedisConfig;
import cn.badminton.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户Redis存储库
 * 存储结构: badminton:user:{user_id} (Hash)
 * 
 * 作者: xiaolei
 */
@Repository
public class UserRepository {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 保存用户信息
     */
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("用户不能为空");
        }
        if (user.getId() == null || user.getId().trim().isEmpty()) {
            user.setId(java.util.UUID.randomUUID().toString());
        }

        String key = RedisConfig.RedisKeys.userKey(user.getId());
        user.updateTimestamp();
        
        // 使用Hash存储用户信息
        Map<String, Object> userMap = convertUserToMap(user);
        redisTemplate.opsForHash().putAll(key, userMap);
        
        // 设置过期时间
        redisTemplate.expire(key, RedisConfig.RedisTTL.USER_CACHE, TimeUnit.SECONDS);
        
        // 创建索引以支持按手机号和微信OpenID查询
        createUserIndexes(user);
        
        return user;
    }

    /**
     * 根据ID查找用户
     */
    public User findById(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }

        String key = RedisConfig.RedisKeys.userKey(userId);
        Map<Object, Object> userMap = redisTemplate.opsForHash().entries(key);
        
        if (userMap.isEmpty()) {
            return null;
        }

        return convertMapToUser(userMap);
    }

    /**
     * 根据手机号查找用户
     */
    public User findByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }

        String phoneIndexKey = "badminton:index:phone:" + phone;
        String userId = (String) redisTemplate.opsForValue().get(phoneIndexKey);
        
        if (userId != null) {
            return findById(userId);
        }

        // 如果索引不存在，扫描所有用户键查找匹配的手机号
        Set<String> keys = redisTemplate.keys(RedisConfig.RedisKeys.USER_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return null;
        }

        for (String key : keys) {
            Object phoneValue = redisTemplate.opsForHash().get(key, "phone");
            if (phone.equals(phoneValue)) {
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
            redisTemplate.opsForHash().put(key, "updatedAt", LocalDateTime.now());
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
        map.put("createdAt", user.getCreatedAt());
        map.put("updatedAt", user.getUpdatedAt());
        map.put("deletedAt", user.getDeletedAt());
        map.put("organizationId", user.getOrganizationId());
        return map;
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
        
        user.setCreatedAt((LocalDateTime) map.get("createdAt"));
        user.setUpdatedAt((LocalDateTime) map.get("updatedAt"));
        user.setDeletedAt((LocalDateTime) map.get("deletedAt"));
        
        Object organizationId = map.get("organizationId");
        if (organizationId instanceof Integer) {
            user.setOrganizationId((Integer) organizationId);
        } else if (organizationId instanceof String && !((String) organizationId).isEmpty()) {
            user.setOrganizationId(Integer.valueOf((String) organizationId));
        }
        
        return user;
    }
}
