package com.coreledger.service;

import cn.hutool.core.collection.CollUtil;
import com.coreledger.common.PageQueryResult;
import com.coreledger.common.mapper.ledger.LedgerConverter;
import com.coreledger.common.mapper.ledger.LedgerItemConverter;
import com.coreledger.common.mapper.ledger.PaymentRecordConverter;
import com.coreledger.config.CustomMetrics;
import com.coreledger.dto.ledger.*;
import com.coreledger.utils.SecurityUtils;
import com.coreledger.vo.LedgerListStatsVO;
import com.coreledger.vo.LedgerListVO;
import com.coreledger.entity.Customer;
import com.coreledger.entity.Ledger;
import com.coreledger.entity.LedgerItem;
import com.coreledger.entity.PaymentRecord;
import com.coreledger.enums.BusinessCode;
import com.coreledger.enums.LedgerStatus;
import com.coreledger.enums.PaymentMethod;
import com.coreledger.enums.Status;
import com.coreledger.exception.BusinessException;
import com.coreledger.exception.NotFoundException;
import com.coreledger.mapper.LedgerMapper;
import com.coreledger.repository.CustomerRepository;
import com.coreledger.repository.LedgerRepository;
import com.coreledger.repository.LedgerItemRepository;
import com.coreledger.repository.PaymentRecordRepository;
import com.coreledger.utils.specification.PredicateBuilder;
import com.coreledger.vo.LedgerVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Base64;

