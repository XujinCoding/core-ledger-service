package com.coreledger.repository;

import com.coreledger.entity.CustomerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 客户历史Repository接口
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface CustomerHistoryRepository extends JpaRepository<CustomerHistory, Long>, JpaSpecificationExecutor<CustomerHistory> {

    /**
     * 根据客户ID查询历史记录（按操作时间倒序）
     *
     * @param customerId 客户ID
     * @return 历史记录列表
     */
    List<CustomerHistory> findByCustomerIdOrderByOperationTimeDesc(Long customerId);
}
