package com.coreledger.repository;

import com.coreledger.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 支付流水Repository接口
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long>, JpaSpecificationExecutor<PaymentRecord> {

    /**
     * 根据账本ID和状态查询支付记录列表
     *
     * @param ledgerId 账本ID
     * @param status 状态（1=有效, 0=已删除）
     * @return 支付记录列表
     */
    List<PaymentRecord> findByLedgerIdAndStatus(Long ledgerId, Integer status);
}
