package com.coreledger.service;

import cn.hutool.core.util.StrUtil;
import com.coreledger.common.mapper.customer.CustomerConverter;
import com.coreledger.dto.auth.CustomerRegisterDTO;
import com.coreledger.dto.customer.CustomerProfileUpdateDTO;
import com.coreledger.dto.customer.CustomerSearchDTO;
import com.coreledger.dto.customer.CustomerUpdateDTO;
import com.coreledger.dto.merchant.CreateCustomerDTO;
import com.coreledger.entity.Customer;
import com.coreledger.entity.CustomerHistory;
import com.coreledger.entity.Merchant;
import com.coreledger.entity.SysUser;
import com.coreledger.enums.*;
import com.coreledger.exception.BusinessException;
import com.coreledger.exception.NotFoundException;
import com.coreledger.repository.*;
import com.coreledger.utils.SecurityUtils;
import com.coreledger.utils.specification.PredicateBuilder;
import com.coreledger.vo.customer.CustomerListStatsVO;
import com.coreledger.vo.customer.CustomerStatsVO;
import com.coreledger.vo.customer.CustomerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
    private final SysUserRepository sysUserRepository;
    private final SysAddressRepository addressRepository;
    private final CustomerHistoryRepository historyRepository;
    private final MerchantRepository merchantRepository;
    private final LedgerRepository ledgerRepository;
    private final CustomerConverter customerConverter;
    private final AddressService addressService;
    private final FileUploadService fileUploadService;
    private final SmsService smsService;

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
            customerRepository.findByPhoneAndMerchantId(dto.getPhone(),SecurityUtils.getCurrentMerchantId()).ifPresent(existingCustomer -> {
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
                .equal("merchantId", SecurityUtils.getCurrentMerchantId())
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

        // 将avatarUrl（文件路径）转换为预签名URL
        if (vo.getAvatarUrl() != null && !vo.getAvatarUrl().isEmpty()) {
            String presignedUrl = fileUploadService.generatePresignedUrl(vo.getAvatarUrl());
            vo.setAvatarUrl(presignedUrl);
        }

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
     * @throws BusinessException 当手机号已被同商户其他客户使用时抛出 (BusinessCode.CUSTOMER_PHONE_EXISTS)
     */
    @Transactional
    public CustomerVO createUnregisteredCustomer(CreateCustomerDTO dto) {
        // 1. 校验手机号是否已被同商户其他客户使用
        customerRepository.findByPhoneAndMerchantId(dto.getPhone(), dto.getMerchantId())
                .ifPresent(existingCustomer -> {
                    throw new BusinessException(BusinessCode.CUSTOMER_PHONE_EXISTS);
                });

        // 2. 生成客户编号
        String customerNo = generateCustomerNo();

        // 3. 创建客户
        Customer customer = new Customer();
        customer.setCode(customerNo);
        customer.setMerchantId(dto.getMerchantId());
        customer.setName(dto.getName());
        customer.setAlias(dto.getAlias());
        customer.setAddressId(dto.getAddressId());
        customer.setPhone(dto.getPhone());
        customer.setAddressDetail(dto.getAddressDetail());
        customer.setGender(dto.getGender());
        customer.setAge(dto.getAge());
        customer.setMemo(dto.getRemark());
        customer.setAvatarUrl(dto.getAvatarUrl());
        customer.setIsRegistered(RegisterStatus.UNREGISTERED);
        customer.setStatus(Status.ACTIVE);
        customer = customerRepository.save(customer);
        // 4. 保存客户快照（创建操作）
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
        return customerRepository.findByUserId(userId);
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

            // 将avatarUrl（文件路径）转换为预签名URL
            if (vo.getAvatarUrl() != null && !vo.getAvatarUrl().isEmpty()) {
                String presignedUrl = fileUploadService.generatePresignedUrl(vo.getAvatarUrl());
                vo.setAvatarUrl(presignedUrl);
            }

            return vo;
        }).toList();
    }

    /**
     * 获取当前商户的客户总数
     *
     * @return 客户总数
     */
    public Long countCustomers() {
        Long merchantId = SecurityUtils.getCurrentMerchantId();
        return customerRepository.countByMerchantId(merchantId);
    }

    /**
     * 获取客户列表统计（支持与搜索相同的条件）
     *
     * @param dto 搜索条件
     * @return 客户列表统计VO
     */
    public CustomerListStatsVO getCustomerListStats(CustomerSearchDTO dto) {
        log.info("获取客户列表统计，条件: {}", dto);

        List<Long> addressIds = new ArrayList<>();
        if (dto.getAddressId() != null) {
            addressIds = addressService.getAllChildAddressIds(dto.getAddressId());
            addressIds.add(dto.getAddressId());
        }

        Specification<Customer> spec = PredicateBuilder.<Customer>and()
                .like(StrUtil::isNotBlank, "name", dto.getName())
                .like(StrUtil::isNotBlank, "phone", dto.getPhone())
                .equal("merchantId", SecurityUtils.getCurrentMerchantId())
                .in("addressId", addressIds)
                .build();

        long count = customerRepository.count(spec);
        return new CustomerListStatsVO(count);
    }

    /**
     * 获取当前登录客户的个人信息（从SysUser读取）
     *
     * @return 客户个人信息
     * @throws NotFoundException 当用户不存在时抛出 (BusinessCode.USER_NOT_FOUND)
     */
    public CustomerVO getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(BusinessCode.USER_NOT_FOUND));

        // 将SysUser转换为CustomerVO（保持接口兼容性）
        return genCustomerResult(user);
    }

    private @NotNull CustomerVO genCustomerResult(SysUser user) {
        CustomerVO vo = new CustomerVO();
        vo.setId(user.getId());
        vo.setName(user.getName());
        vo.setPhone(user.getPhone());
        vo.setAlias(user.getNickname());  // nickname映射到alias
        vo.setGender(user.getGender());
        vo.setAge(user.getAge());
        vo.setAddressId(user.getAddressId());
        vo.setAddressDetail(user.getAddressDetail());
        vo.setAvatarUrl(user.getAvatarUrl());

        // 设置地址路径
        if (user.getAddressId() != null) {
            addressRepository.findById(user.getAddressId()).ifPresent(address -> {
                vo.setAddressPath(address.getMergerName());
            });
        }
        return vo;
    }

    /**
     * 客户修改个人信息（更新SysUser）
     *
     * 逻辑：
     * 1. 获取当前登录用户ID
     * 2. 查询SysUser
     * 3. 如果修改了手机号，验证短信验证码
     * 4. 更新SysUser信息
     *
     * @param dto 个人信息更新DTO
     * @return 更新后的用户信息VO
     * @throws NotFoundException 当用户不存在时抛出 (BusinessCode.USER_NOT_FOUND)
     * @throws BusinessException 当验证码无效时抛出
     */
    @Transactional
    public CustomerVO updateProfile(CustomerProfileUpdateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 1. 查询SysUser
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(BusinessCode.USER_NOT_FOUND));

        // 2. 如果修改了手机号，需要验证短信验证码
        if (StrUtil.isNotBlank(dto.getPhone()) && !Objects.equals(dto.getPhone(), user.getPhone())) {
            // 验证码必填
            if (StrUtil.isBlank(dto.getSmsCode())) {
                throw new BusinessException(BusinessCode.SMS_CODE_REQUIRED);
            }
            // 验证短信验证码
            smsService.verifySmsCode(dto.getPhone(), dto.getSmsCode(), SmsScene.CHANGE_PHONE);
            // 更新手机号
            user.setPhone(dto.getPhone());
        }

        // 3. 更新SysUser信息
        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getAlias() != null) user.setNickname(dto.getAlias());  // alias映射到nickname
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getAge() != null) user.setAge(dto.getAge());
        if (dto.getAddressId() != null) user.setAddressId(dto.getAddressId());
        if (dto.getAddressDetail() != null) user.setAddressDetail(dto.getAddressDetail());
        if (dto.getAvatarUrl() != null) user.setAvatarUrl(dto.getAvatarUrl());

        // 4. 保存
        user = sysUserRepository.save(user);

        log.info("客户修改个人信息成功, userId: {}", userId);

        // 5. 转换为CustomerVO返回（保持接口兼容性）
        return genCustomerResult(user);
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

    /**
     * 从SysUser创建Customer
     * 用于客户注册时直接绑定商户的场景
     *
     * @param user       系统用户
     * @param merchantId 商户ID
     * @param dto        注册DTO
     * @return 创建的客户
     */
    @Transactional
    public Customer createCustomerFromSysUser(com.coreledger.entity.SysUser user, Long merchantId, CustomerRegisterDTO dto) {
        // 1. 生成客户编号
        String customerNo = generateCustomerNo();

        // 2. 创建客户（从SysUser复制信息）
        Customer customer = new Customer();
        customer.setCode(customerNo);
        customer.setMerchantId(merchantId);
        customer.setUserId(user.getId());
        customer.setName(user.getName());
        customer.setPhone(user.getPhone());
        customer.setAlias(dto.getAlias());
        customer.setGender(user.getGender());
        customer.setAge(user.getAge());
        customer.setAddressId(user.getAddressId());
        customer.setAddressDetail(user.getAddressDetail());
        customer.setAvatarUrl(user.getAvatarUrl());
        customer.setIsRegistered(RegisterStatus.REGISTERED);
        customer.setStatus(Status.ACTIVE);

        customer = customerRepository.save(customer);
        saveCustomerSnapshot(customer, OperationType.CREATE);

        log.info("从SysUser创建客户成功: customerId={}, userId={}, merchantId={}",
                customer.getId(), user.getId(), merchantId);

        return customer;
    }

    /**
     * 保存客户
     *
     * @param customer 客户实体
     * @return 保存后的客户
     */
    @Transactional
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    /**
     * 根据用户ID和商户ID查询客户
     *
     * @param userId     用户ID
     * @param merchantId 商户ID
     * @return 客户信息
     */
    public Optional<Customer> findByUserIdAndMerchantId(Long userId, Long merchantId) {
        return customerRepository.findByUserIdAndMerchantId(
                userId, merchantId);
    }
}

