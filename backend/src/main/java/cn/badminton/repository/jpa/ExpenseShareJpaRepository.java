package cn.badminton.repository.jpa;

import cn.badminton.model.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 费用分摊JPA存储库接口
 * 提供基础的CRUD操作和自定义查询方法
 *
 * 作者: xiaolei
 */
@Repository
public interface ExpenseShareJpaRepository extends JpaRepository<ExpenseShare, String> {

    /**
     * 根据费用记录ID查找分摊记录
     */
    List<ExpenseShare> findByExpenseIdAndDeletedAtIsNull(String expenseId);

    /**
     * 根据用户ID查找分摊记录
     */
    List<ExpenseShare> findByUserIdAndDeletedAtIsNull(String userId);

    /**
     * 根据状态查找分摊记录
     */
    List<ExpenseShare> findByStatusAndDeletedAtIsNull(Integer status);

    /**
     * 查找待结算的分摊记录
     */
    @Query("SELECT s FROM ExpenseShare s WHERE s.status = 1 AND s.deletedAt IS NULL")
    List<ExpenseShare> findPendingShares();

    /**
     * 查找已结算的分摊记录
     */
    @Query("SELECT s FROM ExpenseShare s WHERE s.status = 2 AND s.deletedAt IS NULL")
    List<ExpenseShare> findSettledShares();

    /**
     * 查找用户的待结算分摊
     */
    @Query("SELECT s FROM ExpenseShare s WHERE s.userId = :userId AND s.status = 1 AND s.deletedAt IS NULL")
    List<ExpenseShare> findPendingSharesByUser(@Param("userId") String userId);

    /**
     * 统计费用记录的分摊总金额
     */
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM ExpenseShare s WHERE s.expenseId = :expenseId AND s.deletedAt IS NULL")
    BigDecimal sumAmountByExpenseId(@Param("expenseId") String expenseId);

    /**
     * 统计用户的总分摊金额
     */
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM ExpenseShare s WHERE s.userId = :userId AND s.deletedAt IS NULL")
    BigDecimal sumAmountByUserId(@Param("userId") String userId);

    /**
     * 统计用户已结算的分摊金额
     */
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM ExpenseShare s WHERE s.userId = :userId AND s.status = 2 AND s.deletedAt IS NULL")
    BigDecimal sumSettledAmountByUserId(@Param("userId") String userId);

    /**
     * 统计用户待结算的分摊金额
     */
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM ExpenseShare s WHERE s.userId = :userId AND s.status = 1 AND s.deletedAt IS NULL")
    BigDecimal sumPendingAmountByUserId(@Param("userId") String userId);

    /**
     * 查找指定时间范围内结算的分摊记录
     */
    @Query("SELECT s FROM ExpenseShare s WHERE s.settledAt >= :startTime AND s.settledAt <= :endTime AND s.deletedAt IS NULL")
    List<ExpenseShare> findBySettledAtBetween(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计费用分摊的人数
     */
    @Query("SELECT COUNT(s) FROM ExpenseShare s WHERE s.expenseId = :expenseId AND s.deletedAt IS NULL")
    long countSharesByExpenseId(@Param("expenseId") String expenseId);

    /**
     * 检查费用是否已完全结算
     */
    @Query("SELECT COUNT(s) = 0 FROM ExpenseShare s WHERE s.expenseId = :expenseId AND s.status = 1 AND s.deletedAt IS NULL")
    boolean isExpenseFullySettled(@Param("expenseId") String expenseId);

    /**
     * 查找金额最大的分摊记录
     */
    @Query("SELECT s FROM ExpenseShare s WHERE s.deletedAt IS NULL ORDER BY s.amount DESC LIMIT :limit")
    List<ExpenseShare> findTopSharesByAmount(@Param("limit") int limit);

    /**
     * 查找最近结算的分摊记录
     */
    @Query("SELECT s FROM ExpenseShare s WHERE s.status = 2 AND s.deletedAt IS NULL ORDER BY s.settledAt DESC LIMIT :limit")
    List<ExpenseShare> findRecentSettledShares(@Param("limit") int limit);

    /**
     * 统计每个用户的分摊统计信息
     */
    @Query("SELECT s.userId, COUNT(s), COALESCE(SUM(s.amount), 0), " +
           "SUM(CASE WHEN s.status = 1 THEN s.amount ELSE 0 END), " +
           "SUM(CASE WHEN s.status = 2 THEN s.amount ELSE 0 END) " +
           "FROM ExpenseShare s WHERE s.deletedAt IS NULL GROUP BY s.userId")
    List<Object[]> getUserShareStatistics();
}