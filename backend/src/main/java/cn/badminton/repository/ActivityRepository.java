package cn.badminton.repository;

import cn.badminton.config.RedisConfig;
import cn.badminton.model.BookingActivity;
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
 * 活动Redis存储库
 * 存储结构: badminton:activity:{activity_id} (Hash)
 * 
 * 作者: xiaolei
 */
@Repository
public class ActivityRepository {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private HashOperations<String, String, Object> hashOps;

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOps = redisTemplate.opsForHash();
    }

    /**
     * 保存活动
     */
    public BookingActivity save(BookingActivity activity) {
        if (activity.getId() == null || activity.getId().isEmpty()) {
            activity.setId(UUID.randomUUID().toString());
        }
        activity.updateTimestamp();

        String key = RedisConfig.RedisKeys.activityKey(activity.getId());
        
        // 保存活动数据到Hash
        Map<String, Object> activityMap = convertActivityToMap(activity);
        redisTemplate.opsForHash().putAll(key, activityMap);
        
        // 设置过期时间
        redisTemplate.expire(key, RedisConfig.RedisTTL.ACTIVITY_CACHE, TimeUnit.SECONDS);

        // 创建索引
        createActivityIndexes(activity);

        return activity;
    }

    /**
     * 根据ID查找活动
     */
    public BookingActivity findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }

        String key = RedisConfig.RedisKeys.activityKey(id);
        Map<Object, Object> activityMap = redisTemplate.opsForHash().entries(key);
        
        if (activityMap.isEmpty()) {
            return null;
        }

        return convertMapToActivity(activityMap);
    }

    /**
     * 根据发起人查找活动
     */
    public List<BookingActivity> findByOrganizer(String organizer) {
        if (organizer == null || organizer.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String organizerIndexKey = "badminton:index:organizer:" + organizer;
        Set<Object> activityIds = redisTemplate.opsForSet().members(organizerIndexKey);
        List<BookingActivity> activities = new ArrayList<>();
        
        if (activityIds != null) {
            for (Object activityId : activityIds) {
                BookingActivity activity = findById((String) activityId);
                if (activity != null) {
                    activities.add(activity);
                }
            }
        }
        
        return activities;
    }

    /**
     * 根据状态查找活动
     */
    public List<BookingActivity> findByStatus(Integer status) {
        if (status == null) {
            return new ArrayList<>();
        }

        String statusIndexKey = "badminton:index:status:" + status;
        Set<Object> activityIds = redisTemplate.opsForSet().members(statusIndexKey);
        List<BookingActivity> activities = new ArrayList<>();
        
        if (activityIds != null) {
            for (Object activityId : activityIds) {
                BookingActivity activity = findById((String) activityId);
                if (activity != null) {
                    activities.add(activity);
                }
            }
        }
        
        return activities;
    }

    /**
     * 获取所有活动
     */
    public List<BookingActivity> findAll() {
        Set<String> keys = redisTemplate.keys(RedisConfig.RedisKeys.ACTIVITY_PREFIX + "*");
        List<BookingActivity> activities = new ArrayList<>();
        
        if (keys != null) {
            for (String key : keys) {
                String activityId = key.substring(RedisConfig.RedisKeys.ACTIVITY_PREFIX.length());
                BookingActivity activity = findById(activityId);
                if (activity != null) {
                    activities.add(activity);
                }
            }
        }
        
        return activities;
    }

    /**
     * 获取进行中的活动
     */
    public List<BookingActivity> findOngoingActivities() {
        return findByStatus(BookingActivity.STATUS_ONGOING);
    }

    /**
     * 获取待确认的活动
     */
    public List<BookingActivity> findPendingActivities() {
        return findByStatus(BookingActivity.STATUS_PENDING);
    }

    /**
     * 根据时间范围查找活动
     */
    public List<BookingActivity> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return findAll().stream()
                .filter(activity -> {
                    LocalDateTime activityStart = activity.getStartTime();
                    LocalDateTime activityEnd = activity.getEndTime();
                    return (activityStart.isAfter(startTime) || activityStart.isEqual(startTime)) &&
                           (activityEnd.isBefore(endTime) || activityEnd.isEqual(endTime));
                })
                .collect(Collectors.toList());
    }

    /**
     * 删除活动
     */
    public void deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return;
        }

        BookingActivity activity = findById(id);
        if (activity != null) {
            // 删除索引
            deleteActivityIndexes(activity);
        }

        // 删除活动数据
        String activityKey = RedisConfig.RedisKeys.activityKey(id);
        String participantsKey = RedisConfig.RedisKeys.activityParticipantsKey(id);
        
        redisTemplate.delete(activityKey);
        redisTemplate.delete(participantsKey);
    }

    /**
     * 检查活动是否存在
     */
    public boolean existsById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(RedisConfig.RedisKeys.activityKey(id)));
    }

    /**
     * 更新活动状态
     */
    public void updateStatus(String id, Integer newStatus) {
        if (id == null || id.trim().isEmpty()) {
            return;
        }
        
        BookingActivity activity = findById(id);
        if (activity != null) {
            Integer oldStatus = activity.getStatus();
            
            // 更新状态
            String key = RedisConfig.RedisKeys.activityKey(id);
            hashOps.put(key, "status", newStatus.toString());
            hashOps.put(key, "updatedAt", LocalDateTime.now().toString());
            
            // 更新索引
            String oldStatusIndexKey = "badminton:index:status:" + oldStatus;
            String newStatusIndexKey = "badminton:index:status:" + newStatus;
            redisTemplate.opsForSet().remove(oldStatusIndexKey, id);
            redisTemplate.opsForSet().add(newStatusIndexKey, id);
        }
    }

    /**
     * 更新参与人数
     */
    public void updateCurrentPlayers(String id, Integer currentPlayers) {
        if (id == null || id.trim().isEmpty()) {
            return;
        }
        
        String key = RedisConfig.RedisKeys.activityKey(id);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            hashOps.put(key, "currentPlayers", currentPlayers.toString());
            hashOps.put(key, "updatedAt", LocalDateTime.now().toString());
        }
    }

    /**
     * 添加活动参与者
     */
    public void addParticipant(String activityId, String userId) {
        if (activityId == null || userId == null) {
            return;
        }

        String key = RedisConfig.RedisKeys.activityParticipantsKey(activityId);
        redisTemplate.opsForSet().add(key, userId);
        redisTemplate.expire(key, RedisConfig.RedisTTL.ACTIVITY_CACHE, TimeUnit.SECONDS);
    }

    /**
     * 移除活动参与者
     */
    public void removeParticipant(String activityId, String userId) {
        if (activityId == null || userId == null) {
            return;
        }

        String key = RedisConfig.RedisKeys.activityParticipantsKey(activityId);
        redisTemplate.opsForSet().remove(key, userId);
    }

    /**
     * 获取活动参与者列表
     */
    public Set<String> getParticipants(String activityId) {
        if (activityId == null || activityId.trim().isEmpty()) {
            return Collections.emptySet();
        }

        String key = RedisConfig.RedisKeys.activityParticipantsKey(activityId);
        Set<Object> participants = redisTemplate.opsForSet().members(key);
        
        if (participants == null || participants.isEmpty()) {
            return Collections.emptySet();
        }

        return participants.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    /**
     * 创建活动索引
     */
    private void createActivityIndexes(BookingActivity activity) {
        if (activity.getOrganizer() != null && !activity.getOrganizer().trim().isEmpty()) {
            String organizerIndexKey = "badminton:index:organizer:" + activity.getOrganizer();
            redisTemplate.opsForSet().add(organizerIndexKey, activity.getId());
            redisTemplate.expire(organizerIndexKey, RedisConfig.RedisTTL.ACTIVITY_CACHE, TimeUnit.SECONDS);
        }
        
        if (activity.getStatus() != null) {
            String statusIndexKey = "badminton:index:status:" + activity.getStatus();
            redisTemplate.opsForSet().add(statusIndexKey, activity.getId());
            redisTemplate.expire(statusIndexKey, RedisConfig.RedisTTL.ACTIVITY_CACHE, TimeUnit.SECONDS);
        }
    }

    /**
     * 删除活动索引
     */
    private void deleteActivityIndexes(BookingActivity activity) {
        if (activity.getOrganizer() != null && !activity.getOrganizer().trim().isEmpty()) {
            String organizerIndexKey = "badminton:index:organizer:" + activity.getOrganizer();
            redisTemplate.opsForSet().remove(organizerIndexKey, activity.getId());
        }
        
        if (activity.getStatus() != null) {
            String statusIndexKey = "badminton:index:status:" + activity.getStatus();
            redisTemplate.opsForSet().remove(statusIndexKey, activity.getId());
        }
    }

    /**
     * 将BookingActivity对象转换为Map
     */
    private Map<String, Object> convertActivityToMap(BookingActivity activity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", activity.getId());
        map.put("title", activity.getTitle());
        map.put("organizer", activity.getOrganizer());
        map.put("venue", activity.getVenue());
        map.put("address", activity.getAddress() != null ? activity.getAddress() : "");
        map.put("startTime", activity.getStartTime().toString());
        map.put("endTime", activity.getEndTime().toString());
        map.put("maxPlayers", activity.getMaxPlayers().toString());
        map.put("currentPlayers", activity.getCurrentPlayers().toString());
        map.put("fee", activity.getFee().toString());
        map.put("description", activity.getDescription() != null ? activity.getDescription() : "");
        map.put("status", activity.getStatus().toString());
        map.put("tenant", activity.getTenant());
        map.put("state", activity.getState());
        map.put("createdAt", activity.getCreatedAt().toString());
        map.put("updatedAt", activity.getUpdatedAt().toString());
        map.put("deletedAt", activity.getDeletedAt() != null ? activity.getDeletedAt().toString() : null);
        map.put("organizationId", activity.getOrganizationId());
        return map;
    }

    /**
     * 将Map转换为BookingActivity对象
     */
    private BookingActivity convertMapToActivity(Map<Object, Object> map) {
        BookingActivity activity = new BookingActivity();
        
        activity.setId((String) map.get("id"));
        activity.setTitle((String) map.get("title"));
        activity.setOrganizer((String) map.get("organizer"));
        activity.setVenue((String) map.get("venue"));
        activity.setAddress((String) map.get("address"));
        
        Object startTimeStr = map.get("startTime");
        if (startTimeStr != null) {
            activity.setStartTime(LocalDateTime.parse(startTimeStr.toString()));
        }
        
        Object endTimeStr = map.get("endTime");
        if (endTimeStr != null) {
            activity.setEndTime(LocalDateTime.parse(endTimeStr.toString()));
        }
        
        Object maxPlayers = map.get("maxPlayers");
        if (maxPlayers instanceof Integer) {
            activity.setMaxPlayers((Integer) maxPlayers);
        } else if (maxPlayers instanceof String) {
            activity.setMaxPlayers(Integer.valueOf((String) maxPlayers));
        }
        
        Object currentPlayers = map.get("currentPlayers");
        if (currentPlayers instanceof Integer) {
            activity.setCurrentPlayers((Integer) currentPlayers);
        } else if (currentPlayers instanceof String) {
            activity.setCurrentPlayers(Integer.valueOf((String) currentPlayers));
        }
        
        Object feeStr = map.get("fee");
        if (feeStr != null) {
            activity.setFee(new BigDecimal(feeStr.toString()));
        }
        
        activity.setDescription((String) map.get("description"));
        
        Object status = map.get("status");
        if (status instanceof Integer) {
            activity.setStatus((Integer) status);
        } else if (status instanceof String) {
            activity.setStatus(Integer.valueOf((String) status));
        }
        
        Object tenant = map.get("tenant");
        if (tenant instanceof Integer) {
            activity.setTenant((Integer) tenant);
        } else if (tenant instanceof String) {
            activity.setTenant(Integer.valueOf((String) tenant));
        }
        
        Object state = map.get("state");
        if (state instanceof Integer) {
            activity.setState((Integer) state);
        } else if (state instanceof String) {
            activity.setState(Integer.valueOf((String) state));
        }
        
        Object createdAtStr = map.get("createdAt");
        if (createdAtStr != null) {
            activity.setCreatedAt(LocalDateTime.parse(createdAtStr.toString()));
        }
        
        Object updatedAtStr = map.get("updatedAt");
        if (updatedAtStr != null) {
            activity.setUpdatedAt(LocalDateTime.parse(updatedAtStr.toString()));
        }
        
        Object deletedAtStr = map.get("deletedAt");
        if (deletedAtStr != null && !deletedAtStr.toString().isEmpty()) {
            activity.setDeletedAt(LocalDateTime.parse(deletedAtStr.toString()));
        }
        
        Object organizationId = map.get("organizationId");
        if (organizationId instanceof Integer) {
            activity.setOrganizationId((Integer) organizationId);
        } else if (organizationId instanceof String && !((String) organizationId).isEmpty()) {
            activity.setOrganizationId(Integer.valueOf((String) organizationId));
        }
        
        return activity;
    }
}