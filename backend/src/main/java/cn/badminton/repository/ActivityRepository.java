package cn.badminton.repository;

import cn.badminton.model.BookingActivity;
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
import java.util.stream.Collectors;

/**
 * 活动Redis存储库
 * 存储结构: badminton:activity:{activity_id} (Hash)
 * 
 * 作者: xiaolei
 */
@Repository
public class ActivityRepository {

    private static final String KEY_PREFIX = "badminton:activity:";
    private static final String ORGANIZER_INDEX_PREFIX = "badminton:activity:organizer:";
    private static final String STATUS_INDEX_PREFIX = "badminton:activity:status:";

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
        activity.setUpdatedAt(LocalDateTime.now());

        String key = KEY_PREFIX + activity.getId();
        
        // 保存活动数据
        hashOps.put(key, "id", activity.getId());
        hashOps.put(key, "title", activity.getTitle());
        hashOps.put(key, "organizer", activity.getOrganizer());
        hashOps.put(key, "venue", activity.getVenue());
        hashOps.put(key, "address", activity.getAddress() != null ? activity.getAddress() : "");
        hashOps.put(key, "startTime", activity.getStartTime().toString());
        hashOps.put(key, "endTime", activity.getEndTime().toString());
        hashOps.put(key, "maxPlayers", activity.getMaxPlayers().toString());
        hashOps.put(key, "currentPlayers", activity.getCurrentPlayers().toString());
        hashOps.put(key, "fee", activity.getFee().toString());
        hashOps.put(key, "description", activity.getDescription() != null ? activity.getDescription() : "");
        hashOps.put(key, "status", activity.getStatus().toString());
        hashOps.put(key, "createdAt", activity.getCreatedAt().toString());
        hashOps.put(key, "updatedAt", activity.getUpdatedAt().toString());

        // 创建索引
        redisTemplate.opsForSet().add(ORGANIZER_INDEX_PREFIX + activity.getOrganizer(), activity.getId());
        redisTemplate.opsForSet().add(STATUS_INDEX_PREFIX + activity.getStatus(), activity.getId());

        return activity;
    }

    /**
     * 根据ID查找活动
     */
    public BookingActivity findById(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        String key = KEY_PREFIX + id;
        Map<String, Object> activityMap = hashOps.entries(key);
        
        if (activityMap.isEmpty()) {
            return null;
        }

        return mapToActivity(activityMap);
    }

    /**
     * 根据发起人查找活动
     */
    public List<BookingActivity> findByOrganizer(String organizer) {
        if (organizer == null || organizer.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Object> activityIds = redisTemplate.opsForSet().members(ORGANIZER_INDEX_PREFIX + organizer);
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

        Set<Object> activityIds = redisTemplate.opsForSet().members(STATUS_INDEX_PREFIX + status);
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
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        List<BookingActivity> activities = new ArrayList<>();
        
        if (keys != null) {
            for (String key : keys) {
                String activityId = key.substring(KEY_PREFIX.length());
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
        if (id == null || id.isEmpty()) {
            return;
        }

        BookingActivity activity = findById(id);
        if (activity != null) {
            // 删除索引
            redisTemplate.opsForSet().remove(ORGANIZER_INDEX_PREFIX + activity.getOrganizer(), id);
            redisTemplate.opsForSet().remove(STATUS_INDEX_PREFIX + activity.getStatus(), id);
        }

        // 删除活动数据
        redisTemplate.delete(KEY_PREFIX + id);
    }

    /**
     * 检查活动是否存在
     */
    public boolean existsById(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + id));
    }

    /**
     * 更新活动状态
     */
    public void updateStatus(String id, Integer newStatus) {
        if (id == null || id.isEmpty()) {
            return;
        }
        
        BookingActivity activity = findById(id);
        if (activity != null) {
            Integer oldStatus = activity.getStatus();
            
            // 更新状态
            String key = KEY_PREFIX + id;
            hashOps.put(key, "status", newStatus.toString());
            hashOps.put(key, "updatedAt", LocalDateTime.now().toString());
            
            // 更新索引
            redisTemplate.opsForSet().remove(STATUS_INDEX_PREFIX + oldStatus, id);
            redisTemplate.opsForSet().add(STATUS_INDEX_PREFIX + newStatus, id);
        }
    }

    /**
     * 更新参与人数
     */
    public void updateCurrentPlayers(String id, Integer currentPlayers) {
        if (id == null || id.isEmpty()) {
            return;
        }
        
        String key = KEY_PREFIX + id;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            hashOps.put(key, "currentPlayers", currentPlayers.toString());
            hashOps.put(key, "updatedAt", LocalDateTime.now().toString());
        }
    }

    /**
     * 转换Map为BookingActivity对象
     */
    private BookingActivity mapToActivity(Map<String, Object> activityMap) {
        BookingActivity activity = new BookingActivity();
        
        activity.setId((String) activityMap.get("id"));
        activity.setTitle((String) activityMap.get("title"));
        activity.setOrganizer((String) activityMap.get("organizer"));
        activity.setVenue((String) activityMap.get("venue"));
        activity.setAddress((String) activityMap.get("address"));
        activity.setStartTime(LocalDateTime.parse((String) activityMap.get("startTime")));
        activity.setEndTime(LocalDateTime.parse((String) activityMap.get("endTime")));
        activity.setMaxPlayers(Integer.valueOf((String) activityMap.get("maxPlayers")));
        activity.setCurrentPlayers(Integer.valueOf((String) activityMap.get("currentPlayers")));
        activity.setFee(Double.valueOf((String) activityMap.get("fee")));
        activity.setDescription((String) activityMap.get("description"));
        activity.setStatus(Integer.valueOf((String) activityMap.get("status")));
        activity.setCreatedAt(LocalDateTime.parse((String) activityMap.get("createdAt")));
        activity.setUpdatedAt(LocalDateTime.parse((String) activityMap.get("updatedAt")));
        
        return activity;
    }
}