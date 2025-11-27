package com.coreledger.repository;

import com.coreledger.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 支付流水Repository接口
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long>, JpaSpecificationExecutor<PaymentRecord> {
}
