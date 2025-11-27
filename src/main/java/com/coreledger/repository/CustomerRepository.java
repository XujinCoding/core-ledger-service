package com.coreledger.repository;

import com.coreledger.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 客户Repository接口
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    /**
     * 根据手机号查询客户
     *
     * @param phone 手机号
     * @return 客户信息
     */
    Optional<Customer> findByPhone(String phone);
}
