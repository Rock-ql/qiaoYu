package cn.badminton.repository.jpa;

import cn.badminton.model.ExpenseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 费用记录JPA存储库接口
 * 提供基础的CRUD操作和自定义查询方法
 *
 * 作者: xiaolei
 */
@Repository
public interface ExpenseJpaRepository extends JpaRepository<ExpenseRecord, String> {

    /**
     * 根据活动ID查找费用记录
     */
    List<ExpenseRecord> findByActivityIdAndDeletedAtIsNull(String activityId);

    /**
     * 根据付款人查找费用记录
     */
    List<ExpenseRecord> findByPayerIdAndDeletedAtIsNull(String payerId);

    /**
     * 根据费用类型查找记录
     */
    List<ExpenseRecord> findByTypeAndDeletedAtIsNull(String type);

    /**
     * 查找指定金额范围内的费用记录
     */
    @Query("SELECT e FROM ExpenseRecord e WHERE e.totalAmount >= :minAmount AND e.totalAmount <= :maxAmount AND e.deletedAt IS NULL")
    List<ExpenseRecord> findByAmountRange(@Param("minAmount") BigDecimal minAmount,
                                        @Param("maxAmount") BigDecimal maxAmount);

    /**
     * 根据分摊方式查找记录
     */
    List<ExpenseRecord> findBySplitMethodAndDeletedAtIsNull(String splitMethod);

    /**
     * 查找指定时间范围内的费用记录
     */
    @Query("SELECT e FROM ExpenseRecord e WHERE e.createdAt >= :startTime AND e.createdAt <= :endTime AND e.deletedAt IS NULL")
    List<ExpenseRecord> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 统计活动的总费用
     */
    @Query("SELECT COALESCE(SUM(e.totalAmount), 0) FROM ExpenseRecord e WHERE e.activityId = :activityId AND e.deletedAt IS NULL")
    BigDecimal sumTotalAmountByActivityId(@Param("activityId") String activityId);

    /**
     * 统计用户支付的总金额
     */
    @Query("SELECT COALESCE(SUM(e.totalAmount), 0) FROM ExpenseRecord e WHERE e.payerId = :payerId AND e.deletedAt IS NULL")
    BigDecimal sumTotalAmountByPayerId(@Param("payerId") String payerId);

    /**
     * 根据费用类型统计总金额
     */
    @Query("SELECT e.type, COALESCE(SUM(e.totalAmount), 0) FROM ExpenseRecord e WHERE e.deletedAt IS NULL GROUP BY e.type")
    List<Object[]> sumAmountByType();

    /**
     * 查找最大金额的费用记录
     */
    @Query("SELECT e FROM ExpenseRecord e WHERE e.deletedAt IS NULL ORDER BY e.totalAmount DESC LIMIT :limit")
    List<ExpenseRecord> findTopExpensesByAmount(@Param("limit") int limit);

    /**
     * 查找最近创建的费用记录
     */
    List<ExpenseRecord> findTop10ByDeletedAtIsNullOrderByCreatedAtDesc();

    /**
     * 统计每月费用趋势
     */
    @Query("SELECT YEAR(e.createdAt), MONTH(e.createdAt), COALESCE(SUM(e.totalAmount), 0) " +
           "FROM ExpenseRecord e WHERE e.deletedAt IS NULL " +
           "GROUP BY YEAR(e.createdAt), MONTH(e.createdAt) " +
           "ORDER BY YEAR(e.createdAt) DESC, MONTH(e.createdAt) DESC")
    List<Object[]> findMonthlyExpenseTrends();

    /**
     * 根据描述模糊搜索费用记录
     */
    List<ExpenseRecord> findByDescriptionContainingAndDeletedAtIsNull(String description);
}