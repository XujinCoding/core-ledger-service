package com.coreledger.service;

import cn.hutool.core.util.StrUtil;
import com.coreledger.converter.LedgerConverter;
import com.coreledger.dto.ledger.CreateLedgerDTO;
import com.coreledger.dto.ledger.DiscountSettleDTO;
import com.coreledger.dto.ledger.ReceivePaymentDTO;
import com.coreledger.dto.ledger.UpdateLedgerItemsDTO;
import com.coreledger.entity.Customer;
import com.coreledger.entity.Ledger;
import com.coreledger.entity.LedgerItem;
import com.coreledger.entity.PaymentRecord;
import com.coreledger.enums.LedgerStatus;
import com.coreledger.enums.PaymentMethod;
import com.coreledger.enums.Status;
import com.coreledger.exception.BusinessException;
import com.coreledger.enums.BusinessCode;
import com.coreledger.exception.NotFoundException;
import com.coreledger.repository.CustomerRepository;
import com.coreledger.repository.LedgerItemRepository;
import com.coreledger.repository.LedgerRepository;
import com.coreledger.repository.PaymentRecordRepository;
import com.coreledger.utils.specification.PredicateBuilder;
import com.coreledger.vo.ledger.LedgerDetailVO;
import com.coreledger.vo.ledger.LedgerListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 账单服务
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

    /**
     * 创建账单
     *
     * @param dto 创建账单DTO
     * @return 账单详情
     */
    @Transactional
    public LedgerDetailVO createLedger(CreateLedgerDTO dto) {
        log.info("创建账单: customerId={}, items={}", dto.getCustomerId(), dto.getItems().size());

        // 1. 校验客户是否存在
        Customer customer = customerRepository.findByIdAndStatus(dto.getCustomerId(), Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("客户不存在"));

        // 2. 转换并创建账单
        Ledger ledger = ledgerConverter.toEntity(dto);
        ledger.setStatus(Status.ACTIVE);

        // 3. 转换并添加明细项
        List<LedgerItem> items = ledgerConverter.toItemEntityList(dto.getItems());
        Ledger finalLedger = ledger;
        items.forEach(item -> {
            item.setStatus(Status.ACTIVE);
            finalLedger.addItem(item);
        });

        // 4. 重新计算总金额
        ledger.recalculateTotalAmount();

        // 5. 保存账单（级联保存明细）
        ledger = ledgerRepository.save(ledger);

        log.info("账单创建成功, ID: {}, totalAmount: {}", ledger.getId(), ledger.getTotalAmount());

        // 6. 返回详情
        return getLedgerDetail(ledger.getId());
    }

    /**
     * 修改账单明细（限制状态：仅进行中和部分缴费可修改）
     *
     * @param ledgerId 账单ID
     * @param dto      更新明细DTO
     * @return 账单详情
     */
    @Transactional
    public LedgerDetailVO updateLedgerItems(Long ledgerId, UpdateLedgerItemsDTO dto) {
        log.info("修改账单明细: ledgerId={}", ledgerId);

        // 1. 查询账单
        Ledger ledger = ledgerRepository.findByIdAndStatus(ledgerId, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("账单不存在"));

        // 2. 校验状态（只允许进行中和部分缴费状态修改）
        if (!Arrays.asList(LedgerStatus.IN_PROGRESS, LedgerStatus.PARTIAL).contains(ledger.getLedgerStatus())) {
            throw new BusinessException(BusinessCode.LEDGER_STATUS_NOT_ALLOWED, "账单状态为【" + ledger.getLedgerStatus().getDescription() + "】，不允许修改明细");
        }

        // 3. 处理新增明细
        if (dto.getAddItems() != null && !dto.getAddItems().isEmpty()) {
            List<LedgerItem> newItems = ledgerConverter.toItemEntityListFromAddDTO(dto.getAddItems());
            newItems.forEach(item -> {
                item.setStatus(Status.ACTIVE);
                ledger.addItem(item);
            });
        }

        // 4. 处理修改明细（只允许修改价格和数量，不允许修改商品）
        if (dto.getUpdateItems() != null && !dto.getUpdateItems().isEmpty()) {
            Map<Long, LedgerItem> itemMap = ledger.getItems().stream()
                    .collect(Collectors.toMap(LedgerItem::getId, item -> item));

            for (UpdateLedgerItemsDTO.UpdateItemDTO updateDTO : dto.getUpdateItems()) {
                LedgerItem item = itemMap.get(updateDTO.getId());
                if (item == null) {
                    throw new NotFoundException("明细项不存在: " + updateDTO.getId());
                }

                // 只更新价格和数量
                if (updateDTO.getPrice() != null) {
                    item.setPrice(updateDTO.getPrice());
                }
                if (updateDTO.getQuantity() != null) {
                    item.setQuantity(updateDTO.getQuantity());
                }
            }
        }

        // 5. 处理删除明细
        if (dto.getDeleteItemIds() != null && !dto.getDeleteItemIds().isEmpty()) {
            List<LedgerItem> itemsToRemove = ledger.getItems().stream()
                    .filter(item -> dto.getDeleteItemIds().contains(item.getId()))
                    .collect(Collectors.toList());

            if (itemsToRemove.size() != dto.getDeleteItemIds().size()) {
                throw new NotFoundException("部分明细项不存在");
            }

            itemsToRemove.forEach(ledger::removeItem);
        }

        // 6. 校验明细项不能为空
        if (ledger.getItems().isEmpty()) {
            throw new BusinessException(BusinessCode.LEDGER_ITEMS_EMPTY, "账单至少需要一个明细项");
        }

        // 7. 重新计算总金额
        ledger.recalculateTotalAmount();

        // 8. 保存账单
        ledgerRepository.save(ledger);

        log.info("账单明细修改成功: ledgerId={}, newTotalAmount={}", ledgerId, ledger.getTotalAmount());

        // 9. 返回详情
        return getLedgerDetail(ledgerId);
    }

    /**
     * 收款（部分缴费或全额缴费）
     *
     * @param ledgerId 账单ID
     * @param dto      收款DTO
     * @return 账单详情
     */
    @Transactional
    public LedgerDetailVO receivePayment(Long ledgerId, ReceivePaymentDTO dto) {
        log.info("收款: ledgerId={}, amount={}", ledgerId, dto.getAmount());

        // 1. 查询账单
        Ledger ledger = ledgerRepository.findByIdAndStatus(ledgerId, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("账单不存在"));

        // 2. 校验状态（只允许进行中、部分缴费、赊账中状态收款）
        if (!Arrays.asList(LedgerStatus.IN_PROGRESS, LedgerStatus.PARTIAL, LedgerStatus.ON_CREDIT).contains(ledger.getLedgerStatus())) {
            throw new BusinessException(BusinessCode.LEDGER_STATUS_NOT_ALLOWED, "账单状态为【" + ledger.getLedgerStatus().getDescription() + "】，不允许收款");
        }

        // 3. 校验金额（不允许超额收款）
        BigDecimal remainingAmount = ledger.getRemainingAmount();
        if (dto.getAmount().compareTo(remainingAmount) > 0) {
            throw new BusinessException(BusinessCode.LEDGER_PAYMENT_EXCEED, "收款金额不能超过剩余应收金额（剩余: " + remainingAmount + "元）");
        }

        // 4. 创建支付记录
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setLedgerId(ledgerId);
        paymentRecord.setAmount(dto.getAmount());
        paymentRecord.setPaymentMethod(PaymentMethod.fromValue(dto.getPaymentMethod()));
        paymentRecord.setMemo(dto.getMemo());
        paymentRecord.setStatus(Status.ACTIVE);
        paymentRecordRepository.save(paymentRecord);

        // 5. 更新账单实收金额
        ledger.setPaidAmount(ledger.getPaidAmount().add(dto.getAmount()));

        // 6. 更新账单状态
        BigDecimal newRemainingAmount = ledger.getRemainingAmount();
        if (newRemainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            // 全额缴费，状态变为已结清
            ledger.setLedgerStatus(LedgerStatus.CLEARED);
        } else {
            // 部分缴费，状态变为部分缴费
            ledger.setLedgerStatus(LedgerStatus.PARTIAL);
        }

        ledgerRepository.save(ledger);

        log.info("收款成功: ledgerId={}, paidAmount={}, status={}", ledgerId, ledger.getPaidAmount(), ledger.getLedgerStatus());

        // 7. 返回详情
        return getLedgerDetail(ledgerId);
    }

    /**
     * 优惠结清（抹零）
     *
     * @param ledgerId 账单ID
     * @param dto      优惠结清DTO
     * @return 账单详情
     */
    @Transactional
    public LedgerDetailVO discountSettle(Long ledgerId, DiscountSettleDTO dto) {
        log.info("优惠结清: ledgerId={}, discountAmount={}", ledgerId, dto.getDiscountAmount());

        // 1. 查询账单
        Ledger ledger = ledgerRepository.findByIdAndStatus(ledgerId, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("账单不存在"));

        // 2. 校验状态（只允许进行中、部分缴费、赊账中状态优惠结清）
        if (!Arrays.asList(LedgerStatus.IN_PROGRESS, LedgerStatus.PARTIAL, LedgerStatus.ON_CREDIT).contains(ledger.getLedgerStatus())) {
            throw new BusinessException(BusinessCode.LEDGER_STATUS_NOT_ALLOWED, "账单状态为【" + ledger.getLedgerStatus().getDescription() + "】，不允许优惠结清");
        }

        // 3. 校验优惠金额
        BigDecimal remainingAmount = ledger.getRemainingAmount();
        if (dto.getDiscountAmount().compareTo(remainingAmount) > 0) {
            throw new BusinessException(BusinessCode.LEDGER_PAYMENT_EXCEED, "优惠金额不能超过剩余应收金额（剩余: " + remainingAmount + "元）");
        }

        // 4. 更新优惠金额
        ledger.setDiscountAmount(ledger.getDiscountAmount().add(dto.getDiscountAmount()));

        // 5. 更新账单状态为已结清
        ledger.setLedgerStatus(LedgerStatus.CLEARED);

        ledgerRepository.save(ledger);

        log.info("优惠结清成功: ledgerId={}, discountAmount={}", ledgerId, ledger.getDiscountAmount());

        // 6. 返回详情
        return getLedgerDetail(ledgerId);
    }

    /**
     * 赊账
     *
     * @param ledgerId 账单ID
     * @return 账单详情
     */
    @Transactional
    public LedgerDetailVO onCredit(Long ledgerId) {
        log.info("赊账: ledgerId={}", ledgerId);

        // 1. 查询账单
        Ledger ledger = ledgerRepository.findByIdAndStatus(ledgerId, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("账单不存在"));

        // 2. 校验状态（只允许进行中状态转为赊账）
        if (ledger.getLedgerStatus() != LedgerStatus.IN_PROGRESS) {
            throw new BusinessException(BusinessCode.LEDGER_STATUS_NOT_ALLOWED, "只有进行中的账单才能赊账");
        }

        // 3. 更新账单状态为赊账中
        ledger.setLedgerStatus(LedgerStatus.ON_CREDIT);
        ledgerRepository.save(ledger);

        log.info("赊账成功: ledgerId={}", ledgerId);

        // 4. 返回详情
        return getLedgerDetail(ledgerId);
    }

    /**
     * 关闭账单
     *
     * @param ledgerId 账单ID
     * @return 账单详情
     */
    @Transactional
    public LedgerDetailVO closeLedger(Long ledgerId) {
        log.info("关闭账单: ledgerId={}", ledgerId);

        // 1. 查询账单
        Ledger ledger = ledgerRepository.findByIdAndStatus(ledgerId, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("账单不存在"));

        // 2. 校验状态（已结清和已关闭的账单不能关闭）
        if (Arrays.asList(LedgerStatus.CLEARED, LedgerStatus.CLOSED).contains(ledger.getLedgerStatus())) {
            throw new BusinessException(BusinessCode.LEDGER_STATUS_NOT_ALLOWED, "账单状态为【" + ledger.getLedgerStatus().getDescription() + "】，不允许关闭");
        }

        // 3. 更新账单状态为已关闭
        ledger.setLedgerStatus(LedgerStatus.CLOSED);
        ledgerRepository.save(ledger);

        log.info("账单关闭成功: ledgerId={}", ledgerId);

        // 4. 返回详情
        return getLedgerDetail(ledgerId);
    }

    /**
     * 查询账单列表
     *
     * @param customerId   客户ID筛选
     * @param ledgerStatus 账单状态筛选
     * @param pageable     分页参数
     * @return 账单分页列表
     */
    public Page<LedgerListVO> listLedgers(Long customerId, LedgerStatus ledgerStatus, Pageable pageable) {
        log.info("查询账单列表: customerId={}, ledgerStatus={}", customerId, ledgerStatus);

        // 1. 使用 PredicateBuilder 构建动态查询条件
        Specification<Ledger> spec = PredicateBuilder.<Ledger>and()
                .equal("status", Status.ACTIVE)
                .equal(customerId != null, "customerId", customerId)
                .equal(ledgerStatus != null, "ledgerStatus", ledgerStatus)
                .build();

        // 2. 分页查询
        Page<Ledger> ledgerPage = ledgerRepository.findAll(spec, pageable);

        // 3. 转换为VO
        Page<LedgerListVO> voPage = ledgerPage.map(ledgerConverter::toListVO);

        // 4. 批量查询客户信息
        List<Long> customerIds = voPage.getContent().stream()
                .map(LedgerListVO::getCustomerId)
                .distinct()
                .collect(Collectors.toList());

        if (!customerIds.isEmpty()) {
            Map<Long, Customer> customerMap = customerRepository.findAllById(customerIds).stream()
                    .collect(Collectors.toMap(Customer::getId, customer -> customer));

            voPage.getContent().forEach(vo -> {
                Customer customer = customerMap.get(vo.getCustomerId());
                if (customer != null) {
                    vo.setCustomerName(customer.getName());
                    vo.setCustomerPhone(customer.getPhone());
                }
            });
        }

        return voPage;
    }

    /**
     * 查询账单详情
     *
     * @param id 账单ID
     * @return 账单详情
     */
    public LedgerDetailVO getLedgerDetail(Long id) {
        log.info("查询账单详情: {}", id);

        // 1. 查询账单
        Ledger ledger = ledgerRepository.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("账单不存在"));

        // 2. 转换为VO
        LedgerDetailVO vo = ledgerConverter.toDetailVO(ledger);

        // 3. 查询客户信息
        Customer customer = customerRepository.findByIdAndStatus(ledger.getCustomerId(), Status.ACTIVE)
                .orElse(null);
        if (customer != null) {
            vo.setCustomerName(customer.getName());
            vo.setCustomerPhone(customer.getPhone());
            vo.setCustomerAddressDetail(customer.getAddressDetail());
            // TODO: 查询完整地址
            // String fullAddress = sysAddressService.getFullAddress(customer.getAddressId());
            // vo.setCustomerAddress(fullAddress);
        }

        // 4. 转换明细列表
        vo.setItems(ledgerConverter.toItemVOList(ledger.getItems()));

        // 5. 查询支付记录
        List<PaymentRecord> paymentRecords = paymentRecordRepository.findByLedgerIdAndStatusOrderByCreateInstantDesc(id, Status.ACTIVE);
        vo.setPaymentRecords(ledgerConverter.toPaymentRecordVOList(paymentRecords));

        return vo;
    }

    /**
     * 删除账单（软删除）
     *
     * @param id 账单ID
     */
    @Transactional
    public void deleteLedger(Long id) {
        log.info("删除账单: {}", id);

        // 1. 查询账单
        Ledger ledger = ledgerRepository.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("账单不存在"));

        // 2. 校验状态（已结清的账单不允许删除）
        if (ledger.getLedgerStatus() == LedgerStatus.CLEARED) {
            throw new BusinessException(BusinessCode.LEDGER_ALREADY_CLEARED, "已结清的账单不允许删除");
        }

        // 3. 软删除账单
        ledger.setStatus(Status.INACTIVE);
        ledgerRepository.save(ledger);

        // 4. 软删除明细项
        ledger.getItems().forEach(item -> item.setStatus(Status.INACTIVE));
        ledgerItemRepository.saveAll(ledger.getItems());

        // 5. 软删除支付记录
        List<PaymentRecord> paymentRecords = paymentRecordRepository.findByLedgerIdAndStatusOrderByCreateInstantDesc(id, Status.ACTIVE);
        paymentRecords.forEach(record -> record.setStatus(Status.INACTIVE));
        paymentRecordRepository.saveAll(paymentRecords);

        log.info("账单删除成功: {}", id);
    }
}
