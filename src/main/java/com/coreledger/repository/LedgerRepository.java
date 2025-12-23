package com.coreledger.repository;

import com.coreledger.entity.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * 账本Repository接口
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface LedgerRepository extends JpaRepository<Ledger, Long>, JpaSpecificationExecutor<Ledger> {

    /**
     * 统计客户的订单数量
     *
     * @param customerId 客户ID
     * @return 订单数量
     */
    @Query("SELECT COUNT(l) FROM Ledger l WHERE l.customerId = :customerId")
    Integer countByCustomerId(@Param("customerId") Long customerId);

    /**
     * 统计客户的总消费金额（已支付金额）
     *
     * @param customerId 客户ID
     * @return 总消费金额
     */
    @Query("SELECT COALESCE(SUM(l.paidAmount), 0) FROM Ledger l WHERE l.customerId = :customerId")
    BigDecimal sumPaidAmountByCustomerId(@Param("customerId") Long customerId);
}
