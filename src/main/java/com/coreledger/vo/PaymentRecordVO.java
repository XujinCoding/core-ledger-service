package com.coreledger.vo;

import com.coreledger.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录 VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
public class PaymentRecordVO {

    /** 支付记录ID */
    private Long id;

    /** 账本ID */
    private Long ledgerId;

    /** 支付金额 */
    private BigDecimal amount;

    /** 支付方式 */
    private PaymentMethod paymentMethod;

    /** 支付方式描述 */
    private String paymentMethodDesc;

    /** 备注 */
    private String memo;

    /** 支付时间 */
    private LocalDateTime createInstant;
}
