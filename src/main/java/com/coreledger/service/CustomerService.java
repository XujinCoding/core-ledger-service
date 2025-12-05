package com.coreledger.service;

import cn.hutool.core.util.StrUtil;
import com.coreledger.common.mapper.customer.CustomerConverter;
import com.coreledger.dto.customer.CustomerAddressUpdateDTO;
import com.coreledger.dto.customer.CustomerCreateDTO;
import com.coreledger.dto.customer.CustomerSearchDTO;
import com.coreledger.dto.customer.CustomerUpdateDTO;
import com.coreledger.entity.Customer;
import com.coreledger.entity.CustomerHistory;
import com.coreledger.entity.SysAddress;
import com.coreledger.enums.BusinessCode;
import com.coreledger.enums.CustomerType;
import com.coreledger.enums.OperationType;
import com.coreledger.enums.Status;
import com.coreledger.exception.BusinessException;
import com.coreledger.exception.NotFoundException;
import com.coreledger.repository.CustomerHistoryRepository;
import com.coreledger.repository.CustomerRepository;
import com.coreledger.repository.SysAddressRepository;
import com.coreledger.utils.specification.PredicateBuilder;
import com.coreledger.vo.customer.CustomerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 客户业务服务类
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
    private final SysAddressRepository addressRepository;
    private final CustomerHistoryRepository historyRepository;
    private final CustomerConverter customerConverter;
    private final AddressService addressService;

    /**
     * 创建客户
     *
     * @param dto 客户创建请求DTO
     * @return 创建成功的客户VO
     * @throws NotFoundException 当地址不存在时抛出 (BusinessCode.ADDRESS_NOT_FOUND)
     * @throws BusinessException 当地址不是村级地址时抛出 (BusinessCode.ADDRESS_MUST_BE_VILLAGE)
     */
    @Transactional
    public CustomerVO createCustomer(CustomerCreateDTO dto) {
        // 1. 检查手机号是否已存在
        Customer customer = customerRepository.findByPhone(dto.getPhone()).orElse(null);

        if (customer != null) {
            // 1.1 如果客户已存在且为潜在客户，激活该客户
            if (CustomerType.POTENTIAL.equals(customer.getCustomerType())) {
                // 更新客户信息
                customer.setName(dto.getName());
                customer.setAlias(dto.getAlias());
                customer.setGender(dto.getGender());
                customer.setAge(dto.getAge());
                customer.setAddressId(dto.getAddressId());
                customer.setAddressDetail(dto.getAddressDetail());
                customer.setCustomerType(CustomerType.ACTIVE);

                customer = customerRepository.save(customer);

                // 保存客户快照（更新操作）
                saveCustomerSnapshot(customer, OperationType.UPDATE);

                log.info("激活潜在客户成功, ID: {}, 姓名: {}, 手机号: {}", customer.getId(), customer.getName(), customer.getPhone());

                return toVOWithAddressPath(customer);
            } else {
                // 1.2 如果已存在且为活跃客户，抛出异常
                throw new BusinessException(BusinessCode.CUSTOMER_PHONE_EXISTS);
            }
        }

        // 2. 校验地址是否存在且为村级地址
        SysAddress address = addressRepository.findByIdAndStatus(dto.getAddressId(), Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException(BusinessCode.ADDRESS_NOT_FOUND));

        if (!address.isVillageLevel()) {
            throw new BusinessException(BusinessCode.ADDRESS_MUST_BE_VILLAGE);
        }

        // 3. 创建新客户
        customer = customerConverter.toEntity(dto);
        customer = customerRepository.save(customer);

        // 4. 保存客户快照（创建操作）
        saveCustomerSnapshot(customer, OperationType.CREATE);

        log.info("创建客户成功, ID: {}, 姓名: {}, 手机号: {}", customer.getId(), customer.getName(), customer.getPhone());

        // 5. 转换为VO并设置地址路径
        return toVOWithAddressPath(customer);
    }

    /**
     * 修改客户
     *
     * @param id  客户ID
     * @param dto 客户更新请求DTO
     * @return 修改后的客户VO
     * @throws NotFoundException 当客户不存在时抛出 (BusinessCode.CUSTOMER_NOT_FOUND)
     * @throws BusinessException 当手机号已被其他客户使用时抛出 (BusinessCode.CUSTOMER_PHONE_EXISTS)
     */
    @Transactional
    public CustomerVO updateCustomer(Long id, CustomerUpdateDTO dto) {
        // 1. 查询客户
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(BusinessCode.CUSTOMER_NOT_FOUND));

        // 2. 如果修改了手机号，校验手机号是否已被其他客户使用
        if (StrUtil.isNotBlank(dto.getPhone()) && !Objects.equals(dto.getPhone(), customer.getPhone())) {
            customerRepository.findByPhone(dto.getPhone()).ifPresent(existingCustomer -> {
                if (!Objects.equals(existingCustomer.getId(), id)) {
                    throw new BusinessException(BusinessCode.CUSTOMER_PHONE_EXISTS);
                }
            });
        }

        // 3. 更新客户信息
        customerConverter.updateEntity(dto, customer);
        customer = customerRepository.save(customer);

        // 4. 保存客户快照（更新操作）
        saveCustomerSnapshot(customer, OperationType.UPDATE);

        log.info("修改客户成功, ID: {}, 姓名: {}", id, customer.getName());

        // 5. 转换为VO并设置地址路径
        return toVOWithAddressPath(customer);
    }


    /**
     * 获取客户详情
     *
     * @param id 客户ID
     * @return 客户VO
     * @throws NotFoundException 当客户不存在时抛出 (BusinessCode.CUSTOMER_NOT_FOUND)
     */
    public CustomerVO getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(BusinessCode.CUSTOMER_NOT_FOUND));

        return toVOWithAddressPath(customer);
    }

    /**
     * 条件查询客户列表
     *
     * <p>当传入地址ID时，会查询该地址下所有子地址（包括子孙地址）的客户</p>
     *
     * @param dto      查询条件DTO
     * @param pageable 分页参数
     * @return 客户列表
     */
    public Page<CustomerVO> searchCustomers(CustomerSearchDTO dto, Pageable pageable) {
        List<Long> addressIds = new ArrayList<>();
        if (dto.getAddressId() != null) {
            addressIds= addressService.getAllChildAddressIds(dto.getAddressId());
            // 将父级地址ID也加入查询列表
            addressIds.add(dto.getAddressId());
        }
        Specification<Customer> spec = PredicateBuilder.<Customer>and()
                .like(StrUtil::isNotBlank, "name", dto.getName())
                .like(StrUtil::isNotBlank, "phone", dto.getPhone())
                .in("addressId", addressIds)
                .build();

        return customerRepository.findAll(spec, pageable)
                .map(this::toVOWithAddressPath);
    }

    /**
     * 删除客户
     *
     * @param id 客户ID
     * @throws NotFoundException 当客户不存在时抛出 (BusinessCode.CUSTOMER_NOT_FOUND)
     */
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(BusinessCode.CUSTOMER_NOT_FOUND));

        // 设置为潜在客户
        customer.setCustomerType(CustomerType.POTENTIAL);
        customerRepository.save(customer);

        // 保存客户快照（删除操作）
        saveCustomerSnapshot(customer, OperationType.DELETE);

        log.info("删除客户成功, ID: {}, 姓名: {}", id, customer.getName());
    }

    /**
     * 转换为VO并设置地址路径
     *
     * @param customer 客户实体
     * @return 客户VO
     */
    private CustomerVO toVOWithAddressPath(Customer customer) {
        CustomerVO vo = customerConverter.toVO(customer);

        // 查询地址信息并设置完整路径
        addressRepository.findById(customer.getAddressId()).ifPresent(address -> {
            vo.setAddressPath(address.getMergerName());
        });

        return vo;
    }

    /**
     * 保存客户快照到历史表
     *
     * @param customer      客户实体
     * @param operationType 操作类型
     */
    private void saveCustomerSnapshot(Customer customer, OperationType operationType) {
        CustomerHistory history = CustomerHistory.fromCustomer(customer, operationType);
        historyRepository.save(history);
        log.debug("保存客户快照, 客户ID: {}, 操作类型: {}", customer.getId(), operationType.getDescription());
    }
}