/**
 * 账本业务服务类
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final LedgerItemRepository ledgerItemRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final CustomerRepository customerRepository;
    private final LedgerConverter ledgerConverter;
    private final LedgerItemConverter ledgerItemConverter;
    private final PaymentRecordConverter paymentRecordConverter;
    private final LedgerMapper ledgerMapper;
    private final CustomMetrics customMetrics;
    private final FileUploadService fileUploadService;

    /**
     * 新增账单
     *
     * @param dto 新增账单DTO
     * @return 账单VO
     * @throws NotFoundException 当客户不存在时
     */
    @Transactional
    public LedgerVO createLedger(CreateLedgerDTO dto) {
        long startTime = System.currentTimeMillis();
        log.info("创建账单，客户ID: {}", dto.getCustomerId());

        try {
            // 1. 校验客户是否存在
            Customer customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new NotFoundException(BusinessCode.CUSTOMER_NOT_FOUND));

            // 2. 创建账单主表
            Ledger ledger = new Ledger();
            ledger.setCustomerId(dto.getCustomerId());
            ledger.setLedgerStatus(LedgerStatus.IN_PROGRESS);
            ledger.setTotalAmount(BigDecimal.ZERO);
            ledger.setPaidAmount(BigDecimal.ZERO);
            ledger.setDiscountAmount(BigDecimal.ZERO);
            ledger.setMerchantId(dto.getMerchantId());
            ledger = ledgerRepository.save(ledger);

            // 3. 创建明细（如果有）
            BigDecimal totalAmount = BigDecimal.ZERO;
            if (CollUtil.isNotEmpty(dto.getItems())) {
                for (LedgerItemDTO itemDTO : dto.getItems()) {
                    LedgerItem item = ledgerItemConverter.toEntity(itemDTO);
                    item.setLedgerId(ledger.getId());
                    item.setStatus(Status.ACTIVE);
                    ledgerItemRepository.save(item);
                totalAmount = totalAmount.add(itemDTO.getAmount());
            }

            // 4. 更新总金额
            ledger.setTotalAmount(totalAmount);
            ledger = ledgerRepository.save(ledger);
        }

        log.info("账单创建成功，账单ID: {}, 总金额: {}", ledger.getId(), totalAmount);

        // Track metrics
        customMetrics.incrementLedgerCreated();
        customMetrics.recordLedgerProcessingTime(System.currentTimeMillis() - startTime);

        return buildLedgerVO(ledger, customer.getName());
        } catch (Exception e) {
            customMetrics.recordLedgerProcessingTime(System.currentTimeMillis() - startTime);
            throw e;
        }
    }

    /**
     * 批量更新账单明细（增删改）
     *
     * @param ledgerId 账单ID
     * @param dto      更新明细DTO
     * @return 账单VO
     * @throws NotFoundException 当账单或明细不存在时
     * @throws BusinessException 当账单状态不允许操作时
     */
    @Transactional
    public LedgerVO updateLedgerItems(Long ledgerId, UpdateLedgerItemsDTO dto) {
        log.info("批量更新账单明细，账单ID: {}", ledgerId);

        // 1. 查询账单
        Ledger ledger = getLedgerById(ledgerId);
        // 如果赊账中编辑, 那状态还是赊账中
        // 如果部分收款编辑, 那还是部分收款
        // 如果进行中编辑, 那还是进行中
        // 已关闭和已结清状态, 不允许编辑
        // 2. 校验账单状态
        if (LedgerStatus.CLOSED.equals(ledger.getLedgerStatus())
                ||LedgerStatus.CLEARED.equals(ledger.getLedgerStatus())) {
            throw new BusinessException(BusinessCode.LEDGER_STATUS_NOT_ALLOWED);
        }

        // 3. 查询数据库中的所有有效明细ID
        List<LedgerItem> dbItems = ledgerItemRepository.findByLedgerIdAndStatus(ledgerId, Status.ACTIVE);
        List<Long> dbItemIds = dbItems.stream()
                .map(LedgerItem::getId)
                .toList();

        // 4. 获取请求中的所有明细ID
        Set<Long> requestItemIds = dto.getItems().stream()
                .map(LedgerItemDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 5. 找出需要软删除的ID（数据库有但请求没有）
        List<Long> deleteIds = dbItemIds.stream()
                .filter(id -> !requestItemIds.contains(id))
                .collect(Collectors.toList());

        // 6. 软删除明细
        if (!deleteIds.isEmpty()) {
            List<LedgerItem> itemsToDelete = ledgerItemRepository.findAllById(deleteIds);
            itemsToDelete.forEach(item -> item.setStatus(Status.INACTIVE));
            ledgerItemRepository.saveAll(itemsToDelete);
            log.info("软删除明细，数量: {}", deleteIds.size());
        }

        // 7. 新增或修改明细
        for (LedgerItemDTO itemDTO : dto.getItems()) {
            if (itemDTO.getId() == null) {
                // 新增
                LedgerItem newItem = ledgerItemConverter.toEntity(itemDTO);
                newItem.setLedgerId(ledgerId);
                newItem.setStatus(Status.ACTIVE);
                ledgerItemRepository.save(newItem);
                log.info("新增明细，商品: {}", itemDTO.getProductName());
            } else {
                // 修改
                LedgerItem existingItem = ledgerItemRepository.findById(itemDTO.getId())
                        .orElseThrow(() -> new NotFoundException(BusinessCode.LEDGER_ITEM_NOT_FOUND));

                // 校验明细是否属于该账单
                if (!ledgerId.equals(existingItem.getLedgerId())) {
                    throw new BusinessException(BusinessCode.LEDGER_ITEM_NOT_BELONG);
                }

                // 校验明细是否已删除
                if (Status.INACTIVE.equals(existingItem.getStatus())) {
                    throw new BusinessException(BusinessCode.LEDGER_ITEM_DELETED);
                }

                // 更新字段
                existingItem.setProductId(itemDTO.getProductId());
                existingItem.setProductName(itemDTO.getProductName());
                existingItem.setSkuId(itemDTO.getSkuId());
                existingItem.setSkuName(itemDTO.getSkuName());
                existingItem.setPrice(itemDTO.getPrice());
                existingItem.setQuantity(itemDTO.getQuantity());
                existingItem.setAmount(itemDTO.getAmount());

                ledgerItemRepository.save(existingItem);
                log.info("修改明细，ID: {}, 商品: {}", itemDTO.getId(), itemDTO.getProductName());
            }
        }

        // 8. 重新计算总额
        recalculateTotalAmount(ledger);

        log.info("账单明细更新完成，账单ID: {}", ledgerId);
        return getLedgerDetail(ledgerId);
    }

    /**
     * 记账（支持部分/全额支付）
     *
     * @param ledgerId 账单ID
     * @param dto      记账DTO
     * @return 账单VO
     * @throws NotFoundException 当账单不存在时
     * @throws BusinessException 当账单状态不允许或支付金额超限时
     */
    @Transactional
    public LedgerVO recordLedger(Long ledgerId, RecordLedgerDTO dto) {
        log.info("记账操作，账单ID: {}, 支付金额: {}", ledgerId, dto.getPaymentAmount());

        // 校验
        Ledger ledger = getLedgerById(ledgerId);
        validateRecordLedger(ledger, dto);

        // 处理签名图片
        if (dto.getSignatureImage() != null && !dto.getSignatureImage().isEmpty()) {
            String signatureUrl = saveSignatureImage(ledgerId, dto.getSignatureImage());
            ledger.setSignatureImageUrl(signatureUrl);
        }

        // 操作
        if (dto.getPaymentAmount().compareTo(BigDecimal.ZERO) > 0) {
            createPaymentRecord(ledger, dto.getPaymentAmount(), dto.getPaymentMethod(), dto.getMemo());
            ledger.setPaidAmount(ledger.getPaidAmount().add(dto.getPaymentAmount()));
        }

        updateLedgerStatusAfterRecord(ledger);
        ledgerRepository.save(ledger);

        // 返回
        log.info("记账完成，账单ID: {}", ledgerId);
        return getLedgerDetail(ledgerId);
    }

    /**
     * 结账（支付+优惠）
     *
     * @param ledgerId 账单ID
     * @param dto      结账DTO
     * @return 账单VO
     * @throws NotFoundException 当账单不存在时
     * @throws BusinessException 当账单状态不允许或支付金额超限时
     */
    @Transactional
    public LedgerVO settleLedger(Long ledgerId, SettleLedgerDTO dto) {
        log.info("结账操作，账单ID: {}, 支付金额: {}", ledgerId, dto.getPaymentAmount());

        // 校验
        Ledger ledger = getLedgerById(ledgerId);
        validateSettleLedger(ledger, dto);

        // 操作
        if (dto.getPaymentAmount().compareTo(BigDecimal.ZERO) > 0) {
            createPaymentRecord(ledger, dto.getPaymentAmount(), dto.getPaymentMethod(), dto.getMemo());
            ledger.setPaidAmount(ledger.getPaidAmount().add(dto.getPaymentAmount()));
        }

        BigDecimal discount = ledger.getTotalAmount()
                .subtract(ledger.getPaidAmount())
                .subtract(ledger.getDiscountAmount());
        ledger.setDiscountAmount(ledger.getDiscountAmount().add(discount));
        ledger.setLedgerStatus(LedgerStatus.CLEARED);

        ledgerRepository.save(ledger);

        // 返回
        log.info("结账完成，账单ID: {}, 优惠金额: {}", ledgerId, discount);
        return getLedgerDetail(ledgerId);
    }

    /**
     * 新增支付记录（统一支付接口）
     * <p>不考虑状态，只要账单未结清且未关闭就可以添加支付记录</p>
     * <p>根据剩余金额自动更新账单状态</p>
     *
     * @param ledgerId 账单ID
     * @param dto      支付记录DTO
     * @return 账单VO
     * @throws NotFoundException 当账单不存在时
     * @throws BusinessException 当账单状态不允许或支付金额超限时
     */
    @Transactional
    public LedgerVO addPaymentRecord(Long ledgerId, AddPaymentRecordDTO dto) {
        log.info("新增支付记录，账单ID: {}, 支付金额: {}", ledgerId, dto.getPaymentAmount());

        // 校验
        Ledger ledger = getLedgerById(ledgerId);
        validateAddPaymentRecord(ledger, dto);

        // 操作
        createPaymentRecord(ledger, dto.getPaymentAmount(), dto.getPaymentMethod(), dto.getMemo());
        ledger.setPaidAmount(ledger.getPaidAmount().add(dto.getPaymentAmount()));
        updateLedgerStatusAfterPayment(ledger);
        ledgerRepository.save(ledger);

        // 返回
        log.info("支付记录添加完成，账单ID: {}", ledgerId);
        return getLedgerDetail(ledgerId);
    }

    /**
     * 修改账单备注
     *
     * @param ledgerId 账单ID
     * @param dto      修改备注DTO
     * @return 账单VO
     * @throws NotFoundException 当账单不存在时
     */
    @Transactional
    public void updateLedgerMemo(Long ledgerId, UpdateLedgerMemoDTO dto) {
        log.info("修改账单备注，账单ID: {}", ledgerId);

        Ledger ledger = getLedgerById(ledgerId);
        ledger.setMemo(dto.getMemo());
        ledgerRepository.save(ledger);

        log.info("账单备注修改完成，账单ID: {}", ledgerId);
    }

    /**
     * 关闭账单
     *
     * @param ledgerId 账单ID
     * @param dto      关闭DTO
     * @throws NotFoundException 当账单不存在时
     * @throws BusinessException 当账单状态不允许或已有支付记录时
     */
    @Transactional
    public void closeLedger(Long ledgerId, CloseLedgerDTO dto) {
        log.info("关闭账单，账单ID: {}, 原因: {}", ledgerId, dto.getReason());

        // 1. 查询账单
        Ledger ledger = getLedgerById(ledgerId);

        // 2. 校验账单状态（只有进行中可以关闭）
        if (!LedgerStatus.IN_PROGRESS.equals(ledger.getLedgerStatus())) {
            throw new BusinessException(BusinessCode.LEDGER_STATUS_NOT_ALLOWED);
        }

        // 3. 校验无支付记录
        if (ledger.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException(BusinessCode.LEDGER_HAS_PAYMENT);
        }

        // 4. 更新状态
        ledger.setLedgerStatus(LedgerStatus.CLOSED);
        ledgerRepository.save(ledger);

        log.info("账单已关闭，账单ID: {}", ledgerId);
    }

    /**
     * 根据客户查询账单列表（支持状态和时间过滤）
     *
     * @param queryDTO 查询条件
     * @param pageable 分页参数
     * @return 分页账单列表
     */
    public Page<LedgerListVO> queryLedgersByCustomer(LedgerQueryDTO queryDTO, Pageable pageable) {
        log.info("查询客户账单列表，客户ID: {}, 状态: {}", queryDTO.getCustomerId(), queryDTO.getLedgerStatus());

        // 使用 PredicateBuilder 构建动态查询条件
        Specification<Ledger> spec = PredicateBuilder.<Ledger>and()
                .equal("customerId", queryDTO.getCustomerId())
                .equal("ledgerStatus", queryDTO.getLedgerStatus())
                .betweenLocalDate(queryDTO.getCreatedAtStart() != null && queryDTO.getCreatedAtEnd() != null,
                        "createInstant", queryDTO.getCreatedAtStart(), queryDTO.getCreatedAtEnd())
                .build();

        // 执行查询
        Page<Ledger> ledgerPage = ledgerRepository.findAll(spec, pageable);

        // 批量查询客户信息
        return fillLedgerInfo(ledgerPage);
    }

    /**
     * 搜索账单列表（MyBatis连表查询，支持客户姓名和电话模糊查询）
     *
     * @param condition 搜索条件
     * @return 分页账单列表
     */
    public PageQueryResult<LedgerListVO> searchLedgers(LedgerSearchDTO condition) {
        try (var page = PageHelper.startPage(condition.getPageNumber(), condition.getPageSize())) {
            condition.setMerchantId(SecurityUtils.getCurrentMerchantId());
            List<LedgerListVO> list = ledgerMapper.searchLedgers(condition);
            PageInfo<LedgerListVO> result = new PageInfo<>(list);
            return new PageQueryResult<>(list,result.getPages(), result.getTotal());
        }
    }

    /**
     * 查询进行中的账单列表
     *
     * @param pageable 分页参数
     * @return 分页账单列表
     */
    public Page<LedgerListVO> queryInProgressLedgers(Pageable pageable) {
        log.info("查询进行中的账单列表");

        // 使用 PredicateBuilder 构建查询条件
        Specification<Ledger> spec = PredicateBuilder.<Ledger>and()
                .equal("merchantId",SecurityUtils.getCurrentMerchantId())
                .in("ledgerStatus", List.of(LedgerStatus.IN_PROGRESS,LedgerStatus.PARTIAL))
                .build();

        Page<Ledger> ledgerPage = ledgerRepository.findAll(spec, pageable);

        return fillLedgerInfo(ledgerPage);
    }

    private Page<LedgerListVO> fillLedgerInfo(Page<Ledger> ledgerPage) {
        // 批量查询客户信息
        List<Long> customerIds = ledgerPage.getContent().stream()
                .map(Ledger::getCustomerId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> customerNameMap = new HashMap<>();
        if (!customerIds.isEmpty()) {
            List<Customer> customers = customerRepository.findAllById(customerIds);
            customerNameMap = customers.stream()
                    .collect(Collectors.toMap(Customer::getId, Customer::getName));
        }

        // 转换为VO
        Map<Long, String> finalCustomerNameMap = customerNameMap;
        return ledgerPage.map(ledger -> {
            LedgerListVO vo = ledgerConverter.toListVO(ledger);
            vo.setCustomerName(finalCustomerNameMap.get(ledger.getCustomerId()));
            return vo;
        });
    }

    /**
     * 查询账单详情
     *
     * @param ledgerId 账单ID
     * @return 账单VO
     * @throws NotFoundException 当账单不存在时
     */
    public LedgerVO getLedgerDetail(Long ledgerId) {
        Ledger ledger = getLedgerById(ledgerId);
        Customer customer = customerRepository.findById(ledger.getCustomerId())
                .orElseThrow(() -> new NotFoundException(BusinessCode.CUSTOMER_NOT_FOUND));

        return buildLedgerVO(ledger, customer.getName());
    }

    /**
     * 获取账单列表统计数据（支持与搜索相同的条件）
     *
     * @param dto 搜索条件
     * @return 账单列表统计VO
     */
    public LedgerListStatsVO getLedgerListStats(LedgerSearchDTO dto) {
        log.info("获取账单列表统计，条件: {}", dto);
        dto.setMerchantId(SecurityUtils.getCurrentMerchantId());
        return ledgerMapper.statsLedgers(dto);
    }

    /**
     * 根据ID查询账单（内部方法）
     *
     * @param ledgerId 账单ID
     * @return 账单实体
     * @throws NotFoundException 当账单不存在时
     */
    private Ledger getLedgerById(Long ledgerId) {
        return ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new NotFoundException(BusinessCode.LEDGER_NOT_FOUND));
    }

    /**
     * 重新计算账单总额（内部方法）
     *
     * @param ledger 账单实体
     */
    private void recalculateTotalAmount(Ledger ledger) {
        BigDecimal totalAmount = ledgerItemRepository.findByLedgerIdAndStatus(ledger.getId(), Status.ACTIVE)
                .stream()
                .map(LedgerItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ledger.setTotalAmount(totalAmount);
        ledgerRepository.save(ledger);

        log.info("重新计算总额，账单ID: {}, 新总额: {}", ledger.getId(), totalAmount);
    }

    /**
     * 创建支付记录（内部方法）
     *
     * @param ledger 账单实体
     * @param amount 支付金额
     * @param method 支付方式
     * @param memo   备注
     */
    private void createPaymentRecord(Ledger ledger, BigDecimal amount,
                                     PaymentMethod method, String memo) {
        PaymentRecord record = new PaymentRecord();
        record.setLedgerId(ledger.getId());
        record.setAmount(amount);
        record.setPaymentMethod(method);
        record.setStatus(Status.ACTIVE);
        record.setMemo(memo);
        paymentRecordRepository.save(record);

        log.info("创建支付记录，账单ID: {}, 金额: {}, 方式: {}",
                ledger.getId(), amount, method.getDescription());
    }

    /**
     * 构建账单VO（内部方法）
     *
     * @param ledger       账单实体
     * @param customerName 客户姓名
     * @return 账单VO
     */
    private LedgerVO buildLedgerVO(Ledger ledger, String customerName) {
        LedgerVO vo = ledgerConverter.toVO(ledger);
        vo.setCustomerName(customerName);

        // 查询明细
        List<LedgerItem> items = ledgerItemRepository.findByLedgerIdAndStatus(ledger.getId(), Status.ACTIVE);
        vo.setItems(ledgerItemConverter.toVOList(items));

        // 查询支付记录
        List<PaymentRecord> payments = paymentRecordRepository.findByLedgerIdAndStatus(ledger.getId(), Status.ACTIVE);
        vo.setPaymentRecords(paymentRecordConverter.toVOList(payments));

        return vo;
    }

    // ==================== 校验方法 ====================

    /**
     * 校验记账操作
     *
     * @param ledger 账单实体
     * @param dto    记账DTO
     * @throws BusinessException 校验失败时
     */
    private void validateRecordLedger(Ledger ledger, RecordLedgerDTO dto) {
        // 校验账单状态（只有进行中的账单可以记账）
        if (!LedgerStatus.IN_PROGRESS.equals(ledger.getLedgerStatus()) && !LedgerStatus.PARTIAL.equals(ledger.getLedgerStatus())) {
            throw new BusinessException(BusinessCode.LEDGER_STATUS_NOT_ALLOWED);
        }

        // 校验支付金额
        if (dto.getPaymentAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(BusinessCode.PAYMENT_AMOUNT_INVALID);
        }

        if (dto.getPaymentAmount().compareTo(ledger.getTotalAmount()) > 0) {
            throw new BusinessException(BusinessCode.LEDGER_PAYMENT_EXCEED);
        }

        // 校验支付方式（有支付金额时必填）
        if (dto.getPaymentAmount().compareTo(BigDecimal.ZERO) > 0 && dto.getPaymentMethod() == null) {
            throw new BusinessException(BusinessCode.LEDGER_PAYMENT_METHOD_REQUIRED);
        }
    }

    /**
     * 校验结账操作
     *
     * @param ledger 账单实体
     * @param dto    结账DTO
     * @throws BusinessException 校验失败时
     */
    private void validateSettleLedger(Ledger ledger, SettleLedgerDTO dto) {
        // 校验账单状态（进行中或赊账中可以结账）
        if (LedgerStatus.CLEARED.equals(ledger.getLedgerStatus())
                || LedgerStatus.CLOSED.equals(ledger.getLedgerStatus())) {
            throw new BusinessException(BusinessCode.LEDGER_STATUS_NOT_ALLOWED);
        }

        // 校验支付金额
        if (dto.getPaymentAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(BusinessCode.PAYMENT_AMOUNT_INVALID);
        }

        BigDecimal remaining = ledger.getTotalAmount().subtract(ledger.getPaidAmount());
        if (dto.getPaymentAmount().compareTo(remaining) > 0) {
            throw new BusinessException(BusinessCode.LEDGER_PAYMENT_EXCEED);
        }

        // 校验支付方式
        if (dto.getPaymentAmount().compareTo(BigDecimal.ZERO) > 0 && dto.getPaymentMethod() == null) {
            throw new BusinessException(BusinessCode.LEDGER_PAYMENT_METHOD_REQUIRED);
        }
    }

    /**
     * 校验新增支付记录操作
     *
     * @param ledger 账单实体
     * @param dto    支付记录DTO
     * @throws BusinessException 校验失败时
     */
    private void validateAddPaymentRecord(Ledger ledger, AddPaymentRecordDTO dto) {
        // 校验账单状态（已结清和已关闭不允许添加支付记录）
        if (LedgerStatus.CLEARED.equals(ledger.getLedgerStatus())) {
            throw new BusinessException(BusinessCode.LEDGER_ALREADY_CLEARED);
        }
        if (LedgerStatus.CLOSED.equals(ledger.getLedgerStatus())) {
            throw new BusinessException(BusinessCode.LEDGER_ALREADY_CLOSED);
        }

        // 校验支付金额必须大于0
        if (dto.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(BusinessCode.PAYMENT_AMOUNT_INVALID);
        }

        // 计算剩余应收金额
        BigDecimal remaining = ledger.getTotalAmount()
                .subtract(ledger.getPaidAmount())
                .subtract(ledger.getDiscountAmount());

        // 校验支付金额不能超过剩余应收金额
        if (dto.getPaymentAmount().compareTo(remaining) > 0) {
            throw new BusinessException(BusinessCode.LEDGER_PAYMENT_EXCEED,
                    String.format("支付金额%.2f超过剩余应收金额%.2f", dto.getPaymentAmount(), remaining));
        }
    }

    /**
     * 根据支付金额自动更新账单状态
     *
     * @param ledger 账单实体
     */
    private void updateLedgerStatusAfterPayment(Ledger ledger) {
        BigDecimal remaining = ledger.getTotalAmount()
                .subtract(ledger.getPaidAmount())
                .subtract(ledger.getDiscountAmount());

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            // 已结清
            ledger.setLedgerStatus(LedgerStatus.CLEARED);
            log.info("账单已结清，账单ID: {}", ledger.getId());
        } else if (ledger.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            // 有支付记录但未结清，状态为赊账中
            ledger.setLedgerStatus(LedgerStatus.PARTIAL);
            log.info("账单部分付款，账单ID: {}, 剩余: {}", ledger.getId(), remaining);
        }
    }


    /**
     * 根据支付金额自动更新账单状态
     *
     * @param ledger 账单实体
     */
    private void updateLedgerStatusAfterRecord(Ledger ledger) {
        BigDecimal remaining = ledger.getTotalAmount()
                .subtract(ledger.getPaidAmount())
                .subtract(ledger.getDiscountAmount());

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            // 已结清
            ledger.setLedgerStatus(LedgerStatus.CLEARED);
            log.info("账单已结清，账单ID: {}", ledger.getId());
        } else {
            // 有支付记录但未结清，状态为赊账中
            ledger.setLedgerStatus(LedgerStatus.ON_CREDIT);
            log.info("账单赊账中，账单ID: {}, 剩余: {}", ledger.getId(), remaining);
        }
    }

    /**
     * 保存签名图片
     *
     * @param ledgerId 账单ID
     * @param signatureImage 签名图片（base64编码）
     * @return 签名图片URL
     */
    private String saveSignatureImage(Long ledgerId, String signatureImage) {
        try {
            // 检查是否为base64格式
            if (signatureImage == null || signatureImage.isEmpty()) {
                log.warn("签名图片为空，账单ID: {}", ledgerId);
                return null;
            }

            // 解析base64数据
            String base64Data = signatureImage;
            if (signatureImage.startsWith("data:image")) {
                // 移除 data:image/png;base64, 前缀
                int commaIndex = signatureImage.indexOf(",");
                if (commaIndex > 0) {
                    base64Data = signatureImage.substring(commaIndex + 1);
                }
            }

            // 解码base64
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // 生成文件名
            String fileName = "signature_" + ledgerId + "_" + System.currentTimeMillis() + ".png";

            // 上传到COS，使用自定义路径前缀
            String objectKey = fileUploadService.uploadBytes(
                imageBytes,
                fileName,
                "image/png",
                "signatures/"
            );

            log.info("签名图片上传成功，账单ID: {}, objectKey: {}", ledgerId, objectKey);

            // 返回对象键（路径），前端可以通过API获取预签名URL
            return objectKey;

        } catch (IllegalArgumentException e) {
            log.error("Base64解码失败，账单ID: {}", ledgerId, e);
            throw new BusinessException(BusinessCode.INVALID_PARAMETER, "签名图片格式错误");
        } catch (Exception e) {
            log.error("保存签名图片失败，账单ID: {}", ledgerId, e);
            throw new BusinessException(BusinessCode.INTERNAL_ERROR, "保存签名图片失败: " + e.getMessage());
        }
    }
}
