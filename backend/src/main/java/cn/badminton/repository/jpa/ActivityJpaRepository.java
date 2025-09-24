package cn.badminton.repository.jpa;

import cn.badminton.model.BookingActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动JPA存储库接口
 * 提供基础的CRUD操作和自定义查询方法
 *
 * 作者: xiaolei
 */
@Repository
public interface ActivityJpaRepository extends JpaRepository<BookingActivity, String> {

    /**
     * 根据发起人查找活动
     */
    List<BookingActivity> findByOrganizerAndDeletedAtIsNull(String organizer);

    /**
     * 根据状态查找活动
     */
    List<BookingActivity> findByStatusAndDeletedAtIsNull(Integer status);

    /**
     * 根据场地查找活动
     */
    List<BookingActivity> findByVenueContainingAndDeletedAtIsNull(String venue);

    /**
     * 查找指定时间范围内的活动
     */
    @Query("SELECT a FROM BookingActivity a WHERE a.startTime >= :startTime AND a.endTime <= :endTime AND a.deletedAt IS NULL")
    List<BookingActivity> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 查找今天的活动
     */
    @Query("SELECT a FROM BookingActivity a WHERE DATE(a.startTime) = CURRENT_DATE AND a.deletedAt IS NULL")
    List<BookingActivity> findTodayActivities();

    /**
     * 查找即将开始的活动（1小时内）
     */
    @Query("SELECT a FROM BookingActivity a WHERE a.startTime > CURRENT_TIMESTAMP AND a.startTime <= :oneHourLater AND a.status = 1 AND a.deletedAt IS NULL")
    List<BookingActivity> findUpcomingActivities(@Param("oneHourLater") LocalDateTime oneHourLater);

    /**
     * 查找已过期但状态未更新的活动
     */
    @Query("SELECT a FROM BookingActivity a WHERE a.endTime < CURRENT_TIMESTAMP AND a.status IN (1, 2) AND a.deletedAt IS NULL")
    List<BookingActivity> findExpiredActivities();

    /**
     * 查找还有空位的活动
     */
    @Query("SELECT a FROM BookingActivity a WHERE a.currentPlayers < a.maxPlayers AND a.status = 1 AND a.deletedAt IS NULL")
    List<BookingActivity> findAvailableActivities();

    /**
     * 根据标题模糊搜索活动
     */
    List<BookingActivity> findByTitleContainingAndDeletedAtIsNull(String title);

    /**
     * 统计用户发起的活动数量
     */
    long countByOrganizerAndDeletedAtIsNull(String organizer);

    /**
     * 查找最近创建的活动
     */
    List<BookingActivity> findTop10ByDeletedAtIsNullOrderByCreatedAtDesc();

    /**
     * 查找热门活动（参与人数多的）
     */
    @Query("SELECT a FROM BookingActivity a WHERE a.deletedAt IS NULL ORDER BY a.currentPlayers DESC LIMIT :limit")
    List<BookingActivity> findPopularActivities(@Param("limit") int limit);
}