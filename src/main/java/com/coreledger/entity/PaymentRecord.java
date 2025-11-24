package com.coreledger.entity;

import com.coreledger.config.converter.PaymentMethodConverter;
import com.coreledger.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 支付流水实体
 *
 * <p>对应数据库表: payment_record</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "payment_record")
public class PaymentRecord extends BaseEntity {

    /**
     * 账本ID
     */
    @Column(name = "ledger_id", nullable = false)
    private Long ledgerId;

    /**
     * 支付金额
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * 支付方式: 1=现金, 2=微信, 3=支付宝, 4=银行转账
     */
    @Column(name = "payment_method", nullable = false)
    @Convert(converter = PaymentMethodConverter.class)
    private PaymentMethod paymentMethod = PaymentMethod.CASH;
}
