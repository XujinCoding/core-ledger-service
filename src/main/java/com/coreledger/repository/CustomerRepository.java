package com.coreledger.repository;

import com.coreledger.entity.Customer;
import com.coreledger.enums.CustomerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
     * 根据user_id和merchant_id查询
     */
    Optional<Customer> findByUserIdAndMerchantId(Long userId, Long merchantId);

    /**
     * 根据user_id查询所有客户
     */
    List<Customer> findByUserId(Long userId);

    /**
     * 根据phone和merchant_id查询
     */
    Optional<Customer> findByPhoneAndMerchantId(String phone, Long merchantId);

    /**
     * 根据phone和merchant_id和user_id为NULL查询（未注册客户）
     */
    Optional<Customer> findByPhoneAndMerchantIdAndUserIdIsNull(String phone, Long merchantId);

    /**
     * 根据merchant_id查询所有客户
     */
    List<Customer> findByMerchantId(Long merchantId);

    /**
     * 检查user_id和merchant_id是否已存在
     */
    boolean existsByUserIdAndMerchantId(Long userId, Long merchantId);

    /**
     * 统计商户的客户数量
     */
    long countByMerchantId(Long merchantId);
}
