package com.coreledger.repository;

import com.coreledger.entity.LedgerItem;
import com.coreledger.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 账本明细 Repository
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface LedgerItemRepository extends JpaRepository<LedgerItem, Long> {

    /**
     * 根据账本ID查询所有有效明细
     *
     * @param ledgerId 账本ID
     * @param status 状态 (1=有效, 0=删除)
     * @return 明细列表
     */
    List<LedgerItem> findByLedgerIdAndStatus(Long ledgerId, Status status);

    /**
     * 根据账本ID删除所有明细（软删除）
     *
     * @param ledgerId 账本ID
     */
    void deleteByLedgerId(Long ledgerId);
}
