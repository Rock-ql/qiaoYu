package cn.badminton.repository.jpa;

import cn.badminton.model.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 参与记录JPA存储库接口
 * 提供基础的CRUD操作和自定义查询方法
 *
 * 作者: xiaolei
 */
@Repository
public interface ParticipationJpaRepository extends JpaRepository<Participation, String> {

    /**
     * 根据活动ID查找参与记录
     */
    List<Participation> findByActivityIdAndDeletedAtIsNull(String activityId);

    /**
     * 根据用户ID查找参与记录
     */
    List<Participation> findByUserIdAndDeletedAtIsNull(String userId);

    /**
     * 根据活动ID和用户ID查找参与记录
     */
    Optional<Participation> findByActivityIdAndUserIdAndDeletedAtIsNull(String activityId, String userId);

    /**
     * 根据状态查找参与记录
     */
    List<Participation> findByStatusAndDeletedAtIsNull(Integer status);

    /**
     * 查找活动的已确认参与者
     */
    @Query("SELECT p FROM Participation p WHERE p.activityId = :activityId AND p.status = 1 AND p.deletedAt IS NULL")
    List<Participation> findConfirmedParticipants(@Param("activityId") String activityId);

    /**
     * 查找活动的发起人参与记录
     */
    @Query("SELECT p FROM Participation p WHERE p.activityId = :activityId AND p.isOrganizer = true AND p.deletedAt IS NULL")
    Optional<Participation> findActivityOrganizer(@Param("activityId") String activityId);

    /**
     * 统计活动的参与人数
     */
    @Query("SELECT COUNT(p) FROM Participation p WHERE p.activityId = :activityId AND p.status = 1 AND p.deletedAt IS NULL")
    long countConfirmedParticipants(@Param("activityId") String activityId);

    /**
     * 统计用户参与的活动数量
     */
    @Query("SELECT COUNT(p) FROM Participation p WHERE p.userId = :userId AND p.status = 1 AND p.deletedAt IS NULL")
    long countUserActivities(@Param("userId") String userId);

    /**
     * 查找用户最近参与的活动
     */
    @Query("SELECT p FROM Participation p WHERE p.userId = :userId AND p.deletedAt IS NULL ORDER BY p.joinTime DESC LIMIT :limit")
    List<Participation> findRecentParticipations(@Param("userId") String userId, @Param("limit") int limit);

    /**
     * 查找指定时间范围内的参与记录
     */
    @Query("SELECT p FROM Participation p WHERE p.joinTime >= :startTime AND p.joinTime <= :endTime AND p.deletedAt IS NULL")
    List<Participation> findByJoinTimeBetween(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 检查用户是否已参与活动
     */
    @Query("SELECT COUNT(p) > 0 FROM Participation p WHERE p.activityId = :activityId AND p.userId = :userId AND p.status = 1 AND p.deletedAt IS NULL")
    boolean existsConfirmedParticipation(@Param("activityId") String activityId, @Param("userId") String userId);

    /**
     * 查找活跃参与者（参与活动最多的用户）
     */
    @Query("SELECT p.userId, COUNT(p) as participationCount FROM Participation p WHERE p.status = 1 AND p.deletedAt IS NULL GROUP BY p.userId ORDER BY participationCount DESC LIMIT :limit")
    List<Object[]> findMostActiveParticipants(@Param("limit") int limit);
}