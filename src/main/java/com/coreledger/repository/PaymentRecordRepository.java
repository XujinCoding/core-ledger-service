package com.coreledger.repository;

import com.coreledger.entity.PaymentRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 支付流水 Repository
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    /**
     * 根据ID和状态查询支付记录
     *
     * @param id 支付记录ID
     * @param status 状态 (1=有效, 0=删除)
     * @return 支付记录
     */
    Optional<PaymentRecord> findByIdAndStatus(Long id, Integer status);

    /**
     * 根据账本ID查询所有有效支付记录
     *
     * @param ledgerId 账本ID
     * @param status 状态 (1=有效)
     * @return 支付记录列表
     */
    List<PaymentRecord> findByLedgerIdAndStatus(Long ledgerId, Integer status);

    /**
     * 查询支付流水（分页）
     *
     * @param status 状态 (1=有效)
     * @param pageable 分页参数
     * @return 支付记录分页列表
     */
    Page<PaymentRecord> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据账本ID查询支付流水（分页）
     *
     * @param ledgerId 账本ID
     * @param status 状态 (1=有效)
     * @param pageable 分页参数
     * @return 支付记录分页列表
     */
    Page<PaymentRecord> findByLedgerIdAndStatus(Long ledgerId, Integer status, Pageable pageable);
}
