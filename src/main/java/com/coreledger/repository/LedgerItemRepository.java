package com.coreledger.repository;

import com.coreledger.entity.LedgerItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 账本明细Repository接口
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface LedgerItemRepository extends JpaRepository<LedgerItem, Long>, JpaSpecificationExecutor<LedgerItem> {

    /**
     * 根据账本ID和状态查询明细列表
     *
     * @param ledgerId 账本ID
     * @param status 明细状态（1=有效, 0=已删除）
     * @return 明细列表
     */
    List<LedgerItem> findByLedgerIdAndStatus(Long ledgerId, Integer status);

    /**
     * 根据账本ID查询所有有效明细列表
     *
     * @param ledgerId 账本ID
     * @return 有效明细列表
     */
    default List<LedgerItem> findActiveByLedgerId(Long ledgerId) {
        return findByLedgerIdAndStatus(ledgerId, 1);
    }
}
