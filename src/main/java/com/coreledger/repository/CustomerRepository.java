package com.coreledger.repository;

import com.coreledger.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 客户 Repository
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * 根据ID和状态查询客户
     *
     * @param id 客户ID
     * @param status 状态 (1=有效, 0=删除)
     * @return 客户
     */
    Optional<Customer> findByIdAndStatus(Long id, Integer status);

    /**
     * 根据手机号查询客户
     *
     * @param phone 手机号
     * @return 客户
     */
    Optional<Customer> findByPhone(String phone);

    /**
     * 根据手机号和状态查询客户
     *
     * @param phone 手机号
     * @param status 状态 (1=有效)
     * @return 客户
     */
    Optional<Customer> findByPhoneAndStatus(String phone, Integer status);

    /**
     * 根据姓名模糊查询客户（分页）
     *
     * @param name 姓名关键词
     * @param status 状态 (1=有效)
     * @param pageable 分页参数
     * @return 客户分页列表
     */
    Page<Customer> findByNameContainingAndStatus(String name, Integer status, Pageable pageable);

    /**
     * 根据地址ID查询客户（分页）
     *
     * @param addressId 地址ID
     * @param status 状态 (1=有效)
     * @param pageable 分页参数
     * @return 客户分页列表
     */
    Page<Customer> findByAddressIdAndStatus(Long addressId, Integer status, Pageable pageable);

    /**
     * 查询所有有效客户（分页）
     *
     * @param status 状态 (1=有效)
     * @param pageable 分页参数
     * @return 客户分页列表
     */
    Page<Customer> findByStatus(Integer status, Pageable pageable);
}
