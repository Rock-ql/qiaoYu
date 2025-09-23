package cn.badminton.service;

import cn.badminton.model.BookingActivity;
import cn.badminton.model.Participation;
import cn.badminton.repository.ActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动管理服务
 * 负责约球活动的创建、管理、参与等核心业务逻辑
 * 
 * 作者: xiaolei
 */
@Service
public class ActivityService {
    
    private static final Logger logger = LoggerFactory.getLogger(ActivityService.class);
    
    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private UserService userService;

    /**
     * 创建约球活动
     */
    public BookingActivity createActivity(String organizerId, String title, String venue, 
                                        LocalDateTime startTime, LocalDateTime endTime, 
                                        Integer maxPlayers, String description, String address) {
        logger.info("创建约球活动，发起人: {}, 标题: {}", organizerId, title);
        
        try {
            // 参数验证
            if (organizerId == null || organizerId.trim().isEmpty()) {
                throw new IllegalArgumentException("发起人不能为空");
            }
            
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("活动标题不能为空");
            }
            
            if (venue == null || venue.trim().isEmpty()) {
                throw new IllegalArgumentException("场地名称不能为空");
            }
            
            if (startTime == null || endTime == null) {
                throw new IllegalArgumentException("开始时间和结束时间不能为空");
            }
            
            if (startTime.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("开始时间不能早于当前时间");
            }
            
            if (endTime.isBefore(startTime)) {
                throw new IllegalArgumentException("结束时间不能早于开始时间");
            }
            
            if (maxPlayers == null || maxPlayers < 2) {
                throw new IllegalArgumentException("最大人数不能少于2人");
            }
            
            // 验证发起人是否存在
            if (userService.findById(organizerId) == null) {
                throw new IllegalArgumentException("发起人不存在");
            }
            
            // 创建活动
            BookingActivity activity = new BookingActivity(title.trim(), organizerId, venue.trim(), 
                                                         startTime, endTime, maxPlayers);
            if (description != null && !description.trim().isEmpty()) {
                activity.setDescription(description.trim());
            }
            if (address != null && !address.trim().isEmpty()) {
                activity.setAddress(address.trim());
            }
            
            activity = activityRepository.save(activity);
            
            // 增加发起人的活动参与次数
            userService.incrementUserActivities(organizerId);
            
            logger.info("创建约球活动成功，活动ID: {}", activity.getId());
            return activity;
            
        } catch (Exception e) {
            logger.error("创建约球活动失败，发起人: {}, 错误信息: {}", organizerId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 参加活动
     */
    public boolean joinActivity(String activityId, String userId, String remark) {
        logger.info("用户参加活动，活动ID: {}, 用户ID: {}", activityId, userId);
        
        try {
            // 获取活动
            BookingActivity activity = activityRepository.findById(activityId);
            if (activity == null) {
                throw new IllegalArgumentException("活动不存在");
            }
            
            // 验证用户是否存在
            if (userService.findById(userId) == null) {
                throw new IllegalArgumentException("用户不存在");
            }
            
            // 检查活动是否可以参加
            if (!activity.canJoin()) {
                throw new IllegalArgumentException("活动不可参加（可能已满员或已结束）");
            }
            
            // 检查用户是否已经参加
            if (activity.getOrganizer().equals(userId)) {
                throw new IllegalArgumentException("发起人无需重复参加");
            }
            
            // 增加参与人数
            if (!activity.addPlayer()) {
                throw new IllegalArgumentException("活动已满员");
            }
            
            // 保存活动
            activityRepository.save(activity);
            
            // 增加用户的活动参与次数
            userService.incrementUserActivities(userId);
            
            logger.info("用户参加活动成功，活动ID: {}, 用户ID: {}", activityId, userId);
            return true;
            
        } catch (Exception e) {
            logger.error("用户参加活动失败，活动ID: {}, 用户ID: {}, 错误信息: {}", activityId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 退出活动
     */
    public boolean leaveActivity(String activityId, String userId) {
        logger.info("用户退出活动，活动ID: {}, 用户ID: {}", activityId, userId);
        
        try {
            // 获取活动
            BookingActivity activity = activityRepository.findById(activityId);
            if (activity == null) {
                throw new IllegalArgumentException("活动不存在");
            }
            
            // 发起人不能退出自己的活动
            if (activity.getOrganizer().equals(userId)) {
                throw new IllegalArgumentException("发起人不能退出自己的活动，请取消活动");
            }
            
            // 检查活动状态
            if (activity.getStatus() == BookingActivity.STATUS_COMPLETED || 
                activity.getStatus() == BookingActivity.STATUS_CANCELLED) {
                throw new IllegalArgumentException("已结束或已取消的活动不能退出");
            }
            
            // 减少参与人数
            if (activity.removePlayer()) {
                activityRepository.save(activity);
                logger.info("用户退出活动成功，活动ID: {}, 用户ID: {}", activityId, userId);
                return true;
            } else {
                throw new IllegalArgumentException("退出活动失败");
            }
            
        } catch (Exception e) {
            logger.error("用户退出活动失败，活动ID: {}, 用户ID: {}, 错误信息: {}", activityId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 取消活动
     */
    public void cancelActivity(String activityId, String userId) {
        logger.info("取消活动，活动ID: {}, 用户ID: {}", activityId, userId);
        
        try {
            // 获取活动
            BookingActivity activity = activityRepository.findById(activityId);
            if (activity == null) {
                throw new IllegalArgumentException("活动不存在");
            }
            
            // 检查权限（只有发起人可以取消活动）
            if (!activity.getOrganizer().equals(userId)) {
                throw new IllegalArgumentException("只有活动发起人可以取消活动");
            }
            
            // 检查活动是否可以取消
            if (!activity.canCancel()) {
                throw new IllegalArgumentException("活动不可取消（可能已完成）");
            }
            
            // 更新活动状态
            activity.setStatus(BookingActivity.STATUS_CANCELLED);
            activityRepository.save(activity);
            
            logger.info("取消活动成功，活动ID: {}", activityId);
            
        } catch (Exception e) {
            logger.error("取消活动失败，活动ID: {}, 用户ID: {}, 错误信息: {}", activityId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 开始活动
     */
    public void startActivity(String activityId, String userId) {
        logger.info("开始活动，活动ID: {}, 用户ID: {}", activityId, userId);
        
        try {
            // 获取活动
            BookingActivity activity = activityRepository.findById(activityId);
            if (activity == null) {
                throw new IllegalArgumentException("活动不存在");
            }
            
            // 检查权限（只有发起人可以开始活动）
            if (!activity.getOrganizer().equals(userId)) {
                throw new IllegalArgumentException("只有活动发起人可以开始活动");
            }
            
            // 检查活动状态
            if (activity.getStatus() != BookingActivity.STATUS_PENDING) {
                throw new IllegalArgumentException("只有待确认的活动可以开始");
            }
            
            // 更新活动状态
            activity.setStatus(BookingActivity.STATUS_ONGOING);
            activityRepository.save(activity);
            
            logger.info("开始活动成功，活动ID: {}", activityId);
            
        } catch (Exception e) {
            logger.error("开始活动失败，活动ID: {}, 用户ID: {}, 错误信息: {}", activityId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 完成活动
     */
    public void completeActivity(String activityId, String userId) {
        logger.info("完成活动，活动ID: {}, 用户ID: {}", activityId, userId);
        
        try {
            // 获取活动
            BookingActivity activity = activityRepository.findById(activityId);
            if (activity == null) {
                throw new IllegalArgumentException("活动不存在");
            }
            
            // 检查权限（只有发起人可以完成活动）
            if (!activity.getOrganizer().equals(userId)) {
                throw new IllegalArgumentException("只有活动发起人可以完成活动");
            }
            
            // 检查活动状态
            if (activity.getStatus() != BookingActivity.STATUS_ONGOING) {
                throw new IllegalArgumentException("只有进行中的活动可以完成");
            }
            
            // 更新活动状态
            activity.setStatus(BookingActivity.STATUS_COMPLETED);
            activityRepository.save(activity);
            
            logger.info("完成活动成功，活动ID: {}", activityId);
            
        } catch (Exception e) {
            logger.error("完成活动失败，活动ID: {}, 用户ID: {}, 错误信息: {}", activityId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 根据ID获取活动
     */
    public BookingActivity getActivityById(String activityId) {
        logger.debug("根据ID获取活动: {}", activityId);
        
        try {
            BookingActivity activity = activityRepository.findById(activityId);
            logger.debug("获取活动结果: {}", activity != null ? "找到" : "未找到");
            return activity;
            
        } catch (Exception e) {
            logger.error("根据ID获取活动失败，活动ID: {}, 错误信息: {}", activityId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取所有活动
     */
    public List<BookingActivity> getAllActivities() {
        logger.debug("获取所有活动");
        
        try {
            List<BookingActivity> activities = activityRepository.findAll();
            logger.debug("获取所有活动成功，活动数量: {}", activities.size());
            return activities;
            
        } catch (Exception e) {
            logger.error("获取所有活动失败，错误信息: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 根据状态获取活动
     */
    public List<BookingActivity> getActivitiesByStatus(Integer status) {
        logger.debug("根据状态获取活动，状态: {}", status);
        
        try {
            List<BookingActivity> activities = activityRepository.findByStatus(status);
            logger.debug("根据状态获取活动成功，活动数量: {}", activities.size());
            return activities;
            
        } catch (Exception e) {
            logger.error("根据状态获取活动失败，状态: {}, 错误信息: {}", status, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取用户发起的活动
     */
    public List<BookingActivity> getUserActivities(String userId) {
        logger.debug("获取用户发起的活动，用户ID: {}", userId);
        
        try {
            List<BookingActivity> activities = activityRepository.findByOrganizer(userId);
            logger.debug("获取用户发起的活动成功，活动数量: {}", activities.size());
            return activities;
            
        } catch (Exception e) {
            logger.error("获取用户发起的活动失败，用户ID: {}, 错误信息: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取可参加的活动（待确认且未满员）
     */
    public List<BookingActivity> getAvailableActivities() {
        logger.debug("获取可参加的活动");
        
        try {
            List<BookingActivity> allActivities = activityRepository.findByStatus(BookingActivity.STATUS_PENDING);
            List<BookingActivity> availableActivities = allActivities.stream()
                    .filter(activity -> activity.canJoin())
                    .collect(Collectors.toList());
            
            logger.debug("获取可参加的活动成功，活动数量: {}", availableActivities.size());
            return availableActivities;
            
        } catch (Exception e) {
            logger.error("获取可参加的活动失败，错误信息: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 根据时间范围获取活动
     */
    public List<BookingActivity> getActivitiesByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        logger.debug("根据时间范围获取活动，开始时间: {}, 结束时间: {}", startTime, endTime);
        
        try {
            List<BookingActivity> activities = activityRepository.findByTimeRange(startTime, endTime);
            logger.debug("根据时间范围获取活动成功，活动数量: {}", activities.size());
            return activities;
            
        } catch (Exception e) {
            logger.error("根据时间范围获取活动失败，错误信息: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 更新活动信息
     */
    public BookingActivity updateActivity(BookingActivity activity) {
        logger.info("更新活动信息，活动ID: {}", activity.getId());
        
        try {
            // 验证活动是否存在
            BookingActivity existingActivity = activityRepository.findById(activity.getId());
            if (existingActivity == null) {
                throw new IllegalArgumentException("活动不存在");
            }
            
            // 验证数据有效性
            if (!activity.isValid()) {
                throw new IllegalArgumentException("活动数据无效");
            }
            
            // 只有待确认的活动可以修改
            if (existingActivity.getStatus() != BookingActivity.STATUS_PENDING) {
                throw new IllegalArgumentException("只有待确认的活动可以修改");
            }
            
            BookingActivity updatedActivity = activityRepository.save(activity);
            logger.info("活动信息更新成功，活动ID: {}", activity.getId());
            return updatedActivity;
            
        } catch (Exception e) {
            logger.error("更新活动信息失败，活动ID: {}, 错误信息: {}", activity.getId(), e.getMessage(), e);
            throw e;
        }
    }
}