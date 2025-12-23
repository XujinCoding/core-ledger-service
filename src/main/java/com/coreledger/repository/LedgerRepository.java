package com.coreledger.repository;

import com.coreledger.entity.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账本Repository接口
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface LedgerRepository extends JpaRepository<Ledger, Long>, JpaSpecificationExecutor<Ledger> {

    // ==================== 商户统计查询 ====================

    /**
     * 统计商户本月销售总额（totalAmount）
     */
    @Query("SELECT COALESCE(SUM(l.totalAmount), 0) FROM Ledger l " +
           "WHERE l.merchantId = :merchantId " +
           "AND l.createInstant >= :startTime " +
           "AND l.createInstant < :endTime")
    BigDecimal sumMonthlySales(@Param("merchantId") Long merchantId,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    /**
     * 统计商户待收款金额（赊账中账单的未付金额）
     */
    @Query("SELECT COALESCE(SUM(l.totalAmount - l.paidAmount - l.discountAmount), 0) FROM Ledger l " +
           "WHERE l.merchantId = :merchantId " +
           "AND l.ledgerStatus = com.coreledger.enums.LedgerStatus.ON_CREDIT")
    BigDecimal sumPendingAmount(@Param("merchantId") Long merchantId);

    /**
     * 统计商户本月订单数
     */
    @Query("SELECT COUNT(l) FROM Ledger l " +
           "WHERE l.merchantId = :merchantId " +
           "AND l.createInstant >= :startTime " +
           "AND l.createInstant < :endTime")
    Integer countMonthlyOrders(@Param("merchantId") Long merchantId,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    /**
     * 统计商户今日销售总额
     */
    @Query("SELECT COALESCE(SUM(l.totalAmount), 0) FROM Ledger l " +
           "WHERE l.merchantId = :merchantId " +
           "AND l.createInstant >= :startTime " +
           "AND l.createInstant < :endTime")
    BigDecimal sumTodaySales(@Param("merchantId") Long merchantId,
                             @Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime);

    /**
     * 统计商户今日已收款
     */
    @Query("SELECT COALESCE(SUM(l.paidAmount), 0) FROM Ledger l " +
           "WHERE l.merchantId = :merchantId " +
           "AND l.createInstant >= :startTime " +
           "AND l.createInstant < :endTime")
    BigDecimal sumTodayPayment(@Param("merchantId") Long merchantId,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    /**
     * 统计商户今日新增欠款（赊账中账单的未付金额，仅今日创建）
     */
    @Query("SELECT COALESCE(SUM(l.totalAmount - l.paidAmount - l.discountAmount), 0) FROM Ledger l " +
           "WHERE l.merchantId = :merchantId " +
           "AND l.ledgerStatus = com.coreledger.enums.LedgerStatus.ON_CREDIT " +
           "AND l.createInstant >= :startTime " +
           "AND l.createInstant < :endTime")
    BigDecimal sumTodayDebt(@Param("merchantId") Long merchantId,
                            @Param("startTime") LocalDateTime startTime,
                            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计商户今日订单数
     */
    @Query("SELECT COUNT(l) FROM Ledger l " +
           "WHERE l.merchantId = :merchantId " +
           "AND l.createInstant >= :startTime " +
           "AND l.createInstant < :endTime")
    Integer countTodayOrders(@Param("merchantId") Long merchantId,
                             @Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime);

    /**
     * 统计客户的订单数量
     *
     * @param customerId 客户ID
     * @return 订单数量
     */
    @Query("SELECT COUNT(l) FROM Ledger l WHERE l.customerId = :customerId")
    Integer countByCustomerId(@Param("customerId") Long customerId);

    /**
     * 统计商户的账单数量
     *
     * @param merchantId 商户ID
     * @return 账单数量
     */
    long countByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 统计客户的总消费金额（已支付金额）
     *
     * @param customerId 客户ID
     * @return 总消费金额
     */
    @Query("SELECT COALESCE(SUM(l.paidAmount), 0) FROM Ledger l WHERE l.customerId = :customerId")
    BigDecimal sumPaidAmountByCustomerId(@Param("customerId") Long customerId);
}
