package cn.badminton.repository;

import cn.badminton.model.User;
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
 * 用户Redis存储库
 * 存储结构: badminton:user:{user_id} (Hash)
 * 
 * 作者: xiaolei
 */
@Repository
public class UserRepository {

    private static final String KEY_PREFIX = "badminton:user:";
    private static final String PHONE_INDEX_PREFIX = "badminton:user:phone:";
    private static final String WECHAT_INDEX_PREFIX = "badminton:user:wechat:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private HashOperations<String, String, Object> hashOps;

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOps = redisTemplate.opsForHash();
    }

    /**
     * 保存用户
     */
    public User save(User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId(UUID.randomUUID().toString());
        }
        user.setUpdatedAt(LocalDateTime.now());

        String key = KEY_PREFIX + user.getId();
        
        // 保存用户数据
        hashOps.put(key, "id", user.getId());
        hashOps.put(key, "phone", user.getPhone());
        hashOps.put(key, "nickname", user.getNickname());
        hashOps.put(key, "avatar", user.getAvatar() != null ? user.getAvatar() : "");
        hashOps.put(key, "password", user.getPassword());
        hashOps.put(key, "status", user.getStatus().toString());
        hashOps.put(key, "createdAt", user.getCreatedAt().toString());
        hashOps.put(key, "updatedAt", user.getUpdatedAt().toString());
        hashOps.put(key, "totalActivities", user.getTotalActivities().toString());
        hashOps.put(key, "totalExpense", user.getTotalExpense().toString());
        hashOps.put(key, "wxOpenId", user.getWxOpenId() != null ? user.getWxOpenId() : "");
        hashOps.put(key, "wxUnionId", user.getWxUnionId() != null ? user.getWxUnionId() : "");

        // 创建索引
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            redisTemplate.opsForValue().set(PHONE_INDEX_PREFIX + user.getPhone(), user.getId());
        }
        if (user.getWxOpenId() != null && !user.getWxOpenId().isEmpty()) {
            redisTemplate.opsForValue().set(WECHAT_INDEX_PREFIX + user.getWxOpenId(), user.getId());
        }

        return user;
    }

    /**
     * 根据ID查找用户
     */
    public User findById(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        String key = KEY_PREFIX + id;
        Map<String, Object> userMap = hashOps.entries(key);
        
        if (userMap.isEmpty()) {
            return null;
        }

        return mapToUser(userMap);
    }

    /**
     * 根据手机号查找用户
     */
    public User findByPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return null;
        }

        String userId = (String) redisTemplate.opsForValue().get(PHONE_INDEX_PREFIX + phone);
        if (userId == null) {
            return null;
        }

        return findById(userId);
    }

    /**
     * 根据微信OpenID查找用户
     */
    public User findByWxOpenId(String wxOpenId) {
        if (wxOpenId == null || wxOpenId.isEmpty()) {
            return null;
        }

        String userId = (String) redisTemplate.opsForValue().get(WECHAT_INDEX_PREFIX + wxOpenId);
        if (userId == null) {
            return null;
        }

        return findById(userId);
    }

    /**
     * 删除用户
     */
    public void deleteById(String id) {
        if (id == null || id.isEmpty()) {
            return;
        }

        User user = findById(id);
        if (user != null) {
            // 删除索引
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                redisTemplate.delete(PHONE_INDEX_PREFIX + user.getPhone());
            }
            if (user.getWxOpenId() != null && !user.getWxOpenId().isEmpty()) {
                redisTemplate.delete(WECHAT_INDEX_PREFIX + user.getWxOpenId());
            }
        }

        // 删除用户数据
        redisTemplate.delete(KEY_PREFIX + id);
    }

    /**
     * 检查用户是否存在
     */
    public boolean existsById(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + id));
    }

    /**
     * 检查手机号是否已存在
     */
    public boolean existsByPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(PHONE_INDEX_PREFIX + phone));
    }

    /**
     * 获取所有用户ID
     */
    public List<String> findAllIds() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        List<String> userIds = new ArrayList<>();
        
        if (keys != null) {
            for (String key : keys) {
                userIds.add(key.substring(KEY_PREFIX.length()));
            }
        }
        
        return userIds;
    }

    /**
     * 获取所有用户
     */
    public List<User> findAll() {
        List<String> userIds = findAllIds();
        List<User> users = new ArrayList<>();
        
        for (String userId : userIds) {
            User user = findById(userId);
            if (user != null) {
                users.add(user);
            }
        }
        
        return users;
    }

    /**
     * 更新用户状态
     */
    public void updateStatus(String id, Integer status) {
        if (id == null || id.isEmpty()) {
            return;
        }
        
        String key = KEY_PREFIX + id;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            hashOps.put(key, "status", status.toString());
            hashOps.put(key, "updatedAt", LocalDateTime.now().toString());
        }
    }

    /**
     * 转换Map为User对象
     */
    private User mapToUser(Map<String, Object> userMap) {
        User user = new User();
        
        user.setId((String) userMap.get("id"));
        user.setPhone((String) userMap.get("phone"));
        user.setNickname((String) userMap.get("nickname"));
        user.setAvatar((String) userMap.get("avatar"));
        user.setPassword((String) userMap.get("password"));
        user.setStatus(Integer.valueOf((String) userMap.get("status")));
        user.setCreatedAt(LocalDateTime.parse((String) userMap.get("createdAt")));
        user.setUpdatedAt(LocalDateTime.parse((String) userMap.get("updatedAt")));
        user.setTotalActivities(Integer.valueOf((String) userMap.get("totalActivities")));
        user.setTotalExpense(Double.valueOf((String) userMap.get("totalExpense")));
        
        String wxOpenId = (String) userMap.get("wxOpenId");
        if (wxOpenId != null && !wxOpenId.isEmpty()) {
            user.setWxOpenId(wxOpenId);
        }
        
        String wxUnionId = (String) userMap.get("wxUnionId");
        if (wxUnionId != null && !wxUnionId.isEmpty()) {
            user.setWxUnionId(wxUnionId);
        }
        
        return user;
    }
}