package com.coreledger.repository;

import com.coreledger.entity.LedgerItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 账本明细Repository接口
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface LedgerItemRepository extends JpaRepository<LedgerItem, Long>, JpaSpecificationExecutor<LedgerItem> {
}
