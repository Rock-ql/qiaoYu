package cn.badminton.repository.jpa;

import cn.badminton.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户JPA存储库接口
 * 提供基础的CRUD操作和自定义查询方法
 *
 * 作者: xiaolei
 */
@Repository
public interface UserJpaRepository extends JpaRepository<User, String> {

    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhone(String phone);

    /**
     * 根据微信OpenID查找用户
     */
    Optional<User> findByWxOpenId(String wxOpenId);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 检查微信OpenID是否存在
     */
    boolean existsByWxOpenId(String wxOpenId);

    /**
     * 根据状态查找用户
     */
    List<User> findByStatus(Integer status);

    /**
     * 查找活跃用户（状态为1且未删除）
     */
    @Query("SELECT u FROM User u WHERE u.status = 1 AND u.deletedAt IS NULL")
    List<User> findActiveUsers();

    /**
     * 根据昵称模糊搜索用户
     */
    @Query("SELECT u FROM User u WHERE u.nickname LIKE %:nickname% AND u.deletedAt IS NULL")
    List<User> findByNicknameLike(@Param("nickname") String nickname);

    /**
     * 统计活跃用户总数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 1 AND u.deletedAt IS NULL")
    long countActiveUsers();

    /**
     * 查找参与活动数最多的前N个用户
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL ORDER BY u.totalActivities DESC LIMIT :limit")
    List<User> findTopUsersByActivities(@Param("limit") int limit);

    /**
     * 查找消费金额最多的前N个用户
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL ORDER BY u.totalExpense DESC LIMIT :limit")
    List<User> findTopUsersByExpense(@Param("limit") int limit);
}