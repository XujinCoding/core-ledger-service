package com.coreledger.service;

import cn.hutool.core.util.StrUtil;
import com.coreledger.common.mapper.customer.CustomerConverter;
import com.coreledger.dto.auth.CustomerRegisterDTO;
import com.coreledger.dto.customer.CustomerSearchDTO;
import com.coreledger.dto.customer.CustomerUpdateDTO;
import com.coreledger.dto.merchant.CreateCustomerDTO;
import com.coreledger.entity.Customer;
import com.coreledger.entity.CustomerHistory;
import com.coreledger.entity.Merchant;
import com.coreledger.enums.*;
import com.coreledger.exception.BusinessException;
import com.coreledger.exception.NotFoundException;
import com.coreledger.repository.CustomerHistoryRepository;
import com.coreledger.repository.CustomerRepository;
import com.coreledger.repository.LedgerRepository;
import com.coreledger.repository.MerchantRepository;
import com.coreledger.repository.SysAddressRepository;
import com.coreledger.utils.AppSessionContext;
import com.coreledger.utils.specification.PredicateBuilder;
import com.coreledger.vo.customer.CustomerStatsVO;
import com.coreledger.vo.customer.CustomerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private final MerchantRepository merchantRepository;
    private final LedgerRepository ledgerRepository;
    private final CustomerConverter customerConverter;
    private final AddressService addressService;

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
     * 获取客户统计信息
     *
     * @param id 客户ID
     * @return 客户统计信息VO
     * @throws NotFoundException 当客户不存在时抛出 (BusinessCode.CUSTOMER_NOT_FOUND)
     */
    public CustomerStatsVO getCustomerStats(Long id) {
        // 校验客户是否存在
        if (!customerRepository.existsById(id)) {
            throw new NotFoundException(BusinessCode.CUSTOMER_NOT_FOUND);
        }

        // 统计订单数量和总金额
        Integer orderCount = ledgerRepository.countByCustomerId(id);
        BigDecimal totalAmount = ledgerRepository.sumPaidAmountByCustomerId(id);

        // 计算平均消费金额
        BigDecimal avgAmount = BigDecimal.ZERO;
        if (orderCount != null && orderCount > 0 && totalAmount != null) {
            avgAmount = totalAmount.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);
        }

        return CustomerStatsVO.builder()
                .totalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO)
                .orderCount(orderCount != null ? orderCount : 0)
                .avgAmount(avgAmount)
                .build();
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
                .equal("merchantId", AppSessionContext.getMerchantId())
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
        customer.setStatus(Status.ACTIVE);
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

    /**
     * 创建客户（用于商户手动创建或客户注册时）
     *
     * @param dto 创建客户DTO
     * @return 客户VO
     */
    @Transactional
    public CustomerVO createUnregisteredCustomer(CreateCustomerDTO dto) {
        // 1. 生成客户编号
        String customerNo = generateCustomerNo();
        
        // 2. 创建客户
        Customer customer = new Customer();
        customer.setCode(customerNo);
        customer.setMerchantId(dto.getMerchantId());
        customer.setName(dto.getName());
        customer.setAddressId(dto.getAddressId());
        customer.setPhone(dto.getPhone());
        customer.setAddressDetail(dto.getAddressDetail());
        customer.setGender(dto.getGender());
        customer.setAge(dto.getAge());
        customer.setIsRegistered(RegisterStatus.UNREGISTERED);
        customer.setStatus(Status.ACTIVE);
        customer = customerRepository.save(customer);
        // 3. 保存客户快照（创建操作）
        saveCustomerSnapshot(customer, OperationType.CREATE);
        log.info("创建客户成功: customerId={}, customerNo={}", customer.getId(), customerNo);
        
        return toVOWithAddressPath(customer);
    }

    /**
     * 绑定客户到用户
     */
    @Transactional
    public void bindCustomerToUser(Long customerId, Long userId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException(BusinessCode.CUSTOMER_NOT_FOUND));
        
        customer.setUserId(userId);
        customer.setIsRegistered(RegisterStatus.REGISTERED);
        customerRepository.save(customer);

        log.info("绑定客户成功: customerId={}, userId={}", customerId, userId);

    }

    /**
     * 根据手机号和商户ID查询未注册客户
     */
    public Optional<Customer> findUnregisteredCustomerByPhoneAndMerchantId(String phone, Long merchantId) {
        return customerRepository.findByPhoneAndMerchantIdAndUserIdIsNull(phone, merchantId);
    }


    /**
     * 根据ID查询客户
     */
    public Optional<Customer> findById(Long customerId) {
        return customerRepository.findById(customerId);
    }

    /**
     * 根据用户ID查询该用户是客户的所有关系
     */
    public List<Customer> findFormalByUserId(Long userId) {
        return customerRepository.findByUserIdAndCustomerType(userId,CustomerType.FORMAL);
    }

    /**
     * 根据用户ID查询模板客户（TEMPLATE 类型）
     */
    public Optional<Customer> findTemplateByUserId(Long userId) {
        return customerRepository.findByUserIdAndCustomerType(userId, CustomerType.TEMPLATE).stream().findFirst();
    }

    /**
     * 创建模板客户（TEMPLATE 类型，未绑定商户）
     * 用于保存用户在首次注册时填写的客户信息
     */
    @Transactional
    public Customer createTemplateCustomer(CustomerRegisterDTO dto, Long userId) {
        // 1. 生成客户编号
        String customerNo = generateCustomerNo();

        // 2. 创建模板客户
        Customer customer = new Customer();
        customer.setCode(customerNo);
        customer.setMerchantId(null);  // 模板客户不绑定商户
        customer.setUserId(userId);
        customer.setName(dto.getCustomerName());
        customer.setPhone(dto.getPhone());
        customer.setAlias(dto.getAlias());
        customer.setGender(dto.getGender() != null ? dto.getGender() : com.coreledger.enums.Gender.UNKNOWN);
        customer.setAge(dto.getAge());
        customer.setAddressId(dto.getAddressId());
        customer.setAddressDetail(dto.getAddressDetail());
        customer.setCustomerType(CustomerType.TEMPLATE);  // 标记为模板
        customer.setIsRegistered(RegisterStatus.REGISTERED);
        customer.setStatus(Status.ACTIVE);

        customer = customerRepository.save(customer);
        log.info("创建模板客户成功: customerId={}, customerNo={}, userId={}", customer.getId(), customerNo, userId);

        return customer;
    }

    /**
     * 从模板客户创建正式客户（FORMAL 类型）
     * 用于客户绑定商户时，从模板复制数据创建正式客户
     */
    @Transactional
    public Customer createFormalCustomerFromTemplate(Customer templateCustomer, Long merchantId) {
        // 1. 生成客户编号
        String customerNo = generateCustomerNo();

        // 2. 从模板复制数据创建正式客户
        Customer formalCustomer = new Customer();
        formalCustomer.setCode(customerNo);
        formalCustomer.setMerchantId(merchantId);  // 绑定商户
        formalCustomer.setUserId(templateCustomer.getUserId());
        formalCustomer.setName(templateCustomer.getName());
        formalCustomer.setPhone(templateCustomer.getPhone());
        formalCustomer.setAlias(templateCustomer.getAlias());
        formalCustomer.setGender(templateCustomer.getGender());
        formalCustomer.setAge(templateCustomer.getAge());
        formalCustomer.setAddressId(templateCustomer.getAddressId());
        formalCustomer.setAddressDetail(templateCustomer.getAddressDetail());
        formalCustomer.setCustomerType(CustomerType.FORMAL);  // 标记为正式
        formalCustomer.setIsRegistered(RegisterStatus.REGISTERED);
        formalCustomer.setStatus(Status.ACTIVE);

        formalCustomer = customerRepository.save(formalCustomer);
        log.info("从模板创建正式客户成功: formalCustomerId={}, templateCustomerId={}, merchantId={}", 
                formalCustomer.getId(), templateCustomer.getId(), merchantId);

        return formalCustomer;
    }

    /**
     * 将客户列表转换为 CustomerVO 列表
     * 只填充地址路径，商户信息由 Controller 层批量填充
     *
     * @param customers 客户实体列表
     * @return 客户VO列表
     */
    public List<CustomerVO> toVOListWithMerchantName(List<Customer> customers) {
        // 1. 批量查询地址信息
        Set<Long> merchantIds = customers.stream()
                .map(Customer::getMerchantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> merchantMap = merchantRepository.findAllById(merchantIds)
                .stream().collect(Collectors.toMap(Merchant::getId, Merchant::getName, (a, b) -> a));
        // 2. 转换为 VO 填充商户名称
        return customers.stream().map(customer -> {
            CustomerVO vo = customerConverter.toVO(customer);
            vo.setMerchantName(merchantMap.get(vo.getMerchantId()));
            return vo;
        }).toList();
    }

    /**
     * 获取当前商户的客户总数
     *
     * @return 客户总数
     */
    public Long countCustomers() {
        Long merchantId = AppSessionContext.getMerchantId();
        return customerRepository.countByMerchantId(merchantId);
    }

    /**
     * 生成客户编号
     * 格式: C_yyyyMMddHHmmss_随机3位
     */
    private String generateCustomerNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%03d", new Random().nextInt(1000));
        return "C_" + timestamp + "_" + random;
    }
}
