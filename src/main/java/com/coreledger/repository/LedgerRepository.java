package com.coreledger.repository;

import com.coreledger.entity.Ledger;
import com.coreledger.enums.LedgerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 账本 Repository
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface LedgerRepository extends JpaRepository<Ledger, Long> {

    /**
     * 根据ID和状态查询账本
     *
     * @param id 账本ID
     * @param status 状态 (1=有效, 0=删除)
     * @return 账本
     */
    Optional<Ledger> findByIdAndStatus(Long id, Integer status);

    /**
     * 查询活跃账本（主页面）
     * 状态为 IN_PROGRESS 或 PARTIAL
     *
     * @param statuses 账本状态列表
     * @param status 数据状态 (1=有效)
     * @param pageable 分页参数
     * @return 账本分页列表
     */
    Page<Ledger> findByLedgerStatusInAndStatus(List<LedgerStatus> statuses, Integer status, Pageable pageable);

    /**
     * 查询客户的所有账本
     *
     * @param customerId 客户ID
     * @param status 数据状态 (1=有效)
     * @param pageable 分页参数
     * @return 账本分页列表
     */
    Page<Ledger> findByCustomerIdAndStatus(Long customerId, Integer status, Pageable pageable);

    /**
     * 查询指定状态的账本
     *
     * @param ledgerStatus 账本状态
     * @param status 数据状态 (1=有效)
     * @param pageable 分页参数
     * @return 账本分页列表
     */
    Page<Ledger> findByLedgerStatusAndStatus(LedgerStatus ledgerStatus, Integer status, Pageable pageable);
}
