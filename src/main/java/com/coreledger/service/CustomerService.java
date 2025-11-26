package com.coreledger.service;

import cn.hutool.core.util.StrUtil;
import com.coreledger.converter.CustomerConverter;
import com.coreledger.dto.customer.CreateCustomerDTO;
import com.coreledger.dto.customer.UpdateCustomerDTO;
import com.coreledger.entity.Customer;
import com.coreledger.enums.CustomerType;
import com.coreledger.enums.LedgerStatus;
import com.coreledger.enums.Status;
import com.coreledger.exception.BusinessException;
import com.coreledger.enums.BusinessCode;
import com.coreledger.exception.NotFoundException;
import com.coreledger.mapper.CustomerMapper;
import com.coreledger.repository.CustomerRepository;
import com.coreledger.repository.LedgerRepository;
import com.coreledger.utils.specification.PredicateBuilder;
import com.coreledger.vo.customer.CustomerDetailVO;
import com.coreledger.vo.customer.CustomerListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 客户服务
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final LedgerRepository ledgerRepository;
    private final CustomerMapper customerMapper;
    private final CustomerConverter customerConverter;

    /**
     * 创建客户
     *
     * @param dto 创建客户DTO
     * @return 客户详情
     */
    @Transactional
    public CustomerDetailVO createCustomer(CreateCustomerDTO dto) {
        log.info("创建客户: {}", dto.getName());

        // 1. 校验手机号是否已存在
        Optional<Customer> existingCustomer = customerRepository.findByPhone(dto.getPhone());
        if (existingCustomer.isPresent()) {
            throw new BusinessException(BusinessCode.CUSTOMER_PHONE_EXISTS, "手机号已存在: " + dto.getPhone());
        }

        // 2. TODO: 校验地址ID是否存在且为村级(level=5)
        // SysAddress address = sysAddressRepository.findById(dto.getAddressId())
        //     .orElseThrow(() -> new BusinessException("地址不存在"));
        // if (address.getLevel() != 5) {
        //     throw new BusinessException("地址必须选择到村级");
        // }

        // 3. 转换并保存
        Customer customer = customerConverter.toEntity(dto);
        customer.setCustomerType(CustomerType.ACTIVE);  // 新客户默认为活跃客户
        customer.setStatus(Status.ACTIVE);
        customer = customerRepository.save(customer);

        log.info("客户创建成功, ID: {}", customer.getId());

        // 4. 返回详情
        return getCustomerDetail(customer.getId());
    }

    /**
     * 查询客户列表（含欠款统计）
     *
     * @param keyword 关键词（姓名/手机号模糊查询）
     * @param addressId 地址ID筛选
     * @param customerType 客户类型筛选
     * @param pageable 分页参数
     * @return 客户分页列表
     */
    public Page<CustomerListVO> listCustomers(String keyword, Long addressId, CustomerType customerType, Pageable pageable) {
        log.info("查询客户列表: keyword={}, addressId={}, customerType={}", keyword, addressId, customerType);

        // 1. 使用 PredicateBuilder 构建动态查询条件
        Specification<Customer> spec = PredicateBuilder.<Customer>and()
                .equal("status", Status.ACTIVE)
                .equal(customerType != null, "customerType", customerType)
                .equal(addressId != null, "addressId", addressId)
                .build();

        // 2. 如果有关键词，添加OR条件（姓名或手机号）
        if (StrUtil.isNotBlank(keyword)) {
            Specification<Customer> keywordSpec = (root, query, cb) -> cb.or(
                    cb.like(root.get("name"), "%" + keyword + "%"),
                    cb.like(root.get("phone"), "%" + keyword + "%")
            );
            spec = spec.and(keywordSpec);
        }

        // 2. 分页查询
        Page<Customer> customerPage = customerRepository.findAll(spec, pageable);

        // 3. 转换为VO
        Page<CustomerListVO> voPage = customerPage.map(customerConverter::toListVO);

        // 4. 批量查询欠款统计
        List<Long> customerIds = voPage.getContent().stream()
                .map(CustomerListVO::getId)
                .collect(Collectors.toList());

        if (!customerIds.isEmpty()) {
            List<CustomerMapper.CustomerDebtSummary> debtSummaries = customerMapper.batchQueryDebtSummary(customerIds);
            Map<Long, CustomerMapper.CustomerDebtSummary> debtMap = debtSummaries.stream()
                    .collect(Collectors.toMap(CustomerMapper.CustomerDebtSummary::getCustomerId, Function.identity()));

            // 5. 填充欠款统计
            voPage.getContent().forEach(vo -> {
                CustomerMapper.CustomerDebtSummary summary = debtMap.get(vo.getId());
                if (summary != null) {
                    vo.setDebtAmount(summary.getDebtAmount());
                    vo.setCreditAmount(summary.getCreditAmount());
                    vo.setActiveLedgerCount(summary.getActiveLedgerCount());
                }
            });
        }

        // 6. TODO: 批量查询完整地址
        // List<Long> addressIds = voPage.getContent().stream()
        //     .map(CustomerListVO::getAddressId).distinct().collect(Collectors.toList());
        // Map<Long, String> addressMap = sysAddressService.batchGetFullAddress(addressIds);
        // voPage.getContent().forEach(vo -> vo.setFullAddress(addressMap.get(vo.getAddressId())));

        return voPage;
    }

    /**
     * 查询客户详情
     *
     * @param id 客户ID
     * @return 客户详情
     */
    public CustomerDetailVO getCustomerDetail(Long id) {
        log.info("查询客户详情: {}", id);

        // 1. 查询客户
        Customer customer = customerRepository.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("客户不存在"));

        // 2. 转换为VO
        CustomerDetailVO vo = customerConverter.toDetailVO(customer);

        // 3. 查询账单统计
        CustomerMapper.CustomerLedgerSummary ledgerSummary = customerMapper.queryLedgerSummary(id);
        if (ledgerSummary != null) {
            CustomerDetailVO.LedgerSummary summary = new CustomerDetailVO.LedgerSummary();
            summary.setInProgressCount(ledgerSummary.getInProgressCount());
            summary.setInProgressAmount(ledgerSummary.getInProgressAmount());
            summary.setPartialCount(ledgerSummary.getPartialCount());
            summary.setPartialAmount(ledgerSummary.getPartialAmount());
            summary.setPartialPaidAmount(ledgerSummary.getPartialPaidAmount());
            summary.setCreditCount(ledgerSummary.getCreditCount());
            summary.setCreditAmount(ledgerSummary.getCreditAmount());
            summary.setClearedCount(ledgerSummary.getClearedCount());
            summary.setTotalDebt(ledgerSummary.getTotalDebt());
            summary.setTotalCredit(ledgerSummary.getTotalCredit());
            vo.setLedgerSummary(summary);
        }

        // 4. TODO: 查询完整地址
        // String fullAddress = sysAddressService.getFullAddress(customer.getAddressId());
        // vo.setFullAddress(fullAddress);

        return vo;
    }

    /**
     * 更新客户信息
     *
     * @param id 客户ID
     * @param dto 更新客户DTO
     * @return 客户详情
     */
    @Transactional
    public CustomerDetailVO updateCustomer(Long id, UpdateCustomerDTO dto) {
        log.info("更新客户信息: {}", id);

        // 1. 查询客户
        Customer customer = customerRepository.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("客户不存在"));

        // 2. 如果修改了手机号，校验是否重复
        if (dto.getPhone() != null && !dto.getPhone().equals(customer.getPhone())) {
            Optional<Customer> existingCustomer = customerRepository.findByPhone(dto.getPhone());
            if (existingCustomer.isPresent()) {
                throw new BusinessException(BusinessCode.CUSTOMER_PHONE_EXISTS, "手机号已存在: " + dto.getPhone());
            }
        }

        // 3. TODO: 如果修改了地址，校验地址是否存在且为村级

        // 4. 更新字段（只更新非null字段）
        customerConverter.updateEntity(dto, customer);
        customerRepository.save(customer);

        log.info("客户信息更新成功: {}", id);

        // 5. 返回详情
        return getCustomerDetail(id);
    }

    /**
     * 删除客户（软删除+级联检查）
     *
     * @param id 客户ID
     */
    @Transactional
    public void deleteCustomer(Long id) {
        log.info("删除客户: {}", id);

        // 1. 查询客户
        Customer customer = customerRepository.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("客户不存在"));

        // 2. 级联检查：查询是否有未结清账单
        long activeCount = ledgerRepository.countByCustomerIdAndLedgerStatusInAndStatus(
                id,
                Arrays.asList(LedgerStatus.IN_PROGRESS, LedgerStatus.PARTIAL, LedgerStatus.ON_CREDIT),
                Status.ACTIVE
        );

        if (activeCount > 0) {
            throw new BusinessException(BusinessCode.BUSINESS_ERROR, "该客户有" + activeCount + "笔未结清账单，无法删除");
        }

        // 3. 软删除 + 标记为潜在客户
        customer.setStatus(Status.INACTIVE);
        customer.setCustomerType(CustomerType.POTENTIAL);
        customerRepository.save(customer);

        log.info("客户删除成功: {}", id);
    }
}
