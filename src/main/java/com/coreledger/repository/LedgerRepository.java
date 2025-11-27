package com.coreledger.repository;

import com.coreledger.entity.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 账本Repository接口
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface LedgerRepository extends JpaRepository<Ledger, Long>, JpaSpecificationExecutor<Ledger> {
}
