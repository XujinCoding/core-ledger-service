package com.coreledger.vo;

import com.coreledger.enums.LedgerStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账单列表 VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class LedgerListVO {

    /** 账单ID */
    private Long id;

    /** 客户ID */
    private Long customerId;

    /** 客户姓名 */
    private String customerName;

    /** 应收总金额 */
    private BigDecimal totalAmount;

    /** 实收金额 */
    private BigDecimal paidAmount;

    /** 优惠金额 */
    private BigDecimal discountAmount;

    /** 剩余欠款 */
    private BigDecimal remainingAmount;

    /** 账单状态 */
    private LedgerStatus ledgerStatus;

    /** 账单状态描述 */
    private String ledgerStatusDesc;

    /** 创建时间 */
    private LocalDateTime createInstant;
}
